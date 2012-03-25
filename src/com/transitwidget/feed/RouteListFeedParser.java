package com.transitwidget.feed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.transitwidget.feed.model.Route;


import android.util.Log;

public class RouteListFeedParser extends FeedParser {
	private static final String TAG = RouteListFeedParser.class.getName();

	// XML tag and attributes
	private static final String ROUTE = "route";
	
	public RouteListFeedParser(String agency) throws MalformedURLException {
		super(getCommandUrl("routeList", agency));
		Log.i(TAG, "Loading feed from URL: " + feedUrl);
	}

	public List<Route> parse() throws XmlPullParserException, IOException {
		List<Route> routes = new ArrayList<Route>();
		Log.i(TAG, "Parsing feed");
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		
		xpp.setInput(getInputStream(), null);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();
				
				if (name.equals(ROUTE)) {
					Route route = new Route();
					Map<String, String> attributes = parseAttributes(xpp);
					
					route.setTag(attributes.get(ATTR_TAG));
					route.setTitle(attributes.get(ATTR_TITLE));
					
					routes.add(route);
				}
			}
			eventType = xpp.next();
		}
		return routes;
	}
}
