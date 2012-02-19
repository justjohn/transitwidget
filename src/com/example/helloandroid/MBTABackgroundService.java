/**
 * 
 */
package com.example.helloandroid;

import java.util.Arrays;
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


	public static final String EXTRA_END_TIME = "end-time";
	public static final String EXTRA_AGENCY = "agency";
	public static final String EXTRA_STOP_TAG = "stop-tag";
	public static final String EXTRA_ROUTE_TAG = "route-tag";
	
	public MBTABackgroundService() {
		super("MBTABackgroundService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service started");
		
	}
	
	
	private void test() {
		NextBusObserverConfig cfg = new NextBusObserverConfig(getApplicationContext(), 0);
		cfg.save();
		
		
	}
	
	

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "onHandleIntent " + intent.getAction());
		
		
		long endTime = intent.getLongExtra(EXTRA_END_TIME, 0);
		String agency = intent.getStringExtra(EXTRA_AGENCY);
		String routeTag = intent.getStringExtra(EXTRA_ROUTE_TAG);
		String stopTag = intent.getStringExtra(EXTRA_STOP_TAG);
		

		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(0);
		
		Log.i(TAG, String.format("onHandleIntent: endTime=%d agency=%s routeTag=%s stopTag=%s",
				endTime, agency, routeTag, stopTag));
				
		long now = System.currentTimeMillis();
		if (now >= endTime) {
			AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			alarmManager.cancel(getPendingIntent(getApplicationContext(), intent));
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
	
	public static Intent createPredictionIntent(
			Context ctx, String id, long endTime, String agency, String stopTag, String routeTag) {
		Intent intent = new Intent(ctx, MBTABackgroundService.class);
		intent.setAction(MBTABackgroundService.ACTION_WAKEUP);
		intent.putExtra(EXTRA_END_TIME, endTime);
		intent.putExtra(EXTRA_AGENCY, agency);
		intent.putExtra(EXTRA_STOP_TAG, stopTag);
		intent.putExtra(EXTRA_ROUTE_TAG, routeTag);
		return intent;
	}

	public static PendingIntent getPendingIntent(Context ctx, Intent intent) {
		PendingIntent pi = PendingIntent.getService(
				ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		return pi;
	}
	
}
