package com.quicklift;

import android.app.ProgressDialog;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OffersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ListView list;
    private SharedPreferences log_id;
    private DatabaseReference db;
    ArrayList<String> offers=new ArrayList<>();
    TextView code,offer;
    String refcode="";
    ProgressDialog dialog;

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
        setContentView(R.layout.content_offer_screen);

        getSupportActionBar().setTitle("Offers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("ReferalCode/"+log_id.getString("id",null));

        dialog=new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading ... Please Wait !!!");
        dialog.show();

        code=(TextView)findViewById(R.id.code);
        offer=(TextView)findViewById(R.id.offer);
        list=(ListView)findViewById(R.id.list);

        db.child("code").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    code.setText("Referal Code : "+dataSnapshot.getValue(String.class));
                    refcode=dataSnapshot.getValue(String.class);

//                    Use Promocode: Love20\nget Rs. 20 off instantly
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Invite Friends");
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

//        db.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                offers.clear();
//                for (DataSnapshot data:dataSnapshot.getChildren()){
//                    offers.add(data.getValue(String.class));
//                }
//                list.setAdapter(new CustomAdapter());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_rides) {
            intent = new Intent(OffersActivity.this, CustomerRides.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(OffersActivity.this,EditProfile.class));
            finish();
        } else if (id == R.id.nav_drive_with_us) {

        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(OffersActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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

        } else if (id == R.id.nav_payment) {

        } else if (id == R.id.nav_support) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","qiklift@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Message : ");
            startActivity(Intent.createChooser(emailIntent, "Requesting Support !"));
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

//    class CustomAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return offers.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return 0;
//        }
//
//        @Override
//        public View getView(int position, View view, ViewGroup parent) {
//            view=getLayoutInflater().inflate(R.layout.offerlayout,null);
//            TextView title=(TextView)view.findViewById(R.id.title);
//            final TextView detail=(TextView)view.findViewById(R.id.details);
//
//            title.setText(offers.get(offers.size()-position-1));
//            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Offers/"+offers.get(offers.size()-position-1));
//            ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    detail.setText(dataSnapshot.getValue(String.class));
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//            return view;
//        }
//    }

    public void share(View view){
        if (!refcode.equals("")) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Download Quicklift app and win 100% off upto Rs. 50 on your first ride.\n " +
                            "Playstore Link Here !!!!!" +
                            "\nUse Referal Code : "+refcode);
            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        }
    }
}
