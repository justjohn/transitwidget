package com.example.helloandroid.prefs;

import com.example.helloandroid.feed.model.Route;

public class NextBusRoute extends NextBusValue {
	public NextBusRoute init(Route model) {
		super.init(model.getTitle(), model.getTitle(), model.getTag());
		return this;
	}

	public void initFromTag(String tag) {
		
	}
}
