package com.example.helloandroid;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;


/**
 * The configuration screen for the ExampleAppWidgetProvider widget sample.
 */
public class MBTAWidgetConfig extends Activity {
    static final String TAG = "ExampleAppWidgetConfigure";

    private static final String PREFS_NAME
            = "com.mbta.widget.MBTAWidgetProvider";
    
    private static final String PREF_ROUTE = "prefix_route_";
    private static final String PREF_START = "prefix_start_";
    private static final String PREF_END = "prefix_end_";
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    Spinner routeSpinner;
    Spinner startPointSpinner;
    Spinner endPointSpinner;
    
    ArrayAdapter<CharSequence> routeAdapter;
    ArrayAdapter<CharSequence> startPointAdapter;
    ArrayAdapter<CharSequence> endPointAdapter;
    
    public MBTAWidgetConfig() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.widget_config);

        // Bind the action for the save button.
        findViewById(R.id.saveBtn).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        routeAdapter = ArrayAdapter.createFromResource(this, R.array.default_routes, R.id.routeSpinner);
        routeSpinner.setAdapter(routeAdapter);
        
        startPointSpinner = (Spinner) findViewById(R.id.startPointSpinner);
        startPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_points, R.id.startPointSpinner);
        startPointSpinner.setAdapter(startPointAdapter);
        
        endPointSpinner = (Spinner) findViewById(R.id.endPointSpinner);
        endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_points, R.id.endPointSpinner);
        endPointSpinner.setAdapter(endPointAdapter);
        
        loadPreferences(MBTAWidgetConfig.this, mAppWidgetId);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MBTAWidgetConfig.this;

            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            savePreferences(context, mAppWidgetId);

            // Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            
            /*MBTAWidgetProvider.updateAppWidget(context, appWidgetManager,
                    mAppWidgetId, titlePrefix);
             */
            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
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

    void updateEndPoints() {
    	if(routeSpinner.getSelectedItemPosition() > 0) {
            startPointAdapter = ArrayAdapter.createFromResource(this, R.array.sample_points, R.id.startPointSpinner);
            endPointAdapter = ArrayAdapter.createFromResource(this, R.array.sample_points, R.id.endPointSpinner);
    	} else {
    		// No Route Selected
    		startPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_points, R.id.startPointSpinner);
            endPointAdapter = ArrayAdapter.createFromResource(this, R.array.default_points, R.id.endPointSpinner);
    	}
        startPointSpinner.setAdapter(startPointAdapter);
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
        updateEndPoints();
        String startPoint = prefs.getString(PREF_START + appWidgetId, null);
        pos = startPointAdapter.getPosition(startPoint);
        if(pos > 0) {
        	startPointSpinner.setSelection(pos);
        } else {
        	startPointSpinner.setSelection(0);
        }
        String endPoint = prefs.getString(PREF_END + appWidgetId, null);
        pos = endPointAdapter.getPosition(endPoint);
        if(pos > 0) {
        	endPointSpinner.setSelection(pos);
        } else {
        	endPointSpinner.setSelection(0);
        }
    }
}