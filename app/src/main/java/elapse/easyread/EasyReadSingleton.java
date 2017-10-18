package elapse.easyread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.places.Place;

/**
 * Created by Joshua on 2017-10-07.
 */

public class EasyReadSingleton {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    //Variables for preferences
    private String userId = "admin";
    private String searchMaxDistance = "25";
    private Place searchLocation;
    private static EasyReadSingleton INSTANCE ;

    private EasyReadSingleton(Context context) {
        mCtx = context;
        mRequestQueue = Volley.newRequestQueue(context);
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

    public static synchronized EasyReadSingleton getInstance(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = new EasyReadSingleton(ctx);
        }
        return INSTANCE;

    }

    public static synchronized EasyReadSingleton getInstance() {
        return INSTANCE;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
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

    public String getUserId(){return userId;};

    public void setUserId(String id){userId=id;}

    public String getSearchMaxDistance(){return searchMaxDistance;}
    public void setSearchMaxDistance(String maxDistance){this.searchMaxDistance =maxDistance;}

    public Place getSearchLocation(){return searchLocation;}
    public void setSearchLocation(Place place){this.searchLocation = place;}

}
