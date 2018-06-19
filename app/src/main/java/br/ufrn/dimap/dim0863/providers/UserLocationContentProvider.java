package br.ufrn.dimap.dim0863.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;

public class UserLocationContentProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "br.ufrn.dimap.dim0863.provider";
    public static final String URL = "content://" + PROVIDER_NAME + "/user_location";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String _ID = "_id";
    public static final String DATE = "date";
    public static final String LOGIN = "login";
    public static final String LAT = "lat";
    public static final String LON = "lon";

    private static HashMap<String, String> USER_LOCATION_PROJECTION_MAP;

    static final int USER_LOCATION = 1;
    static final int USER_LOCATION_ID = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "user_location", USER_LOCATION);
        uriMatcher.addURI(PROVIDER_NAME, "user_location/#", USER_LOCATION_ID);
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "UfrnDrivers";
    static final String USER_LOCATION_TABLE_NAME = "user_location";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            "CREATE TABLE " + USER_LOCATION_TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATE + " TEXT NOT NULL, " +
                    LOGIN + " TEXT NOT NULL, " +
                    LAT + " REAL NOT NULL, " +
                    LON + " REAL NOT NULL);";

    //Helper class that actually creates and manages the provider's underlying data repository.
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + USER_LOCATION_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        //Create a write able database which will trigger its creation if it doesn't already exist.
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        //Add a new user location record
        long rowID = db.insert(USER_LOCATION_TABLE_NAME, "", values);

        //If record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(USER_LOCATION_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case USER_LOCATION:
                qb.setProjectionMap(USER_LOCATION_PROJECTION_MAP);
                break;

            case USER_LOCATION_ID:
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder.equals("")){
            //By default sort on license plate value
            sortOrder = LOGIN;
        }

        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);
        //register to watch a content URI for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case USER_LOCATION:
                count = db.delete(USER_LOCATION_TABLE_NAME, selection, selectionArgs);
                break;

            case USER_LOCATION_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(USER_LOCATION_TABLE_NAME, _ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case USER_LOCATION:
                count = db.update(USER_LOCATION_TABLE_NAME, values, selection, selectionArgs);
                break;

            case USER_LOCATION_ID:
                count = db.update(USER_LOCATION_TABLE_NAME, values,_ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            //Get all car info
            case USER_LOCATION:
                return "vnd.android.cursor.dir/vnd.example.user_location";
            //Get a particular car info
            case USER_LOCATION_ID:
                return "vnd.android.cursor.item/vnd.example.user_location";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

}
