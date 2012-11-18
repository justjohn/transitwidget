package com.transitwidget.feed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.transitwidget.feed.model.BusPrediction;


import android.util.Log;

public class PredictionFeedParser extends FeedParser {
	private static final String TAG = PredictionFeedParser.class.getName();

	// XML tag and attributes
	private static final String PREDICTIONS = "predictions";
	private static final String PREDICTION = "prediction";
	private static final String DIRECTION = "direction";
	
	private static final String ATTR_EPOCH_TIME = "epochTime";
	private static final String ATTR_DIR_TAG = "dirTag";
	private static final String ATTR_VEHICLE = "vehicle";
	private static final String ATTR_BLOCK = "block";
	private static final String ATTR_TRIP_TAG = "tripTag";
	private static final Object ATTR_ROUTE_TAG = "routeTag";
	private static final Object ATTR_STOP_TITLE = "stopTitle";

	
	private String directionTag;
	
	public PredictionFeedParser(String agency, String stopTag, String directionTag, String routeTag) throws MalformedURLException {
		super(getPredictionUrl(agency, stopTag, routeTag));
		Log.i(TAG, "Loading feed from URL: " + feedUrl);
		this.directionTag = directionTag;
	}

	public List<BusPrediction> parse() throws XmlPullParserException, IOException {
		List<BusPrediction> predictions = new ArrayList<BusPrediction>();
		Log.i(TAG, "Parsing feed");
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		// The routeTag attribute from the <predictions> tag
		String route = "Unknown";
		String direction = "Unknown";
		String stopTitle = "Unknown";
		
		xpp.setInput(getInputStream(), null);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();
				
				if (name.equals(PREDICTIONS)) {
					Map<String, String> attributes = parseAttributes(xpp);
					route = attributes.get(ATTR_ROUTE_TAG);
					stopTitle = attributes.get(ATTR_STOP_TITLE);
					
				} else
				if (name.equals(DIRECTION)) {
					Map<String, String> attributes = parseAttributes(xpp);
					direction = attributes.get(ATTR_TITLE);
					
				} else
				if (name.equals(PREDICTION)) {
					BusPrediction prediciton = new BusPrediction();
					Map<String, String> attributes = parseAttributes(xpp);

					prediciton.setRoute(route);
					prediciton.setStopTitle(stopTitle);

					prediciton.setBlock(attributes.get(ATTR_BLOCK));
					prediciton.setDirTag(attributes.get(ATTR_DIR_TAG));
					prediciton.setVehicle(attributes.get(ATTR_VEHICLE));
					prediciton.setTripTag(attributes.get(ATTR_TRIP_TAG));
					
					prediciton.setEpochTime(Long.parseLong(attributes.get(ATTR_EPOCH_TIME)));
					
					// limit predictions to the selected direction
					if (directionTag.equals(prediciton.getDirTag())) {
						predictions.add(prediciton);
					}
				}
				
			}
			eventType = xpp.next();
		}
		return predictions;
	}

	public static String getPredictionUrl(String agency, String stopTag, String routeTag) {
		return getCommandUrl("predictions", agency) + "&s=" + stopTag + "&r=" + routeTag;
	}
}
