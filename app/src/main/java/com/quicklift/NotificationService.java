package com.quicklift;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.IntDef;
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
                        notification(text, "Driver is on it's way ...");
                    } else if (text.equals("Located")) {
                        notification(text, "Driver arrived at the pick up location ...");
                    } else if (text.equals("Trip Started")) {
                        notification(text, "The trip has started ...");
                    } else if (text.equals("Trip Ended")) {
                        notification(text, "You have arrived at your destination ...");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_STICKY;
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
//        if (text.equals("Accept")){
//            title="Driver Arriving !";
//        }
//        else if (text.equals("Located")){
//            title="Driver Arrived !";
//        }
//        else if (text.equals("Trip Started")){
//            title="Trip Started !";
//        }
//        else if (text.equals("Trip Ended")){
//            title="Reached Destination !";
//        }

        Notification noti = new Notification.Builder(this)
                .setContentTitle("Driver")
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.logo))
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sound))
                .build();

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //NotificationManager.notify().

        mNotificationManager.notify(001, noti);
    }
}
