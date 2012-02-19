package com.example.helloandroid.feed.model;

public class BusPrediction {
	private String route;
	private String direction;
	private String stopTitle;
	
	private long epochTime;
	private String dirTag;
	private String vehicle;
	private String block;
	private String tripTag;
	
	public BusPrediction(long epochTime, String route) {
		this.setEpochTime(epochTime);
		this.setRoute(route);
	}
	
	public BusPrediction() {
	}
	
	@Override
	public String toString() {
		long now = System.currentTimeMillis();
		long seconds = (getEpochTime() - now) / 1000;
		long minutes = seconds / 60;
		seconds -= minutes * 60;
		return getRoute() + " : " + minutes + "m " + seconds + "s";
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public long getEpochTime() {
		return epochTime;
	}

	public void setEpochTime(long epochTime) {
		this.epochTime = epochTime;
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}

	public String getTripTag() {
		return tripTag;
	}

	public void setTripTag(String tripTag) {
		this.tripTag = tripTag;
	}

	public String getDirTag() {
		return dirTag;
	}

	public void setDirTag(String dirTag) {
		this.dirTag = dirTag;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getStopTitle() {
		return stopTitle;
	}

	public void setStopTitle(String stopTitle) {
		this.stopTitle = stopTitle;
	}
}
