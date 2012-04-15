package com.transitwidget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

import com.transitwidget.adapters.BaseItemAdapter;
import com.transitwidget.prefs.NextBusAgency;
import com.transitwidget.prefs.NextBusDirection;
import com.transitwidget.prefs.NextBusObserverConfig;
import com.transitwidget.prefs.NextBusRoute;
import com.transitwidget.prefs.NextBusStop;
import com.transitwidget.prefs.NextBusValue;
import com.transitwidget.service.AlarmSchedulerService;
import com.transitwidget.utils.CalendarUtils;

/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class WidgetConfigActivity extends Activity {
    static final String TAG = "MBTAWidgetConfig";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    NoDefaultSpinner agencySpinner;
    NoDefaultSpinner routeSpinner;
    NoDefaultSpinner directionSpinner;
    NoDefaultSpinner stopSpinner;
    
    Button saveButton;
    
    BaseItemAdapter<? extends NextBusValue> routeAdapter;
    BaseItemAdapter<? extends NextBusValue> agencyAdapter;
    BaseItemAdapter<? extends NextBusValue> directionAdapter;
    BaseItemAdapter<? extends NextBusValue> endPointAdapter;
    NextBusObserverConfig config;
    
    public WidgetConfigActivity() {
        super();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    }

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                final Context context = WidgetConfigActivity.this;

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
        
        config = new NextBusObserverConfig(WidgetConfigActivity.this, mAppWidgetId);
        new UpdateAgencies().execute(config);

        Log.i(TAG, "End OnCreate");
    }

    private BaseItemAdapter<NextBusValue> createAdapter(List<? extends NextBusValue> data, Spinner spinner) {
		ArrayList<NextBusValue> tmp = new ArrayList<NextBusValue>();
		BaseItemAdapter<NextBusValue> adapter;
		
		tmp.add(new NextBusValue("Unspecified"));
		if(data != null) {
			tmp.addAll(data);
		}
		adapter =  new BaseItemAdapter<NextBusValue>(WidgetConfigActivity.this, android.R.layout.simple_spinner_item, tmp);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return adapter;
	}
    
	private void resetRouteSpinner() {
        routeSpinner.setEnabled(false);
		routeAdapter = createAdapter(null, routeSpinner);
	}

	private void resetStopSpinner() {
        stopSpinner.setEnabled(false);
        endPointAdapter = createAdapter(null, stopSpinner);
        saveButton.setEnabled(false);
	}

	private void resetDirectionSpinner() {
        directionSpinner.setEnabled(false);
		directionAdapter = createAdapter(null, directionSpinner);
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
        	agencyAdapter = createAdapter(result, agencySpinner);
        	agencySpinner.setEnabled(true);
        }
    }
    
    private class UpdateRoutes extends AsyncTask<NextBusObserverConfig, Void, List<NextBusRoute>> {
		@Override
		protected List<NextBusRoute> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getRoutes();
		}

        protected void onPostExecute(List<NextBusRoute> result) {
        	routeAdapter = createAdapter(result, routeSpinner);
        	routeSpinner.setEnabled(true);
        }
    }
    
    private class UpdateDirection extends AsyncTask<NextBusObserverConfig, Void, List<NextBusDirection>> {
		@Override
		protected List<NextBusDirection> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getDirections();
		}

        protected void onPostExecute(List<NextBusDirection> result) {
        	directionAdapter = createAdapter(result, directionSpinner);
        	directionSpinner.setEnabled(true);
        }
    }
    
    private class UpdateEndPoint extends AsyncTask<NextBusObserverConfig, Void, List<NextBusStop>> {
		@Override
		protected List<NextBusStop> doInBackground(NextBusObserverConfig... arg) {
			return arg[0].getStops();
		}

        protected void onPostExecute(List<NextBusStop> result) {
        	endPointAdapter = createAdapter(result, stopSpinner);
        	stopSpinner.setEnabled(true);
            saveButton.setEnabled(false);
        }
    }
}