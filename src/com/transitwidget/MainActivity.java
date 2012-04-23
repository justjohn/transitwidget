package com.transitwidget;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.transitwidget.api.ServiceProvider;
import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.fragments.DirectionListFragment;
import com.transitwidget.fragments.FavoritesFragment;
import com.transitwidget.fragments.RouteListFragment;
import com.transitwidget.fragments.StopFragment;
import com.transitwidget.fragments.StopListFragment;
import com.transitwidget.utils.AdapterUtils;

public class MainActivity extends SherlockFragmentActivity implements RouteListFragment.Listener, DirectionListFragment.Listener, StopListFragment.Listener, OnBackStackChangedListener {
    public static final String PREFS = "prefs";

    private static final String TAG = MainActivity.class.getName();

    private static final String STATE_TAG = "tag";
    private static final String STATE_ROUTE = "route";
    private static final String STATE_DIRECTION = "direction";
    private static final String STATE_DIRECTION_TITLE = "directionTitle";

    private static final String TAG_ROUTES = "routes";
    private static final String TAG_FAVORITES = "favorites";
	
    private LoadAgencyTask loadAgencyTask = new LoadAgencyTask();
    
    private TextView breadcrumbs;
    private NoDefaultSpinner agencySpinner;
    private SimpleCursorAdapter agencyAdapter;

    /** The tab/fragment currently active (one of TAG_ROUTES/TAG_FAVORITES/etc...) */
    private String mTag = null;
    
    private String mAgency = null;

    // The routes tab
    private String mRoute = null;
    private String mDirection = null;
    /** Short name of direction. */
    private String mDirectionTitle = null;
    private String mStop = null;

    private ActionBar actionBar;
    private FragmentManager mFragmentManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);
        
        breadcrumbs = (TextView) findViewById(R.id.breadcrumbs);
        
        mAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getString("agencyTag", null);
        
        // Check if anything has been saved...
        if (savedInstanceState != null) {
            mTag = savedInstanceState.getString(STATE_TAG);
            mRoute = savedInstanceState.getString(STATE_ROUTE);
            mDirection = savedInstanceState.getString(STATE_DIRECTION);
            mDirectionTitle = savedInstanceState.getString(STATE_DIRECTION_TITLE);
        }
        
        // default to routes browser tab
    	if (mTag == null) mTag = TAG_ROUTES;
    	
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
        		.setText("Routes")
        		.setIcon(R.drawable.notepad)
        		.setTabListener(new TabListener() {
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
			public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
				// load fragment
		    	mTag = TAG_ROUTES;
				loadBasedOnPrefs();
			}
		}));
        actionBar.addTab(actionBar.newTab()
        		.setText("Favorites")
        		.setIcon(R.drawable.heart)
        		.setTabListener(new TabListener() {
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
			public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
				// load fragment
		    	mTag = TAG_FAVORITES;
				loadFavorites();
			}
		}));
        
        agencySpinner = (NoDefaultSpinner) findViewById(R.id.agencySpinner);
        agencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
				Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(Agency.CONTENT_URI, id), new String[] { Agency.TAG }, null, null, null);
				String tag = null;
				if (cursor.moveToFirst()) {
					tag = cursor.getString(0);
				}

				Log.i(TAG, "Saving selected agency with id " + id + " and tag " + tag);
				getSharedPreferences(PREFS, MODE_PRIVATE).edit()
						.putLong("agency", id)
						.putString("agencyTag", tag)
					.commit();
				
				if (tag != null) {
					mAgency = tag;
					loadRouteList();
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {}
		});        
        loadAgencyTask.execute("");
    }
    
    private void loadBasedOnPrefs() {
    	
    	if (mTag.equals(TAG_ROUTES)) {
	        if (mDirection != null) {
	        	directionSelected(mDirection);
	        } else if (mRoute != null) {
	        	routeSelected(mRoute);
	        } else if (mAgency != null) {
	        	loadRouteList();
	        }
    	} else if (mTag.equals(TAG_FAVORITES)) {
    		// favorites
    		loadFavorites();
    	}
    }
    
    private void loadFavorites() {
    	Bundle args = new Bundle();
    	args.putString(RouteListFragment.ARG_AGENCY_TAG, mAgency);
    	Fragment fragment = Fragment.instantiate(this, FavoritesFragment.class.getName(), args);
    	loadFragment(fragment, false);
    }
    
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (loadAgencyTask.dialog != null) loadAgencyTask.dialog.cancel();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	outState.putString(STATE_TAG, mTag);
    	outState.putString(STATE_DIRECTION, mDirection);
    	outState.putString(STATE_DIRECTION_TITLE, mDirectionTitle);
    	outState.putString(STATE_ROUTE, mRoute);
    }
    
    public void loadRouteList() {
    	Bundle args = new Bundle();
    	args.putString(RouteListFragment.ARG_AGENCY_TAG, mAgency);
    	Fragment fragment = Fragment.instantiate(this, RouteListFragment.class.getName(), args);
    	loadFragment(fragment, false);
    }

	public void routeSelected(String routeTag) {
		Log.i(TAG, "Route selected: " + routeTag);
		mRoute = routeTag;
		mDirection = null;
        mDirectionTitle = null;
		mStop = null;

    	Bundle args = new Bundle();
    	args.putString(DirectionListFragment.ARG_AGENCY_TAG, mAgency);
    	args.putString(DirectionListFragment.ARG_ROUTE_TAG, routeTag);
    	Fragment fragment = Fragment.instantiate(this, DirectionListFragment.class.getName(), args);
    	loadFragment(fragment, true);
		
	}

	public void directionSelected(String tag) {
		Log.i(TAG, "Direction selected: " + tag);
		mDirection = tag;
		mStop = null;
        
        // Lookup title for direction
        String selection = Direction.AGENCY + " = ? AND " + Direction.ROUTE + " = ? AND " + Direction.TAG + " = ?";
        String[] selectionArgs = { mAgency, mRoute, mDirection };
        Cursor c = getContentResolver().query(Direction.CONTENT_URI, null, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            mDirectionTitle = new Direction(c, this).getTitle();
        } else {
            Log.e(TAG, "Unable to lookup direction with tag " + mDirection);
            mDirectionTitle = mDirection;
        }
        c.close();

    	Bundle args = new Bundle();
    	args.putString(StopListFragment.ARG_AGENCY_TAG, mAgency);
    	args.putString(StopListFragment.ARG_DIRECTION_TAG, tag);
    	Fragment fragment = Fragment.instantiate(this, StopListFragment.class.getName(), args);
    	loadFragment(fragment, true);
	}

	public void stopSelected(String tag) {
		Log.i(TAG, "Stop selected: " + tag);
		mStop = tag;
		
    	Bundle args = new Bundle();
    	args.putString(StopFragment.ARG_AGENCY_TAG, mAgency);
    	args.putString(StopFragment.ARG_ROUTE_TAG, mRoute);
    	args.putString(StopFragment.ARG_DIRECTION_TAG, mDirection);
    	args.putString(StopFragment.ARG_STOP_TAG, mStop);
    	Fragment fragment = Fragment.instantiate(this, StopFragment.class.getName(), args);
    	loadFragment(fragment, true);
	}
	
	/**
	 * Builds a bread crumb based on the currently selected tab/route/direction.
	 * 
	 * @param backStackEmpty Is the back stack empty (that is, are we at the root of the view tree.)
	 * @return The bread crumb text.
	 */
	private String buildBreadCrumb(boolean backStackEmpty) {
		StringBuilder text = new StringBuilder();
		if (mTag.equals(TAG_ROUTES)) {
			if (backStackEmpty) {
				text.append("Select a Route");
                
			} else {
				if (mRoute != null) text.append("Route ").append(mRoute);
				if (mDirectionTitle != null) text.append(" / ").append(mDirectionTitle);
			}
		} else if (mTag.equals(TAG_FAVORITES)) {
			text.append("Select a Favorite");
		}
		return text.toString();
	}

    
	/**
	 * Load a fragment.
	 * 
	 * @param fragment The fragment to load.
	 * @param addToBackStack Should the fragment be added to the back stack. If false, the back stack will be reset.
	 */
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
    	FragmentTransaction ft = mFragmentManager.beginTransaction();
    	ft.replace(R.id.main_fragment_container, fragment)
    		.setBreadCrumbTitle(buildBreadCrumb(false));
    	if (addToBackStack) {
    		ft.addToBackStack(fragment.getClass().getName());
    	} else {
    		clearBackStack();
    	}
    	ft.commit();
    	
    	// trigger breadcrumbs to update
    	onBackStackChanged();
    }
    
    /**
     * Remove all fragment transactions from the back stack.
     */
    public void clearBackStack() {
		while(mFragmentManager.getBackStackEntryCount() > 0) mFragmentManager.popBackStackImmediate();
    }
    
	public void onBackStackChanged() {
		int count = mFragmentManager.getBackStackEntryCount();
		CharSequence title;
		if (count >= 1) { 
			title = mFragmentManager.getBackStackEntryAt(count - 1).getBreadCrumbTitle();
		} else {
			title = buildBreadCrumb(true);
		}
		breadcrumbs.setText(title);
	}

    private class LoadAgencyTask extends AsyncTask<String, Integer, Cursor> {
		public ProgressDialog dialog;
        @Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MainActivity.this, "", "Loading Transit Agencies. Please wait...", true);
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
        @Override
		protected void onPostExecute(Cursor cursor) {
	    	dialog.dismiss();
	        long selectedAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getLong("agency", -1);
			Log.i(TAG, "get selected agency with id " + selectedAgency);
	        
	        startManagingCursor(cursor);
	        agencyAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.single_list_item, cursor, new String[] {Agency.TITLE}, new int[] {R.id.value});
	        agencySpinner.setAdapter(agencyAdapter);
	        
	        int position = 0;
	        if (selectedAgency > 0) {
	        	position = AdapterUtils.getAdapterPositionById(agencySpinner.getAdapter(), selectedAgency);
	        }
	        agencySpinner.setSelection(position);
		}
	}
}
