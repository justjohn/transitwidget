package com.example.helloandroid.feed.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.helloandroid.adapters.BaseItem;
import com.example.helloandroid.provider.TransitServiceDataProvider;

public class Agency extends BaseItem {
	public static final String LOGTAG = Agency.class.getName();
	
	public static final String TABLE_NAME = "agencies";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.agendy";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.agendy";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String SERVICE = "service";
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	public static final String SHORT_TITLE = "short_title";
	public static final String REGION_TITLE = "region_title";

	public Agency() {}
	
	public Agency(Cursor cursor) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.service = cursor.getString(cursor.getColumnIndex(SERVICE));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		setTag(cursor.getString(cursor.getColumnIndex(TAG)));
		this.shortTitle = cursor.getString(cursor.getColumnIndex(SHORT_TITLE));
		this.regionTitle = cursor.getString(cursor.getColumnIndex(REGION_TITLE));
	}
	
	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(SERVICE, service);
		values.put(TAG, getTag());
		values.put(TITLE, title);
		values.put(SHORT_TITLE, shortTitle);
		values.put(REGION_TITLE, regionTitle);
		return values;
	}
	
	private int id;
	private String service;
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

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}
	

    /**
     * Creates the underlying database.
     */
    public static void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " ( " 
				   + _ID + " INTEGER PRIMARY KEY, "
				   + SERVICE + " TEXT, "
				   + TAG + " TEXT, "
				   + TITLE + " TEXT, "
				   + SHORT_TITLE + " TEXT, "
				   + REGION_TITLE + " TEXT"
			   + " );";

		Log.w(LOGTAG, "Creating service data agency table with sql " + sql);
		db.execSQL(sql);
    }

    /**
     * Upgrade the database tables.
     */
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to do here (yet)
    }
}
