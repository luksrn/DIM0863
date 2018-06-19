package br.ufrn.dimap.dim0863.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import br.ufrn.dimap.dim0863.sync.UserLocationSyncAdapter;

/**
 * Define a Service that returns an IBinder for the sync adapter class,
 * allowing the sync adapter framework to call onPerformSync().
 */
public class UserLocationSyncService extends Service {

    private static UserLocationSyncAdapter syncAdapter = null;

    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new UserLocationSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    /**
     * Return an object that allows the system to invoke the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes to call onPerformSync().
         * The object is created in the base class code when the UserLocationSyncAdapter
         * constructors call super()
         */
        return syncAdapter.getSyncAdapterBinder();
    }

}
