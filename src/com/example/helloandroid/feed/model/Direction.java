package com.example.helloandroid.feed.model;

import java.util.ArrayList;
import java.util.List;

public class Direction {
	private String tag;
	private String title;
	private String name;
	
	private List<Stop> stops = new ArrayList<Stop>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	
	public void addStop(Stop stop) {
		stops.add(stop);
	}
	public List<Stop> getStops() {
		return stops;
	}
	public void setStops(List<Stop> stops) {
		this.stops = stops;
	}
	
	@Override
	public String toString() {
		return "DIRECTION (tag: " + tag + ", title: " + title + ", name: " + name + ", stops: " + stops.toString() + ")";
	}
}
