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
import com.transitwidget.feed.model.Direction;

public class DirectionListFragment extends SherlockListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";
    public static final String ARG_ROUTE_TAG = "routeTag";

	private SimpleCursorAdapter mAdapter;
    private Listener mListener;
    
    public DirectionListFragment(Listener listener, Bundle args) {
        super();
        mListener = listener;
        setArguments(args);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	
        String[] from = {Direction.TITLE};
        int[] to = {R.id.value};
        
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.single_list_item, null, from, to);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        String agencyTag = getArguments().getString(ARG_AGENCY_TAG);
        String routeTag = getArguments().getString(ARG_ROUTE_TAG);
        
        loadDirectionList(agencyTag, routeTag);
    }
    
    public void loadDirectionList(final String agencyTag, final String routeTag) {
    	final Activity activity = this.getActivity();
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		protected void onPreExecute() {
    			setListShown(false);
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    	        String selection = Direction.AGENCY + " = ? AND " + Direction.ROUTE + " = ?";
    	        String[] selecitonArgs = { agencyTag, routeTag };
    			Cursor cursor = activity.getContentResolver().query(Direction.CONTENT_URI, null, selection, selecitonArgs, null);

    	    	if (cursor.getCount() == 0) {
    	    		// load from network and cache to DB.
    				ServiceProvider.getRouteConfig(activity, agencyTag, routeTag);
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
    	}.execute("");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(Direction.CONTENT_URI, id), new String[] { Direction.TAG }, null, null, null);
		String tag = null;
		if (cursor.moveToFirst()) {
			tag = cursor.getString(0);
		}

		if (tag != null) {
    		mListener.directionSelected(tag);
		}
    }
    
    public static interface Listener {
    	void directionSelected(String tag);
    }
}
