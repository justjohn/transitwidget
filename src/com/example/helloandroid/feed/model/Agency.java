package com.example.helloandroid.feed.model;

public class Agency {
	private String tag;
	private String title;
	private String shortTitle;
	private String regionTitle;
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
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
		return "title: " + title + ", tag: " + tag + ", shortTitle: " + shortTitle + ", regionTitle: " + regionTitle;
	}
}
