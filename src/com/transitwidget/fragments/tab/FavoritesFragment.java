package com.transitwidget.fragments.tab;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.transitwidget.MainActivity;
import com.transitwidget.R;
import com.transitwidget.fragments.FavoritesListFragment;
import com.transitwidget.fragments.RouteListFragment;
import com.transitwidget.fragments.StopFragment;
import com.transitwidget.fragments.StopListFragment;

/**
 *
 * @author john
 */
public class FavoritesFragment extends SherlockFragment
        implements FragmentManager.OnBackStackChangedListener, 
        StopListFragment.Listener {
    
    private static final String TAG = FavoritesFragment.class.getName();
    
    private static final String STATE_ROUTE = "route";
    private static final String STATE_STOP = "stop";
    private static final String STATE_DIRECTION = "direction";
    private static final String STATE_FAVORITE = "favorite";
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
     * The currently selected favorite.
     */
    private String mFavorite = null;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab_frame, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
            mFavorite = savedInstanceState.getString(STATE_FAVORITE);
        }
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_STOP, mStop);
        outState.putString(STATE_ROUTE, mRoute);
        outState.putString(STATE_DIRECTION, mDirection);
        outState.putString(STATE_FAVORITE, mFavorite);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        String oldAgency = mAgency;
        mAgency = mActivity.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE).getString("agencyTag", null);
        
        Log.i(TAG, "onResume with agency " + mAgency);
        
        if (mAgency != null) {
            loadFavorites();
        }
    }
    
    private void loadFavorites() {
        mFavorite = null;

        Bundle args = new Bundle();
        args.putString(RouteListFragment.ARG_AGENCY_TAG, mAgency);
        FavoritesListFragment fragment = new FavoritesListFragment(this, args);
        loadFragment(fragment, false);
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
        ft.replace(R.id.frame_fragment_container, fragment);
                //.setBreadCrumbTitle(buildBreadCrumb(false));
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
            //title = buildBreadCrumb(true);
        }
        //breadcrumbs.setText(title);
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
}
