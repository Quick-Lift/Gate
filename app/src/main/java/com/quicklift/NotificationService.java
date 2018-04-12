package com.quicklift;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {
    SharedPreferences log;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
       // Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Response/"+log.getString("id",null));
        db.child("resp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String text = dataSnapshot.getValue(String.class);
                    //Log.v("TAG","started");
                    if (text.equals("Accept")) {
                        String str="Driver is on it's way ...\nOTP : "+log.getString("otp",null);
                        notification(text, str);
                    } else if (text.equals("Located")) {
                        String str="Driver arrived at the pick up location ...\nOTP : "+log.getString("otp",null);
                        notification(text, str);
                    } else if (text.equals("Trip Started")) {
                        String str="The trip has started ...";
                        notification(text, str);
                    } else if (text.equals("Trip Ended")) {
                        String str="You have arrived at your destination ...";
                        notification(text, str);
                        Handler handle = new Handler();
                        handle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                removeservice();
                            }
                        }, 2000);
                    } else if (text.equals("Cancel")) {
                        String str="The trip is cancelled by driver ...";
                        notification(text, str);
                        removeservice();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_STICKY;
    }

    public void removeservice(){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log.getString("driver",null)+"/"+log.getString("id",null));
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Response/"+log.getString("id",null));
        db.removeValue();
        ref.removeValue();

        SharedPreferences.Editor editor;
        editor=log.edit();
        editor.putString("driver","");
        editor.putString("ride","");
        editor.remove("status");
        editor.commit();

        onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void notification (String text,String message){
        String title="";

        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        Notification noti = new NotificationCompat.Builder(this,channelId)
                .setContentTitle("Driver")
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo))
                .setSmallIcon(R.drawable.carfinal)
                .setAutoCancel(false)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound))
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        mNotificationManager.notify(001, noti);
    }
}
