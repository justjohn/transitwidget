package com.transitwidget.prefs;

import com.transitwidget.feed.model.Agency;

public class NextBusAgency extends NextBusValue {
	public NextBusAgency init(Agency model) {
		super.init(model.getShortTitle(), model.getTitle(), model.getTag());
		return this;
	}

	public void initFromTag(String tag) {
		throw new UnsupportedOperationException();
	}
}
