package com.transitwidget.receiver;

import com.transitwidget.PredictionWidgetProvider;
import com.transitwidget.prefs.NextBusObserverConfig;
import com.transitwidget.service.AlarmSchedulerService;
import com.transitwidget.utils.CalendarUtils;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

public class WakeupReceiver extends BroadcastReceiver {

	private static final String TAG = WakeupReceiver.class.getName();

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.i(TAG, "onReceive: " + intent.getAction());

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
		int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(ctx, PredictionWidgetProvider.class));
		
		for (int widgetId : widgetIds) {
			NextBusObserverConfig config = new NextBusObserverConfig(ctx, widgetId);
			Log.i(TAG, "Reset alarm for widget " + widgetId + " to " + DateFormat.format("h:mmaa", CalendarUtils.getCalendarWithTimeFromMidnight(config.getStartObserving())));

			Intent serviceIntent = new Intent(ctx, AlarmSchedulerService.class);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_WIDGET_ID, widgetId);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_START_TIME, config.getStartObserving());
			ctx.startService(serviceIntent);
		}
	}

}
