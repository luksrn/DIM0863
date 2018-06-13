package br.ufrn.dimap.dim0863.sync;

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

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class CarInfoContentProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "br.ufrn.dimap.dim0863.provider";
    public static final String URL = "content://" + PROVIDER_NAME + "/car_info";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String _ID = "_id";
    public static final String LICENSE_PLATE = "license_plate";
    public static final String SPEED = "speed";
    public static final String RPM = "rpm";

    private static HashMap<String, String> CAR_INFO_PROJECTION_MAP;

    static final int CAR_INFO = 1;
    static final int CAR_INFO_ID = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "car_info", CAR_INFO);
        uriMatcher.addURI(PROVIDER_NAME, "car_info/#", CAR_INFO_ID);
    }

    //Database specific constant declarations
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "College";
    static final String CAR_INFO_TABLE_NAME = "car_info";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE =
            "CREATE TABLE " + CAR_INFO_TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LICENSE_PLATE + " TEXT NOT NULL, " +
                    SPEED + " INTEGER NOT NULL, " +
                    RPM + " INTEGER NOT NULL);";

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
            db.execSQL("DROP TABLE IF EXISTS " +  CAR_INFO_TABLE_NAME);
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
        //Add a new car info record
        long rowID = db.insert(CAR_INFO_TABLE_NAME, "", values);

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
        qb.setTables(CAR_INFO_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case CAR_INFO:
                qb.setProjectionMap(CAR_INFO_PROJECTION_MAP);
                break;

            case CAR_INFO_ID:
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder.equals("")){
            //By default sort on license plate value
            sortOrder = LICENSE_PLATE;
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
            case CAR_INFO:
                count = db.delete(CAR_INFO_TABLE_NAME, selection, selectionArgs);
                break;

            case CAR_INFO_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(CAR_INFO_TABLE_NAME, _ID +  " = " + id +
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
            case CAR_INFO:
                count = db.update(CAR_INFO_TABLE_NAME, values, selection, selectionArgs);
                break;

            case CAR_INFO_ID:
                count = db.update(CAR_INFO_TABLE_NAME, values,_ID + " = " + uri.getPathSegments().get(1) +
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
            //Get all student records
            case CAR_INFO:
                return "vnd.android.cursor.dir/vnd.example.car_info";
            //Get a particular student
            case CAR_INFO_ID:
                return "vnd.android.cursor.item/vnd.example.car_info";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

}
