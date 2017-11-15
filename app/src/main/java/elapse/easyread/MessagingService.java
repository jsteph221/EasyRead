package elapse.easyread;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import static android.content.ContentValues.TAG;

public class MessagingService extends FirebaseMessagingService {
    public MessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map resp = remoteMessage.getData();
            String loggedUser = EasyReadSingleton.getInstance(getApplicationContext()).getUserId();
            if (loggedUser != null && loggedUser == (String) resp.get("logged")){
                sendMessageBroadcast((String) resp.get("from"),(String) resp.get("data"));
                sendNotification(resp);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    private void sendNotification(Map msg){
        int notifyID = 1;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_add_exchange)
                .setContentTitle("New Message from : " + msg.get("user"))
                .setContentText((String) msg.get("msg"));
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifyID, mBuilder.build());
    }

    private final void sendMessageBroadcast(final String fromUser,final String data){
        final Intent i = new Intent();
        i.setAction("com.easyread.android.MESSAGE_RECIEVED");
        i.putExtra("from_user",fromUser);
        i.putExtra("data",data);
        this.sendBroadcast(i);
    }
}
