package com.transitwidget.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import com.transitwidget.R;
import com.transitwidget.prefs.NextBusObserverConfig;
import com.transitwidget.utils.TimeUtils;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateWidgetService extends Service {
	private static final String TAG = UpdateWidgetService.class.getName();
	
	public static final String EXTRA_NEXT_TIME = "EXTRA_NEXT_TIME";
	public static final String EXTRA_SECOND_TIME = "EXTRA_SECOND_TIME";

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(TAG, "Called");

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

		int[] widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : widgetIds) {

			String routeInfo;
			String stopInfo = "";
			String relativeTime = "--";
            String absoluteTime = "";
			
			NextBusObserverConfig prefs = new NextBusObserverConfig(getApplicationContext(), widgetId);
			if (prefs.getRoute() == null) {
				// Not a known ID, display a error message.
				routeInfo = "No data.";
				
			} else {
				String route = prefs.getRoute().getLongLabel();
				String stop = prefs.getStop().getLongLabel();
				String direction = prefs.getDirection().getShortLabel();
				
				long nextTimeMs = intent.getLongExtra(EXTRA_NEXT_TIME, -1);
                relativeTime = TimeUtils.formatTimeOfNextBus(nextTimeMs);
                absoluteTime = TimeUtils.formatAbsoluteTimeOfNextBus(nextTimeMs);
	
				if (direction == null) direction = "";
				if (stop == null) stop = "";
				
				routeInfo =  route + " -> " + direction;
				stopInfo = stop;
			}
			
			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
			// Set the text
			remoteViews.setTextViewText(R.id.route, routeInfo);
			remoteViews.setTextViewText(R.id.stop, stopInfo);
			remoteViews.setTextViewText(R.id.next_time, relativeTime);
			remoteViews.setTextViewText(R.id.absolute_time, absoluteTime);

			// Register an onClickListener to refresh the widget
			Intent clickIntent = new Intent(this.getApplicationContext(), MBTABackgroundService.class);

			clickIntent.setAction(MBTABackgroundService.ACTION_WAKEUP_IMMEDIATE + "-" + widgetId);
			clickIntent.putExtra(MBTABackgroundService.EXTRA_IMMEDIATE, true);
			clickIntent.putExtra(MBTABackgroundService.EXTRA_WIDGET_ID, widgetId);

			PendingIntent pendingIntent = PendingIntent.getService(
					getApplicationContext(), 0, clickIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
			
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
