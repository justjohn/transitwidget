package com.transitwidget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.transitwidget.fragments.AgencyListFragment;

/**
 *
 * @author john
 */
public class AgencySelectActivity extends SherlockFragmentActivity
                                  implements AgencyListFragment.Listener {
    private FragmentManager mFragmentManager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agency);

        mFragmentManager = getSupportFragmentManager();
        
        
        Fragment fragment = Fragment.instantiate(this, AgencyListFragment.class.getName());
        loadFragment(fragment);
    }
    
    public void agencySelected(String tag) {
        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                .putString("agencyTag", tag).commit();
        
        finish();
    }
    
    
    /**
     * Load a fragment.
     *
     * @param fragment The fragment to load.
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.add(R.id.main_fragment_container, fragment);
        ft.commit();
    }
}
