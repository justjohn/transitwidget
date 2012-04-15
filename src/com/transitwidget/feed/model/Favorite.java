package com.transitwidget.feed.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.transitwidget.provider.TransitServiceDataProvider;

public class Favorite {
	public static final String LOGTAG = Favorite.class.getName();
	
	public static final String TABLE_NAME = "favorites";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.favorite";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.favorite";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	
	public static final String STOP_LABEL = "stopLabel";
	public static final String ROUTE_LABEL = "routeLabel";
	
	public static final String ROUTE = "route";
	public static final String DIRECTION = "direction";
	public static final String STOP = "stop";
	public static final String AGENCY = "agency";

	private int id;
	
	// Id references to data in other tables
	private String stop;
	private String route;
	private String direction;
	
	private String agency;

	private String stopLabel;
	private String routeLabel;
	
	public Favorite() {}
	
	public Favorite(Cursor cursor) {
		id = cursor.getInt(cursor.getColumnIndex(_ID));
		agency = cursor.getString(cursor.getColumnIndex(AGENCY));
		route = cursor.getString(cursor.getColumnIndex(ROUTE));
		direction = cursor.getString(cursor.getColumnIndex(DIRECTION));
		stop = cursor.getString(cursor.getColumnIndex(STOP));
		stopLabel = cursor.getString(cursor.getColumnIndex(STOP_LABEL));
		routeLabel = cursor.getString(cursor.getColumnIndex(ROUTE_LABEL));
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(STOP, stop);
		values.put(DIRECTION, direction);
		values.put(ROUTE, route);
		values.put(AGENCY, agency);
		values.put(STOP_LABEL, stopLabel);
		values.put(ROUTE_LABEL, routeLabel);
		return values;
	}

	@Override
	public String toString() {
		return "FAVORITE (route: " + route + ", direction: " + direction + ", stop: " + stop + ")";
	}

	public String getStopLabel() {
		return stopLabel;
	}

	public void setStopLabel(String stopLabel) {
		this.stopLabel = stopLabel;
	}

	public String getRouteLabel() {
		return routeLabel;
	}

	public void setRouteLabel(String routeLabel) {
		this.routeLabel = routeLabel;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(String stop) {
		this.stop = stop;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}
	

    /**
     * Creates the underlying database.
     */
    public static void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " ( " 
					   + _ID + " INTEGER PRIMARY KEY, "
					   + AGENCY + " TEXT, "
					   + ROUTE + " TEXT, "
					   + DIRECTION + " TEXT, "
					   + STOP + " TEXT, "
					   + ROUTE_LABEL + " TEXT, "
					   + STOP_LABEL + " TEXT"
				   + " );";

		Log.w(LOGTAG, "Creating service data favorites table with sql " + sql);
		db.execSQL(sql);
    }

    /**
     * Upgrade the database tables.
     */
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if (newVersion == 2) onCreate(db);
    }
}
