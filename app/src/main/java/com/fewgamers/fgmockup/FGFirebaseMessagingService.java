package com.fewgamers.fgmockup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Administrator on 1/20/2018.
 */

public class FGFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("Remote message", "From: " + remoteMessage.getFrom());

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelID, channelName;
        channelID = "notify_001";
        channelName = "my_channel";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
        if (remoteMessage.getNotification() == null) {
            Log.e("Geen notification", "De payload bevat geen notification");
        }
        try {
            builder.setSmallIcon(R.mipmap.cs_icon_564);
        } catch (NullPointerException exception) {
            Log.e("Icon missing", "No icon given in firebase notification");
        }
        try {
            builder.setContentText(remoteMessage.getNotification().getBody());
        } catch (NullPointerException exception) {
            Log.e("Body missing", "No body given in firebase notification");
        }
        try {
            builder.setContentTitle(remoteMessage.getNotification().getTitle());
        } catch (NullPointerException exception) {
            Log.e("Title missing", "No title given in firebase notification");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify(1, builder.build());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("Remote payload", "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("Remotee body", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
