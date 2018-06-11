package br.ufrn.dimap.dim0863.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ObdDataService extends Service {

    public static final String OBD_MAC_ADDRESS = "OBD_MAC_ADDRESS";
    private static final String OBD_SERVICE_TAG = "ObdDtService";
    private static final UUID BLUETOOTH_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter = null;

    private ConnectingThread connectingThread;
    private ConnectedThread connectedThread;
    private boolean stopConnectedThread;

    private String searchedObdAddress;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(OBD_SERVICE_TAG, "Service created");
        stopConnectedThread = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(OBD_SERVICE_TAG, "Service started");

        this.searchedObdAddress = intent.getStringExtra(OBD_MAC_ADDRESS);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkBTState();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopConnectedThread = true;

        if (connectedThread != null) {
            connectedThread.closeStreams();
        }
        if (connectingThread != null) {
            connectingThread.closeSocket();
        }
        Log.d(OBD_SERVICE_TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if (this.bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado pelo dispositivo.",
                    Toast.LENGTH_LONG).show();
            stopSelf();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Log.d(OBD_SERVICE_TAG, "Bluetooth enabled!");
                findBluetoothDevices();
            } else {
                Log.d(OBD_SERVICE_TAG, "Bluetooth not ON. Stopping service");
                stopSelf();
            }
        }
    }

    private void findBluetoothDevices() {
        if(bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

        IntentFilter findDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiverBluetoothFindDevices, findDevicesIntent);
    }

    private class ConnectingThread extends Thread {

        private static final String OBD_SERVICE_CONNECTING_TAG = "ObdDtServiceConnecting";

        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        ConnectingThread(BluetoothDevice device) {
            Log.d(OBD_SERVICE_CONNECTING_TAG, "In ConnectingThread constructor");
            this.device = device;

            BluetoothSocket temp = null;
            Log.d(OBD_SERVICE_CONNECTING_TAG, "OBD MAC address: " + searchedObdAddress);
            Log.d(OBD_SERVICE_CONNECTING_TAG, "Bluetooth UUID: " + BLUETOOTH_MODULE_UUID);

            try {
                temp = this.device.createRfcommSocketToServiceRecord(BLUETOOTH_MODULE_UUID);
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket created: " + temp.toString());
            } catch (IOException e) {
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket creation failed:" + e.toString());
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket creation failed. Stopping service");
                stopSelf();
            }
            socket = temp;
        }

        @Override
        public void run() {
            super.run();
            Log.d(OBD_SERVICE_CONNECTING_TAG, "In ConnectingThread run");

            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();

                Log.d(OBD_SERVICE_CONNECTING_TAG, "Bluetooth socket connected");
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();

//                //I send a character when resuming.beginning transmission to check device is connected
//                //If it is not an exception will be thrown in the write method and finish() will be called
//                connectedThread.write("x");
            } catch (IOException e) {
                try {
                    Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket connection failed: " + e.toString());
                    Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket connection failed. Stopping service");
                    socket.close();
                    stopSelf();
                } catch (IOException e2) {
                    Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket closing failed:" + e2.toString());
                    Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket closing failed. Stopping service");
                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Connected thread start failed: " + e.toString());
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Connected thread start failed. Stopping service");
                stopSelf();
            }
        }

        void closeSocket() {
            try {
                socket.close();  //Don't leave Bluetooth sockets open when leaving activity
            } catch (IOException e) {
                //insert code to deal with this
                Log.d(OBD_SERVICE_CONNECTING_TAG, e.toString());
                Log.d(OBD_SERVICE_CONNECTING_TAG, "Socket closing failed. Stopping service");
                stopSelf();
            }
        }
    }

    private class ConnectedThread extends Thread {

        private static final String OBD_SERVICE_CONNECTED_TAG = "ObdDtServiceConnected";

        private final InputStream inputStream;
        private final OutputStream outputStream;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(OBD_SERVICE_CONNECTED_TAG, "In ConnectedThread constructor");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.d(OBD_SERVICE_CONNECTED_TAG, e.toString());
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Unable to read/write. Stopping service");
                stopSelf();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            Log.d(OBD_SERVICE_CONNECTED_TAG, "In ConnectedThread run");

            try {
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Echo Off");
                new EchoOffCommand().run(inputStream, outputStream);
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Line Feed Off");
                new LineFeedOffCommand().run(inputStream, outputStream);
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Timeout Off");
                new TimeoutCommand(125).run(inputStream, outputStream);
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Select Protocol");
                new SelectProtocolCommand(ObdProtocols.AUTO).run(inputStream, outputStream);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            while (!stopConnectedThread) {
                try {
                    SpeedCommand speedCommand = new SpeedCommand();
                    speedCommand.run(inputStream, outputStream);

                    RPMCommand rpmCommand = new RPMCommand();
                    rpmCommand.run(inputStream, outputStream);

                    Log.d(OBD_SERVICE_CONNECTED_TAG, "Speed: " + speedCommand.getFormattedResult() + ", RPM: " + rpmCommand.getFormattedResult());

                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    Log.d(OBD_SERVICE_CONNECTED_TAG, e.toString());
                    Log.d(OBD_SERVICE_CONNECTED_TAG, "Unable to read/write. Stopping service");
                    stopSelf();
                    break;
                }
            }
        }

        void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                //insert code to deal with this
                Log.d(OBD_SERVICE_CONNECTED_TAG, e.toString());
                Log.d(OBD_SERVICE_CONNECTED_TAG, "Stream closing failed. Stopping service");
                stopSelf();
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
                        Toast.makeText(context, "Bluetooth desabilitado",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(context, "Desabilitando bluetooth",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context, "Bluetooth habilitado",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(context, "Habilitando bluetooth",
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
                        Toast.makeText(context, "Descoberta habilitada",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Toast.makeText(context, "Descoberta desabilitada. Apto para receber conexões",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Toast.makeText(context, "Descoberta desabilitada. Não é possível receber conexões",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTING:
                        Toast.makeText(context, "Conectando...",
                                Toast.LENGTH_LONG).show();
                        break;

                    case BluetoothAdapter.STATE_CONNECTED:
                        Toast.makeText(context, "Conectado",
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
                Log.d(OBD_SERVICE_TAG, "Found a device: " + device.getName() + " - " + device.getAddress());

                if (device.getAddress() != null && device.getAddress().equals(searchedObdAddress)) {
                    Log.d(OBD_SERVICE_TAG, "Searched device found.");
                    Log.d(OBD_SERVICE_TAG, "Attempting to connect to remote device: " + searchedObdAddress);
                    connectingThread = new ConnectingThread(device);
                    connectingThread.start();

                    unregisterReceiver(this);
                }
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
}
