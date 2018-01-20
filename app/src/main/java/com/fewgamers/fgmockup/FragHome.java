package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Administrator on 12/6/2017.
 */

public class FragHome extends FragBase {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fraghome, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button notifyBt = (Button) getActivity().findViewById(R.id.notifyTestButton);
        notifyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                String channelID, channelName;
                channelID = "notify_001";
                channelName = "my_channel";

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(getActivity(), channelID)
                                .setSmallIcon(R.mipmap.cs_icon_564)
                                .setContentTitle("Test!")
                                .setContentText("Message hier");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(notificationChannel);
                }
                manager.notify(1, builder.build());
            }
        });
    }
}
