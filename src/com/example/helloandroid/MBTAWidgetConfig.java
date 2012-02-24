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
import android.widget.Button;
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
import com.example.helloandroid.utils.CalendarUtils;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class MBTAWidgetConfig extends Activity {
    static final String TAG = "MBTAWidgetConfig";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    NoDefaultSpinner agencySpinner;
    NoDefaultSpinner routeSpinner;
    NoDefaultSpinner directionSpinner;
    NoDefaultSpinner stopSpinner;
    
    Button saveButton;
    
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

        // Set the view layout resource to use.
        setContentView(R.layout.widget_config);

        // Bind the action for the save button.
        saveButton = (Button) findViewById(R.id.saveBtn);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
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
        });

        findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
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
        
        agencySpinner = (NoDefaultSpinner) findViewById(R.id.agencySpinner);
        routeSpinner = (NoDefaultSpinner) findViewById(R.id.routeSpinner);
        directionSpinner = (NoDefaultSpinner) findViewById(R.id.directionSpinner);
        stopSpinner = (NoDefaultSpinner) findViewById(R.id.endPointSpinner);

        resetRouteSpinner();
        resetDirectionSpinner();
        resetStopSpinner();
        
        agencySpinner.setEnabled(false);
        agencySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
    			Log.i(TAG, "Agency selected: " + id);
    	        resetRouteSpinner();
    	        resetDirectionSpinner();
    	        resetStopSpinner();
    			updateRoute();
    		}
    		public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        routeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
    			Log.i(TAG, "Route selected: " + id);
    	        resetDirectionSpinner();
    	        resetStopSpinner();
    			updateDirection();
    		}
    		public void onNothingSelected(AdapterView<?> arg0) {}
        });
     
        directionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    	        resetStopSpinner();
    			updateStop();
    		}
    		public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        stopSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
		        saveButton.setEnabled(true);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
		        saveButton.setEnabled(false);
			}
        });
        
        config = new NextBusObserverConfig(MBTAWidgetConfig.this, mAppWidgetId);
        new UpdateAgencies().execute(config);

        Log.i(TAG, "End OnCreate");
    }

	private void resetRouteSpinner() {
        routeSpinner.setEnabled(false);
		ArrayAdapter routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
	}

	private void resetStopSpinner() {
        stopSpinner.setEnabled(false);
		ArrayAdapter stopAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        stopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stopSpinner.setAdapter(stopAdapter);

        saveButton.setEnabled(false);
	}

	private void resetDirectionSpinner() {
        directionSpinner.setEnabled(false);
		ArrayAdapter directionAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directionSpinner.setAdapter(directionAdapter);
	}
    
    // Write the prefix to the SharedPreferences object for this widget
    void savePreferences(Context context, int appWidgetId) {
        int pos;
        pos = stopSpinner.getSelectedItemPosition();
        config.setStop((NextBusStop) endPointAdapter.getItem(pos));
        config.save();
        Log.i(TAG, config.getStops().toString());
    }

    void updateRoute() {
    	if(agencySpinner.getSelectedItemPosition() >= 0) {
    		int pos = agencySpinner.getSelectedItemPosition();
            config.setAgency((NextBusAgency) agencyAdapter.getItem(pos));
    		new UpdateRoutes().execute(config);
    	}
    }
    
    void updateDirection() {
    	if(routeSpinner.getSelectedItemPosition() >= 0) {
    		int pos = routeSpinner.getSelectedItemPosition();
            config.setRoute((NextBusRoute) routeAdapter.getItem(pos));
    		new UpdateDirection().execute(config);
    	}
    }
    
    void updateStop() {
    	if(directionSpinner.getSelectedItemPosition() >= 0) {
    		int pos = directionSpinner.getSelectedItemPosition();
            config.setDirection((NextBusDirection) directionAdapter.getItem(pos));
    		new UpdateEndPoint().execute(config);
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
        	agencySpinner.setEnabled(true);
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
        	routeSpinner.setEnabled(true);
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
        	directionSpinner.setEnabled(true);
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
        	stopSpinner.setAdapter(endPointAdapter);
        	stopSpinner.setEnabled(true);
            saveButton.setEnabled(false);
        }
    }
}