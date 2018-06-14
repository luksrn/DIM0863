package br.ufrn.dimap.dim0863.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.ufrn.dimap.dim0863.domain.CarInfo;
import br.ufrn.dimap.dim0863.sync.CarInfoContentProvider;
import br.ufrn.dimap.dim0863.util.DateUtil;

public class CarInfoDao {

    private static CarInfoDao instance = null;

    public static CarInfoDao getInstance() {
        if(instance == null) {
            instance = new CarInfoDao();
        }
        return instance;
    }

    public Uri add(ContentResolver contentResolver, CarInfo carInfo) {
        ContentValues values = new ContentValues();
        values.put(CarInfoContentProvider.DATE, DateUtil.convertToString(carInfo.getDate()));
        values.put(CarInfoContentProvider.LICENSE_PLATE, carInfo.getLicensePlate());
        values.put(CarInfoContentProvider.SPEED, carInfo.getSpeed());
        values.put(CarInfoContentProvider.RPM, carInfo.getRpm());

        return contentResolver.insert(CarInfoContentProvider.CONTENT_URI, values);
    }

    public int remove(ContentResolver contentResolver, CarInfo carInfo) {
        int id = carInfo.getId();
        String carInfoURL= CarInfoContentProvider.URL + "/" + id;
        Uri carInfoURI = Uri.parse(carInfoURL);

        return contentResolver.delete(carInfoURI, null, null);
    }

    public List<CarInfo> findAll(ContentResolver contentResolver) {
        List<CarInfo> tempCarInfoList = new ArrayList<>();

        String[] projection = {
                CarInfoContentProvider._ID,
                CarInfoContentProvider.LICENSE_PLATE,
                CarInfoContentProvider.RPM,
                CarInfoContentProvider.SPEED
        };

        Cursor cursor = contentResolver.query(CarInfoContentProvider.CONTENT_URI, projection, null, null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider._ID));
                    String strDate = cursor.getString(cursor.getColumnIndexOrThrow(CarInfoContentProvider.DATE));
                    Date date = DateUtil.convertFromString(strDate);
                    String licensePlate = cursor.getString(cursor.getColumnIndexOrThrow(CarInfoContentProvider.LICENSE_PLATE));
                    int speed = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider.SPEED));
                    int rpm = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider.RPM));

                    CarInfo carInfo = new CarInfo(_id, date, licensePlate, speed, rpm);
                    tempCarInfoList.add(carInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return tempCarInfoList;
    }

}
