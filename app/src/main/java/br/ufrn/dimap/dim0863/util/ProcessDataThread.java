package br.ufrn.dimap.dim0863.util;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;

public class ProcessDataThread extends Thread {

    private final BluetoothSocket socket;
    private boolean storeData;

    public ProcessDataThread(BluetoothSocket socket) {
        Log.d("ProcessDataThread", "Creating thread");
        this.socket = socket;
        this.storeData = true;
    }

    @Override
    public void run() {
        Log.d("ProcessDataThread", "Running thread");

        try {
            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        while(this.storeData) {
            try {
                SpeedCommand speedCommand = new SpeedCommand();
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());

                RPMCommand rpmCommand = new RPMCommand();
                rpmCommand.run(socket.getInputStream(), socket.getOutputStream());

                Log.d("ProcessDataThread", "Speed: " + speedCommand.getFormattedResult());
                Log.d("ProcessDataThread", "RPM: " + rpmCommand.getFormattedResult());

                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** Will cancel an in-progress data storing and close the socket */
    public void cancel() {
        this.storeData = false; //TODO Stop storing data

        //Close connection
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
