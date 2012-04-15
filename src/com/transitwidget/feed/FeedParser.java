package com.transitwidget.feed;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

abstract class FeedParser {
	private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed";

	public static final String ATTR_TAG = "tag";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_LAT = "lat";
	public static final String ATTR_LON = "lon";
	
    final URL feedUrl;

    public FeedParser(String feedUrl) throws MalformedURLException {
        this.feedUrl = new URL(feedUrl);
    }

    public InputStream getInputStream() throws IOException {
        return feedUrl.openConnection().getInputStream();
    }
    
    public Map<String, String> parseAttributes(XmlPullParser xpp) {
    	Map<String, String> map = new HashMap<String, String>();
		for (int i=0, l=xpp.getAttributeCount(); i < l; i++) {
			String attributeName = xpp.getAttributeName(i);
			String attributeValue = xpp.getAttributeValue(i);
			map.put(attributeName, attributeValue);
		}	
		return map;
    }

	public static String getCommandUrl(String command) {
		return BASE_URL + "?command=" + command;
	}
	public static String getCommandUrl(String command, String agency) {
		return getCommandUrl(command) + "&a=" + agency;
	}
	public static String getCommandUrl(String command, String agency, String route) {
		return getCommandUrl(command, agency) + "&r=" + route;
	}
}
