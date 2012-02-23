package com.example.helloandroid;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.helloandroid.api.ServiceProvider;
import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;
import com.example.helloandroid.feed.model.Stop;
import com.example.helloandroid.prefs.NextBusAgency;
import com.example.helloandroid.prefs.NextBusDirection;
import com.example.helloandroid.prefs.NextBusObserverConfig;
import com.example.helloandroid.prefs.NextBusRoute;
import com.example.helloandroid.prefs.NextBusStop;
import com.example.helloandroid.provider.contract.WidgetConfiguration;
import com.example.helloandroid.service.AlarmSchedulerService;
import com.example.helloandroid.service.MBTABackgroundService;
import com.example.helloandroid.utils.AdapterUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();
    
    private ListView routeList; 
    private SimpleCursorAdapter routeAdapter;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        routeList = (ListView)findViewById(R.id.listView);

        Cursor agencyCursor = getContentResolver().query(Agency.CONTENT_URI, null, null, null, null);
        startManagingCursor(agencyCursor);

        Spinner agencySpinner = (Spinner) findViewById(R.id.agencySpinner);
        agencySpinner.setAdapter(new SimpleCursorAdapter(getApplicationContext(), R.layout.single_list_item, agencyCursor, new String[] {Agency.TITLE}, new int[] {R.id.value}));
        
        long selectedAgency = getSharedPreferences("prefs", MODE_PRIVATE).getLong("agency", -1);
        int position = 0;
        if (selectedAgency > 0) {
        	position = AdapterUtils.getAdapterPositionById(agencySpinner.getAdapter(), selectedAgency);
        }
        agencySpinner.setSelection(position);
        
        agencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(Agency.CONTENT_URI, id), new String[] { Agency.TAG }, null, null, null);
				String tag = null;
				if (cursor.moveToFirst()) {
					tag = cursor.getString(0);
				}

				getSharedPreferences("prefs", MODE_PRIVATE).edit()
						.putLong("agency", id)
						.putString("agencyTag", tag)
					.commit();
				
				if (tag != null) {
					loadRouteList(tag);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().commit();
			}
		});

        String[] from = {Route.TITLE};
        int[] to = {R.id.value};

        routeAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.single_list_item, null, from, to);
        routeList.setAdapter(routeAdapter);
        
        String selectedAgencyTag = getSharedPreferences("prefs", MODE_PRIVATE).getString("agencyTag", null);
        if (selectedAgencyTag != null) {
        	loadRouteList(selectedAgencyTag);
        }
    }
    
    public void loadRouteList(String agencyTag) {
    	final Activity activity = this;
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		ProgressDialog dialog;
    		protected void onPreExecute() {
    			dialog = ProgressDialog.show(activity, "", "Loading. Please wait...", true);
    			
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    			Cursor cursor = getContentResolver().query(Route.CONTENT_URI, null, Route.AGENCY + " = ?", new String[] { params[0] }, null);

    	    	if (cursor.getCount() == 0) {
    	    		// load from network and cache to DB.
    				ServiceProvider.getRoutes(getApplicationContext(), params[0]);
    		    	cursor.requery();
    	    	}
    	    	
    	    	return cursor;
    		}
    		protected void onPostExecute(Cursor cursor) {
    	        startManagingCursor(cursor);
    	    	routeAdapter.changeCursor(cursor);
    	    	dialog.hide();
    		}
    	}.execute(agencyTag);
    }
    
    public int getTimeFromBeginingOfDay(Calendar cal) {
    	int hours = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int seconds = cal.get(Calendar.SECOND);
    	
    	return seconds + minute * 60 + hours * 60 * 60;
    }
}
