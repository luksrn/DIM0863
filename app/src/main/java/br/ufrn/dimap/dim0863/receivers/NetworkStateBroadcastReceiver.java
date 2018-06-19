package br.ufrn.dimap.dim0863.receivers;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = "NetworkState";

    public static final String USER_LOCATION_AUTHORITY = "br.ufrn.dimap.dim0863.user.provider";
    public static final String CAR_INFO_AUTHORITY = "br.ufrn.dimap.dim0863.car.provider";

    public static final String ACCOUNT_TYPE = "br.ufrn.dimap.dim0863";

    public static final String ACCOUNT = "default_account";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action != null && action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                Log.d(TAG, "WiFi is now enabled");

                Account account = new Account(ACCOUNT, ACCOUNT_TYPE);

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                /*
                 * Request the sync for the default account, authority, and manual sync settings
                 */
                ContentResolver.requestSync(account, USER_LOCATION_AUTHORITY, settingsBundle);
                ContentResolver.requestSync(account, CAR_INFO_AUTHORITY, settingsBundle);
            } else {
                Log.d(TAG, "WiFi is now disabled");

                //TODO Verify sync stop
                Account account = new Account(ACCOUNT, ACCOUNT_TYPE);

                ContentResolver.cancelSync(account, USER_LOCATION_AUTHORITY);
                ContentResolver.cancelSync(account, CAR_INFO_AUTHORITY);
            }
        }
    }

}
