package com.example.helloandroid;

import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.helloandroid.provider.contract.WidgetConfiguration;
import com.example.helloandroid.service.UpdateWidgetService;

public class PredictionWidgetProvider extends AppWidgetProvider {
	private static final String LOG = PredictionWidgetProvider.class.getName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.w(LOG, "Widget onUpdate called for ids: " + Arrays.toString(appWidgetIds));

		// Get all ids
		ComponentName thisWidget = new ComponentName(context, PredictionWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		// Update the widgets via the service
		context.startService(intent);
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		Log.w(LOG, "Widget onDeleted called for ids: " + appWidgetIds);
		
		// Remove widget configuration
		for (int widgetId : appWidgetIds) {
			context.getContentResolver().delete(WidgetConfiguration.CONTENT_URI, WidgetConfiguration.WIDGET_ID + " = ?", new String[] { String.valueOf(widgetId) });
		}
	}
}
