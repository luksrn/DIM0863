package br.ufrn.dimap.dim0863.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class DataSyncService extends Service {

    //Get data stored locally and send to FIWARE when connected to WiFi

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
