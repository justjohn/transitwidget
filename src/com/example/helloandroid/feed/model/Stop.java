package com.example.helloandroid.feed.model;

public class Stop {
	private String tag;
	private String title;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public String toString() {
		return "STOP (tag: " + tag + ", title: " + title + ")";
	}
}
