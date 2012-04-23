package com.transitwidget.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.transitwidget.R;
import com.transitwidget.api.NextBusAPI;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Stop;
import com.transitwidget.utils.TimeUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StopFragment extends Fragment {
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
    private String mRoute;
    private String mAgency;
    
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

    class UpdateTask extends AsyncTask<String, String, List<BusPrediction>> {
        private final String TAG = UpdateTask.class.getName();
        
        @Override
        protected List<BusPrediction> doInBackground(String... params) {
            // Update the data, send notification
            List<BusPrediction> predictions = new NextBusAPI().getPredictions(mAgency, mStop.getTag(), mDirection, mRoute);
            Log.i(TAG, "Got predictions: " + predictions);
            if (predictions == null) {
                Log.w(TAG, "Unable to load predictions");
                return null;
            }

            if (predictions.isEmpty()) {
                return null;
            }
            return predictions;
        }
        @Override
        protected void onPostExecute(List<BusPrediction> predictions) {
            Log.i(TAG, "Got bus predictions: " + predictions);
            if (predictions == null) {
                // Show no data message
                
            } else {
                BusPrediction nextPrediction = predictions.get(0);
                predictions.remove(0);
                
                String nextTime = TimeUtils.formatTimeOfNextBus(nextPrediction.getEpochTime());
                String absoluteTime = TimeUtils.formatAbsoluteTimeOfNextBus(nextPrediction.getEpochTime());

                mNextTime.setText(nextTime);
                mAbsoluteTime.setText(absoluteTime);
                
                List<Map<String,?>> predictionsList = new LinkedList<Map<String,?>>();  
                    
                for (BusPrediction prediction : predictions) {
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
    }
    
    class UpdateRunnable implements Runnable {
        public void run() {
            new UpdateTask().execute("");
            
            // mHandler.postDelayed(new UpdateRunnable(), 30000);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }

	@Override
	public void onStart() {
		super.onStart();
		final String stopTag = getArguments().getString(ARG_STOP_TAG);
		mAgency = getArguments().getString(ARG_AGENCY_TAG);
		mRoute = getArguments().getString(ARG_ROUTE_TAG);
		mDirection = getArguments().getString(ARG_DIRECTION_TAG);
		
		new AsyncTask<String, String, Cursor>() {
			@Override
			protected Cursor doInBackground(String... params) {
				String selection = Stop.TAG + " = ? AND " + Stop.AGENCY + " = ?"; 
				String[] selectionArgs = {stopTag, mAgency};
				Cursor cursor = mActivity.getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
				return cursor;
			}
            @Override
			protected void onPostExecute(Cursor result) {
				if (result != null && result.moveToFirst()) {
                    mStop = new Stop(result);
					mStopLabel.setText(mStop.getTitle());
                    
                    mHandler.post(new UpdateRunnable());
				}
			}
		}.execute("");
	}
}
