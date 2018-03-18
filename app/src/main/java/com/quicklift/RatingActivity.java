package com.quicklift;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RatingActivity extends AppCompatActivity {
    Cursor cursor;
    SQLQueries sqlQueries;
    private DatabaseReference lastride;
    private SharedPreferences log_id;
    RatingBar ratingBar;
    Map<String,Object> map;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        getSupportActionBar().setTitle("Rate Driver");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        lastride=FirebaseDatabase.getInstance().getReference("LastRide/"+log_id.getString("id",null));
        ratingBar=(RatingBar)findViewById(R.id.rating);
        ratingBar.setRating(getIntent().getFloatExtra("rating",0));

        lastride.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                map=(Map<String, Object>) dataSnapshot.getValue();
                if (map.get("status").toString().equals("")){
                    displayrideinfo(map);
                }
                else if (map.get("status").toString().equals("rated")){
                    displayrideinfo(map);
                }
                else {
                    findViewById(R.id.rating_bar).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayrideinfo(Map<String,Object> map) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers/" + map.get("driver").toString());

        ((TextView) findViewById(R.id.timestamp)).setText(map.get("date").toString());
        ((TextView) findViewById(R.id.location)).setText(map.get("destination").toString());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    ((TextView) findViewById(R.id.driver_name)).setText(map.get("name").toString());
                    if (!map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        ((CircleImageView) findViewById(R.id.driver_pic)).setImageBitmap(decbyte);
                    }
                    findViewById(R.id.rating_bar).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void submit(View view){
        DatabaseReference db=FirebaseDatabase.getInstance().getReference("CustomerFeedback/"+map.get("rideid").toString());
        CheckBox box1=(CheckBox)findViewById(R.id.box1);
        CheckBox box2=(CheckBox)findViewById(R.id.box2);
        CheckBox box3=(CheckBox)findViewById(R.id.box3);
        CheckBox box4=(CheckBox)findViewById(R.id.box4);
        CheckBox box5=(CheckBox)findViewById(R.id.box5);
        CheckBox box6=(CheckBox)findViewById(R.id.box6);

        if (box1.isChecked()){
            db.push().setValue(box1.getText().toString());
        }
        if (box2.isChecked()){
            db.push().setValue(box2.getText().toString());
        }
        if (box3.isChecked()){
            db.push().setValue(box3.getText().toString());
        }
        if (box4.isChecked()){
            db.push().setValue(box4.getText().toString());
        }
        if (box5.isChecked()){
            db.push().setValue(box5.getText().toString());
        }
        if (box6.isChecked()){
            db.push().setValue(box6.getText().toString());
        }

        lastride.child("status").setValue("rated");
        Toast.makeText(this, "Thank you for your feedback !", Toast.LENGTH_SHORT).show();
        finish();
    }
}
