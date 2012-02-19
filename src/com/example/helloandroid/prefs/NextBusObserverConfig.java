/**
 * 
 */
package com.example.helloandroid.prefs;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author james
 *
 */
public class NextBusObserverConfig {
    private static final String PREFS_NAME_PREFIX =
    		NextBusObserverConfig.class.getName();
    
    private static final String PREF_AGENCY = "agency";
    private static final String PREF_ROUTE = "route";
    private static final String PREF_DIRECTION = "direction";
    private static final String PREF_STOP = "stop";

	private final Context ctx;
	private final int widgetId;
	private final String prefsName;
	private SharedPreferences prefs;
	private boolean modified = false;

	private NextBusAgency agency;
	private List<NextBusAgency> agencies;
	private NextBusRoute route;
	private List<NextBusRoute> routes;
	private NextBusDirection direction;
	private List<NextBusDirection> directions;
	private NextBusStop stop;
	private List<NextBusStop> stops;

	public NextBusObserverConfig(Context ctx, int widgetId) {
		this.ctx = ctx;
		this.widgetId = widgetId;
		this.prefsName = PREFS_NAME_PREFIX + "_" + widgetId;
		SharedPreferences prefs = ctx.getSharedPreferences(this.prefsName, 0);
		this.agency = initValue(new NextBusAgency(), PREF_AGENCY);
		this.route = initValue(new NextBusRoute(), PREF_ROUTE);
		this.direction = initValue(new NextBusDirection(), PREF_DIRECTION);
		this.stop = initValue(new NextBusStop(), PREF_STOP);
	}

	private <T extends NextBusValue> T initValue(T v, String key) {
		String s = prefs.getString(key, null);
		if (s == null)
			return null;
		v.initFromPrefs(s);
		return v;
	}

    public void save() {
    	SharedPreferences.Editor prefs = ctx.getSharedPreferences(this.prefsName, 0).edit();
    	
		saveValue(prefs, this.agency, PREF_AGENCY);
		saveValue(prefs, this.route, PREF_ROUTE);
		saveValue(prefs, this.direction, PREF_DIRECTION);
		saveValue(prefs, this.stop, PREF_STOP);
    	
        prefs.commit();
    }

	private static void saveValue(Editor prefs, NextBusValue value, String key) {
		String s = value.toPrefsString();
		prefs.putString(key, s);
	}

	private static boolean safeEquals(Object a, Object b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}
	
	public List<NextBusAgency> getAgencies() {
		// TODO Fetch from nextbus feed if first time.
		return agencies;
	}

	
	public NextBusAgency getAgency() {
		return agency;
	}
	public void setAgency(NextBusAgency newAgency) {
		if (safeEquals(agency, newAgency)) {
			return;
		}
		modified = true;
		agency = newAgency;
	}

	public NextBusRoute getRoute() {
		return route;
	}

	public void setRoute(NextBusRoute route) {
		this.route = route;
	}

	public NextBusStop getStop() {
		return stop;
	}

	public void setStop(NextBusStop stop) {
		this.stop = stop;
	}

	public List<NextBusRoute> getRoutes() {
		return routes;
	}

	public List<NextBusDirection> getDirections() {
		return directions;
	}

	public List<NextBusStop> getStops() {
		return stops;
	}
	

}
