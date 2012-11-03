package com.transitwidget.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.actionbarsherlock.app.SherlockListFragment;
import com.transitwidget.R;
import com.transitwidget.api.ServiceProvider;
import com.transitwidget.feed.model.Route;

public class RouteListFragment extends SherlockListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";

	private SimpleCursorAdapter mAdapter;
    private Listener mListener;
    
    public RouteListFragment() {
        super();
    }
    
    public RouteListFragment(Listener listener, Bundle args) {
        super();
        mListener = listener;
        setArguments(args);
    }
        
    @Override
    public void onStart() {
    	super.onStart();
    	
        String[] from = {Route.TITLE};
        int[] to = {com.transitwidget.R.id.value};
        
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.single_list_item, null, from, to);
        setListAdapter(mAdapter);
        
        String agencyTag = getArguments().getString(ARG_AGENCY_TAG);
        loadRouteList(agencyTag);
    }
    
    public void loadRouteList(String agencyTag) {
    	final Activity activity = this.getActivity();
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		protected void onPreExecute() {
    			setListShown(false);
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    			Cursor cursor = activity.getContentResolver().query(Route.CONTENT_URI, null, Route.AGENCY + " = ?", new String[] { params[0] }, null);

    	    	if (cursor.getCount() == 0) {
    	    		// load from network and cache to DB.
    				ServiceProvider.getRoutes(activity, params[0]);
    		    	cursor.requery();
    	    	}
    	    	
    	    	return cursor;
    		}
    		protected void onPostExecute(Cursor cursor) {
    			activity.startManagingCursor(cursor);
    			mAdapter.changeCursor(cursor);
    			if (isVisible()) {
    				setListShown(true);
    			}
    		}
    	}.execute(agencyTag);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(Route.CONTENT_URI, id), new String[] { Route.TAG }, null, null, null);
		String tag = null;
		if (cursor.moveToFirst()) {
			tag = cursor.getString(0);
		}

		if (tag != null) {
    		mListener.routeSelected(tag);
		}
    }
    
    public static interface Listener {
    	void routeSelected(String tag);
    }
}
