package com.transitwidget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.transitwidget.fragments.tab.BrowseFragment;
import com.transitwidget.fragments.tab.FavoritesFragment;

public class MainActivity extends SherlockFragmentActivity {
    private static final String TAG = MainActivity.class.getName();

    public static final String PREFS = "prefs";
    private static final String STATE_TAG = "tag";
    private static final String TAG_ROUTES = "routes";
    private static final String TAG_FAVORITES = "favorites";
    
    private static final int PAGER_ROUTE_POSITION = 0;
    private static final int PAGER_FAVORITE_POSITION = 1;
    
    /**
     * The tab/fragment currently active (one of TAG_ROUTES/etc...)
     */
    private String mTag = null;
    /**
     * Selected transit agency.
     */
    private String mAgency = null;
    
    private ActionBar mActionBar;
    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    
    /**
     * Favorites tab.
     */
    private Tab mFavoriteTab;
    /**
     * Route/Direction/Stop tab.
     */
    private Tab mRouteTab;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mViewPager = (ViewPager)findViewById(R.id.main_view_pager);
        mAdapter = new MyAdapter(getSupportFragmentManager());

        mAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getString("agencyTag", null);
        
        // Check if anything has been saved...
        if (savedInstanceState != null) {
            mTag = savedInstanceState.getString(STATE_TAG);
        }

        if (mTag == null) {
            mTag = TAG_ROUTES; // default to routes
        }

        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mRouteTab = mActionBar.newTab().setText("Routes")
                .setIcon(R.drawable.notepad)
                .setTabListener(new TabListener() {
            public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
            public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
            public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
                Log.i(TAG, "Selecting tab routes");
                mTag = TAG_ROUTES;
                if (mViewPager.getCurrentItem() != 0) {
                    mViewPager.setCurrentItem(PAGER_ROUTE_POSITION);
                }
            }
        });

        mFavoriteTab = mActionBar.newTab().setText("Favorites")
                .setIcon(R.drawable.heart)
                .setTabListener(new TabListener() {
            public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
            public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}
            public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
                Log.i(TAG, "Selecting tab favorites");
                mTag = TAG_FAVORITES;
                if (mViewPager.getCurrentItem() != 1) {
                    mViewPager.setCurrentItem(PAGER_FAVORITE_POSITION);
                }
            }
        });
        
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float f, int i1) {}
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        if (!mActionBar.getSelectedTab().equals(mRouteTab)) {
                            mActionBar.selectTab(mRouteTab);
                        }
                        break;
                    case 1:
                        if (!mActionBar.getSelectedTab().equals(mFavoriteTab)) {
                            mActionBar.selectTab(mFavoriteTab);
                        }
                        break;
                }
            }
            public void onPageScrollStateChanged(int i) {}
        });
        
        mActionBar.addTab(mRouteTab);
        mActionBar.addTab(mFavoriteTab);
    }

    @Override
    protected void onResume() {
        super.onResume();     
        mAgency = getSharedPreferences(PREFS, MODE_PRIVATE).getString("agencyTag", null);
        
        Log.i(TAG, "onResume with agency " + mAgency);
        
        if (mAgency != null) {
            mViewPager.setAdapter(mAdapter);
        
            if (mTag.equals(TAG_FAVORITES)) {
                mViewPager.setCurrentItem(PAGER_FAVORITE_POSITION);
                mActionBar.selectTab(mFavoriteTab);
            } else {
                mViewPager.setCurrentItem(PAGER_ROUTE_POSITION);
                mActionBar.selectTab(mRouteTab);
            }
        } else {
            selectAgency();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Check if anything has been saved...
        if (savedInstanceState != null) {
            mTag = savedInstanceState.getString(STATE_TAG);
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
                return true;
                
            case R.id.menu_oss:
                Intent intent = new Intent(this, OSSActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(STATE_TAG, mTag);
    }

    /**
     * Load agency selection fragment.
     */
    public void selectAgency() {
        // load agency activity
        Intent intent = new Intent(this, AgencySelectActivity.class);
        startActivity(intent);
    }
    
    public class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BrowseFragment();
                default: // case 1
                    return new FavoritesFragment();
            }
        }
    }
}
