package com.example.helloandroid;

import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	private static final String TAG = UpdateWidgetService.class.getName();
	
	public static final String EXTRA_NEXT_TIME = "EXTRA_NEXT_TIME";
	public static final String EXTRA_NEXT_ROUTE = "EXTRA_NEXT_ROUTE";
	public static final String EXTRA_NEXT_STOP = "EXTRA_NEXT_STOP";
	public static final String EXTRA_NEXT_DIRECTION = "EXTRA_NEXT_DIRECTION";	
	public static final String EXTRA_SECOND_TIME = "EXTRA_SECOND_TIME";

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "Called");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		long now = System.currentTimeMillis();

		String route = intent.getStringExtra(EXTRA_NEXT_ROUTE);
		String stop = intent.getStringExtra(EXTRA_NEXT_STOP);
		String direction = intent.getStringExtra(EXTRA_NEXT_DIRECTION);
		long nextTimeMs = intent.getLongExtra(EXTRA_NEXT_TIME, now);
		long seconds = (nextTimeMs - now) / 1000;
		long minutes = seconds / 60;
		seconds -= minutes * 60;

		if (direction == null) direction = "";
		if (stop == null) stop = "";
		if (route == null) {
			route = "No data.";
		} else {
			route += " -> " + direction;
		}
		
		ComponentName thisWidget = new ComponentName(getApplicationContext(), PredictionWidgetProvider.class);
		int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		
		Log.w(TAG, "From Intent" + String.valueOf(allWidgetIds.length));
		Log.w(TAG, "Direct" + String.valueOf(allWidgetIds2.length));

		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
			// Set the text
			remoteViews.setTextViewText(R.id.update, route + "\n" + stop);
			remoteViews.setTextViewText(R.id.next_time, minutes + "m " + seconds + "s");

			// Register an onClickListener
			Intent clickIntent = new Intent(this.getApplicationContext(),
					PredictionWidgetProvider.class);

			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		stopSelf();

		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
