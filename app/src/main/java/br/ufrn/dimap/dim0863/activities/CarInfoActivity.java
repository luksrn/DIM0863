package br.ufrn.dimap.dim0863.activities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.adapter.CarInfoListAdapter;
import br.ufrn.dimap.dim0863.domain.CarInfo;
import br.ufrn.dimap.dim0863.sync.CarInfoContentProvider;


public class CarInfoActivity extends AppCompatActivity {

    private ListView lvCarInfo;
    private List<CarInfo> carInfoList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);

        Button btnAddCarInfo = findViewById(R.id.btn_add_car_info);
        Button btnListCarInfo = findViewById(R.id.btn_list_car_info);

        btnAddCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCarInfo();
            }
        });

        btnListCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCarInfoList();
            }
        });

        carInfoList = new ArrayList<>();
        lvCarInfo = findViewById(R.id.lv_car_info);
    }

    private void addCarInfo() {
        Random random = new Random();

        ContentValues values = new ContentValues();
        values.put(CarInfoContentProvider.LICENSE_PLATE, "ABC-1234");
        values.put(CarInfoContentProvider.SPEED, random.nextInt(100));
        values.put(CarInfoContentProvider.RPM, random.nextInt(5000));

        Uri uri = getContentResolver().insert(CarInfoContentProvider.CONTENT_URI, values);
        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
    }

    private void loadCarInfoList() {
        List<CarInfo> tempCarInfoList = new ArrayList<>();

        String URL = "content://br.ufrn.dimap.dim0863.provider/car_info";
        Uri carInfoUri = Uri.parse(URL);

        String[] projection = {
                CarInfoContentProvider._ID,
                CarInfoContentProvider.LICENSE_PLATE,
                CarInfoContentProvider.RPM,
                CarInfoContentProvider.SPEED
        };

        Cursor cursor = getContentResolver().query(carInfoUri, projection, null, null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int _id = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider._ID));
                    String licensePlate = cursor.getString(cursor.getColumnIndexOrThrow(CarInfoContentProvider.LICENSE_PLATE));
                    int speed = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider.SPEED));
                    int rpm = cursor.getInt(cursor.getColumnIndexOrThrow(CarInfoContentProvider.RPM));

                    CarInfo carInfo = new CarInfo(_id, licensePlate, speed, rpm);
                    tempCarInfoList.add(carInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        CarInfoActivity.this.carInfoList = tempCarInfoList;

        CarInfoListAdapter carInfoListAdapter = new CarInfoListAdapter(CarInfoActivity.this, R.layout.view_car_info, CarInfoActivity.this.carInfoList);
        lvCarInfo.setAdapter(carInfoListAdapter);
    }

}
