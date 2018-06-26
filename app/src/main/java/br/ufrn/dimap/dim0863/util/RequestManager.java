package br.ufrn.dimap.dim0863.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by lucascriistiano on 02/05/2018.
 */
public class RequestManager {

    private static RequestManager instance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    public static final String WEBSERVICE_ENDPOINT = "http://192.168.1.100:8080";
    public static final String API_ENDPOINT = WEBSERVICE_ENDPOINT + "/api/v1";
    public static final String CHAVEIRO_ENDPOINT = RequestManager.API_ENDPOINT + "/keychain";
    public static final String LOCATION_ENDPOINT = RequestManager.API_ENDPOINT + "/location";
    public static final String CAR_DATA_ENDPOINT = RequestManager.API_ENDPOINT + "/car/data";
    public static final String UPDATE_TOKEN_ENDPOINT = RequestManager.API_ENDPOINT + "/firebase/update-token";

    private RequestManager(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized RequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new RequestManager(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
