package com.example.helloandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.helloandroid.api.ServiceProvider;
import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.fragments.DirectionListFragment;
import com.example.helloandroid.fragments.RouteListFragment;
import com.example.helloandroid.fragments.StopListFragment;
import com.example.helloandroid.utils.AdapterUtils;

public class MainActivity extends FragmentActivity implements RouteListFragment.Listener, DirectionListFragment.Listener, StopListFragment.Listener {
    private static final String PREFS = "prefs";

	private static final String TAG = MainActivity.class.getName();

	private static final String STATE_ROUTE = "route";
	private static final String STATE_DIRECTION = "direction";
    
    private NoDefaultSpinner agencySpinner;
    private SimpleCursorAdapter agencyAdapter;

	private String mAgency = null;
	private String mRoute = null;
	private String mDirection = null;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        agencySpinner = (NoDefaultSpinner) findViewById(R.id.agencySpinner);
        agencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(Agency.CONTENT_URI, id), new String[] { Agency.TAG }, null, null, null);
				String tag = null;
				if (cursor.moveToFirst()) {
					tag = cursor.getString(0);
				}

				getSharedPreferences(PREFS, MODE_PRIVATE).edit()
						.putLong("agency", id)
						.putString("agencyTag", tag)
					.commit();
				
				if (tag != null) {
					mAgency = tag;
					loadRouteList();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().commit();
			}
		});
        
        mAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getString("agencyTag", null);
        
        Log.i(TAG, "Got state " + savedInstanceState);
        
        // Check if anything has been saved...
        if (savedInstanceState != null) {
	        mRoute = savedInstanceState.getString(STATE_ROUTE);
	        mDirection = savedInstanceState.getString(STATE_DIRECTION);
        }
        
        if (mDirection != null) {
        	directionSelected(mDirection);
        } else if (mRoute != null) {
        	routeSelected(mRoute);
        } else if (mAgency != null) {
        	loadRouteList();
        }
        loadAgencyList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	outState.putString(STATE_DIRECTION, mDirection);
    	outState.putString(STATE_ROUTE, mRoute);
    }
    
    public void loadAgencyList() {
    	final Activity activity = this;
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		ProgressDialog dialog;
    		protected void onPreExecute() {
    			dialog = ProgressDialog.show(activity, "", "Loading Transit Agencies. Please wait...", true);
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    	        Cursor cursor = getContentResolver().query(Agency.CONTENT_URI, null, null, null, null);

    	    	if (cursor.getCount() == 0) {
    	    		// load from network and cache to DB.
    				ServiceProvider.getAgencies(getApplicationContext());
    		    	cursor.requery();
    	    	}
    	    	
    	    	return cursor;
    		}
    		protected void onPostExecute(Cursor cursor) {
    	        startManagingCursor(cursor);
    	        agencyAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.single_list_item, cursor, new String[] {Agency.TITLE}, new int[] {R.id.value});
    	        agencySpinner.setAdapter(agencyAdapter);
    	    	dialog.dismiss();

    	        long selectedAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getLong("agency", -1);
    	        int position = 0;
    	        if (selectedAgency > 0) {
    	        	position = AdapterUtils.getAdapterPositionById(agencySpinner.getAdapter(), selectedAgency);
    	        }
    	        agencySpinner.setSelection(position);
    		}
    	}.execute("");
    }
    
    private void loadFragment(Fragment fragment) {
    	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    	ft.replace(R.id.main_fragment_container, fragment);
    	ft.commit();
    }
    
    public void loadRouteList() {
    	Bundle args = new Bundle();
    	args.putString(RouteListFragment.ARG_AGENCY_TAG, mAgency);
    	Fragment fragment = Fragment.instantiate(this, RouteListFragment.class.getName(), args);
    	loadFragment(fragment);
    }

	public void routeSelected(String routeTag) {
		Log.i(TAG, "Route selected: " + routeTag);
		mRoute = routeTag;

    	Bundle args = new Bundle();
    	args.putString(DirectionListFragment.ARG_AGENCY_TAG, mAgency);
    	args.putString(DirectionListFragment.ARG_ROUTE_TAG, routeTag);
    	Fragment fragment = Fragment.instantiate(this, DirectionListFragment.class.getName(), args);
    	loadFragment(fragment);
		
	}

	public void directionSelected(String tag) {
		Log.i(TAG, "Direction selected: " + tag);
		mDirection = tag;

    	Bundle args = new Bundle();
    	args.putString(StopListFragment.ARG_AGENCY_TAG, mAgency);
    	args.putString(StopListFragment.ARG_DIRECTION_TAG, tag);
    	Fragment fragment = Fragment.instantiate(this, StopListFragment.class.getName(), args);
    	loadFragment(fragment);
	}

	public void stopSelected(String tag) {
		Log.i(TAG, "Stop selected: " + tag);
	}
}
