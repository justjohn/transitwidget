package com.transitwidget.feed.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.transitwidget.provider.TransitServiceDataProvider;
import com.transitwidget.utils.StringUtils;

public class Direction {
	public static final String LOGTAG = Direction.class.getName();
	
	public static final String TABLE_NAME = "directions";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.direction";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.direction";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	public static final String NAME = "name";
	public static final String STOPS = "stops";
	public static final String AGENCY = "agency";
	public static final String ROUTE = "route";
	
	private static final String SEP_CHAR = ",";
	
	public Direction() {}

	public Direction(Cursor cursor, Context context) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.tag = cursor.getString(cursor.getColumnIndex(TAG));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		this.name = cursor.getString(cursor.getColumnIndex(NAME));
		this.agency = cursor.getString(cursor.getColumnIndex(AGENCY));
		this.route = cursor.getString(cursor.getColumnIndex(ROUTE));
		
		String stopsStr = cursor.getString(cursor.getColumnIndex(STOPS));
		String[] stopTags = stopsStr.split(SEP_CHAR);
		fillStops(stopTags, context);
	}
	
	/**
	 * Add the stops in the right order from the database to this direction.
	 * 
	 * @param stopTags
	 * @param context
	 */
	private void fillStops(String[] stopTags, Context context) {
		// Get the stop from the database
		String selection = Stop.AGENCY + " = ? AND " + Stop.TAG + " IN (" + StringUtils.join(stopTags, ",", "\"") + ")";
		String[] selectionArgs = { agency };
		Cursor cursor = context.getContentResolver().query(Stop.CONTENT_URI, null, selection, selectionArgs, null);
		
		// Get a handle on the elements form the database. 
		// 		Because stops can be shared between routes/directions the order
		// 		in the database isn't the same as this route segment.
		Map<String, Stop> stopMap = new HashMap<String, Stop>();
		while(cursor.moveToNext()) {
			Stop stop = new Stop(cursor);
			stopMap.put(stop.getTag(), stop);
		}
		cursor.close();
		
		// Add the stops in the right order
		for (String stopTag : stopTags) {
			addStop(stopMap.get(stopTag));
		}
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(TITLE, title);
		values.put(TAG, tag);
		values.put(NAME, name);
		values.put(AGENCY, agency);
		values.put(ROUTE, route);
		String[] stopsStr = new String[getStops().size()];
		int i = 0;
		for (Stop stop : getStops()) {
			stopsStr[i++] = stop.getTag();
		}
		values.put(STOPS, StringUtils.join(stopsStr, SEP_CHAR));
		return values;
	}
	
	private int id;
	private String tag;
	private String title;
	private String name;
	private String agency;
	private String route;
	
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
				   + TAG + " TEXT, "
				   + NAME + " TEXT, "
				   + TITLE + " TEXT, "
				   + AGENCY + " TEXT, "
				   + ROUTE + " TEXT, "
				   + STOPS + " TEXT"
			   + " );";

		Log.w(LOGTAG, "Creating service data direction table with sql " + sql);
		db.execSQL(sql);
    }

    /**
     * Upgrade the database tables.
     */
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to do here (yet)
    }

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
