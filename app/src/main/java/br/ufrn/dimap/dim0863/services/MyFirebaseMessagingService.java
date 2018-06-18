package br.ufrn.dimap.dim0863.services;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import br.ufrn.dimap.dim0863.domain.AppNotification;
import br.ufrn.dimap.dim0863.receivers.CollectDataBroadcastReceiver;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseListener";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String action = data.get("action");

            if (action != null && !action.equals("")) {
                if (action.equals(AppNotification.START_COLLECT.name())) {
                    Log.d(TAG, "Requesting collect start");
                    Intent startDataCollectIntent = new Intent(CollectDataBroadcastReceiver.START_COLLECT_REQUESTED);
                    sendBroadcast(startDataCollectIntent);

                } else if (action.equals(AppNotification.STOP_COLLECT.name())) {
                    Log.d(TAG, "Requesting collect stop");
                    Intent stopDataCollectIntent = new Intent(CollectDataBroadcastReceiver.STOP_COLLECT_REQUESTED);
                    sendBroadcast(stopDataCollectIntent);

                } else {
                    Log.d(TAG, "Unknown message received. Message: " + action);
                }
            } else {
                Log.d(TAG, "Unknown message received. Message data payload: " + data);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

}
