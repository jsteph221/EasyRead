package elapse.easyread;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Joshua on 2017-10-26.
 */

public class FirebaseIdService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIdService";

    public static String getToken(){
        return FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Token: " + refreshedToken);
    }
}
