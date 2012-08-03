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
import com.transitwidget.api.ServiceProvider;
import com.transitwidget.feed.model.Agency;

public class AgencyListFragment extends SherlockListFragment {
    private SimpleCursorAdapter mAdapter;
    private Listener mListener;

    @Override
    public void onStart() {
      super.onStart();

      mListener = (Listener)getActivity();

      String[] from = {Agency.TITLE};
      int[] to = {com.transitwidget.R.id.value};

      mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.single_list_item, null, from, to);
      setListAdapter(mAdapter);

      loadAgencyList();
    }

    public void loadAgencyList() {
      final Activity activity = this.getActivity();

      new AsyncTask<String, Integer, Cursor>() {
        protected void onPreExecute() {
          setListShown(false);
        }
        @Override
        protected Cursor doInBackground(String... params) {
          Cursor cursor = activity.getContentResolver().query(Agency.CONTENT_URI, null, null, null, null);

          if (cursor.getCount() == 0) {
            // load from network and cache to DB.
            ServiceProvider.getAgencies(activity);
            cursor.close();
            
            cursor = activity.getContentResolver().query(Agency.CONTENT_URI, null, null, null, null);
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
      }.execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);
      Cursor cursor = getActivity().getContentResolver().query(ContentUris.withAppendedId(Agency.CONTENT_URI, id), new String[] { Agency.TAG }, null, null, null);
      String tag = null;
      if (cursor.moveToFirst()) {
        tag = cursor.getString(0);
      }

      if (tag != null) {
          mListener.agencySelected(tag);
      }
    }

    public static interface Listener {
      void agencySelected(String tag);
    }
}
