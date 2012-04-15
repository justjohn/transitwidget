package com.transitwidget.utils;

import java.util.Calendar;

public class CalendarUtils {
	private CalendarUtils() { }
	
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

}
