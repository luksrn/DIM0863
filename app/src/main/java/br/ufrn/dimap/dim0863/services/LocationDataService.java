package br.ufrn.dimap.dim0863.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Date;

import br.ufrn.dimap.dim0863.dao.UserLocationDao;
import br.ufrn.dimap.dim0863.domain.UserLocation;
import br.ufrn.dimap.dim0863.util.Session;

public class LocationDataService extends Service {

    private static final String TAG = "LocationDataService";
    private LocationManager locationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0f;

    private class UserLocationListener implements LocationListener {

        Location lastLocation;

        private UserLocationListener(String provider) {
            Log.d(TAG, "LocationListener: " + provider);
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, String.format("onLocationChanged: (%f, %f)", location.getLatitude(), location.getLongitude()));
            lastLocation.set(location);
            storeUserLocation(location);
        }

        private void storeUserLocation(Location location) {
            Log.d(TAG, "Storing user location");

            Session session = new Session(getApplicationContext());
            String username = session.getusename();

            UserLocation userLocation = new UserLocation(new Date(), location.getLatitude(), location.getLongitude());
            UserLocationDao.getInstance().add(getContentResolver(), username, userLocation);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }
    }

    LocationListener[] locationListeners = new LocationListener[] {
            new UserLocationListener(LocationManager.GPS_PROVIDER),
            new UserLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        initializeLocationManager();
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[1]);
        } catch(java.lang.SecurityException ex) {
            Log.i(TAG, "Fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Network provider does not exist. " + ex.getMessage());
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[0]);
        } catch(java.lang.SecurityException ex) {
            Log.i(TAG, "Fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "GPS provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if(locationManager != null) {
            for (LocationListener locationListener : locationListeners) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception ex) {
                    Log.i(TAG, "Fail to remove location listeners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if(locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

}
