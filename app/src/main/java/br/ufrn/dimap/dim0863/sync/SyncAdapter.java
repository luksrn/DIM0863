package br.ufrn.dimap.dim0863.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import br.ufrn.dimap.dim0863.dao.CarInfoDao;
import br.ufrn.dimap.dim0863.domain.CarInfo;
import br.ufrn.dimap.dim0863.util.RequestManager;

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    //Get data stored locally and send to FIWARE when connected to WiFi

    private final static String TAG = "SyncAdapter";

    // Define a variable to contain a content resolver instance
    private ContentResolver contentResolver;

    /**
     * Set up the sync adapter
     */
    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        //If your app uses a content resolver, get an instance of it from the incoming Context
        contentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the constructor maintains compatibility with
     * Android 3.0 and later platform versions
     */
    SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
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
        List<CarInfo> carInfoList = CarInfoDao.getInstance().findAll(contentResolver);

        for (final CarInfo carInfo : carInfoList) {
            JSONObject requestJSON = new JSONObject();
            try {
                requestJSON.put("date", new Date());
                requestJSON.put("license_plate", carInfo.getLicensePlate());
                requestJSON.put("speed", carInfo.getSpeed());
                requestJSON.put("rpm", carInfo.getRpm());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, RequestManager.CAR_DATA_ENDPOINT, requestJSON, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    //Removes information sent
                    CarInfoDao.getInstance().remove(contentResolver, carInfo);
                    Log.d(TAG, "Removing car info with id " + carInfo.getId());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //Keep object to be sent later
                    Log.d(TAG, "Error while removing car info with id " + carInfo.getId());
                }
            });

            RequestManager.getInstance(getContext()).addToRequestQueue(request);
        }
    }

}
