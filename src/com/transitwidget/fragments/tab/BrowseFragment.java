package com.transitwidget.fragments.tab;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.transitwidget.MainActivity;
import com.transitwidget.R;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.fragments.DirectionListFragment;
import com.transitwidget.fragments.RouteListFragment;
import com.transitwidget.fragments.StopFragment;
import com.transitwidget.fragments.StopListFragment;

/**
 * Wraps route/direction/stop browser.
 * 
 * @author john
 */
public class BrowseFragment extends SherlockFragment implements
        RouteListFragment.Listener,
        DirectionListFragment.Listener,
        StopListFragment.Listener,
        FragmentManager.OnBackStackChangedListener {
    
    private static final String TAG = BrowseFragment.class.getName();
    
    private static final String STATE_ROUTE = "route";
    private static final String STATE_STOP = "stop";
    private static final String STATE_DIRECTION = "direction";
    private static final String STATE_DIRECTION_TITLE = "directionTitle";
    
    /**
     * The containing activity.
     */
    private Activity mActivity = null;
    /**
     * The fragment manager.
     */
    private FragmentManager mFragmentManager = null;
    /**
     * Selected transit agency.
     */
    private String mAgency = null;
    /**
     * Currently selected route.
     */
    private String mRoute = null;
    /**
     * Currently selected route direction.
     */
    private String mDirection = null;
    /**
     * Currently selected stop.
     */
    private String mStop = null;
    /**
     * Short name of direction.
     */
    private String mDirectionTitle = null;
    
    /**
     * Show what route/direction is selected.
     */
    private TextView breadcrumbs;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab_browse, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        
        breadcrumbs = (TextView) view.findViewById(R.id.breadcrumbs);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mFragmentManager = getActivity().getSupportFragmentManager();
        
        mFragmentManager.addOnBackStackChangedListener(this);
        
        mAgency = mActivity.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getString("agencyTag", null);
        
        // Check if anything has been saved...
        if (savedInstanceState != null) {
            mStop = savedInstanceState.getString(STATE_STOP);
            mRoute = savedInstanceState.getString(STATE_ROUTE);
            mDirection = savedInstanceState.getString(STATE_DIRECTION);
            mDirectionTitle = savedInstanceState.getString(STATE_DIRECTION_TITLE);
        }
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_STOP, mStop);
        outState.putString(STATE_ROUTE, mRoute);
        outState.putString(STATE_DIRECTION, mDirection);
        outState.putString(STATE_DIRECTION_TITLE, mDirectionTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        String oldAgency = mAgency;
        mAgency = mActivity.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getString("agencyTag", null);
        
        Log.i(TAG, "onResume with agency " + mAgency);
        
        if (mAgency != null) {
            loadSelected();
        }
    }
    
    /**
     * Reset after a configuration change.
     */
    private void loadSelected() {
        Log.i(TAG, "loadSelected -> Stop: " + mStop + ", Direction: " + mDirection + ", Route: " + mRoute + ", Agency: " + mAgency);

        if (mStop != null) {
            stopSelected(mStop);
        } else if (mDirection != null) {
            directionSelected(mDirection);
        } else if (mRoute != null) {
            routeSelected(mRoute);
        } else if (mAgency != null) {
            agencySelected();
        }
    }

    /**
     * Load route list with selected agency.
     */
    public void agencySelected() {
        mStop = null;
        mDirection = null;
        mDirectionTitle = null;
        mRoute = null;

        Bundle args = new Bundle();
        args.putString(RouteListFragment.ARG_AGENCY_TAG, mAgency);
        RouteListFragment fragment = new RouteListFragment(this, args);
        loadFragment(fragment, false);
    }

    /**
     * Load direction list for selected route.
     *
     * @param routeTag
     */
    public void routeSelected(String routeTag) {
        Log.i(TAG, "Route selected: " + routeTag);
        mRoute = routeTag;
        mDirection = null;
        mDirectionTitle = null;
        mStop = null;

        Bundle args = new Bundle();
        args.putString(DirectionListFragment.ARG_AGENCY_TAG, mAgency);
        args.putString(DirectionListFragment.ARG_ROUTE_TAG, routeTag);
        DirectionListFragment fragment = new DirectionListFragment(this, args);
        loadFragment(fragment, true);

    }

    /**
     * Load stop list with selected direction.
     *
     * @param tag
     */
    public void directionSelected(String tag) {
        Log.i(TAG, "Direction selected: " + tag);
        mDirection = tag;
        mStop = null;

        // Lookup title for direction
        String selection = Direction.AGENCY + " = ? AND " + Direction.ROUTE + " = ? AND " + Direction.TAG + " = ?";
        String[] selectionArgs = {mAgency, mRoute, mDirection};
        Cursor c = mActivity.getContentResolver().query(Direction.CONTENT_URI, null, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            mDirectionTitle = new Direction(c, mActivity).getTitle();
        } else {
            Log.e(TAG, "Unable to lookup direction with tag " + mDirection);
            mDirectionTitle = mDirection;
        }
        c.close();

        Bundle args = new Bundle();
        args.putString(StopListFragment.ARG_AGENCY_TAG, mAgency);
        args.putString(StopListFragment.ARG_DIRECTION_TAG, tag);
        StopListFragment fragment = new StopListFragment(this, args);
        loadFragment(fragment, true);
    }

    /**
     * Load stop details fragment.
     *
     * @param tag
     */
    public void stopSelected(String tag) {
        Log.i(TAG, "Stop selected: " + tag);
        mStop = tag;

        Bundle args = new Bundle();
        args.putString(StopFragment.ARG_AGENCY_TAG, mAgency);
        args.putString(StopFragment.ARG_ROUTE_TAG, mRoute);
        args.putString(StopFragment.ARG_DIRECTION_TAG, mDirection);
        args.putString(StopFragment.ARG_STOP_TAG, mStop);
        Fragment fragment = Fragment.instantiate(mActivity, StopFragment.class.getName(), args);
        loadFragment(fragment, true);
    }

    public void stopSelected(String tag, String direction, String route) {
        Log.i(TAG, "Stop selected: " + tag + " with direction: " + direction + " and route: " + route);
        mStop = tag;
        mRoute = route;
        mDirection = direction;

        stopSelected(tag);
    }
    
    /**
     * Builds a bread crumb based on the currently selected tab/route/direction.
     *
     * @param backStackEmpty Is the back stack empty (that is, are we at the
     * root of the view tree.)
     * @return The bread crumb text.
     */
    private String buildBreadCrumb(boolean backStackEmpty) {
        StringBuilder text = new StringBuilder();
        if (backStackEmpty || mRoute == null) {
            text.append("Select a Route");

        } else {
            text.append("Route ").append(mRoute);

            if (mDirectionTitle != null) {
                text.append(" / ").append(mDirectionTitle);
            }
        }
        return text.toString();
    }
    
    /**
     * Load a fragment.
     *
     * @param fragment The fragment to load.
     * @param addToBackStack Should the fragment be added to the back stack. If
     * false, the back stack will be reset.
     */
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.replace(R.id.browse_fragment_container, fragment)
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
        while (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStackImmediate();
        }
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
}
