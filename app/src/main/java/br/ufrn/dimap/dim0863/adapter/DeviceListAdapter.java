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

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private final List<BluetoothDevice> devices;
    private final LayoutInflater layoutInflater;
    private final int viewResourceId;


    public DeviceListAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<BluetoothDevice> devices) {
        super(context, textViewResourceId, devices);

        this.devices = devices;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.viewResourceId = textViewResourceId;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = this.layoutInflater.inflate(viewResourceId, null);
        }

        BluetoothDevice device = devices.get(position);

        if(device != null) {
            TextView tvDeviceName = convertView.findViewById(R.id.tv_device_name);
            TextView tvDeviceAddress = convertView.findViewById(R.id.tv_device_address);

            String deviceName = device.getName();
            String deviceAddress = device.getAddress();

            if(deviceName != null) {
                tvDeviceName.setText(deviceName);
            }
            if(deviceAddress != null) {
                tvDeviceAddress.setText(deviceAddress);
            }
        }
        return convertView;
    }
}
