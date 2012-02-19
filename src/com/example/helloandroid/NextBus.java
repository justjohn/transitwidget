/**
 * 
 */
package com.example.helloandroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.example.helloandroid.feed.AgencyListFeedParser;
import com.example.helloandroid.feed.PredictionFeedParser;
import com.example.helloandroid.feed.RouteConfigFeedParser;
import com.example.helloandroid.feed.RouteListFeedParser;
import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.feed.model.BusPrediction;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;

/**
 * @author james
 *
 */
public class NextBus {
	private static final String TAG = NextBus.class.getName();

	public static List<BusPrediction> getPredictions(String agency, String stopTag, String routeTag) {
		List<BusPrediction> data = null;
		
		try {
			PredictionFeedParser parser = new PredictionFeedParser(agency, stopTag, routeTag);
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing predictions XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading predictions XML feed", e);
		}
		
		return data;
	}
	
	public static List<Agency> getAgencies() {
		List<Agency> data = null;
		
		try {
			AgencyListFeedParser parser = new AgencyListFeedParser();
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing agency XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading agency XML feed", e);
		}
		
		return data;
	}
	
	public static List<Route> getRoutes(String agency) {
		List<Route> data = null;
		
		try {
			RouteListFeedParser parser = new RouteListFeedParser(agency);
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing agency XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading agency XML feed", e);
		}
		
		return data;
	}

	public static List<Direction> getRouteConfig(String agency, String route) {
		List<Direction> data = null;
		
		try {
			RouteConfigFeedParser parser = new RouteConfigFeedParser(agency, route);
			data = parser.parse();
			
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing agency XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading agency XML feed", e);
		}
		
		return data;
	}
}
