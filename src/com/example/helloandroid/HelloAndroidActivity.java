package com.example.helloandroid;

import java.util.List;

import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;

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
        final Intent intent = MBTABackgroundService.createPredictionIntent(
        		getApplicationContext(), "AM", 0, "mbta", "20761", "77");
        
        startAlarmButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				long now = System.currentTimeMillis();
				
				intent.putExtra(MBTABackgroundService.EXTRA_END_TIME, now + 5 * 60 * 1000);

				PendingIntent pi = MBTABackgroundService.getPendingIntent(getApplicationContext(), intent);
				
				long trigger_time = now + 1000;
				long interval = 30 * 1000;
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