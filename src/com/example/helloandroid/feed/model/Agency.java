package com.example.helloandroid.feed.model;

import com.example.helloandroid.adapters.BaseItem;

public class Agency extends BaseItem {
	private String title;
	private String shortTitle;
	private String regionTitle;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShortTitle() {
		return shortTitle;
	}
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	public String getRegionTitle() {
		return regionTitle;
	}
	public void setRegionTitle(String regionTitle) {
		this.regionTitle = regionTitle;
	}
	
	@Override
	public String toString() {
		return "title: " + title + ", tag: " + getTag() + ", shortTitle: " + shortTitle + ", regionTitle: " + regionTitle;
	}
	
	@Override
	public String getItemLabel() {
		return getShortTitle();
	}
}
