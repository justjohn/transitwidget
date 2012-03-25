package com.transitwidget.api;

import java.util.List;

import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Route;

public interface ServiceAPI {
	public String getName();
	
	public List<BusPrediction> getPredictions(String agency, String stopTag, String directionTag, String routeTag);
	public List<Agency> getAgencies();
	public List<Route> getRoutes(String agency);
	public List<Direction> getRouteConfig(String agency, String route);
}
