package com.example.helloandroid.feed.model;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.helloandroid.adapters.BaseItem;
import com.example.helloandroid.provider.TransitServiceDataProvider;

public class Agency extends BaseItem {
	public static final String TABLE_NAME = "agencies";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.agendy";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.agendy";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String SHORT_TITLE = "short_title";
	public static final String REGION_TITLE = "region_title";

	public Agency() {}
	
	public Agency(Cursor cursor) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		this.shortTitle = cursor.getString(cursor.getColumnIndex(SHORT_TITLE));
		this.regionTitle = cursor.getString(cursor.getColumnIndex(REGION_TITLE));
	}
	
	private int id;
	private String title;
	private String shortTitle;
	private String regionTitle;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShortTitle() {
		return shortTitle;
	}
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	public String getRegionTitle() {
		return regionTitle;
	}
	public void setRegionTitle(String regionTitle) {
		this.regionTitle = regionTitle;
	}
	
	@Override
	public String toString() {
		return "title: " + title + ", tag: " + getTag() + ", shortTitle: " + shortTitle + ", regionTitle: " + regionTitle;
	}
	
	@Override
	public String getItemLabel() {
		return getShortTitle();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
