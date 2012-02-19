/**
 * 
 */
package com.example.helloandroid;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.example.helloandroid.feed.model.BusPrediction;
import com.example.helloandroid.prefs.NextBusObserverConfig;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author james
 *
 */
public class MBTABackgroundService extends IntentService {
	public static final String ACTION_WAKEUP = "mbta-wakeup";
	
	
	private static final String TAG = MBTABackgroundService.class.getName();


	public static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";
	
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
		Log.i(TAG, "onHandleIntent " + intent.getAction());
		
		int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
		
		NextBusObserverConfig config = new NextBusObserverConfig(getApplicationContext(), widgetId);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		cal.add(Calendar.SECOND, config.getStopObserving());
		
		long endTime = cal.getTimeInMillis();
		String agency = config.getAgency().getTag();
		String routeTag = config.getRoute().getTag();
		String stopTag = config.getStop().getTag();
		

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(0);
		
		Log.i(TAG, String.format("onHandleIntent: endTime=%d agency=%s routeTag=%s stopTag=%s",
				endTime, agency, routeTag, stopTag));
				
		long now = System.currentTimeMillis();
		if (now >= endTime) {
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(getPendingIntent(getApplicationContext(), intent, widgetId));
			Log.i(TAG, "onHandleIntent: canceling alarm");
			return;
		}

		// Update the data, send notification
		
		List<BusPrediction> predictions = NextBus.getPredictions(agency, stopTag, routeTag);
		Log.i(TAG, "Got predictions: " + predictions);
		if (predictions == null) {
			Log.w(TAG, "Unable to load predictions");
			return;
		}
		
		BusPrediction nextPrediction = predictions.get(0);
		BusPrediction secondPrediction = predictions.get(1);
		
		int icon = android.R.drawable.ic_menu_compass;
		CharSequence tickerText = predictions.toString();
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		Intent notificationIntent = new Intent("stupid-name");
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		nm.notify(0, notification);

		Log.i(TAG, "Sent notification: " + tickerText);
		
		// Update widget
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), PredictionWidgetProvider.class));
		
		// Un-comment when the preferences are finished
		// int[] widgetIds = {widgetId};

		Log.i(TAG, "Updating widgets: " + Arrays.asList(widgetIds));
		
		Intent updateWidgetIntent = new Intent(getApplicationContext(), UpdateWidgetService.class);
		updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
		
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_NEXT_TIME, nextPrediction.getEpochTime());
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_NEXT_DIRECTION, nextPrediction.getDirection());
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_NEXT_ROUTE, nextPrediction.getRoute());
		updateWidgetIntent.putExtra(UpdateWidgetService.EXTRA_NEXT_STOP, nextPrediction.getStopTitle());
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

	public static PendingIntent getPendingIntent(Context ctx, Intent intent, int widgetId) {
		PendingIntent pi = PendingIntent.getService(
				ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pi;
	}
	
}
