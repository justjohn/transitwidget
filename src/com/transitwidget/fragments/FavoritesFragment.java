package com.transitwidget.fragments;

import com.transitwidget.R;
import com.transitwidget.feed.model.Route;

import android.support.v4.app.ListFragment;
import android.widget.SimpleCursorAdapter;

public class FavoritesFragment extends ListFragment {
    public static final String ARG_AGENCY_TAG = "agencyTag";

	private SimpleCursorAdapter mAdapter;

    @Override
    public void onStart() {
    	super.onStart();
    	
    	// mListener = (Listener)getActivity();
    	
        String[] from = {Route.TITLE};
        int[] to = {R.id.value};
        
        // mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.single_list_item, null, from, to);
        // setListAdapter(mAdapter);
        
        String agencyTag = getArguments().getString(ARG_AGENCY_TAG);
        // loadRouteList(agencyTag);
    }
}
