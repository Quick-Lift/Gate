package com.quicklift;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerRides extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Cursor cursor;
    ListView list;
    DatabaseReference db;
    TextView name,phone;
    CircleImageView image;
    Button ongoing;
    private SharedPreferences log_id;
    ArrayList<Map<String,Object>> ride_list=new ArrayList<Map<String,Object>>();

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
        setContentView(R.layout.content_ride_screen);

        getSupportActionBar().setTitle("Rides");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("Rides");

        list=(ListView)findViewById(R.id.list);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Rides");
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
//        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
//        phone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.phone);
//        image = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.image);
        ongoing=(Button)findViewById(R.id.ongoingride);

        ongoing.setVisibility(View.GONE);

//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(CustomerRides.this,EditProfile.class));
//            }
//        });
//
//        updatenavbar();
        if (!log_id.getString("driver",null).equals("")){
            ongoing.setVisibility(View.VISIBLE);
        }

        ongoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor=log_id.edit();
                editor.putString("show","ride");
                editor.commit();
                startActivity(new Intent(CustomerRides.this,Home.class));
                finish();
            }
        });

        db.orderByChild("customerid").equalTo(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ride_list.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    ride_list.add((Map<String, Object>) data.getValue());
                    //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(CustomerRides.this, ride_list.get(ride_list.size()-1).get("time").toString(), Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(CustomerRides.this, String.valueOf(ride_list.size()), Toast.LENGTH_SHORT).show();
                list.setAdapter(new CustomAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(CustomerRides.this,EditProfile.class));
            finish();
        } else if (id == R.id.nav_drive_with_us) {
            startActivity(new Intent(CustomerRides.this,DriveWithUs.class));
            finish();
        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(CustomerRides.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
            intent = new Intent(CustomerRides.this, OffersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_payment) {
            startActivity(new Intent(CustomerRides.this,Payment.class));
            finish();
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(CustomerRides.this,Support.class));
            finish();
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                    "mailto","qiklift@gmail.com", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Message : ");
//            startActivity(Intent.createChooser(emailIntent, "Requesting Support !"));
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return ride_list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view=getLayoutInflater().inflate(R.layout.ride_info,null);

            final CircleImageView img=(CircleImageView)view.findViewById(R.id.image);
            final TextView veh=(TextView)view.findViewById(R.id.vehiclemodel);
            TextView time=(TextView)view.findViewById(R.id.timestamp);
            TextView source=(TextView)view.findViewById(R.id.source);
            TextView destination=(TextView)view.findViewById(R.id.destination);
            TextView amount=(TextView)view.findViewById(R.id.amount);
            final TextView name=(TextView)view.findViewById(R.id.name);

            time.setText(ride_list.get(position).get("time").toString());
            source.setText(ride_list.get(position).get("source").toString());
            destination.setText(ride_list.get(position).get("destination").toString());
            amount.setText(ride_list.get(position).get("amount").toString());

            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Drivers");
            dref.child(ride_list.get(position).get("driver").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
                    veh.setText(map.get("veh_type").toString()+" , "+map.get("veh_num").toString());
                    name.setText(map.get("name").toString());
                    if (!map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        img.setImageBitmap(decbyte);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return view;
        }
    }
}
