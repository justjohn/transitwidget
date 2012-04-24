package com.transitwidget.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.actionbarsherlock.app.SherlockListFragment;
import com.transitwidget.R;
import com.transitwidget.feed.model.Favorite;
import com.transitwidget.fragments.StopListFragment.Listener;

public class FavoritesFragment extends SherlockListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";

	private SimpleCursorAdapter mAdapter;
	private Listener mListener;

    @Override
    public void onStart() {
    	super.onStart();
    	
    	mListener = (Listener)getActivity();
    	
        String[] from = {Favorite.STOP_LABEL, Favorite.ROUTE, Favorite.DIRECTION};
        int[] to = {R.id.stop, R.id.route, R.id.direction};
        
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.favorite_list_item, null, from, to);
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
    	super.onResume();
    	
        String agencyTag = getArguments().getString(ARG_AGENCY_TAG);
        
        loadList(agencyTag);
    }
    
    public void loadList(final String agencyTag) {
    	final Activity activity = this.getActivity();
    	
    	new AsyncTask<String, Integer, Cursor>() {
    		protected void onPreExecute() {
    			setListShown(false);
    		}
    		@Override
    		protected Cursor doInBackground(String... params) {
    	        String selection = Favorite.AGENCY + " = ?";
    	        String[] selecitonArgs = { agencyTag };
    			Cursor cursor = activity.getContentResolver().query(Favorite.CONTENT_URI, null, selection, selecitonArgs, null);
    			
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
		Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(Favorite.CONTENT_URI, id), new String[] { Favorite.STOP }, null, null, null);
		String tag = null;
		if (cursor.moveToFirst()) {
			tag = cursor.getString(0);
		}

		if (tag != null) {
    		mListener.stopSelected(tag);
		}
    }
}
