package com.transitwidget.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

import com.transitwidget.utils.TimeUtils;

public class AlarmSchedulerService extends IntentService {
	private static final String TAG = AlarmSchedulerService.class.getName();
	
	private static final int INTERVAL = 30 * 1000;

	/** The widget ID to schedule. */
	public static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";
	/** The time of day to begin updating the widget.
	 *  	This should be represented in seconds since midnight. */
	public static final String EXTRA_DAY_START_TIME = "EXTRA_DAY_START_TIME";
	public static final String EXTRA_DAY_END_TIME = "EXTRA_DAY_END_TIME";
	
	public AlarmSchedulerService() {
		super("AlarmSchedulerService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, -1);
		int startSecondsSinceMidnight = intent.getIntExtra(EXTRA_DAY_START_TIME, -1);
		int endSecondsSinceMidnight = intent.getIntExtra(EXTRA_DAY_END_TIME, -1);
		
		// Determine the time today of the seconds since midnight.
		Calendar startCal = TimeUtils.getCalendarWithTimeFromMidnight(startSecondsSinceMidnight);
		Calendar endCal = TimeUtils.getCalendarWithTimeFromMidnight(endSecondsSinceMidnight);
        Calendar now = Calendar.getInstance();
        
        if (endCal.before(startCal)) endCal.add(Calendar.DAY_OF_MONTH, 1);
		
		if (startCal.before(now) && endCal.before(now)) {
			// The time window has already passed today
			// schedule the first update interval in a day
			startCal.add(Calendar.DAY_OF_MONTH, 1);
		}

        Intent serviceIntent = MBTABackgroundService.createPredictionIntent(getApplicationContext(), widgetId);
        serviceIntent.putExtra(MBTABackgroundService.EXTRA_WIDGET_ID, widgetId);
        
		PendingIntent pendingIntent = MBTABackgroundService.getPendingIntent(getApplicationContext(), serviceIntent, widgetId);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startCal.getTimeInMillis(), INTERVAL, pendingIntent);
		
		Log.i(TAG, "Scheduling start time for " + DateFormat.format("MMM dd, yyyy h:mmaa", startCal));
	}
}
