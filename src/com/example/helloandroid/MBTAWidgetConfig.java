package com.example.helloandroid;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;


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
    
    Spinner routeSpinner;
    Spinner agencySpinner;
    Spinner endPointSpinner;
    
    ArrayAdapter<CharSequence> routeAdapter;
    ArrayAdapter<CharSequence> agencyAdapter;
    ArrayAdapter<CharSequence> endPointAdapter;
    
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
        agencyAdapter = ArrayAdapter.createFromResource(this, R.array.default_agencies, android.R.layout.simple_spinner_item);
        agencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agencySpinner.setAdapter(agencyAdapter);
        
        Log.i(TAG, "config route spinner");
        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        routeSpinner.setOnItemSelectedListener(RouteItemSelectListener);
        routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
     
        endPointSpinner = (Spinner) findViewById(R.id.endPointSpinner);
        endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
        endPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endPointSpinner.setAdapter(routeAdapter);
        
        loadPreferences(MBTAWidgetConfig.this, mAppWidgetId);
        Log.i(TAG, "End OnCreate");
    }

    View.OnClickListener SaveOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MBTAWidgetConfig.this;

            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            savePreferences(context, mAppWidgetId);

            // Push widget update to surface with newly set prefix
            //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
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
    }

    void updateRoute() {
    	if(agencySpinner.getSelectedItemPosition() > 0) {
    		routeAdapter = ArrayAdapter.createFromResource(this, R.array.sample_routes, android.R.layout.simple_spinner_item);
    	} else {
    		// No Route Selected
    		routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
    	}
    	
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        routeSpinner.setAdapter(routeAdapter);
    }
    
    void updateEndPoint() {
    	if(routeSpinner.getSelectedItemPosition() > 0) {
    		endPointAdapter = ArrayAdapter.createFromResource(this, R.array.sample_points, android.R.layout.simple_spinner_item);
    	} else {
    		// No Route Selected
    		endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_spinner, android.R.layout.simple_spinner_item);
    	}
    	
    	endPointAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endPointSpinner.setAdapter(endPointAdapter);
    }
    
    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    void loadPreferences(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int pos;
        String route = prefs.getString(PREF_ROUTE + appWidgetId, null);
        pos = routeAdapter.getPosition(route);
        if(pos > 0) {
        	routeSpinner.setSelection(pos);
        } else {
        	routeSpinner.setSelection(0);
        }
        updateRoute();

    }
}