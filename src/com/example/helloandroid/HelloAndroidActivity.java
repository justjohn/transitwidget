package com.example.helloandroid;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelloAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button startAlarmButton = (Button)findViewById(R.id.startalarmbutton);
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final Intent intent = MBTABackgroundService.createPredictionIntent(
        		getApplicationContext(), "AM", 0, "mbta", "20761", "77");
        
        startAlarmButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
//				long elapse_wakeup_time = SystemClock.elapsedRealtime() + 1 * 1000;
				long now = System.currentTimeMillis();
				
				intent.putExtra(MBTABackgroundService.EXTRA_END_TIME, now + 30000);
				
				PendingIntent pi = MBTABackgroundService.getPendingIntent(getApplicationContext(), intent);
				
//				alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapse_wakeup_time, pi);
				
				
				
				long trigger_time = now + 1000;
				long interval = 10 * 1000;
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, trigger_time, interval , pi);
				
				
			}
		});
        

        Button cancelAlarmButton = (Button)findViewById(R.id.cancelalarmbutton);
        cancelAlarmButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				PendingIntent pi = MBTABackgroundService.getPendingIntent(getApplicationContext(), intent);
				alarmManager.cancel(pi);
				
				
			}
		});
        
    }
}