package com.transitwidget.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	private TimeUtils() { }
	
	public static Calendar getCalendarWithTimeFromMidnight(int secondsSinceMidnight) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.SECOND, secondsSinceMidnight);
		return cal;
	}

    
    public static int getTimeFromBeginingOfDay(Calendar cal) {
    	int hours = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int seconds = cal.get(Calendar.SECOND);
    	
    	return seconds + (minute + hours * 60) * 60;
    }
    
    public static String formatTimeOfNextBus(long nextTimeMs) {
        String nextTime;
        long now = System.currentTimeMillis();
        
        if (nextTimeMs <= 0) {
            // No prediction time
            nextTime = "No Data";

        } else {
            long seconds = (nextTimeMs - now) / 1000;
            if (seconds < 0) seconds = 0; // don't display negative times
            
            long minutes = seconds / 60;
            seconds -= minutes * 60;
            nextTime = minutes + "m " + seconds + "s";
        }
        
        return nextTime;
    }
    
    public static String formatAbsoluteTimeOfNextBus(long nextTimeMs) {
        if (nextTimeMs <= 0) {
            return "";
        }
        return "at " + new SimpleDateFormat("h:mma").format(new Date(nextTimeMs));
    }

}
