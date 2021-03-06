package com.quicklift;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripCompleted extends AppCompatActivity {
    private SharedPreferences log_id;
    DatabaseReference db;
    CircleImageView photo;
    TextView amount;
    RatingBar rating;
    Ride ride=new Ride();
    Button save,cancel;
    String driverid;
    SharedPreferences.Editor editor;
    Map<String,Object> datamap;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,Home.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_completed);

        getSupportActionBar().setTitle("Rate Trip");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.v("Checking","started");
        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        editor=log_id.edit();
        driverid=getIntent().getStringExtra("id");

        Log.v("Checking","started2");
        db= FirebaseDatabase.getInstance().getReference("Drivers/"+getIntent().getStringExtra("id"));
        photo=(CircleImageView)findViewById(R.id.photo);
        rating=(RatingBar)findViewById(R.id.rating);
        amount=(TextView)findViewById(R.id.amount);
        save=(Button) findViewById(R.id.save);
        cancel=(Button) findViewById(R.id.cancel);

//        amount.setText("Rs. "+log_id.getString("amount",null));

        editor.putString("driver","");
        editor.putString("ride","");
        editor.remove("status");
        editor.commit();

        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+getIntent().getStringExtra("id")+"/"+log_id.getString("id",null));
        final DatabaseReference db= FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
//        db.removeValue();
//        ref.removeValue();

        Log.v("Checking","started3");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    datamap=(Map<String, Object>) dataSnapshot.getValue();
                    float final_price=0;

                    amount.setText("\u20B9 "+datamap.get("price").toString());
//                    if (!datamap.get("cancel_charge").toString().equals("0")) {
//                        final_price+=Float.valueOf(datamap.get("cancel_charge").toString());
//                    }
//                    if (datamap.containsKey("offer") && !datamap.get("offer").toString().equals("0")) {
//                        final_price-=Float.valueOf(datamap.get("offer").toString());
//                    }
//                    if (datamap.containsKey("parking_price") && !datamap.get("parking_price").toString().equals("0")) {
//                        final_price+=Float.valueOf(datamap.get("parking_price").toString());
//                    }
//
//                    if (dataSnapshot.hasChild("located") && dataSnapshot.hasChild("started") && dataSnapshot.hasChild("waitcharge")) {
//                        try {
//                            Date date1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dataSnapshot.child("started").getValue().toString());
//                            Date date2 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dataSnapshot.child("located").getValue().toString());
//
//                            long diff = date1.getTime() - date2.getTime();
////                            int days = (int) (diff / (1000*60*60*24));
////                            int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
//                            int min = (int) (diff / (1000 * 60));
//                            if (dataSnapshot.hasChild("waittime") && (min > Integer.parseInt(dataSnapshot.child("waittime").getValue().toString()))) {
////                                tot+=(Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString())*(float)(min-Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())));
////                                        map.put("waiting", String.valueOf((int) (Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString()) * (float) (min - Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())))));
//                                final_price += (Float.parseFloat(dataSnapshot.child("waitcharge").getValue().toString()) * (float) (min - Integer.parseInt(dataSnapshot.child("waittime").getValue().toString())));
////                                        Log.v("PRICE", "" + final_price);
//                            }
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if (dataSnapshot.hasChild("ended") && dataSnapshot.hasChild("started") && dataSnapshot.hasChild("timecharge") && dataSnapshot.hasChild("triptime")) {
//                        try {
//                            Date date1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dataSnapshot.child("started").getValue().toString());
//                            Date date2 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse(dataSnapshot.child("ended").getValue().toString());
//
//                            long diff = date2.getTime() - date1.getTime();
////                            int days = (int) (diff / (1000*60*60*24));
////                            int hours = (int) ((diff - (1000*60*60*24*days)) / (1000*60*60));
//                            int min = (int) (diff / (1000 * 60));
//                            final_price += ((float) min) * Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString());
////                                    Log.v("PRICE", "" + final_price);
////                                    map.put("timing", (int) (Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString()) * (float) (min)));
////                            if ((min>Integer.parseInt(dataSnapshot.child("triptime").getValue().toString()))){
////                                tot+=(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min-Integer.parseInt(dataSnapshot.child("triptime").getValue().toString())));
////                                map.put("timing", (int)(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min)));
////                            }else {
////                                map.put("timing", (int)(Float.parseFloat(dataSnapshot.child("timecharge").getValue().toString())*(float)(min)));
////                            }
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                    }
////                    price=(int) (((float)tot)-Float.valueOf(datamap.get("cancel_charge").toString()));
////                    map.put("amount",String.valueOf(price));
////                    total.setText("Rs. "+(int)((float)tot));
//
//                    SQLQueries sqlQueries = new SQLQueries(TripCompleted.this);
//
//                    Cursor cursor = sqlQueries.retrievefare();
//                    Object[] dataTransfer = new Object[18];
//                    String url = getDirectionsUrltwoplaces(datamap.get("st_lat").toString(), datamap.get("st_lng").toString(), datamap.get("en_lat").toString(), datamap.get("en_lng").toString(), datamap.get("d_lat").toString(), datamap.get("d_lng").toString());
//                    TripCompletedPrice getDirectionsData = new TripCompletedPrice();
//                    dataTransfer[0] = url;
//                    dataTransfer[1] = amount;
//                    dataTransfer[2] = final_price;
//                    dataTransfer[3] = datamap.get("veh_type").toString();
//                    dataTransfer[4] = cursor;
//                    dataTransfer[5] = TripCompleted.this;
//                    dataTransfer[6] = datamap.get("cancel_charge").toString();
//                    getDirectionsData.execute(dataTransfer);

                    ref.removeValue();
                    db.removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null)+"/Info");
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
//                amount.setText(String.valueOf(map.get("price").toString()));
//                ride.setDestination(map.get("destination").toString());
//                ride.setSource(map.get("source").toString());
//                ride.setAmount(map.get("price").toString());
//                ride.setCustomerid(map.get("customer_id").toString());
//                ride.setDriver(log_id.getString("driver",null));
//                Date dt=new Date();
//                ride.setTime(dt.toString());
//
//                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Rides");
//                String key=ref.push().getKey();
//                ref.child(key).setValue(ride);
//
//                String date= new SimpleDateFormat("dd-MM-yyyy").format(new Date());
//                LastRide lr=new LastRide();
//                lr.setDate(date);
//                lr.setDestination(ride.getDestination());
//                lr.setDriver(ride.getDriver());
//                lr.setRideid(key);
//                lr.setStatus("");
//                lr.setLat(String.valueOf(map.get("en_lat").toString()));
//                lr.setLng(String.valueOf(map.get("en_lng").toString()));
//
//                ref=FirebaseDatabase.getInstance().getReference("LastRide");
//                ref.child(log_id.getString("id",null)).setValue(lr);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Drivers/"+driverid);
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.v("TAG",getIntent().getStringExtra("id"));
                if (dataSnapshot.exists()) {
//                    Log.v("TAG","hello");
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.containsKey("thumb") && !map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        photo.setImageBitmap(decbyte);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        stopService(new Intent(this, NotificationService.class));
//        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//            @Override
//            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
//                tripstatus.removeValue();
//
//                editor.putString("driver","");
//                editor.putString("ride","");
//                editor.remove("status");
//                editor.commit();
//                Toast.makeText(TripCompleted.this, "Thankyou for feedback !", Toast.LENGTH_SHORT).show();
//            }
//        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("DriversRating/"+driverid);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            Integer rides=Integer.parseInt(map.get("no").toString());
                            Float rate=Float.parseFloat(map.get("rate").toString());

                            rate=(rides*rate)+rating.getRating();
                            rate=rate/(rides+1);

                            reference.child("no").setValue(String.valueOf(rides+1));
                            reference.child("rate").setValue(String.valueOf(rate));

                            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Drivers/"+driverid);
                            dref.child("rate").setValue(String.valueOf(rate));
                        }
                        else {
                            reference.child("no").setValue(String.valueOf(1));
                            reference.child("rate").setValue(String.valueOf(rating.getRating()));
                            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Drivers/"+driverid);
                            dref.child("rate").setValue(String.valueOf(rating.getRating()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("LastRide");
                ref.child(log_id.getString("id",null)+"/status").setValue("rated");

                startActivity(new Intent(TripCompleted.this,Home.class));
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TripCompleted.this,Home.class));
                finish();
            }
        });
    }

//    private String getDirectionsUrltwoplaces(String st_lt,String st_ln,String en_lt,String en_ln,String d_lt,String d_ln) {
//        StringBuilder googleDirectionsUrl=null;
//            googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
//            googleDirectionsUrl.append("origin=" + st_lt + "," + st_ln);
//            googleDirectionsUrl.append("&destination=" + d_lt + "," + d_ln);
//            googleDirectionsUrl.append("&waypoints=optimize:false");
//            googleDirectionsUrl.append("|" + en_lt + "," + en_ln);
//            googleDirectionsUrl.append("&key=" + "AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
//
//        Log.v("Direction",googleDirectionsUrl.toString());
//        return googleDirectionsUrl.toString();
//    }
}
