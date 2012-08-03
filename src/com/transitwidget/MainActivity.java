package com.transitwidget;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.transitwidget.api.ServiceProvider;
import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.fragments.AgencyListFragment;
import com.transitwidget.fragments.DirectionListFragment;
import com.transitwidget.fragments.FavoritesFragment;
import com.transitwidget.fragments.RouteListFragment;
import com.transitwidget.fragments.StopFragment;
import com.transitwidget.fragments.StopListFragment;

public class MainActivity extends SherlockFragmentActivity implements RouteListFragment.Listener,
                                                                    DirectionListFragment.Listener,
                                                                    StopListFragment.Listener,
                                                                    AgencyListFragment.Listener,
                                                                    OnBackStackChangedListener {
  public static final String PREFS = "prefs";

  private static final String TAG = MainActivity.class.getName();

  private static final String STATE_TAG = "tag";
  private static final String STATE_ROUTE = "route";
  private static final String STATE_STOP = "stop";
  private static final String STATE_DIRECTION = "direction";
  private static final String STATE_DIRECTION_TITLE = "directionTitle";

  private static final String TAG_ROUTES = "routes";
  private static final String TAG_FAVORITES = "favorites";
  private static final String TAG_AGENCY = "agency";

  private LoadAgencyTask loadAgencyTask = new LoadAgencyTask();

  private TextView breadcrumbs;

  /** The tab/fragment currently active (one of TAG_ROUTES/etc...) */
  private String mTag = null;

  /** Selected transit agency. */
  private String mAgency = null;
  
  /** Currently selected route. */
  private String mRoute = null;
  /** Currently selected route direction. */
  private String mDirection = null;
  /** Currently selected stop. */
  private String mStop = null;
  
  /** Short name of direction. */
  private String mDirectionTitle = null;

  private ActionBar actionBar;
  private FragmentManager mFragmentManager;

  /** Favorites tab. */
  private Tab mFavoriteTab;
  /** Route/Direction/Stop tab. */
  private Tab mRouteTab;
  /** Agency select tab. */
  private Tab mAgencyTab;

  /** Should changing tabs reset.  Will be set to false after a configuration change. */
  private boolean mReset = true;

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
      mStop = savedInstanceState.getString(STATE_STOP);

      mReset = false;
    }
    
    if (mTag == null && mAgency != null) { 
        mTag = TAG_ROUTES; // default to routes
    }

    actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    mRouteTab = actionBar.newTab().setText("Routes")
                                       .setIcon(R.drawable.notepad)
                                       .setTabListener(new TabListener() {
      public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
          // mReset is used to prevent state from being lost on a configuration change (screen rotation).
          if (mReset) {
            mStop = null;
            mDirection = null;
            mDirectionTitle = null;
            mRoute = null;
            // load fragment
            mTag = TAG_ROUTES;

            clearBackStack();
            loadSelected();
          } else {
              mReset = true;
          }
      }
    });

    mFavoriteTab = actionBar.newTab().setText("Favorites")
                                       .setIcon(R.drawable.heart)
                                       .setTabListener(new TabListener() {
      public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        // load fragment
        mTag = TAG_FAVORITES;
        clearBackStack();
        loadFavorites();
      }
    });
    
    mAgencyTab = actionBar.newTab().setText("Select Transit Agency")
                                    .setTabListener(new TabListener() {
      public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
      public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        mTag = TAG_AGENCY;
      }
    });

    if (mAgency == null) {
      actionBar.addTab(mAgencyTab);

      loadAgencyTask.execute();

    } else {
      actionBar.addTab(mRouteTab);
      actionBar.addTab(mFavoriteTab);

      if (mTag.equals(TAG_FAVORITES)) {
        actionBar.selectTab(mFavoriteTab);
      } else {
        actionBar.selectTab(mRouteTab);
      }
    }
  }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        // Check if anything has been saved...
        if (savedInstanceState != null) {
          mTag = savedInstanceState.getString(STATE_TAG);
          mRoute = savedInstanceState.getString(STATE_ROUTE);
          mDirection = savedInstanceState.getString(STATE_DIRECTION);
          mDirectionTitle = savedInstanceState.getString(STATE_DIRECTION_TITLE);
          mStop = savedInstanceState.getString(STATE_STOP);
        }
    }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_agency:
        selectAgency();
        break;
    }
    return super.onMenuItemSelected(featureId, item);
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
    } else {
      selectAgency();
    }
  }

  private void loadFavorites() {
    mStop = null;
    mDirection = null;
    mDirectionTitle = null;
    mRoute = null;

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
    outState.putString(STATE_STOP, mStop);
  }

  /**
   * Load agency selection fragment.
   */
  public void selectAgency() {
    actionBar.removeAllTabs();
    actionBar.addTab(mAgencyTab);
    
    Fragment fragment = Fragment.instantiate(this, AgencyListFragment.class.getName());
    loadFragment(fragment, false);
  }

  /**
   * Callback with selected agency.
   * @param tag
   */
  public void agencySelected(String tag) {
    mAgency = tag;
    getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putString("agencyTag", mAgency).commit();

    actionBar.removeAllTabs();

    actionBar.addTab(mRouteTab);
    actionBar.addTab(mFavoriteTab);

    // the tab select handler will load the route fragment
    actionBar.selectTab(mRouteTab);
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
    Fragment fragment = Fragment.instantiate(this, RouteListFragment.class.getName(), args);
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
      Fragment fragment = Fragment.instantiate(this, DirectionListFragment.class.getName(), args);
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
      Fragment fragment = Fragment.instantiate(this, StopFragment.class.getName(), args);
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
   * @param backStackEmpty Is the back stack empty (that is, are we at the root of the view tree.)
   * @return The bread crumb text.
   */
  private String buildBreadCrumb(boolean backStackEmpty) {
    StringBuilder text = new StringBuilder();
    if (mTag == null) {
      // leave empty untill agency is selected

    } else if (mTag.equals(TAG_ROUTES)) {
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
    while(mFragmentManager.getBackStackEntryCount() > 0)
      mFragmentManager.popBackStackImmediate();
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
      }

      cursor.close();

      return cursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor) {
      dialog.dismiss();

      if (mAgency == null) {
        selectAgency();
      }
    }
  }
}
