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
    ValueEventListener replistener;
    DatabaseReference db;
    int i=1;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.v("TAG","Service started"+i++);

        log = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
       // Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        db= FirebaseDatabase.getInstance().getReference("Response/"+log.getString("id",null));
        replistener=db.child("resp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String text = dataSnapshot.getValue(String.class);
                    //Log.v("TAG","started");
                    if (text.equals("Accept")) {
                        Log.v("TAG","accepted");

                        String str="Driver is on it's way ...\nOTP : "+log.getString("otp",null);
                        notification(text, str);
                    } else if (text.equals("Located")) {
                        Log.v("TAG","located");

                        String str="Driver arrived at the pick up location ...\nOTP : "+log.getString("otp",null);
                        notification(text, str);
                    } else if (text.equals("Trip Started")) {
                        Log.v("TAG","trip started");
                        String str="The trip has started ...";
                        notification(text, str);
                    } else if (text.equals("Trip Ended")) {
                        Log.v("TAG","trip ended");
                        handleoffer();
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
                        if (log.contains("offer")){
                            SharedPreferences.Editor ed=log.edit();
                            ed.remove("offer");
                            ed.commit();
                        }

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

    private void handleoffer() {
        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("ReferalCode/"+log.getString("id",null));
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("referredby")){
                    String str=dataSnapshot.child("referredby").getValue(String.class);
                    final DatabaseReference db=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+str);
                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild("101")) {
                                    Integer val=Integer.parseInt(dataSnapshot.child("101").getValue(String.class));
                                    db.child("101").setValue(String.valueOf(val+1));
                                    ref.child("referredby").removeValue();
                                }
                                else {
                                    db.child("101").setValue("1");
                                    ref.child("referredby").removeValue();
                                }
                            }
                            else {
                                db.child("101").setValue("1");
                                ref.child("referredby").removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (log.contains("offer")){
            final DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+log.getString("id",null));
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.hasChild(log.getString("offer",null))){

                            Integer val=Integer.parseInt(dataSnapshot.child(log.getString("offer",null)).getValue(String.class));
                            if (val==1)
                                dref.child(log.getString("offer",null)).removeValue();
                            else
                                dref.child(log.getString("offer",null)).setValue(String.valueOf(val-1));
                            SharedPreferences.Editor ed=log.edit();
                            ed.remove("offer");
                            ed.commit();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void removeservice(){
        Log.v("TAG","service ended");

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+log.getString("driver",null)+"/"+log.getString("id",null));
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Response/"+log.getString("id",null));
        dref.removeValue();
        ref.removeValue();
        db.removeEventListener(replistener);

        SharedPreferences.Editor editor;
        editor=log.edit();
        editor.putString("driver","");
        editor.putString("ride","");
        editor.remove("status");
        editor.commit();

        stopSelf();
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
                .setSmallIcon(R.drawable.niji)
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
