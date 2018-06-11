package br.ufrn.dimap.dim0863.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import br.ufrn.dimap.dim0863.R;
import br.ufrn.dimap.dim0863.adapter.DeviceListAdapter;
import br.ufrn.dimap.dim0863.services.ObdDataService;
import br.ufrn.dimap.dim0863.util.BluetoothConnectionThread;
import br.ufrn.dimap.dim0863.util.ServiceTools;


public class BluetoothActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_PERMISSIONS = 1001;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ListView lvNewDevices;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Button btnEnableDisableBluetooth = findViewById(R.id.btn_enable_disable_bluetooth);
        Button btnEnableDisableDiscovery = findViewById(R.id.btn_enable_disable_discovery);
        Button btnFindBluetoothDevices = findViewById(R.id.btn_find_bluetooth_devices);
        Button btnStartDataCollection = findViewById(R.id.btn_start_data_collection);

        btnEnableDisableBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothActivity.this.enableDisableBluetooth();
            }
        });

        btnEnableDisableDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothActivity.this.enableDisableDiscovery();
            }
        });

        btnFindBluetoothDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothActivity.this.findBluetoothDevices();
            }
        });

        btnStartDataCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(BluetoothActivity.this, ObdDataService.class);
                //TODO Store OBD devices addresses on database and use dynamically desired address to connect
                serviceIntent.putExtra(ObdDataService.OBD_MAC_ADDRESS, "60:F8:1D:B4:B6:31");

                if(!ServiceTools.isServiceRunning(BluetoothActivity.this, ObdDataService.class)) {
                    startService(serviceIntent);
                } else {
                    stopService(serviceIntent);
                }
            }
        });

        bluetoothDevices = new ArrayList<>();
        lvNewDevices = findViewById(R.id.lv_new_devices);

        lvNewDevices.setOnItemClickListener(this);
        lvNewDevices.setOnItemLongClickListener(this);

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter bondStateChangedIntent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiverBluetoothBondState, bondStateChangedIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverBluetoothState);
    }

    private void enableDisableBluetooth() {
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

    private void enableDisableDiscovery() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter discoveryIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcastReceiverBluetoothScanMode, discoveryIntent);
    }

    private void findBluetoothDevices() {
        this.bluetoothDevices = new ArrayList<>();

        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        checkBluetoothPermissions();

        bluetoothAdapter.startDiscovery();

        IntentFilter findDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiverBluetoothFindDevices, findDevicesIntent);
    }

    private void checkBluetoothPermissions() {
        int permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BluetoothActivity.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Não foi possível habilitar o Bluetooth.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private final BroadcastReceiver broadcastReceiverBluetoothState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

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

    private final BroadcastReceiver broadcastReceiverBluetoothScanMode = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Toast.makeText(BluetoothActivity.this, "Descoberta habilitada",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(BluetoothActivity.this, "Descoberta desabilitada. Apto para receber conexões",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(BluetoothActivity.this, "Descoberta desabilitada. Não é possível receber conexões",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(BluetoothActivity.this, "Conectando...",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Toast.makeText(BluetoothActivity.this, "Conectado",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverBluetoothFindDevices = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);

                DeviceListAdapter deviceListAdapter = new DeviceListAdapter(context, R.layout.view_device_adapter, bluetoothDevices);
                lvNewDevices.setAdapter(deviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver broadcastReceiverBluetoothBondState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = device.getName();

                //Case 01: Already bonded
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, "Já pareado com o dispositivo '" + deviceName + "'",
                            Toast.LENGTH_LONG).show();
                }
                //Case 02: Creating a bond
                if(device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(context, "Pareando com o dispositivo '" + deviceName + "'",
                            Toast.LENGTH_LONG).show();
                }
                //Case 03: Breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, "Despareado com o dispositivo '" + deviceName + "'",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        bluetoothAdapter.cancelDiscovery();

        BluetoothDevice device = bluetoothDevices.get(i);
        device.createBond();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        bluetoothAdapter.cancelDiscovery();

        BluetoothDevice device = bluetoothDevices.get(i);

        String deviceName = device.getName();

        Toast.makeText(BluetoothActivity.this, "Conectando ao dispositivo '" + deviceName + "'",
                Toast.LENGTH_LONG).show();

        BluetoothConnectionThread connectionThread = new BluetoothConnectionThread(device);
        connectionThread.run();

        return true;
    }
}
