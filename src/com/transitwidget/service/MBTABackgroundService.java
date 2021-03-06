/**
 * 
 */
package com.transitwidget.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;
import com.transitwidget.R;
import com.transitwidget.api.NextBusAPI;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Stop;
import com.transitwidget.prefs.NextBusObserverConfig;
import com.transitwidget.utils.TimeUtils;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Handles reading prediction data from feeds and triggering widget updates.
 */
public class MBTABackgroundService extends IntentService {
	private static final String TAG = MBTABackgroundService.class.getName();
	
	public static final String ACTION_WAKEUP = "ACTION_WAKEUP";
	public static final String ACTION_WAKEUP_IMMEDIATE = "ACTION_WAKEUP_IMMEDIATE";
	
	public static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";	
	public static final String EXTRA_IMMEDIATE = "EXTRA_IMMEDIATE";
	
	public MBTABackgroundService() {
		super("MBTABackgroundService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service started");
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, "onHandleIntent " + action);
		
		int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
		if (widgetId == 0) {
			Log.e(TAG, "Got an invalid or no widgetID in BackgroundService");
			return;
		}
		
		boolean immediate = intent.getBooleanExtra(EXTRA_IMMEDIATE, false);
		
		NextBusObserverConfig config = new NextBusObserverConfig(getApplicationContext(), widgetId);
		
		if (config.getAgency() == null) {
			// This is an old widget without a configuration!
			Log.w(TAG, "Unable to get widget preferences for widget " + widgetId);

			// Remove any alarms for this widget
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(getPendingIntent(getApplicationContext(), widgetId));
			Log.i(TAG, "onHandleIntent: canceling alarm");
			return;
		}

		Calendar endTime = TimeUtils.getCalendarWithTimeFromMidnight(config.getStopObserving());
		// Calendar startTime = CalendarUtils.getCalendarWithTimeFromMidnight(config.getStartObserving());
		String agency = config.getAgency().getTag();
		String routeTag = config.getRoute().getTag();
		String stopTag = config.getStop().getTag();
		String directionTag = config.getDirection().getTag();
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(0);
		
		Log.i(TAG, String.format("onHandleIntent: widgetId=%d endTime=%s agency=%s directionTag=%s routeTag=%s stopTag=%s",
				widgetId, DateFormat.format("h:mmaa", endTime), agency, directionTag, routeTag, stopTag));
				
		long now = System.currentTimeMillis();
		if (!immediate && now >= endTime.getTimeInMillis()) {
			// Cancel the alarm if it wasn't a direct tap to update and the end time is passed
			
			Log.i(TAG, "onHandleIntent: canceling alarm and scheduling for tomorrow");
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(getPendingIntent(getApplicationContext(), widgetId));

			// Schedule the start time tomorrow
			Intent serviceIntent = new Intent(getApplicationContext(), AlarmSchedulerService.class);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_WIDGET_ID, widgetId);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_START_TIME, config.getStartObserving());
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_END_TIME, config.getStopObserving());
			startService(serviceIntent);
			
			return;
		}

		// Update the data, send notification
		
		List<BusPrediction> predictions = new NextBusAPI().getPredictions(agency, stopTag, directionTag, routeTag);
		Log.i(TAG, "Got predictions: " + predictions);
		if (predictions == null) {
			Log.w(TAG, "Unable to load predictions");
			return;
		}
		
		BusPrediction nextPrediction = new BusPrediction();
		BusPrediction secondPrediction = new BusPrediction();
		
		if (predictions.isEmpty()) {
			Log.i(TAG, "No predictions available for selected route.");

		} else {
		
			nextPrediction = predictions.get(0);
			if (predictions.size() > 1) {
				secondPrediction = predictions.get(1);
			}
			
			CharSequence tickerText = predictions.toString();
			long when = System.currentTimeMillis();
	
            // Lookup stop
            String selection = Stop.TAG + " = ? AND " + Stop.AGENCY + " = ?"; 
            String[] selectionArgs = new String[] {stopTag, agency};
            Cursor result = getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
            String stopLabel = "Unknown";
            if (result.moveToFirst()) {
                stopLabel = new Stop(result).getTitle();
            }
            result.close();
        
			Notification notification = new Notification(R.drawable.icon_bus, tickerText, when);
			
			Context context = getApplicationContext();
			Intent notificationIntent = new Intent("notification-action");
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	
			CharSequence contentTitle = TimeUtils.formatTimeOfNextBus(nextPrediction.getEpochTime());
			CharSequence contentText = routeTag + ": " + stopLabel;
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
			nm.notify(widgetId, notification);
	
			Log.i(TAG, "Sent notification: " + tickerText);
		}
		
		int[] widgetIds = {widgetId};

		Log.i(TAG, "Updating widgets: " + Arrays.asList(widgetIds));
		
		Intent updateWidgetIntent = new Intent(getApplicationContext(), UpdateWidgetService.class);
		updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_NEXT_TIME, nextPrediction.getEpochTime());
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_SECOND_TIME, secondPrediction.getEpochTime());

		startService(updateWidgetIntent);
	}


	@Override
	public void onDestroy() {
		Log.i(TAG, "Service destroyed");
		super.onDestroy();
	}
	
	public static Intent createPredictionIntent(Context ctx, int widgetId) {
		Intent intent = new Intent(ctx, MBTABackgroundService.class);
		intent.setAction(MBTABackgroundService.ACTION_WAKEUP + "-" + widgetId);
		intent.putExtra(EXTRA_WIDGET_ID, widgetId);
		return intent;
	}

	public static PendingIntent getPendingIntent(Context ctx, int widgetId) {
		PendingIntent pi = PendingIntent.getService(
				ctx, 0, createPredictionIntent(ctx, widgetId), PendingIntent.FLAG_CANCEL_CURRENT);
		return pi;
	}
}
