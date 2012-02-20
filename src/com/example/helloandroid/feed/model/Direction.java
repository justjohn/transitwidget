package com.example.helloandroid.feed.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.helloandroid.StringUtils;
import com.example.helloandroid.provider.TransitServiceDataProvider;

public class Direction {
	public static final String TABLE_NAME = "directions";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.direction";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.direction";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	public static final String NAME = "name";
	public static final String STOPS = "stops";
	
	private static final String CHAR_SEP = ",";
	
	public Direction() {}

	public Direction(Cursor cursor, Context context) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.tag = cursor.getString(cursor.getColumnIndex(TAG));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		this.name = cursor.getString(cursor.getColumnIndex(NAME));
		
		String stopsStr = cursor.getString(cursor.getColumnIndex(STOPS));
		String[] stopTags = stopsStr.split(CHAR_SEP);
		fillStops(stopTags, context);
	}
	
	private void fillStops(String[] stopTags, Context context) {
		String selection = Stop.TAG + " IN (" + StringUtils.join(stopTags, ",", "\"") + ")";
		Cursor cursor = context.getContentResolver().query(Stop.CONTENT_URI, null, selection, null, null);
		while(cursor.moveToNext()) {
			addStop(new Stop(cursor));
		}
		cursor.close();
	}
	
	private int id;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
