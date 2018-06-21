package br.ufrn.dimap.dim0863.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import br.ufrn.dimap.dim0863.services.LocationDataService;
import br.ufrn.dimap.dim0863.services.ObdDataService;
import br.ufrn.dimap.dim0863.util.ServiceTools;

public class CollectDataBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = "CollectData";

    public static final String START_COLLECT_REQUESTED = "br.ufrn.dimap.dim0863.START_COLLECT_REQUESTED";
    public static final String STOP_COLLECT_REQUESTED = "br.ufrn.dimap.dim0863.STOP_COLLECT_REQUESTED";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action != null) {
            if (action.equals(START_COLLECT_REQUESTED)) {
                Log.d(TAG, "Start collect action");

                Intent startLocationServiceIntent = new Intent(context, LocationDataService.class);
                if(!ServiceTools.isServiceRunning(context, LocationDataService.class)) {
                    context.startService(startLocationServiceIntent);
                }

                Intent startObdServiceIntent = new Intent(context, ObdDataService.class);
                //TODO Store OBD devices addresses on database and use dynamically desired address to connect
                startObdServiceIntent.putExtra(ObdDataService.OBD_MAC_ADDRESS_EXTRA, ObdDataService.OBD_MAC_ADDRESS);
                //TODO Get license plate of authorized car
                startObdServiceIntent.putExtra(ObdDataService.CAR_LICENSE_PLATE_EXTRA, "ABC-1234");

                if(!ServiceTools.isServiceRunning(context, ObdDataService.class)) {
                    context.startService(startObdServiceIntent);
                }

            } else if (action.equals(STOP_COLLECT_REQUESTED)) {
                Log.d(TAG, "Stop collect action");

                Intent stopLocationServiceIntent = new Intent(context, LocationDataService.class);
                if(ServiceTools.isServiceRunning(context, LocationDataService.class)) {
                    context.stopService(stopLocationServiceIntent);
                }

                Intent stopObdServiceIntent = new Intent(context, ObdDataService.class);
                if(ServiceTools.isServiceRunning(context, ObdDataService.class)) {
                    context.stopService(stopObdServiceIntent);
                }
            }
        }
    }

}
