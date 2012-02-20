package com.example.helloandroid.feed.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.helloandroid.provider.TransitServiceDataProvider;

public class Stop {
	public static final String TABLE_NAME = "stops";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.stop";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.stop";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	public static final String AGENCY = "agency";

	public Stop() {}
	
	public Stop(Cursor cursor) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.tag = cursor.getString(cursor.getColumnIndex(TAG));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		this.agency = cursor.getString(cursor.getColumnIndex(AGENCY));
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(TITLE, title);
		values.put(TAG, tag);
		values.put(AGENCY, agency);
		return values;
	}
	
	private int id;
	private String tag;
	private String title;
	private String agency;
	
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
		return "STOP (tag: " + tag + ", title: " + title + ")";
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
				   + AGENCY + " TEXT, "
				   + TITLE + " TEXT"
			   + " );";

		Log.w(TAG, "Creating service data stop table with sql " + sql);
		db.execSQL(sql);
    }

    /**
     * Upgrade the database tables.
     */
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to do here (yet)
    }
}
