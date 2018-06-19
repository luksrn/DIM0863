package br.ufrn.dimap.dim0863.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.ufrn.dimap.dim0863.dao.UserLocationDao;
import br.ufrn.dimap.dim0863.domain.Location;
import br.ufrn.dimap.dim0863.util.DateUtil;
import br.ufrn.dimap.dim0863.util.RequestManager;
import br.ufrn.dimap.dim0863.util.Session;

/**
 * Get data stored locally and send to web server when connected to WiFi
 */
public class UserLocationSyncAdapter extends AbstractThreadedSyncAdapter {

    private final static String TAG = "UserLocationSyncAdapter";

    private ContentResolver contentResolver;

    /**
     * Set up the sync adapter
     */
    public UserLocationSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the constructor maintains compatibility with
     * Android 3.0 and later platform versions
     */
    UserLocationSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    /*
     * Specify the code you want to run in the sync adapter. The entire sync adapter runs
     * in a background thread, so you don't have to set up your own background processing.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //Send user location data
        Session session = new Session(getContext());
        String username = session.getusename();

        List<Location> locationList = UserLocationDao.getInstance().findByLogin(contentResolver, username);
        for (final Location location : locationList) {
            sendUserLocation(username, location);
        }
    }

    private void sendUserLocation(String login, final Location location) {
        JSONObject requestJSON = new JSONObject();
        try {
            requestJSON.put("login", login);

            JSONObject locationObject = new JSONObject();
            locationObject.put("date", DateUtil.convertToString(location.getDate()));
            locationObject.put("lat", location.getLat());
            locationObject.put("lon", location.getLon());
            requestJSON.put("location", locationObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, RequestManager.LOCATION_ENDPOINT, requestJSON,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String result = response.getString("result");
                        if (result != null && result.equals("success")) {
                            //Removes information sent from local database
                            UserLocationDao.getInstance().remove(contentResolver, location);
                            Log.d(TAG, String.format("Removing user location with id %s", location.getId()));
                        } else {
                            Log.d(TAG, String.format("Error while removing user location with id %s. Will try again later. ", location.getId()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Keep object to be sent later
                    Log.e(TAG, String.format("Error on response from saving user location with id %s", location.getId()));
                    Log.e(TAG, error.toString());
                }
            });

        RequestManager.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

}
