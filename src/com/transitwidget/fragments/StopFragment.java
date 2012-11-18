package com.transitwidget.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.transitwidget.R;
import com.transitwidget.api.NextBusAPI;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Favorite;
import com.transitwidget.feed.model.Stop;
import com.transitwidget.utils.TimeUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StopFragment extends SherlockFragment {
    private static final String TAG = StopFragment.class.getName();
    
	public static final String ARG_STOP_TAG = "stop";
	public static final String ARG_ROUTE_TAG = "route";
	public static final String ARG_DIRECTION_TAG = "direction";
	public static final String ARG_AGENCY_TAG = "agency";
    
    public final static String ITEM_TITLE = "title";  
    public final static String ITEM_CAPTION = "caption";
	
	private TextView mStopLabel;
    private TextView mNextTime;
    private TextView mAbsoluteTime;
    private ListView mMorePredictions;
    
    private Stop mStop;
    private String mDirection;
    private String mDirectionTitle;
    private String mRoute;
    private String mAgency;
    
    private List<BusPrediction> mPredictions;
    
    private boolean mFavorite = false;
    
    private Activity mActivity = null;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) { }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.stop, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mStopLabel   = (TextView) view.findViewById(R.id.stop);
        mNextTime    = (TextView) view.findViewById(R.id.next_time);
        mAbsoluteTime = (TextView) view.findViewById(R.id.absolute_time);
        mMorePredictions = (ListView) view.findViewById(R.id.more_predictions);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(TAG, "Changing favorite status of stop " + mStop);
                
                String selection = Favorite.AGENCY + " = ? AND " + Favorite.ROUTE + " = ? AND " + Favorite.STOP + " = ?";
                String[] selectionArgs = {mAgency, mRoute, mStop.getTag()};
                if (mFavorite) {
                    // remove favorite
                    getActivity().getContentResolver().delete(Favorite.CONTENT_URI, selection, selectionArgs);
                } else {
                    
                    // add favorite
                    Favorite fav = new Favorite();
                    fav.setAgency(mAgency);
                    fav.setRoute(mRoute);
                    fav.setStop(mStop.getTag());
                    fav.setDirection(mDirection);
                    
                    // convenience data for list display
                    fav.setDirectionLabel(mDirectionTitle);
                    fav.setStopLabel(mStop.getTitle());
                    
                    getActivity().getContentResolver().insert(Favorite.CONTENT_URI, fav.getContentValues());
                }
                
                mFavorite = !mFavorite;
                
                // flip menu item
                if (mFavorite) {
                    item.setIcon(R.drawable.heart_red);
                    item.setTitle("-Fav");
                } else {
                    item.setIcon(R.drawable.heart);
                    item.setTitle("+Fav");
                }
                return true;
            }
        };
        
        MenuItem item;
        if (mFavorite) {
            item = menu.add("-Fav").setIcon(R.drawable.heart_red);
        } else {
            item = menu.add("+Fav").setIcon(R.drawable.heart);
        }
        item.setOnMenuItemClickListener(listener).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		final String stopTag = getArguments().getString(ARG_STOP_TAG);
		mAgency = getArguments().getString(ARG_AGENCY_TAG);
		mRoute = getArguments().getString(ARG_ROUTE_TAG);
		mDirection = getArguments().getString(ARG_DIRECTION_TAG);
		
        // Log.i(TAG, "Loading stop with agency: " + mAgency + ",  route: " + mRoute + ", direction: " + mDirection);
        
        // Lookup direction
        String selection = Direction.AGENCY + " = ? AND " + Direction.ROUTE + " = ? AND " + Direction.TAG + " = ?";
        String[] selectionArgs = { mAgency, mRoute, mDirection };
        Cursor c = getActivity().getContentResolver().query(Direction.CONTENT_URI, null, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            mDirectionTitle = new Direction(c, getActivity()).getTitle();
        } else {
            Log.w(TAG, "Unable to lookup direction with tag " + mDirection);
            mDirectionTitle = mDirection;
        }
        c.close();
        
        // Lookup stop
        selection = Stop.TAG + " = ? AND " + Stop.AGENCY + " = ?"; 
        selectionArgs = new String[] {stopTag, mAgency};
        Cursor result = mActivity.getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
                
        if (result.moveToFirst()) {
            mStop = new Stop(result);
            mStopLabel.setText(mStop.getTitle());
            
            mFavorite = isFavorite();
        }
        result.close();
        
        setHasOptionsMenu(true);
	}

    @Override
    public void onResume() {
        super.onResume();
        mHandler.post(new UpdateRunnable());
    }
    
    private boolean isFavorite() {
        boolean favorite = false;
        
        String selection = Favorite.AGENCY + " = ? AND " + Favorite.ROUTE + " = ? AND " + Favorite.STOP + " = ?";
        String[] selectionArgs = {mAgency, mRoute, mStop.getTag()};
        Cursor c = getActivity().getContentResolver().query(Favorite.CONTENT_URI, new String[] {Favorite._ID}, selection , selectionArgs, null);
        if (c.moveToFirst()) {
            favorite = true;
        }
        c.close();
        return favorite;
    }

    class UpdateTask extends AsyncTask<String, String, List<BusPrediction>> {
        private final String TAG = UpdateTask.class.getName();
        
        @Override
        protected List<BusPrediction> doInBackground(String... params) {
            // Update the data, send notification
            NextBusAPI api = new NextBusAPI();
            List<BusPrediction> predictions = api.getPredictions(mAgency, mStop.getTag(), mDirection, mRoute);
            // Log.i(TAG, "Got predictions: " + predictions);
            if (predictions == null) {
                Log.w(TAG, "Unable to load predictions");
                return null;
            }

            return predictions;
        }
        @Override
        protected void onPostExecute(List<BusPrediction> predictions) {
            mPredictions = predictions;
            updateUI();
        }
    }
    
    private void updateUI() {
        if (mPredictions == null) {
            // Show no data message

        } else if (mPredictions.size() == 0) {
            mNextTime.setText("No Prediction Data");
            mAbsoluteTime.setText("");
            mMorePredictions.setAdapter(null);

        } else {
            BusPrediction nextPrediction = mPredictions.get(0);

            String nextTime = TimeUtils.formatTimeOfNextBus(nextPrediction.getEpochTime());
            String absoluteTime = TimeUtils.formatAbsoluteTimeOfNextBus(nextPrediction.getEpochTime());

            mNextTime.setText(nextTime);
            mAbsoluteTime.setText(absoluteTime);

            List<Map<String,?>> predictionsList = new LinkedList<Map<String,?>>();  

            boolean skipFirst = true;
            for (BusPrediction prediction : mPredictions) {
                if (skipFirst) {
                    skipFirst = false;
                    continue;
                }
                long predictionTime = prediction.getEpochTime();

                String timeUntil = TimeUtils.formatTimeOfNextBus(predictionTime);
                String timeAt = TimeUtils.formatAbsoluteTimeOfNextBus(predictionTime);

                Map<String,String> item = new HashMap<String, String>();
                item.put(ITEM_TITLE, timeUntil);
                item.put(ITEM_CAPTION, timeAt);

                predictionsList.add(item);
            }

            String[] mapping = { ITEM_TITLE, ITEM_CAPTION };
            int[] fields = { R.id.list_title, R.id.list_caption };

            if (getActivity() != null) {
                mMorePredictions.setAdapter(new SimpleAdapter(getActivity(), predictionsList, R.layout.list_complex, mapping, fields));
            }
        }
    }
    
    class UpdateRunnable implements Runnable {
        int i = 0;
        public void run() {
            // stop updating if this fragment is no longer visible
            if (isResumed()) {
                updateUI();
                if (i % 20 == 0) {
                    i = 0;
                    new UpdateTask().execute();
                }
                i++;
                
                mHandler.postDelayed(this, 1000);
            } else {
                Log.i(TAG, "Stopping prediction check");
            }
        }
    }
}
