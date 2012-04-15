package com.transitwidget.fragments;

import com.transitwidget.R;
import com.transitwidget.feed.model.Stop;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StopFragment extends Fragment {
	public static final String ARG_STOP_TAG = "stop";
	public static final String ARG_AGENCY_TAG = "agency";
	
	private TextView stop;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.stop, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		stop = (TextView) view.findViewById(R.id.stop);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		
	}

	@Override
	public void onStart() {
		super.onStart();
		final String stopTag = getArguments().getString(ARG_STOP_TAG);
		final String agency = getArguments().getString(ARG_AGENCY_TAG);
		
		new AsyncTask<String, String, Cursor>() {
			@Override
			protected Cursor doInBackground(String... params) {
				String selection = Stop.TAG + " = ? AND " + Stop.AGENCY + " = ?"; 
				String[] selectionArgs = {stopTag, agency};
				Cursor cursor = getActivity().getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
				return cursor;
			}
			protected void onPostExecute(Cursor result) {
				if (result != null && result.moveToFirst()) {
					String title = result.getString(result.getColumnIndex(Stop.TITLE));
					stop.setText(title);
				}
			}
		}.execute("");
	}

	
}
