package com.example.helloandroid.prefs;

import com.example.helloandroid.feed.model.Agency;

public class NextBusAgency extends NextBusValue {
	public NextBusAgency init(Agency model) {
		super.init(model.getShortTitle(), model.getTitle(), model.getTag());
		return this;
	}

	public void initFromTag(String tag) {
		throw new UnsupportedOperationException();
	}
}
