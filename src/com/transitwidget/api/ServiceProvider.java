package com.transitwidget.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.transitwidget.feed.model.Agency;
import com.transitwidget.feed.model.BusPrediction;
import com.transitwidget.feed.model.Direction;
import com.transitwidget.feed.model.Route;
import com.transitwidget.feed.model.Stop;

public class ServiceProvider {
	private static final String TAG = ServiceProvider.class.getName();

	private static Map<String, ServiceAPI> services;

	static {
		services = new HashMap<String, ServiceAPI>();
		
		// Add service providers
		addService(new NextBusAPI());
	}
	
	public static void addService(ServiceAPI service) {
		Log.i(TAG, "Adding service " + service + " to service provider");
		services.put(service.getName(), service);
	}
	

	public static List<BusPrediction> getPredictions(String service, String agencyTag, String stopTag, String directionTag, String routeTag) {
		return services.get(service).getPredictions(agencyTag, stopTag, directionTag, routeTag);
	}

	public static Stop getStop(Context ctx, String agencyTag, String stopTag) {
		Stop result = null;
		String selection = Stop.AGENCY + " = ? AND " + Stop.TAG + " = ?";
		String[] selectionArgs = { agencyTag, stopTag };
		Cursor cursor = ctx.getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = new Stop(cursor);
		}
		cursor.close();
		return result;
	}

	public static Direction getDirection(Context ctx, String agencyTag, String directionTag) {
		Direction result = null;
		String selection = Direction.AGENCY + " = ? AND " + Direction.TAG + " = ?";
		String[] selectionArgs = { agencyTag, directionTag };
		Cursor cursor = ctx.getContentResolver().query(Direction.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = new Direction(cursor, ctx);
		}
		cursor.close();
		return result;
	}

	public static Route getRoute(Context ctx, String agencyTag, String routeTag) {
		Route result = null;
		String selection = Route.AGENCY + " = ? AND " + Route.TAG + " = ?";
		String[] selectionArgs = { agencyTag, routeTag };
		Cursor cursor = ctx.getContentResolver().query(Route.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = new Route(cursor);
		}
		cursor.close();
		return result;
	}
	
	public static Agency getAgency(Context ctx, String agencyTag) {
		Agency result = null;
		String selection = Agency.TAG + " = ?";
		String[] selectionArgs = { agencyTag };
		Cursor cursor = ctx.getContentResolver().query(Agency.CONTENT_URI, null, selection, selectionArgs, null);
		if (cursor != null && cursor.moveToFirst()) {
			result = new Agency(cursor);
		}
		cursor.close();
		return result;
	}

	/**
	 * Get a list of all the agencies provided by the service providers.
	 * 
	 * Check for existing agencies stored in the database, if there are any found those are returned.
	 * If nothing is in the database, the list is generated from all the service provider APIs and
	 * persisted to the database.
	 * 
	 * @param ctx
	 * @return
	 */
	public static List<Agency> getAgencies(Context ctx) {
		List<Agency> results = new ArrayList<Agency>();
		
		// Check for database results.
		Cursor cursor = ctx.getContentResolver().query(Agency.CONTENT_URI, null, null, null, null);
		while (cursor != null && cursor.moveToNext()) {
			results.add(new Agency(cursor));
		}
		cursor.close();
		
		if (results.isEmpty()) {
			// Nothing in DB, load from API
			for (ServiceAPI service : services.values()) {
				for (Agency agency : service.getAgencies()) {
					agency.setService(service.getName());
					results.add(agency);
				}
			}
			
			// Persist back to DB
			for (Agency agency : results) {
				// Log.d(TAG, "Persisting agency " + agency + " to database.");
				ctx.getContentResolver().insert(Agency.CONTENT_URI, agency.getContentValues());
			}
		}
		
		return results;
	}
	
	public static List<Route> getRoutes(Context ctx, String agencyTag) {
		List<Route> results = new ArrayList<Route>();

		// Check for database results.
		String selection = Route.AGENCY + " = ?";
		String[] selectionArgs = { agencyTag };
		Cursor cursor = ctx.getContentResolver().query(Route.CONTENT_URI, null, selection, selectionArgs, null);
		while (cursor.moveToNext()) {
			results.add(new Route(cursor));
		}
		cursor.close();
		
		if (results.isEmpty()) {
			// Get service
			String serviceName = getAgencyService(ctx, agencyTag);
			
			ServiceAPI service = services.get(serviceName);
			results.addAll(service.getRoutes(agencyTag));
			
			// Persist back to DB
			for (Route route: results) {
				route.setAgency(agencyTag);
				// Log.d(TAG, "Persisting route " + route + " to database.");
				ctx.getContentResolver().insert(Route.CONTENT_URI, route.getContentValues());
			}
		}
		
		return results;
	}
	
	public static List<Direction> getRouteConfig(Context ctx, String agencyTag, String routeTag) {
		List<Direction> results = new ArrayList<Direction>();

		// Check for database results.
		String selection = Direction.AGENCY + " = ? AND " + Direction.ROUTE + " = ?";
		String[] selectionArgs = { agencyTag, routeTag };
		Cursor cursor = ctx.getContentResolver().query(Direction.CONTENT_URI, null, selection, selectionArgs, null);
		while (cursor.moveToNext()) {
			results.add(new Direction(cursor, ctx));
		}
		cursor.close();

		if (results.isEmpty()) {
			// Get service
			String serviceName = getAgencyService(ctx, agencyTag);
			
			ServiceAPI service = services.get(serviceName);
			results.addAll(service.getRouteConfig(agencyTag, routeTag));
			
			// Persist back to DB
			for (Direction direction: results) {
				// Log.d(TAG, "Persisting directions " + direction + " to database.");
				direction.setAgency(agencyTag);
				direction.setRoute(routeTag);
				ctx.getContentResolver().insert(Direction.CONTENT_URI, direction.getContentValues());
				
				// Persist stops
				for (Stop stop : direction.getStops()) {
					stop.setAgency(agencyTag);
					String stopSelection = Stop.TAG + " = ? AND " + Stop.AGENCY + " = ?";
					String[] stopSelectionArgs = { stop.getTag(), stop.getAgency() };
					Cursor stopCursor = ctx.getContentResolver().query(Stop.CONTENT_URI, null, stopSelection, stopSelectionArgs, null);
					if (!stopCursor.moveToFirst()) {
						// only add the stop if it doesn't exist
						// Log.d(TAG, "Persisting stop " + stop + " to database.");
						ctx.getContentResolver().insert(Stop.CONTENT_URI, stop.getContentValues());
					}
					stopCursor.close();
				}
			}
		}
		
		return results;
	}
	
	private static String getAgencyService(Context ctx, String agencyTag) {
		String[] projection = {Agency.SERVICE};
		String selection =  Agency.TAG + " = ?";
		String[] selectionArgs = { agencyTag };
		Cursor serviceCursor = ctx.getContentResolver().query(Agency.CONTENT_URI, projection, selection, selectionArgs, null);
		serviceCursor.moveToFirst();
		String serviceName = serviceCursor.getString(0);
		serviceCursor.close();
		return serviceName;
	}
}
