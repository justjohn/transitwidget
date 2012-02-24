package com.example.helloandroid.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.helloandroid.R;
import com.example.helloandroid.api.ServiceProvider;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;
import com.example.helloandroid.feed.model.Stop;

public class DirectionListFragment extends ListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";
    public static final String ARG_ROUTE_TAG = "routeTag";

	private SimpleCursorAdapter mAdapter;
    private Listener mListener;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);

    	mListener = (Listener)getActivity();
    	
        String[] from = {Direction.TITLE};
        int[] to = {R.id.value};
        
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.single_list_item, null, from, to);
        setListAdapter(mAdapter);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

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
    			if (getListView() != null) {
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
