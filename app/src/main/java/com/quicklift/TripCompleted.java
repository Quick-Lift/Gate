package com.quicklift;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
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
    SharedPreferences.Editor editor;

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

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        editor=log_id.edit();
        db= FirebaseDatabase.getInstance().getReference("Drivers/"+getIntent().getStringExtra("id"));
        photo=(CircleImageView)findViewById(R.id.photo);
        rating=(RatingBar)findViewById(R.id.rating);
        amount=(TextView)findViewById(R.id.amount);
        save=(Button) findViewById(R.id.save);
        cancel=(Button) findViewById(R.id.cancel);

        amount.setText(log_id.getString("amount",null));

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

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                if (!map.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    photo.setImageBitmap(decbyte);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        stopService(new Intent(this, NotificationService.class));
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
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("LastRide");
                ref.child(log_id.getString("id",null)+"/status").setValue("rated");

                editor.putString("driver","");
                editor.putString("ride","");
                editor.remove("status");
                editor.commit();
                startActivity(new Intent(TripCompleted.this,Home.class));
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("driver","");
                editor.putString("ride","");
                editor.remove("status");
                editor.commit();
                startActivity(new Intent(TripCompleted.this,Home.class));
                finish();
            }
        });
    }
}
