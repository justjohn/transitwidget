package com.transitwidget.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;

/**
 *
 * @author john
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    public static final String TAG = PagerAdapter.class.getName();
    
    private ArrayList<Page> pages = new ArrayList<Page>();
    
    private ViewPager mPager;
    private ViewPager.OnPageChangeListener mListener = null;
    private TextView mHeader = null;
    
    public PagerAdapter(FragmentManager fm, ViewPager pager) {
        super(fm);
        mPager = pager;
        mPager.setAdapter(this);
        
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float f, int i1) {
                if (mListener != null) mListener.onPageScrolled(i, f, i1);
            }
            public void onPageSelected(int i) {
                select(i);
                if (mListener != null) mListener.onPageSelected(i);
            }
            public void onPageScrollStateChanged(int i) {
                if (mListener != null) mListener.onPageScrollStateChanged(i);
            }
        });
    }
    
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }
    
    public void setHeaderView(TextView view) {
        mHeader = view;
    }
    
    public void select(int position) {
        for (int i = 0, l = pages.size(); i < l; i++) {
            Page page = pages.get(i);
            if (page.button != null) page.button.setSelected(i == position);
        }
        if (mHeader != null) {
            mHeader.setText(getTitle(position));
        }
    }
    
    public String getTitle(int position) {
        return pages.get(position).title;
    }
    
    public void addPage(View button, String title, Class page, Bundle args) {
        int pos = pages.size();
        addPage(new Page(title, pos, page, args, button));
    }
    
    public void addPage(View button, String title, Class page) {
        addPage(button, title, page, null);
    }

    public void addPage(Page page) {
        pages.add(page);
    }
    
    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Fragment getItem(int position) {
        Page page = pages.get(position);
        try {
            Fragment fragment = (Fragment) page.fragment.newInstance();
            fragment.setArguments(page.args);
            return fragment;
            
        } catch (IllegalAccessException ex) {
            Log.e(TAG, "Unable to load fragment", ex);
            return null;
        } catch (InstantiationException ex) {
            Log.e(TAG, "Unable to load fragment", ex);
            return null;
        }
    }
    
    class Page {
        public final String title;
        public final Class fragment;
        public final Bundle args;
        public final View button;
        private final int position;
        
        public Page(final String title, final int position, final Class fragment, final Bundle args, final View button) {
            this.title = title;
            this.position = position;
            this.fragment = fragment;
            this.args = args;
            this.button = button;
            
            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        mPager.setCurrentItem(position);
                    }
                });
                // Start with the first button selected
                if (position == 0) {
                    button.setSelected(true);
                }
            }
        }
    }
}
