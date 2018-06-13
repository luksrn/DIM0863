package br.ufrn.dimap.dim0863.sync;

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

    public static final String AUTHORITY = "br.ufrn.dimap.dim0863.provider";

    public static final String ACCOUNT_TYPE = "br.ufrn.dimap.dim0863";

    public static final String ACCOUNT = "default_account";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action != null && action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                Log.d(TAG, "Wifi is now enabled");

                Account account = new Account(ACCOUNT, ACCOUNT_TYPE);

                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                /*
                 * Request the sync for the default account, authority, and manual sync settings
                 */
                ContentResolver.requestSync(account, AUTHORITY, settingsBundle);
            } else {
                Log.d(TAG, "Wifi is now disabled");

                //TODO Stop sync
            }
        }
    }

}
