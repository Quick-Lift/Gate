package com.quicklift;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String channelId = "channel-01";
    String channelName = "Channel Name";
    int importance = NotificationManager.IMPORTANCE_HIGH;
    private NotificationManager notificationManager;
    private String ADMIN_CHANNEL_ID="101";
    Bitmap icon = null;

    @Override
    public void onCreate() {
        super.onCreate();
        icon=BitmapFactory.decodeResource(getResources(),R.drawable.logo);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v("Testing","message received");
        if (remoteMessage.getData() != null) {
            Log.v("Testing", "Message Notification Body: " + remoteMessage.getData().get("body"));
            Log.v("Testing","Notification: "+remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        SharedPreferences log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = log_id.edit();

        editor.putString("firebasetoken",s);
        editor.commit();

        Log.v("Testing",""+s);
    }

    public void sendNotification(RemoteMessage remoteMessage) {
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        int notificationId = new Random().nextInt(60000);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setLargeIcon(icon)
                .setSmallIcon(R.drawable.notification_icon) //a resource for your custom small icon
                .setContentTitle(remoteMessage.getNotification().getTitle()) //the "title" value you sent in your notification
                .setContentText(remoteMessage.getNotification().getBody()) //ditto
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(remoteMessage.getNotification().getBody()))
                .setAutoCancel(true)  //dismisses the notification on click
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = "AdminName";
        String adminChannelDescription = "Desc";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
