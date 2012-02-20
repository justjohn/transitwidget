package com.example.helloandroid.provider;

import com.example.helloandroid.provider.contract.WidgetConfiguration;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class WidgetConfigurationProvider extends ContentProvider {
    private static final String TAG = WidgetConfigurationProvider.class.getName();
	
    private static final String DATABASE_NAME = "transitWidget.db";
	private static final int DATABASE_VERSION = 1;

    public static final String AUTHORITY = "transitwidget.provider.WidgetConfigurationProvider";
    
    // URI matching constants
    private static final int WIDGETS = 1;
    private static final int WIDGET_ID = 2;
    
    private static final UriMatcher sUriMatcher;

    private DatabaseHelper mHelper;
    private SQLiteDatabase database;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, WidgetConfiguration.TABLE_NAME, WIDGETS);
        sUriMatcher.addURI(AUTHORITY, WidgetConfiguration.TABLE_NAME + "/#", WIDGET_ID);
    }
    
    @Override
    public boolean onCreate() {
        mHelper = new DatabaseHelper(getContext());
        database = mHelper.getWritableDatabase();
        
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case WIDGETS:
                qBuilder.setTables(WidgetConfiguration.TABLE_NAME);
                
                break;
            case WIDGET_ID:
                qBuilder.setTables(WidgetConfiguration.TABLE_NAME);
                qBuilder.appendWhere(WidgetConfiguration._ID + " = " + uri.getLastPathSegment());
                
                break;
            default:
                throw new IllegalArgumentException("URI " + uri + " not recognized by widget configuration content provider.");
        }

        // Make the query.
        Cursor c = qBuilder.query(database,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case WIDGETS:
                return WidgetConfiguration.CONTENT_TYPE;
            case WIDGET_ID:
                return WidgetConfiguration.CONTENT_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String table = null;
        Uri baseUri = null;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case WIDGETS:
                table   = WidgetConfiguration.TABLE_NAME;
                baseUri = WidgetConfiguration.CONTENT_URI;
                
                break;
            default:
                throw new IllegalArgumentException("URI " + uri + " cannot be inserted by widget configuration content provider.");
        }
        
        long id = database.insert(table, null, values);
        return Uri.withAppendedPath(baseUri, String.valueOf(id));
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String table = null;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case WIDGETS:
                table = WidgetConfiguration.TABLE_NAME;
                break;
                
            default:
                throw new IllegalArgumentException("URI " + uri + " cannot be deleted by widget configuration content provider.");
        }
        
        int count = database.delete(table, selection, selectionArgs);
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String table = null;
        int match = sUriMatcher.match(uri);
        switch (match)
        {
            case WIDGETS:
                table = WidgetConfiguration.TABLE_NAME;
                break;
                
            default:
                throw new IllegalArgumentException("URI " + uri + " cannot be deleted by widget configuration content provider.");
        }
        
        int count = database.update(table, values, selection, selectionArgs);
        
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    

	 class DatabaseHelper extends SQLiteOpenHelper {
	
		DatabaseHelper(Context context) {
	        // calls the super constructor, requesting the default cursor factory.
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }
	
	    /**
	     * Creates the underlying database.
	     */
	    @Override
	    public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + WidgetConfiguration.TABLE_NAME + " ( " 
					   + WidgetConfiguration._ID + " INTEGER PRIMARY KEY, "
					   + WidgetConfiguration.WIDGET_ID + " INTEGER, "
					   + WidgetConfiguration.AGENCY + " TEXT, "
					   + WidgetConfiguration.DIRECTION + " TEXT, "
					   + WidgetConfiguration.STOP + " TEXT, "
					   + WidgetConfiguration.START_TIME + " INTEGER, "
					   + WidgetConfiguration.END_TIME + " INTEGER, "
					   + WidgetConfiguration.DAYS + " TEXT"
				   + " );";

			Log.w(TAG, "Creating widget configuration table with sql " + sql);
			db.execSQL(sql);
	    }
	
	    /**
	     * Upgrade the database tables.
	     */
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Logs that the database is being upgraded
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			
			// Nothing to do here (yet)
	    }
	}
}
