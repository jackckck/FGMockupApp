package com.fewgamers.fewgamers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Administrator on 1/20/2018.
 */

// class that handles FireBase messaging
public class FGFirebaseMessagingService extends FirebaseMessagingService {
    // override that handles incoming messages, which are to be displayed as notifications
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Remote message", "From: " + remoteMessage.getFrom());

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID, channelName;
        channelID = "notify_001";
        channelName = "my_channel";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        if (remoteMessage.getNotification() == null) {
            Log.e("Notification null", "The remote message did not include a notification");
        }
        try {
            builder.setSmallIcon(R.drawable.ic_stat_name);
        } catch (NullPointerException exception) {
            Log.e("Icon null", "No icon included in firebase notification");
        }
        try {
            builder.setContentText(remoteMessage.getNotification().getBody());
        } catch (NullPointerException exception) {
            Log.e("Body null", "No body included in firebase notification");
        }
        try {
            builder.setContentTitle(remoteMessage.getNotification().getTitle());
        } catch (NullPointerException exception) {
            Log.e("Title null", "No title included in firebase notification");
        }
        // android APK >= 26 requires a notifications channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(1, builder.build());
    }
}
