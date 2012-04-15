package com.transitwidget.feed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.transitwidget.feed.model.Agency;


import android.util.Log;

public class AgencyListFeedParser extends FeedParser {
	private static final String TAG = AgencyListFeedParser.class.getName();

	// XML tag and attributes
	private static final String AGENCY = "agency";
	
	private static final String ATTR_SHORT_TITLE = "shortTitle";
	private static final String ATTR_REGION_TITLE = "regionTitle";
	
	public AgencyListFeedParser() throws MalformedURLException {
		super(getCommandUrl("agencyList"));
		Log.i(TAG, "Loading feed from URL: " + feedUrl);
	}

	public List<Agency> parse() throws XmlPullParserException, IOException {
		List<Agency> agencies = new ArrayList<Agency>();
		Log.i(TAG, "Parsing feed");
		
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		
		xpp.setInput(getInputStream(), null);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String name = xpp.getName();
				
				if (name.equals(AGENCY)) {
					Agency agency = new Agency();
					Map<String, String> attributes = parseAttributes(xpp);
					
					agency.setTag(attributes.get(ATTR_TAG));
					agency.setTitle(attributes.get(ATTR_TITLE));
					agency.setShortTitle(attributes.get(ATTR_SHORT_TITLE));
					agency.setRegionTitle(attributes.get(ATTR_REGION_TITLE));
					
					agencies.add(agency);
				}
			}
			eventType = xpp.next();
		}
		return agencies;
	}
}
