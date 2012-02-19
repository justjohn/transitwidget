package com.example.helloandroid.feed;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

abstract class FeedParser {
    
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
}
