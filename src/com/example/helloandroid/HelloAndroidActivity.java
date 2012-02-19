package com.example.helloandroid;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;
import com.example.helloandroid.prefs.NextBusAgency;
import com.example.helloandroid.prefs.NextBusDirection;
import com.example.helloandroid.prefs.NextBusObserverConfig;
import com.example.helloandroid.prefs.NextBusRoute;
import com.example.helloandroid.prefs.NextBusStop;
import com.example.helloandroid.service.AlarmSchedulerService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelloAndroidActivity extends Activity {
    private static final String TAG = HelloAndroidActivity.class.getName();

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button startAlarmButton = (Button)findViewById(R.id.startalarmbutton);
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        
        final Intent intent = MBTABackgroundService.createPredictionIntent( getApplicationContext(), 0);
        
        new AsyncTask<Integer, String, String>() {
        	@Override
        	protected String doInBackground(Integer... params) {

		        NextBusObserverConfig config = new NextBusObserverConfig(getApplicationContext(), params[0]);
		        config.getAgencies();

		        NextBusAgency agency = new NextBusAgency();
		        agency.init("mbta", "mbta", "mbta");
		        config.setAgency(agency);
		        
		        config.getRoutes();

		        NextBusRoute route = new NextBusRoute();
		        route.init("77", "77", "77");
		        config.setRoute(route);
		        
		        config.getDirections();
		        
		        NextBusDirection direction = new NextBusDirection();
		        direction.init("Arlington Heights via Mass. Ave.", "Arlington Heights via Mass. Ave.", "77_0_var0");
		        config.setDirection(direction);
		        
		        Log.i(TAG, config.getStops().toString());
		        
        		return "";
        	}
        }.execute(0);
        
        
        
        
        startAlarmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, 5); // start 5 seconds from now

				int trigger_time = getTimeFromBeginingOfDay(cal);
				int end_time = trigger_time + 60; // 1 minutes
				
				// PendingIntent pi = MBTABackgroundService.getPendingIntent(getApplicationContext(), intent);
				
				// long interval = 30 * 1000;
				// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, trigger_time, interval , pi);

		        
		        NextBusObserverConfig config = new NextBusObserverConfig(getApplicationContext(), 0);
		        
		        NextBusAgency agency = new NextBusAgency();
		        agency.init("mbta", "mbta", "mbta");
		        config.setAgency(agency);
		        
		        NextBusDirection direction = new NextBusDirection();
		        direction.init("Outbound", "Outbound", "Outbound");
		        config.setDirection(direction);
		        
		        NextBusRoute route = new NextBusRoute();
		        route.init("77", "77", "77");
		        config.setRoute(route);
		        
		        NextBusStop stop = new NextBusStop();
		        stop.init("20761", "20761", "20761");
		        config.setStop(stop);
		        
		        config.setStartObserving(trigger_time);
		        config.setStopObserving(end_time);
		        
		        config.save();
		        
				Intent serviceIntent = new Intent(getApplicationContext(), AlarmSchedulerService.class);
				serviceIntent.putExtra(AlarmSchedulerService.EXTRA_WIDGET_ID, 0);
				serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_START_TIME, trigger_time);
				startService(serviceIntent);
			}
		});
        

        Button cancelAlarmButton = (Button)findViewById(R.id.cancelalarmbutton);
        cancelAlarmButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				PendingIntent pi = MBTABackgroundService.getPendingIntent(getApplicationContext(), intent, 0);
				alarmManager.cancel(pi);
				
				
			}
		});
        
    }
    
    public int getTimeFromBeginingOfDay(Calendar cal) {
    	int hours = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int seconds = cal.get(Calendar.SECOND);
    	
    	return seconds + minute * 60 + hours * 60 * 60;
    }
}
