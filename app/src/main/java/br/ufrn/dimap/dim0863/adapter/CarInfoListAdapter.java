package br.ufrn.dimap.dim0863.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.domain.CarInfo;


public class CarInfoListAdapter extends ArrayAdapter<CarInfo> {

    private final List<CarInfo> carInfoList;
    private final LayoutInflater layoutInflater;
    private final int viewResourceId;


    public CarInfoListAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<CarInfo> carInfoList) {
        super(context, textViewResourceId, carInfoList);

        this.carInfoList = carInfoList;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.viewResourceId = textViewResourceId;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = this.layoutInflater.inflate(viewResourceId, null);
        }

        CarInfo carInfo = carInfoList.get(position);

        if(carInfo != null) {
            TextView tvLicensePlate = convertView.findViewById(R.id.tv_car_info_license_plate);
            TextView tvSpeed = convertView.findViewById(R.id.tv_car_info_speed);
            TextView tvRpm = convertView.findViewById(R.id.tv_car_info_rpm);

            String licensePlate = carInfo.getLicensePlate();
            int speed = carInfo.getSpeed();
            int rpm = carInfo.getRpm();

            if(licensePlate != null) {
                tvLicensePlate.setText(licensePlate);
            }
            tvSpeed.setText(String.format("%d km/h", speed));
            tvRpm.setText(String.format("%d rpm", rpm));
        }
        return convertView;
    }
}
