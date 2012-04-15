/**
 * 
 */
package com.transitwidget.api;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.transitwidget.feed.AgencyListFeedParser;
import com.transitwidget.feed.PredictionFeedParser;
import com.transitwidget.feed.RouteConfigFeedParser;
import com.transitwidget.feed.RouteListFeedParser;
import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Route;

/**
 * Get results from the NextBus API.
 */
public class NextBusAPI implements ServiceAPI {
	private static final String TAG = NextBusAPI.class.getName();

	public String getName() {
		return "NextBusAPI";
	}
	
	public List<BusPrediction> getPredictions(String agency, String stopTag, String directionTag, String routeTag) {
		List<BusPrediction> data = null;
		
		try {
			PredictionFeedParser parser = new PredictionFeedParser(agency, stopTag, directionTag, routeTag);
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing predictions XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading predictions XML feed", e);
		}
		
		return data;
	}
	
	public List<Agency> getAgencies() {
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
	
	public List<Route> getRoutes(String agency) {
		List<Route> data = null;
		
		try {
			RouteListFeedParser parser = new RouteListFeedParser(agency);
			data = parser.parse();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing route XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading route XML feed", e);
		}
		
		return data;
	}

	public List<Direction> getRouteConfig(String agency, String route) {
		List<Direction> data = null;
		
		try {
			RouteConfigFeedParser parser = new RouteConfigFeedParser(agency, route);
			data = parser.parse();
			
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception parsing route config XML feed", e);
		} catch (IOException e) {
			Log.e(TAG, "Exception loading route config XML feed", e);
		}
		
		return data;
	}
}
