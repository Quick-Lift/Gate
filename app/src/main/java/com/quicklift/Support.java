package com.quicklift;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Support extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView name,phone;
    CircleImageView image;
    private SharedPreferences log_id;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_support);

        getSupportActionBar().setTitle("Support");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Support");
//
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
//        phone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.phone);
//        image = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.image);
//
//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Support.this,EditProfile.class));
//            }
//        });
//
//        updatenavbar();
    }

    private void updatenavbar() {
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null));
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                name.setText(map.get("name").toString());
                phone.setText(map.get("phone").toString());
                if (!map.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    image.setImageBitmap(decbyte);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_rides) {
            intent = new Intent(Support.this, CustomerRides.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(Support.this,EditProfile.class));
            finish();
        } else if (id == R.id.nav_drive_with_us) {
            startActivity(new Intent(Support.this,DriveWithUs.class));
            finish();
        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(Support.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            startActivity(callIntent);
        } else if (id == R.id.nav_offers) {
            startActivity(new Intent(Support.this,OffersActivity.class));
            finish();
        } else if (id == R.id.nav_payment) {
            startActivity(new Intent(Support.this,Payment.class));
            finish();
        } else if (id == R.id.nav_support) {
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                    "mailto","qiklift@gmail.com", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Message : ");
//            startActivity(Intent.createChooser(emailIntent, "Requesting Support !"));
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public void about (View view){
        startActivity(new Intent(this,AboutUs.class));
    }

    public void rideoptions (View view){
        startActivity(new Intent(this,RideOptions.class));
    }

    public void faresandcharges (View view){
        startActivity(new Intent(this,FaresandCharges.class));
    }

    public void faq (View view){
        startActivity(new Intent(this,FAQ.class));
    }
}
