package com.example.helloandroid.api;

import java.util.List;

import com.example.helloandroid.feed.model.Agency;
import com.example.helloandroid.feed.model.BusPrediction;
import com.example.helloandroid.feed.model.Direction;
import com.example.helloandroid.feed.model.Route;

public interface ServiceAPI {
	public String getName();
	
	public List<BusPrediction> getPredictions(String agency, String stopTag, String directionTag, String routeTag);
	public List<Agency> getAgencies();
	public List<Route> getRoutes(String agency);
	public List<Direction> getRouteConfig(String agency, String route);
}
