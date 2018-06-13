package br.ufrn.dimap.dim0863.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    //Get data stored locally and send to FIWARE when connected to WiFi

    private final static String TAG = "SyncAdapter";

    // Define a variable to contain a content resolver instance
    ContentResolver contentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        //If your app uses a content resolver, get an instance of it from the incoming Context
        contentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the constructor maintains compatibility with
     * Android 3.0 and later platform versions
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        //If your app uses a content resolver, get an instance of it from the incoming Context
        contentResolver = context.getContentResolver();
    }

    /*
     * Specify the code you want to run in the sync adapter. The entire sync adapter runs
     * in a background thread, so you don't have to set up your own background processing.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //TODO Perform real sync
        for(int i = 0; i < 10; i++) {
            Log.d(TAG, "Syncing data " + i);
        }
    }

}
