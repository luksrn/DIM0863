package br.ufrn.dimap.dim0863.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.adapter.CarInfoListAdapter;
import br.ufrn.dimap.dim0863.dao.CarInfoDao;
import br.ufrn.dimap.dim0863.domain.CarInfo;


public class CarInfoActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

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
        lvCarInfo.setOnItemLongClickListener(this);
    }

    private void addCarInfo() {
        Random random = new Random();
        CarInfo carInfo = new CarInfo(new Date(), "ABC-1234", random.nextInt(100), random.nextInt(5000));

        Uri uri = CarInfoDao.getInstance().add(getContentResolver(), carInfo);
        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
    }

    private void loadCarInfoList() {
        CarInfoActivity.this.carInfoList = CarInfoDao.getInstance().findAll(getContentResolver());
        CarInfoListAdapter carInfoListAdapter = new CarInfoListAdapter(CarInfoActivity.this, R.layout.view_car_info, CarInfoActivity.this.carInfoList);
        lvCarInfo.setAdapter(carInfoListAdapter);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        CarInfo carInfo = carInfoList.get(i);
        CarInfoDao.getInstance().remove(getContentResolver(), carInfo);

        loadCarInfoList();

        return false;
    }

}
