package com.transitwidget.feed;

import android.util.Log;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Stop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class RouteConfigFeedParser extends FeedParser {
	private static final String TAG = RouteConfigFeedParser.class.getName();

	// XML tag and attributes
	private static final String STOP = "stop";
	private static final String DIRECTION = "direction";

	private static final String ATTR_NAME = "name";
	private static final String ATTR_STOP_ID = "stopId";
	
	public RouteConfigFeedParser(String agency, String route) throws MalformedURLException {
		super(getCommandUrl("routeConfig", agency, route));
		Log.i(TAG, "Loading feed from URL: " + feedUrl);
	}

	public List<Direction> parse() throws XmlPullParserException, IOException {
		Map<String, Stop> stops = new HashMap<String, Stop>();
		List<Direction> directions = new ArrayList<Direction>();
		
		Direction inDirection = null;
		
		Log.i(TAG, "Parsing feed");
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		
		xpp.setInput(getInputStream(), null);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();

				if (name.equals(DIRECTION)) {
					Map<String, String> attributes = parseAttributes(xpp);
					
					Direction direction = new Direction();
					direction.setName(attributes.get(ATTR_NAME));
					direction.setTag(attributes.get(ATTR_TAG));
					direction.setTitle(attributes.get(ATTR_TITLE));

					inDirection = direction;
					directions.add(direction);
					
				} else
				if (name.equals(STOP)) {
					Map<String, String> attributes = parseAttributes(xpp);
					
					if (inDirection == null) {
						// Stop definitions
						Stop stop = new Stop();
						
						stop.setTag(attributes.get(ATTR_TAG));
						stop.setTitle(attributes.get(ATTR_TITLE));
						if (attributes.get(ATTR_STOP_ID) != null) {
							stop.setStopId(Integer.parseInt(attributes.get(ATTR_STOP_ID)));
						}
						
						stops.put(stop.getTag(), stop);
					} else {
						// Stops in a route direction
						inDirection.addStop(stops.get(attributes.get(ATTR_TAG)));
					}
				}
			} else 
			if (eventType == XmlPullParser.END_TAG) {
				String name = xpp.getName();
				if (name.equals(DIRECTION)) {
					inDirection = null;
				}
			}
			eventType = xpp.next();
		}
		return directions;
	}
}
