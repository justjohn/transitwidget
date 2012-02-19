/**
 * 
 */
package com.example.helloandroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.example.helloandroid.feed.PredictionFeedParser;

/**
 * @author james
 *
 */
public class NextBus {
	private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed";
	private static final String TAG = NextBus.class.getName();

	public static List<BusPrediction> getPredictions(String agency, String stopTag, String routeTag) {
		String url = getPredictionUrl(agency, stopTag, routeTag);
		List<BusPrediction> data = null;
		
		try {
			PredictionFeedParser parser = new PredictionFeedParser(url);
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading XML feed", e);
		}
		
		long now = System.currentTimeMillis();
		
		ArrayList<BusPrediction> fake = new ArrayList<BusPrediction>();
		fake.add(new BusPrediction(now + 30000, "77"));
		fake.add(new BusPrediction(now + 60000, "78"));
		fake.add(new BusPrediction(now + 90000, "44"));
		
		return data;
	}

	
	public static String getPredictionUrl(String agency, String stopTag, String routeTag) {
		return getCommandUrl("predictions", agency) + "&stopId=" + stopTag + "&routeTag=" + routeTag;
	}
	
	public static String getCommandUrl(String command, String agency) {
		return BASE_URL + "?command=" + command + "&a=" + agency;
	}
}
