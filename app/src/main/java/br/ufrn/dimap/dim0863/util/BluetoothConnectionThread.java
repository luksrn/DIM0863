package br.ufrn.dimap.dim0863.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectionThread extends Thread {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothSocket socket;

    public BluetoothConnectionThread(BluetoothDevice device) {
        Log.d("BTConnectionThread", "Creating thread");

        // Use a temporary object that is later assigned to socket, because socket is final
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = tmp;
    }

    @Override
    public void run() {
        Log.d("BTConnectionThread", "Running thread");

        // Cancel discovery because it will slow down the connection
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(socket);
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        ProcessDataThread processDataThread = new ProcessDataThread(socket);
        processDataThread.run();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
