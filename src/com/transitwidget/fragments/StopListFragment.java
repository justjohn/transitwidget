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
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Stop;
import com.transitwidget.utils.StringUtils;

public class StopListFragment extends SherlockListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";
    public static final String ARG_DIRECTION_TAG = "routeTag";

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
        String directionTag = getArguments().getString(ARG_DIRECTION_TAG);
        
        loadStopList(agencyTag, directionTag);
    }
    
    public void loadStopList(final String agencyTag, final String directionTag) {
    	final Activity activity = this.getActivity();
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		protected void onPreExecute() {
    			setListShown(false);
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    	        String selection = Direction.AGENCY + " = ? AND " + Direction.TAG + " = ?";
    	        String[] selecitonArgs = { agencyTag, directionTag };
    			Cursor cursor = activity.getContentResolver().query(Direction.CONTENT_URI, new String[] { Direction.STOPS }, selection, selecitonArgs, null);
    			cursor.moveToFirst();
    			String stopStr = cursor.getString(0);
    			String[] stopTags = stopStr.split(",");
    			
    	        String stopSelection = Stop.AGENCY + " = ? AND " + Stop.TAG + " IN (" + StringUtils.join(stopTags, ",", "\"") + ")";
    	        String[] stopSelecitonArgs = { agencyTag };
    			Cursor stopsCursor = activity.getContentResolver().query(Stop.CONTENT_URI, null, stopSelection, stopSelecitonArgs, null);

    	    	return stopsCursor;
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
		Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(Stop.CONTENT_URI, id), new String[] { Stop.TAG }, null, null, null);
		String tag = null;
		if (cursor.moveToFirst()) {
			tag = cursor.getString(0);
		}

		if (tag != null) {
    		mListener.stopSelected(tag);
		}
    }
    
    public static interface Listener {
    	void stopSelected(String tag);
    }
}
