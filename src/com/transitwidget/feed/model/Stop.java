package com.transitwidget.feed.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.transitwidget.provider.TransitServiceDataProvider;

public class Stop {
	public static final String LOGTAG = Stop.class.getName();
	
	public static final String TABLE_NAME = "stops";
	
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/transitwidget.service.stop";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/transitwidget.service.stop";
    
    public static final Uri CONTENT_URI = Uri.parse("content://" + TransitServiceDataProvider.AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = BaseColumns._ID;
	public static final String TITLE = "title";
	public static final String TAG = "tag";
	public static final String STOPID = "stopId";
	public static final String AGENCY = "agency";
	public static final String FAVORITE = "favorite";

	public Stop() {}
	
	public Stop(Cursor cursor) {
		this.id = cursor.getInt(cursor.getColumnIndex(_ID));
		this.tag = cursor.getString(cursor.getColumnIndex(TAG));
		this.title = cursor.getString(cursor.getColumnIndex(TITLE));
		this.agency = cursor.getString(cursor.getColumnIndex(AGENCY));
		this.stopId = cursor.getInt(cursor.getColumnIndex(STOPID));
		this.favorite = cursor.getInt(cursor.getColumnIndex(FAVORITE));
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(STOPID, stopId);
		values.put(TITLE, title);
		values.put(TAG, tag);
		values.put(AGENCY, agency);
		values.put(FAVORITE, getFavorite());
		return values;
	}

	private int id;
	private int stopId;
	private String tag;
	private String title;
	private String agency;
	private int favorite;
	
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

	public int getStopId() {
		return stopId;
	}

	public void setStopId(int stopId) {
		this.stopId = stopId;
	}

    /**
     * @return the favorite
     */
    public int getFavorite() {
        return favorite;
    }

    /**
     * @param favorite the favorite to set
     */
    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    /**
     * Creates the underlying database.
     */
    public static void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " ( " 
				   + _ID + " INTEGER PRIMARY KEY, "
				   + STOPID + " INTEGER, "
				   + FAVORITE + " INTEGER DEFAULT 0, "
				   + TAG + " TEXT, "
				   + AGENCY + " TEXT, "
				   + TITLE + " TEXT"
			   + " );";

		Log.w(LOGTAG, "Creating service data stop table with sql " + sql);
		db.execSQL(sql);
    }

    /**
     * Upgrade the database tables.
     */
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            String sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FAVORITE + " INTEGER DEFAULT 0;";
            Log.w(LOGTAG, "Adding column favorites to service stop table: " + sql);
            db.execSQL(sql);
        }
    }
}
