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
		Calendar intervalStart = TimeUtils.getCalendarWithTimeFromMidnight(startSecondsSinceMidnight);
		Calendar intervalEnd = TimeUtils.getCalendarWithTimeFromMidnight(endSecondsSinceMidnight);
        Calendar now = Calendar.getInstance();
        
        Calendar updateStartTime = intervalStart;

        /*
         * How to handle start time of the interval when "now" is in the
         * given interval of the day relative to start/end time.
         * 
         * - start time before end time
         * 
         * |--------[---------]--------|
         *  schedule   start   schedule
         *   today      now    tomorrow
         * 
         * - end time before start time
         * 
         * |--------]---------[--------|
         *   start   schedule   start
         *    now     today      now
         */
        
		if (intervalEnd.after(intervalStart)) {
			// schedule the first update interval "tomorrow"
            if (intervalEnd.before(now)) {
                updateStartTime.add(Calendar.DAY_OF_MONTH, 1);
            } else if (intervalStart.after(now)) {
                // leave as intervalStart
            } else {
                updateStartTime = now;
            }
            
		} else {
            if (intervalEnd.before(now) || intervalStart.after(now)) {
                updateStartTime = now;
            } else {
                // leave as intervalStart
            }
        }
        
		PendingIntent pendingIntent = MBTABackgroundService.getPendingIntent(getApplicationContext(), widgetId);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, updateStartTime.getTimeInMillis(), INTERVAL, pendingIntent);
		
		Log.i(TAG, "Scheduling start time for " + DateFormat.format("MMM dd, yyyy h:mmaa", intervalStart));
	}
}
