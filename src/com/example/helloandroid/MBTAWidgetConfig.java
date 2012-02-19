package com.example.helloandroid;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Calendar;
import java.util.List;

import com.example.helloandroid.adapters.BaseItemAdapter;
import com.example.helloandroid.prefs.NextBusAgency;
import com.example.helloandroid.prefs.NextBusDirection;
import com.example.helloandroid.prefs.NextBusObserverConfig;
import com.example.helloandroid.prefs.NextBusRoute;
import com.example.helloandroid.prefs.NextBusStop;
import com.example.helloandroid.service.AlarmSchedulerService;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class MBTAWidgetConfig extends Activity {
    static final String TAG = "MBTAWidgetConfig";

    private static final String PREFS_NAME
            = "com.mbta.widget.MBTAWidgetProvider";
    
    private static final String PREF_ROUTE = "prefix_route_";
    private static final String PREF_START = "prefix_start_";
    private static final String PREF_END = "prefix_end_";
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    Spinner agencySpinner;
    Spinner routeSpinner;
    Spinner directionSpinner;
    Spinner endPointSpinner;
    
    BaseItemAdapter<NextBusRoute> routeAdapter;
    BaseItemAdapter<NextBusAgency> agencyAdapter;
    BaseItemAdapter<NextBusDirection> directionAdapter;
    BaseItemAdapter<NextBusStop> endPointAdapter;
    NextBusObserverConfig config;
    
    public MBTAWidgetConfig() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i(TAG, "Start OnCreate");
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        Log.i(TAG, "config ui widget");
        // Set the view layout resource to use.
        setContentView(R.layout.widget_config);

        Log.i(TAG, "config save button");
        // Bind the action for the save button.
        findViewById(R.id.saveBtn).setOnClickListener(SaveOnClickListener);

        findViewById(R.id.cancelBtn).setOnClickListener(CancelOnClickListener);
        
        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            //finish();
        }
        
        Log.i(TAG, "config agency spinner");
        agencySpinner = (Spinner) findViewById(R.id.agencySpinner);
        agencySpinner.setOnItemSelectedListener(AgencyItemSelectListener);
        ArrayAdapter agencyAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        agencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agencySpinner.setAdapter(agencyAdapter);
        
        Log.i(TAG, "config route spinner");
        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        routeSpinner.setOnItemSelectedListener(RouteItemSelectListener);
        ArrayAdapter routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
     
        directionSpinner = (Spinner) findViewById(R.id.directionSpinner);
        directionSpinner.setOnItemSelectedListener(DirectionItemSelectListener);
        ArrayAdapter directionAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinner.setAdapter(directionAdapter);
        
        endPointSpinner = (Spinner) findViewById(R.id.endPointSpinner);
        ArrayAdapter endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        endPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endPointSpinner.setAdapter(routeAdapter);
        
        config = new NextBusObserverConfig(MBTAWidgetConfig.this, mAppWidgetId);
        new UpdateAgencies().execute(config);

        Log.i(TAG, "End OnCreate");
    }

    View.OnClickListener SaveOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MBTAWidgetConfig.this;

            // Until there are UI selectors, we'll start in 5 seconds and run for 10 minutes.
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, 5); // start 5 seconds from now
			int trigger_time = CalendarUtils.getTimeFromBeginingOfDay(cal);
			int end_time = trigger_time + 600; // 10 minutes
			
			config.setStartObserving(trigger_time);
			config.setStopObserving(end_time);
			
            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            savePreferences(context, mAppWidgetId);

            // Push widget update to surface with newly set prefix
            //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);

			Intent serviceIntent = new Intent(getApplicationContext(), AlarmSchedulerService.class);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_WIDGET_ID, mAppWidgetId);
			serviceIntent.putExtra(AlarmSchedulerService.EXTRA_DAY_START_TIME, trigger_time);
			startService(serviceIntent);
			
            finish();
        }
    };

    View.OnClickListener CancelOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
    
    ListView.OnItemSelectedListener AgencyItemSelectListener = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			updateRoute();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			updateRoute();
		}
    };
    
    ListView.OnItemSelectedListener RouteItemSelectListener = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			updateDirection();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			updateDirection();
		}
    };
    
    ListView.OnItemSelectedListener DirectionItemSelectListener = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			updateEndPoint();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			updateEndPoint();
		}
    };
    
    // Write the prefix to the SharedPreferences object for this widget
    void savePreferences(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_ROUTE + appWidgetId, routeSpinner.getSelectedItem().toString());
        prefs.putString(PREF_START + appWidgetId, routeSpinner.getSelectedItem().toString());
        prefs.putString(PREF_END + appWidgetId, routeSpinner.getSelectedItem().toString());
        prefs.commit();
        
        int pos;
        pos = endPointSpinner.getSelectedItemPosition();
        config.setStop((NextBusStop) endPointAdapter.getItem(pos));
        config.save();
        Log.i(TAG, config.getStops().toString());
    }

    void updateRoute() {
    	if(agencySpinner.getSelectedItemPosition() > 0) {
    		int pos = agencySpinner.getSelectedItemPosition();
            config.setAgency((NextBusAgency) agencyAdapter.getItem(pos));
    		new UpdateRoutes().execute(config);
    	} else {
    		// No Route Selected
    		ArrayAdapter routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
    		routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            routeSpinner.setAdapter(routeAdapter);
    	}
    }
    
    void updateDirection() {
    	if(routeSpinner.getSelectedItemPosition() > 0) {
    		int pos = routeSpinner.getSelectedItemPosition();
            config.setRoute((NextBusRoute) routeAdapter.getItem(pos));
    		new UpdateDirection().execute(config);
    	} else {
    		// No Route Selected
    		ArrayAdapter directionAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
    		directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            directionSpinner.setAdapter(directionAdapter);
    	}
    	
    	
    }
    
    void updateEndPoint() {
    	if(directionSpinner.getSelectedItemPosition() > 0) {
    		int pos = directionSpinner.getSelectedItemPosition();
            config.setDirection((NextBusDirection) directionAdapter.getItem(pos));
    		new UpdateEndPoint().execute(config);
    	} else {
    		// No Route Selected
    		ArrayAdapter endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
    		endPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            endPointSpinner.setAdapter(endPointAdapter);
    	}
    }
    
    private class UpdateAgencies extends AsyncTask<NextBusObserverConfig, Void, List<NextBusAgency>> {
		@Override
		protected List<NextBusAgency> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getAgencies();
		}

        protected void onPostExecute(List<NextBusAgency> result) {
        	agencyAdapter = new BaseItemAdapter(MBTAWidgetConfig.this, android.R.layout.simple_spinner_item, result);
        	agencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	agencySpinner.setAdapter(agencyAdapter);
        }
    }
    
    private class UpdateRoutes extends AsyncTask<NextBusObserverConfig, Void, List<NextBusRoute>> {
		@Override
		protected List<NextBusRoute> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getRoutes();
		}

        protected void onPostExecute(List<NextBusRoute> result) {
        	routeAdapter = new BaseItemAdapter(MBTAWidgetConfig.this, android.R.layout.simple_spinner_item, result);
        	routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	routeSpinner.setAdapter(routeAdapter);
        }
    }
    
    private class UpdateDirection extends AsyncTask<NextBusObserverConfig, Void, List<NextBusDirection>> {
		@Override
		protected List<NextBusDirection> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getDirections();
		}

        protected void onPostExecute(List<NextBusDirection> result) {
        	directionAdapter = new BaseItemAdapter(MBTAWidgetConfig.this, android.R.layout.simple_spinner_item, result);
        	directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	directionSpinner.setAdapter(directionAdapter);
        }
    }
    
    private class UpdateEndPoint extends AsyncTask<NextBusObserverConfig, Void, List<NextBusStop>> {
		@Override
		protected List<NextBusStop> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getStops();
		}

        protected void onPostExecute(List<NextBusStop> result) {
        	endPointAdapter = new BaseItemAdapter(MBTAWidgetConfig.this, android.R.layout.simple_spinner_item, result);
        	endPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	endPointSpinner.setAdapter(endPointAdapter);
        }
    }
}