package com.transitwidget.prefs;

import com.transitwidget.feed.model.Route;

public class NextBusRoute extends NextBusValue {
	public NextBusRoute init(Route model) {
		super.init(model.getTitle(), model.getTitle(), model.getTag());
		return this;
	}

	public void initFromTag(String tag) {
		throw new UnsupportedOperationException();
		
	}
}
