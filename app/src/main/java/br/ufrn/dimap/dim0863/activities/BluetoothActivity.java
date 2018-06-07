package br.ufrn.dimap.dim0863.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import br.ufrn.dimap.dim0863.R;


public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver broadcastReceiverBluetoothState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(BluetoothActivity.this, "Bluetooth desabilitado",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(BluetoothActivity.this, "Desabilitando bluetooth",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(BluetoothActivity.this, "Bluetooth habilitado",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(BluetoothActivity.this, "Habilitando bluetooth",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Button btnEnableDisableBluetooth = findViewById(R.id.btn_enable_disable_bluetooth);
        Button btnFindBluetoothDevices = findViewById(R.id.btn_find_bluetooth_devices);

        btnEnableDisableBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothActivity.this.enableDisableBluetooth();
            }
        });

        btnFindBluetoothDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothActivity.this.findBluetoothDevices();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverBluetoothState);
    }

    private void enableDisableBluetooth() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (this.bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado pelo dispositivo.",
                    Toast.LENGTH_LONG).show();
        } else {
            if (!this.bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothActivity.REQUEST_ENABLE_BT);
            } else {
                this.bluetoothAdapter.disable();
            }

            IntentFilter bluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcastReceiverBluetoothState, bluetoothIntent);
        }
    }

    private void findBluetoothDevices() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BluetoothActivity.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Não foi possível habilitar o Bluetooth.",
                        Toast.LENGTH_LONG).show();
            }
//            else { // resultCode == RESULT_OK
//                Toast.makeText(this, "Bluetooth habilitado.",
//                        Toast.LENGTH_LONG).show();
//            }
        }
    }
}
