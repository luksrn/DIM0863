package br.ufrn.dimap.dim0863.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.ufrn.dimap.dim0863.domain.UserLocation;
import br.ufrn.dimap.dim0863.providers.UserLocationContentProvider;
import br.ufrn.dimap.dim0863.util.DateUtil;

public class UserLocationDao {

    private static UserLocationDao instance = null;

    public static UserLocationDao getInstance() {
        if(instance == null) {
            instance = new UserLocationDao();
        }
        return instance;
    }

    public Uri add(ContentResolver contentResolver, String login, UserLocation userLocation) {
        ContentValues values = new ContentValues();
        values.put(UserLocationContentProvider.DATE, DateUtil.convertToString(userLocation.getDate()));
        values.put(UserLocationContentProvider.LOGIN, login);
        values.put(UserLocationContentProvider.LAT, userLocation.getLat());
        values.put(UserLocationContentProvider.LON, userLocation.getLon());

        return contentResolver.insert(UserLocationContentProvider.CONTENT_URI, values);
    }

    public int remove(ContentResolver contentResolver, UserLocation userLocation) {
        int id = userLocation.getId();
        String userLocationURL= UserLocationContentProvider.URL + "/" + id;
        Uri userLocationURI = Uri.parse(userLocationURL);

        return contentResolver.delete(userLocationURI, null, null);
    }

    public List<UserLocation> findByLogin(ContentResolver contentResolver, String login) {
        List<UserLocation> tempUserLocationList = new ArrayList<>();

        String[] projection = {
                UserLocationContentProvider._ID,
                UserLocationContentProvider.DATE,
                UserLocationContentProvider.LOGIN,
                UserLocationContentProvider.LAT,
                UserLocationContentProvider.LON
        };

        String selectionClause = UserLocationContentProvider.LOGIN + " = ?";
        String[] selectionArgs = { login };

        Cursor cursor = contentResolver.query(UserLocationContentProvider.CONTENT_URI, projection, selectionClause, selectionArgs, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndexOrThrow(UserLocationContentProvider._ID));
                    String strDate = cursor.getString(cursor.getColumnIndexOrThrow(UserLocationContentProvider.DATE));
                    Date date = DateUtil.convertFromString(strDate);
                    double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(UserLocationContentProvider.LAT));
                    double lon = cursor.getInt(cursor.getColumnIndexOrThrow(UserLocationContentProvider.LON));

                    UserLocation userLocation = new UserLocation(_id, date, lat, lon);
                    tempUserLocationList.add(userLocation);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return tempUserLocationList;
    }

}
