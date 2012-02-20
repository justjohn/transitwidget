package com.example.helloandroid.feed.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.helloandroid.provider.TransitServiceDataProvider;

public class Route {
	public static final String TABLE_NAME = "routes";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.route";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.route";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	
	public Route() {}
	
	public Route(Cursor cursor) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.tag = cursor.getString(cursor.getColumnIndex(TAG));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
	}
	
	private int id;
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
		return "ROUTE (tag: " + tag + ", title: " + title + ")";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
