package com.example.helloandroid.prefs;

import com.example.helloandroid.feed.model.Stop;

public class NextBusStop extends NextBusValue {
	public NextBusStop init(Stop model) {
		super.init(model.getTitle(), model.getTitle(), model.getTag());
		return this;
	}

	public void initFromTag(String tag) {
		
	}
}
