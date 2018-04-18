package com.quicklift;

import android.*;
import android.Manifest;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener, NavigationView.OnNavigationItemSelectedListener {
    private GoogleMap mMap;
    GoogleApiClient gpc;
    Marker marker_pick, marker_drop, marker, driver_drop, driver;
    EditText pickup_address, destn_address;
    TextView price_bike, price_car, price_auto, price_rickshaw, price_shareAuto, price_shareCar, price_shareRickshaw;
    TextView time;
    Float estimated_time;
    GeoQuery find_driver_bike,find_driver_car,find_driver_rickshaw,find_driver_auto,find_driver,find_driver_share;
    ArrayList<String> payment_mode = new ArrayList<>();
    ArrayList<String> seats_list = new ArrayList<>();
    ArrayList<String> offer_list = new ArrayList<>();
    Spinner payment;
    Button confirm;
    Data data = new Data();
    Polyline line;
    GeoLocation loc;
    LatLng pickup, cur_loc;
    GeoLocation driver_loc;
    ProgressDialog pdialog;
    int i = 0, found = 0;
    ArrayList<String> driver_list = new ArrayList<String>();
    ArrayList<String> driver_list_share = new ArrayList<String>();
    ArrayList<String> driver_list_bike = new ArrayList<String>();
    ArrayList<String> driver_list_car = new ArrayList<String>();
    ArrayList<String> driver_list_rickshaw = new ArrayList<String>();
    ArrayList<String> driver_list_auto = new ArrayList<String>();
    ArrayList<String> share_driver_list = new ArrayList<String>();
    SharedPreferences log_id;
    Integer no_of_drivers = 0;
    HorizontalScrollView hsv;
    private ImageView final_image;
    private TextView final_price;
    SQLQueries sqlQueries;
    int radius = 5;
    boolean driverfound = false;
    String driverid,arrival_time;
    private TextView name,otp;
    private TextView phone;
    private CircleImageView image;
    Integer screen_status=0;
    ValueEventListener resplistener;
    DatabaseReference lastride;
    SharedPreferences.Editor editor;
    TextView time_bike,time_car,time_auto,time_rickshaw,time_shareAuto,time_shareCar,time_shareRickshaw,final_time;
    boolean doubleBackToExitPressedOnce = false;
    DrawerLayout drawer;
    Spinner seats;
    TextView offer;
    List<Marker> driver_markers = new ArrayList<>();
    List<Marker> driver_markers_share = new ArrayList<>();
    List<Marker> driver_markers_bike = new ArrayList<>();
    List<Marker> driver_markers_car = new ArrayList<>();
    List<Marker> driver_markers_rickshaw = new ArrayList<>();
    List<Marker> driver_markers_auto = new ArrayList<>();
    private LatLng pick_loc;
    int show=0;
    String ridetype=null,vehicletype="car";
    TextView findingridemsg,ridedetails;
    Dialog dialog;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (this.drawer.isDrawerOpen(GravityCompat.START)) {
            this.drawer.closeDrawer(GravityCompat.START);
        }
        //Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
        else {
            if (screen_status == 1) {
                screen_status = 0;

                destn_address.setVisibility(View.VISIBLE);
                pickup_address.setVisibility(View.VISIBLE);

                showoption=0;
                findViewById(R.id.ridedetails).setVisibility(View.GONE);
                findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.layout3).setVisibility(View.GONE);
                findViewById(R.id.layout4).setVisibility(View.GONE);
                findViewById(R.id.rating_bar).setVisibility(View.GONE);

//                place_drivers_bike();
//                place_drivers_auto();
//                place_drivers_car();
//                place_drivers_rickshaw();
            }
//        else {
//            finish();
//        }
            else {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        editor=log_id.edit();
        sqlQueries = new SQLQueries(this);

        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Searching for driver...");
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(true);

        if (googleServicesAvailable()) {
            //Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_home_screen);
            initMap();
        } else {
            Toast.makeText(this, "Unable to load map ! Please turn on location !", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap() {
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);

        pickup_address = (EditText) findViewById(R.id.pickup);
        destn_address = (EditText) findViewById(R.id.destination);
        confirm = (Button) findViewById(R.id.confirm);
        payment = (Spinner) findViewById(R.id.pay_mode);
        seats = (Spinner) findViewById(R.id.seats);
        offer = (TextView) findViewById(R.id.offer);
        hsv = (HorizontalScrollView) findViewById(R.id.fragmentTwo);
        price_auto = (TextView) findViewById(R.id.price_auto);
        price_bike = (TextView) findViewById(R.id.price_bike);
        price_car = (TextView) findViewById(R.id.price_car);
        price_rickshaw = (TextView) findViewById(R.id.price_rickshaw);
        price_shareAuto = (TextView) findViewById(R.id.price_shareAuto);
        price_shareCar = (TextView) findViewById(R.id.price_shareCar);
        price_shareRickshaw = (TextView) findViewById(R.id.price_shareRickshaw);
        time_auto = (TextView) findViewById(R.id.time_auto);
        time_bike = (TextView) findViewById(R.id.time_bike);
        time_car = (TextView) findViewById(R.id.time_car);
        time_rickshaw = (TextView) findViewById(R.id.time_rickshaw);
        time_shareAuto = (TextView) findViewById(R.id.time_shareAuto);
        time_shareCar = (TextView) findViewById(R.id.time_shareCar);
        time_shareRickshaw = (TextView) findViewById(R.id.time_shareRickshaw);
        final_price = (TextView) findViewById(R.id.price);
        final_time = (TextView) findViewById(R.id.time);
        otp = (TextView) findViewById(R.id.otp);
        ridedetails = (TextView) findViewById(R.id.ridedetails);
        final_image = (ImageView) findViewById(R.id.final_image);

        payment_mode.add("Cash");
        payment_mode.add("QuickLift Money");
        payment_mode.add("Bhim Upi");
        payment_mode.add("Paytm");

//        seats_list.add("full");
//        seats_list.add("1");
//        seats_list.add("2");
//        seats_list.add("3");
//        seats_list.add("4");
//
//        ArrayAdapter<String> adapter1 =
//                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
//        adapter1.setDropDownViewResource(R.layout.payment_list);
//        seats.setAdapter(adapter1);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, payment_mode);
        adapter.setDropDownViewResource(R.layout.payment_list);
        payment.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);
        toolbar.setBackgroundColor(0x00ffffff);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        pickup_address.setInputType(0);
        destn_address.setInputType(0);

        pickup_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Home.this, PlaceSelector.class), 1);
            }
        });

        destn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Home.this, PlaceSelector.class), 2);
            }
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        phone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.phone);
        image = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.image);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Home.this,EditProfile.class));
            }
        });
        updatenavbar();

//        if (!log_id.getString("driver", null).equals("")) {
//            tracktripstatus();
//        }
//        else {
           // Toast.makeText(this, log_id.getString("driver", null), Toast.LENGTH_SHORT).show();
            lastride=FirebaseDatabase.getInstance().getReference("LastRide/"+log_id.getString("id",null));
            lastride.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if (map.get("status").toString().equals("")) {
                            displayrideinfo(map);
                        } else if (map.get("status").toString().equals("rated")) {
                            //displayrideinfo(map);
                            locationinfo(map);
                            findViewById(R.id.ride_card_view).setVisibility(View.GONE);
                            //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        findViewById(R.id.rating_bar).setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        if (!log_id.getString("driver",null).equals("")){
            cust_req=FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null));
            cust_req.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){
                        editor.putString("driver","");
                        editor.remove("status");
                        editor.commit();
                    }
                    else if (dataSnapshot.exists() && log_id.contains("show")){
                        show=1;
                        otp.setText("OTP\n"+dataSnapshot.child("otp").getValue(String.class));
                        destn_address.setText(dataSnapshot.child("destination").getValue(String.class));
                        pickup_address.setText(dataSnapshot.child("source").getValue(String.class));
                        pick_loc=new LatLng(dataSnapshot.child("st_lat").getValue(Double.class),dataSnapshot.child("st_lng").getValue(Double.class));
                        editor.remove("show");
                        editor.commit();
                        check_status();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    //    }
        //sqlQueries.lastride("HQMFVDHAu8WwjLqLW0qWLHKbm183","2017-02-20","Huskur Gate");
//        Cursor cursor = sqlQueries.retrievelastride();
//        if (cursor != null && cursor.getCount() > 0) {
//            // Toast.makeText(this, String.valueOf(cursor.getCount()), Toast.LENGTH_SHORT).show();
//            displayrideinfo(cursor);
//
//        } else {
//            displayoffers();
//        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 0;
//                driver_list.clear();
//                if (vehicletype.equals("bike")){
//                    for (int i=0;i<driver_list_bike.size();i++){
//                        driver_list.add(driver_list_bike.get(i));
//                    }
//                } else if (vehicletype.equals("car")){
//                    for (int i=0;i<driver_list_car.size();i++){
//                        driver_list.add(driver_list_car.get(i));
//                    }
//                } else if (vehicletype.equals("auto")){
//                    for (int i=0;i<driver_list_auto.size();i++){
//                        driver_list.add(driver_list_auto.get(i));
//                    }
//                } else if (vehicletype.equals("rickshaw")){
//                    for (int i=0;i<driver_list_rickshaw.size();i++){
//                        driver_list.add(driver_list_rickshaw.get(i));
//                    }
//                }
                dialogdisplay();
                find_driver.removeAllListeners();
//                find_driver_share.removeAllListeners();
//                find_driver_bike.removeAllListeners();
//                find_driver_car.removeAllListeners();
//                find_driver_auto.removeAllListeners();
//                find_driver_rickshaw.removeAllListeners();
                findViewById(R.id.layout4).setVisibility(View.GONE);

                data.setCustomer_id(log_id.getString("id", null));
                //find_driver.removeAllListeners();
                //findViewById(R.id.layout3).setVisibility(View.VISIBLE);
               // Toast.makeText(Home.this, "2", Toast.LENGTH_SHORT).show();
                screen_status=0;
                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                if (ridetype.equals("full"))
                    finddriver();
                else
                    findsharedriver();
            }
        });

        offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Home.this, SelectOffers.class), 3);
            }
        });
    }

    public void dialogdisplay(){
        View view=getLayoutInflater().inflate(R.layout.waitingdialog,null);
        final ImageView img1=(ImageView)view.findViewById(R.id.image1);
//        final ImageView img2=(ImageView)view.findViewById(R.id.image2);
        findingridemsg=(TextView)view.findViewById(R.id.text);

        if (vehicletype.equals("car"))
            img1.setImageResource(R.drawable.carfinal);
        else if (vehicletype.equals("rickshaw"))
            img1.setImageResource(R.drawable.rickshawfinal);
//        img2.setVisibility(View.GONE);
        img1.setScaleX(-1);
        final Animation left_to_right = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.welcome_animation);
        final Animation right_to_left = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.welcome_animation_right_to_left);
//        img2.setAnimation(right_to_left);
        img1.startAnimation(left_to_right);
//        img2.startAnimation(right_to_left);

        left_to_right.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
//                img1.setVisibility(View.GONE);
                img1.setScaleX(1);
                img1.startAnimation(right_to_left);
//                img2.startAnimation(left_to_right);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
//                img2.setAnimation(right_to_left);
            }
        });

        right_to_left.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                img2.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img1.setScaleX(-1);
                img1.startAnimation(left_to_right);
//                img2.setVisibility(View.GONE);
//                img1.setVisibility(View.VISIBLE);
//                img1.setAnimation(left_to_right);
//                img1.startAnimation(right_to_left);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog=new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void findsharedriver() {
        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        data.setOtp(id);
        otp.setText("OTP\n"+id);
        editor.putString("otp",id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(4));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        data.setSeat(seats.getSelectedItem().toString());

        final DatabaseReference share=FirebaseDatabase.getInstance().getReference("Share");
        ShareClass shareClass=new ShareClass();
        shareClass.setSt_lat(marker_pick.getPosition().latitude);
        shareClass.setSt_lng(marker_pick.getPosition().longitude);
        shareClass.setEn_lat(marker_drop.getPosition().latitude);
        shareClass.setEn_lng(marker_drop.getPosition().longitude);
        shareClass.setSeats(seats.getSelectedItem().toString());
        share.child(log_id.getString("id",null)).setValue(shareClass);

//        if (found == 1)
//            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
//        else if (found == 0 && i < driver_list.size())
//            getClosestDriver();
//        else {
//            screen_status=1;
//            findViewById(R.id.layout4).setVisibility(View.VISIBLE);
//            findViewById(R.id.layout3).setVisibility(View.GONE);
//            Toast.makeText(this, "No Ride Found", Toast.LENGTH_SHORT).show();
//            findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
//            findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
////             if (vehicletype.equals("bike"))
////                place_drivers_bike();
////             else if (vehicletype.equals("car"))
////                 place_drivers_car();
////             else if (vehicletype.equals("auto"))
////                 place_drivers_auto();
////             else if (vehicletype.equals("rickshaw"))
////                 place_drivers_rickshaw();
//
//            place_drivers();
//            place_drivers_share();
//        }

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (found == 0 && ++i <= driver_list.size()) {
//                    cust_req.removeValue();
//                    resp.removeValue();
//                    resp.removeEventListener(resplistener);
//                    finddriver();
//                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
//                }
                share.child(log_id.getString("id",null)+"/drivers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        share_driver_list.clear();
                        for (DataSnapshot data:dataSnapshot.getChildren()){
                            share_driver_list.add(data.getKey());
                        }
                        sharedriver=0;
                        share.child(log_id.getString("id",null)).removeValue();
                        sendsharerequest();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }, 20000);
    }

    int sharedriver=0;
    public void sendsharerequest(){
//        for (int i=0;i<driver_list_share.size();i++){
//            for (int j=0;j<share_driver_list.size();j++){
//                if (driver_list_share.get(i).equals(share_driver_list.get(j))){
//
//                }
//            }
//        }
            screen_status=1;
        if (found == 1) {
            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }
        else if (found==0 && sharedriver<share_driver_list.size()) {
//            Toast.makeText(Home.this, "hello "+String.valueOf(sharedriver)+" "+share_driver_list.get(sharedriver), Toast.LENGTH_SHORT).show();
            DatabaseReference dref=null;
            if (vehicletype.equals("car"))
                dref = FirebaseDatabase.getInstance().getReference("DriversWorking/Car/" + share_driver_list.get(sharedriver)+"/seat");
            else if (vehicletype.equals("rickshaw"))
                dref = FirebaseDatabase.getInstance().getReference("DriversWorking/Rickshaw/" + share_driver_list.get(sharedriver)+"/seat");

            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(Home.this, "hello", Toast.LENGTH_SHORT).show();
                        String seat = dataSnapshot.getValue(String.class);
                        if (!seat.equals("full") && (Integer.parseInt(seat) + Integer.parseInt(seats.getSelectedItem().toString())) <= 4) {
                            cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + share_driver_list.get(sharedriver));
                            cust_req.child(log_id.getString("id", null)).setValue(data);
                            found=1;
                            driverid=share_driver_list.get(sharedriver);
                            response();
                        } else {
                            sharedriver++;
                            sendsharerequest();
                        }
                    }
                    else {
                        sharedriver++;
                        sendsharerequest();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if (found!=1 && sharedriver==share_driver_list.size()){
            DatabaseReference share=FirebaseDatabase.getInstance().getReference("Share");
            share.child(log_id.getString("id",null)).removeValue();
            finddriver();
        }
    }

    private void locationinfo(final Map<String, Object> map) {
        ((TextView)findViewById(R.id.ride_location)).setText(map.get("destination").toString());
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference save=FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null)+"/saved");
                if (TextUtils.isEmpty(((EditText)findViewById(R.id.locname)).getText().toString())){
                    Toast.makeText(Home.this, "Please provide location name ...", Toast.LENGTH_SHORT).show();
                }
                else {
                    String key=save.push().getKey();
                    save.child(key+"/name").setValue(((EditText)findViewById(R.id.locname)).getText().toString().toLowerCase());
                    save.child(key+"/locname").setValue(map.get("destination").toString());
                    save.child(key+"/lat").setValue(map.get("lat").toString());
                    save.child(key+"/lng").setValue(map.get("lng").toString());

                    Toast.makeText(Home.this, "Location Saved !", Toast.LENGTH_SHORT).show();
                }
                lastride.removeValue();
            }
        });
        findViewById(R.id.location_card_view).setVisibility(View.VISIBLE);
        findViewById(R.id.rating_bar).setVisibility(View.VISIBLE);
    }

    private void updatenavbar() {
        //Toast.makeText(this, ""+log_id.getString("id",null), Toast.LENGTH_SHORT).show();
        Log.v("TAG","hi"+log_id.getString("id",null));
        DatabaseReference db=FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null));
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    name.setText(map.get("name").toString());
                    phone.setText(map.get("phone").toString());
                    if (!map.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        image.setImageBitmap(decbyte);
                    }
                    findViewById(R.id.ride_card_view).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayrideinfo(Map<String,Object> map) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers/" + map.get("driver").toString());
        RatingBar ratingBar=(RatingBar)findViewById(R.id.rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Intent intent=new Intent(Home.this,RatingActivity.class);
                intent.putExtra("rating",rating);
                startActivity(intent);
            }
        });

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

    private void finddriver() {
        //Log.v("TAG",String.valueOf(i));
        //Toast.makeText(this, ""+String.valueOf(driver_list.size()), Toast.LENGTH_SHORT).show();;
        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        data.setOtp(id);
        otp.setText("OTP\n"+id);
        editor.putString("otp",id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(4));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        data.setSeat(seats.getSelectedItem().toString());
        //Toast.makeText(this, ""+String.valueOf(i), Toast.LENGTH_SHORT).show();
        if (found == 1) {
            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }
        else if (found == 0 && i < driver_list.size())
            getClosestDriver();
        else {
             screen_status=1;
             findViewById(R.id.layout4).setVisibility(View.VISIBLE);
             findViewById(R.id.layout3).setVisibility(View.GONE);
             Toast.makeText(this, "No Ride Found", Toast.LENGTH_SHORT).show();
             DatabaseReference data=FirebaseDatabase.getInstance().getReference("Share");
             data.child(log_id.getString("id",null)).removeValue();
             findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
             findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
//             if (vehicletype.equals("bike"))
//                place_drivers_bike();
//             else if (vehicletype.equals("car"))
//                 place_drivers_car();
//             else if (vehicletype.equals("auto"))
//                 place_drivers_auto();
//             else if (vehicletype.equals("rickshaw"))
//                 place_drivers_rickshaw();
            driverid="";
            place_drivers();
//            place_drivers_share();

            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    public void select_vehicle(View v) {
        boolean valid = true;
        if (TextUtils.isEmpty(pickup_address.getText().toString())) {
            pickup_address.setError("Required.");
            valid = false;
        }
        if (TextUtils.isEmpty(destn_address.getText().toString())) {
            destn_address.setError("Required.");
            valid = false;
        } else {
            screen_status=1;
            if (v == findViewById(R.id.bike)) {
                seatfull();
                vehicletype="bike";
                final_price.setText(price_bike.getText());
                final_time.setText(time_bike.getText());
                final_image.setImageResource(R.drawable.bike1);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                place_drivers();
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.car)) {
                seatfull();
                vehicletype="car";
                final_price.setText(price_car.getText());
                final_time.setText(time_car.getText());
                final_image.setImageResource(R.drawable.carfinal);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                place_drivers();
                ridedetails.setText("This option is for booking car. It allows you to book complete car for yourself.");
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.auto)) {
                seatfull();
                vehicletype="auto";
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_auto.getText());
                final_time.setText(time_auto.getText());
                final_image.setImageResource(R.drawable.erickshaw1);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                place_drivers();
//                remove_drivers_bike();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.rickshaw)) {
                seatfull();
                vehicletype="rickshaw";
                // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
                final_price.setText(price_rickshaw.getText());
                final_time.setText(time_rickshaw.getText());
                final_image.setImageResource(R.drawable.rickshawfinal);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                place_drivers();
                ridedetails.setText("This option is for booking rickshaw. It allows you to book full rickshaw for yourself.");
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_bike();
            } else if (v == findViewById(R.id.shareCar)) {
                seatshare();
                vehicletype="car";
                final_price.setText(price_shareCar.getText());
                final_time.setText(time_shareCar.getText());
                final_image.setImageResource(R.drawable.carfinal);
                seats.setSelection(0);
                seats.setEnabled(true);
                ridetype="share";
                place_drivers();
                ridedetails.setText("This option is for booking share car. It allows you to book only required number of seats for yourself.");
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.shareAuto)) {
                seatshare();
                vehicletype="auto";
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_shareAuto.getText());
                final_time.setText(time_shareAuto.getText());
                final_image.setImageResource(R.drawable.erickshaw1);
                seats.setSelection(0);
                seats.setEnabled(true);
                ridetype="share";
                place_drivers();
//                remove_drivers_bike();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.shareRickshaw)) {
                seatshare();
                vehicletype="rickshaw";
                // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
                final_price.setText(price_shareRickshaw.getText());
                final_time.setText(time_shareRickshaw.getText());
                final_image.setImageResource(R.drawable.rickshawfinal);
                seats.setSelection(0);
                seats.setEnabled(true);
                ridetype="share";
                place_drivers();
                ridedetails.setText("This option is for booking share rickshaw. It allows you to book only required number of seats for yourself.");
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_bike();
            }

            findViewById(R.id.layout4).setVisibility(View.VISIBLE);
            findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
        }
    }

    DatabaseReference cust_req;
    DatabaseReference resp;

    private void getClosestDriver() {
//        Log.v("TAG",String.valueOf(driver_list.size()));
        driverid=driver_list.get(i);
        cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + driver_list.get(i));
        cust_req.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    DatabaseReference ref=null;
                    if (vehicletype.equals("bike"))
                        ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Bike");
                    else if (vehicletype.equals("car"))
                        ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
                    else if (vehicletype.equals("auto"))
                        ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Auto");
                    else if (vehicletype.equals("rickshaw"))
                        ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(driver_list.get(i));
                    cust_req.child(log_id.getString("id",null)).setValue(data);
                } else {
                    ++i;
                    finddriver();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Log.v("TAG",log_id.getString("driver",null)+"\njihij "+ driver_list.get(i));

        response();

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (found == 0 ) {
                    i++;
                    cust_req.child(log_id.getString("id",null)).removeValue();
                    resp.removeValue();
                    resp.removeEventListener(resplistener);
                    finddriver();
                    Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                }
            }
        }, 20000);
    }

    private void check_status(){
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + log_id.getString("driver",null));
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                //Glide.with(Home.this).load(drive.get("thumb").toString()).into((CircleImageView)findViewById(R.id.pic));
                ((TextView)findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
                ((TextView)findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString());

                if (!drive.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(drive.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    ((CircleImageView)findViewById(R.id.pic)).setImageBitmap(decbyte);
                }
                ((CircleImageView)findViewById(R.id.call)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:"+drive.get("phone").toString()));

                        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        startActivity(callIntent);
                    }
                });
                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.rating_bar).setVisibility(View.GONE);
                //SharedPreferences.Editor editor=log_id.edit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
        resplistener=resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        startService(new Intent(Home.this,NotificationService.class));
                        editor.putString("status","accepted");
                        editor.commit();

                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        tracktripstatus();
                        //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
                        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();
                    }
                    else if (map.get("resp").toString().equals("Located")){
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status","located");
                        editor.commit();
                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    }
                    else if (map.get("resp").toString().equals("Trip Started")){
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor=log_id.edit();
                        editor.putString("status","started");
                        editor.commit();

                        mMap.clear();
                        if (marker_pick!=null){
                            marker_pick.remove();
                        }
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        tracktripstatus();
                    }
                    else if (map.get("resp").toString().equals("Trip Ended")){
                        editor.putString("status","ended");
                        editor.commit();
                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(Home.this,TripCompleted.class);
                        intent.putExtra("id",log_id.getString("driver",null));
                        finish();
                        resp.removeEventListener(resplistener);
                    }
                    else if (map.get("resp").toString().equals("Cancel")){
                        Toast.makeText(Home.this, "Trip Cancelled by driver", Toast.LENGTH_SHORT).show();
                        cancel_current_trip();
//                        // Log.v("TAG","cancel");
//
//                        if (driver != null) {
//                            driver.remove();
//                        }
//                        if (marker_drop != null) {
//                            marker_drop.remove();
//                        }
//                        found = 0;
//                        erasePolylines();
//                        //ride.setVisibility(View.GONE);
//                        findViewById(R.id.layout4).setVisibility(View.GONE);
//                        findViewById(R.id.layout3).setVisibility(View.GONE);
//                        findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
//                        findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
//                        findViewById(R.id.canceltrip).setVisibility(View.GONE);
//                        destn_address.setText("");
//                        //DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
//                        //cr.removeValue();
//                        mMap.clear();
//                        if (marker_pick != null) {
//                            marker_pick.remove();
//                        }
//
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Current Location")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                                .position(cur_loc)
//                                .snippet("Pick up");
//                        marker_pick = mMap.addMarker(options);
//                        pickup = cur_loc;

//                        DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
//                        cr.child(log_id.getString("id",null)).removeValue();
//
//                        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
//                        resp.removeValue();
//                        resp.removeEventListener(resplistener);
//
//                        SharedPreferences.Editor editor = log_id.edit();
//                        editor.putString("driver", "");
//                        editor.remove("status");
//                        editor.commit();
//                        finish();
//                        startActivity(getIntent());
//                        found = 0;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void response(){
//        resp = FirebaseDatabase.getInstance().getReference("Response/" + driver_list.get(i));
        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
        resplistener=resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        startService(new Intent(Home.this,NotificationService.class));
                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
                        found = 1;
                        SharedPreferences.Editor editor=log_id.edit();
                        editor.putString("amount",final_price.getText().toString());
                        editor.commit();
                        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + driverid);
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                                final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                                //Glide.with(Home.this).load(drive.get("thumb").toString()).into((CircleImageView)findViewById(R.id.pic));
                                ((TextView)findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
                                ((TextView)findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                                ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString());

                                if (!drive.get("thumb").toString().equals("")) {
                                    byte[] dec = Base64.decode(drive.get("thumb").toString(), Base64.DEFAULT);
                                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                    ((CircleImageView)findViewById(R.id.pic)).setImageBitmap(decbyte);
                                }
                                ((CircleImageView)findViewById(R.id.call)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:"+drive.get("phone").toString()));

                                        if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        startActivity(callIntent);
                                    }
                                });
                                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                                SharedPreferences.Editor editor=log_id.edit();
                                editor.putString("driver",driverid);
                                editor.putString("status","accepted");
                                editor.commit();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                tracktripstatus();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else if (map.get("resp").toString().equals("Located")){
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status","located");
                        editor.commit();
                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    }
                    else if (map.get("resp").toString().equals("Trip Started")){
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor=log_id.edit();
                        editor.putString("status","started");
                        editor.commit();

                        mMap.clear();
                        if (marker_pick!=null){
                            marker_pick.remove();
                        }
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        tracktripstatus();
                    }
                    else if (map.get("resp").toString().equals("Trip Ended")){
                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(Home.this,TripCompleted.class);
                        intent.putExtra("id",log_id.getString("driver",null));
                        startActivity(intent);
                        stopService(new Intent(Home.this, NotificationService.class));
                        finish();
                        resp.removeValue();
                        resp.removeEventListener(resplistener);
                    }
                    else if (map.get("resp").toString().equals("Reject")) {
                        //Toast.makeText(Home.this, "Rejected"+driver_list.size(), Toast.LENGTH_SHORT).show();
                        found = 0;
                        cust_req.child(log_id.getString("id",null)).removeValue();
                        resp.removeValue();
                        resp.removeEventListener(resplistener);
                        stopService(new Intent(Home.this, NotificationService.class));
                        driverid="";
                        ++i;
                        //if (i <= driver_list.size())
                            finddriver();
                    }
                    else if (map.get("resp").toString().equals("Cancel")){
                        Toast.makeText(Home.this, "Trip Cancelled by driver", Toast.LENGTH_SHORT).show();
                        cancel_current_trip();
                        stopService(new Intent(Home.this, NotificationService.class));
                        // Log.v("TAG","cancel");

//                        if (driver != null) {
//                            driver.remove();
//                        }
//                        if (marker_drop != null) {
//                            marker_drop.remove();
//                        }
//                        found = 0;
//                        erasePolylines();
//                        //ride.setVisibility(View.GONE);
//                        findViewById(R.id.layout4).setVisibility(View.GONE);
//                        findViewById(R.id.layout3).setVisibility(View.GONE);
//                        findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
//                        findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
//                        findViewById(R.id.canceltrip).setVisibility(View.GONE);
//                        destn_address.setText("");
//                        //DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
//                        //cr.removeValue();
//                        mMap.clear();
//                        if (marker_pick != null) {
//                            marker_pick.remove();
//                        }
//
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Current Location")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                                .position(cur_loc)
//                                .snippet("Pick up");
//                        marker_pick = mMap.addMarker(options);
//                        pickup = cur_loc;

//                        DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
//                        cr.child(log_id.getString("id",null)).removeValue();
//                        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
//                        resp.removeValue();
//                        resp.removeEventListener(resplistener);
//
//                        SharedPreferences.Editor editor = log_id.edit();
//                        editor.putString("driver", "");
//                        editor.remove("status");
//                        editor.commit();
//                        finish();
//                        startActivity(getIntent());
//                        found = 0;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void cancel_current_trip() {
        Toast.makeText(this, "Cancelling trip", Toast.LENGTH_SHORT).show();
//        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
//        tripstatus.removeValue();
        resp.removeValue();
        resp.removeEventListener(resplistener);
        cust_req.child(log_id.getString("id",null)).removeValue();
        SharedPreferences.Editor editor=log_id.edit();
        editor.putString("driver","");
        editor.remove("ride");
        editor.remove("status");
        editor.commit();
        finish();
        startActivity(getIntent());
    }

    private void tracktripstatus() {
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        findViewById(R.id.rating_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
        //ride.setVisibility(View.GONE);
        //Log.v("TAG","enter");
        mMap.clear();
        if (log_id.getString("status",null).equals("accepted")) {
            if (marker_pick != null) {
                marker_pick.remove();
            }

            final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc);
            if (show==1){
                MarkerOptions opt = new MarkerOptions()
                        .title(pickup_address.getText().toString())
                        .icon(icon)
                        .position(pick_loc)
                        .snippet("Pick Up");
                marker_pick = mMap.addMarker(opt);
            }
            else {
                MarkerOptions opt = new MarkerOptions()
                        .title(pickup_address.getText().toString())
                        .icon(icon)
                        .position(pickup)
                        .snippet("Pick Up");
                marker_pick = mMap.addMarker(opt);
            }
        }
        else {
            cust_req.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (marker_pick != null) {
                        marker_pick.remove();
                    }
                    MarkerOptions opt = new MarkerOptions()
                            .title(dataSnapshot.child("destination").getValue(String.class))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
                            .position(new LatLng(dataSnapshot.child("en_lat").getValue(Double.class),dataSnapshot.child("en_lng").getValue(Double.class)))
                            .snippet("Destination");
                    marker_pick = mMap.addMarker(opt);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
        tripstatus.child("/l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //mMap.clear();
                    ArrayList<Double> map = (ArrayList<Double>) dataSnapshot.getValue();
                    if (Double.parseDouble(map.get(0).toString())!=0 && Double.parseDouble(map.get(0).toString())!=0) {
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        Bitmap b = null;
                        if (vehicletype.equals("rickshaw"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rickshawfinal), 100,60, false);
                        else if (vehicletype.equals("car"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.carfinal), 100,60, false);

                        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);
                        MarkerOptions opt = new MarkerOptions()
                                .title("Driver")
                                .icon(icon)
                                .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
                                .snippet("Driver Location");
                        marker_drop = mMap.addMarker(opt);

//                    if (log_id.contains("status")){
//                        MarkerOptions opt = new MarkerOptions()
//                                .title("Me")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
//                                .snippet("Current Location");
//                        marker_drop = mMap.addMarker(opt);
//                    }
//                    else {
//                        MarkerOptions options1 = new MarkerOptions()
//                                .title("Driver")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
//                                .snippet("Driver Location");
//                        marker_drop = mMap.addMarker(options1);
//                    }
//                    if (log_id.contains("status")){
//                        Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
//                        cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null)+"/Info");
//                        cust_req.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
//                                if (marker_pick!=null){
//                                    marker_pick.remove();
//                                }
//                                MarkerOptions options = new MarkerOptions()
//                                        .title("Destination")
//                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                                        .position(new LatLng(Double.parseDouble(map.get("en_lat").toString()), Double.parseDouble(map.get("en_lng").toString())))
//                                        .snippet("Drop Location");
//                                marker_pick = mMap.addMarker(options);
//
//                                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                    else {
//                        Toast.makeText(Home.this, "bye", Toast.LENGTH_SHORT).show();
//                        if (marker_pick!=null){
//                            marker_pick.remove();
//                        }
//                        MarkerOptions opt = new MarkerOptions()
//                                .title("Pickup")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//                                .position(cur_loc)
//                                .snippet("Pickup Location");
//                        marker_pick = mMap.addMarker(opt);

                        // getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
                        //}
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + log_id.getString("driver", null));
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                    final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                    ((TextView) findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
                    ((TextView) findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                    ((TextView) findViewById(R.id.dname)).setText(drive.get("name").toString());

                    if (!drive.get("thumb").toString().equals("")) {
                        byte[] dec = Base64.decode(drive.get("thumb").toString(), Base64.DEFAULT);
                        Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                        ((CircleImageView) findViewById(R.id.pic)).setImageBitmap(decbyte);
                    }
                    ((CircleImageView) findViewById(R.id.call)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + drive.get("phone").toString()));

                            if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            startActivity(callIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void cancel_trip(View v) {
        Toast.makeText(this, "Cancelling trip", Toast.LENGTH_SHORT).show();
//        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
//        tripstatus.removeValue();
        resp.removeValue();
        resp.removeEventListener(resplistener);
        cust_req.child(log_id.getString("id",null)).removeValue();
        SharedPreferences.Editor editor=log_id.edit();
        editor.putString("driver","");
        editor.remove("ride");
        editor.remove("status");
        editor.commit();
        finish();
        startActivity(getIntent());
    }

    private boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to play services !", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    LocationRequest lct;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //goToLocationZoom(39.008224,-76.8984527,15);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        gpc = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gpc.connect();
    }

    private void goToLocationZoom(double v, double v1, float zoom) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lct = LocationRequest.create();
        lct.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // lct.setInterval(1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(gpc, lct, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(this, "Cannot get current location !", Toast.LENGTH_SHORT).show();
        } else {
            Geocoder gc = new Geocoder(this);
            List<Address> list = null;
            try {
                list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (list!=null) {
                Address address = list.get(0);
                String locality = address.getAddressLine(0);

                double lat = location.getLatitude();
                double lng = location.getLongitude();

                goToLocationZoom(lat, lng, 15);

                if (show==0) {
                   // Toast.makeText(this, "hi2", Toast.LENGTH_SHORT).show();
                    if (marker_pick != null) {
                        marker_pick.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title(address.getLocality())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc))
                            .position(new LatLng(lat, lng))
                            .snippet("Pick up");
                    marker_pick = mMap.addMarker(options);
                }

                pickup = new LatLng(lat, lng);
                cur_loc = new LatLng(lat, lng);

                data.setSt_lat(lat);
                data.setSt_lng(lng);

                pickup_address.setText(address.getLocality());
                place_drivers();
//                place_drivers_share();
            }
        }
    }

    public void place_drivers() {
        driver_list.clear();
        DatabaseReference ref=null ;
        if (vehicletype.equals("bike"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Bike");
        else if (vehicletype.equals("car"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
        else if (vehicletype.equals("auto"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Auto");
        else if (vehicletype.equals("rickshaw"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");

        for (int k = 0; k < driver_markers.size(); k++) {
            driver_markers.get(k).remove();
        }
        driver_markers.clear();
        Bitmap b=null;
        if (vehicletype.equals("rickshaw"))
           b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rickshawfinal),100,60,false);
        else if (vehicletype.equals("car"))
            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.carfinal),100,60,false);

        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);

        GeoFire geoFire = new GeoFire(ref);
        find_driver = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
        find_driver.removeAllListeners();

        find_driver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                driver_list.add(key);

                if (driver_list.size()==1){
                    loc=location;
                }

                MarkerOptions options = new MarkerOptions()
                        .title(vehicletype)
                        .icon(icon)
                        .position(new LatLng(location.latitude, location.longitude));

                driver_markers.add(mMap.addMarker(options));
            }

            @Override
            public void onKeyExited(String key) {
                for (int i=0;i<driver_list.size();i++) {
                    if (key.equals(driver_list.get(i))) {
                        Marker mrk = driver_markers.get(i);
                        mrk.remove();
                        driver_list.remove(i);
                        driver_markers.remove(i);
                        break;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (int i=0;i<driver_list.size();i++){
                    if (key.equals(driver_list.get(i))){
                        MarkerOptions options = new MarkerOptions()
                                .title("Bike")
                                .icon(icon)
                                .position(new LatLng(location.latitude, location.longitude));

                        Marker mrk=driver_markers.get(i);
                        mrk.remove();
                        driver_markers.set(i,mMap.addMarker(options));
                        break;
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
                    Object[] dataTransfer = new Object[9];
                    String url = getDirectionsUrl(loc);
                    GetDirectionsData getDirectionsData = new GetDirectionsData();
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    dataTransfer[2] = time_bike;
                    dataTransfer[3] = time_car;
                    dataTransfer[4] = time_auto;
                    dataTransfer[5] = time_rickshaw;
                    dataTransfer[6] = time_shareAuto;
                    dataTransfer[7] = time_shareCar;
                    dataTransfer[8] = time_shareRickshaw;
                    getDirectionsData.execute(dataTransfer);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

//        if (!vehicletype.equals("bike"))
//            place_drivers_share();
    }

//    public void place_drivers_share() {
//        driver_list_share.clear();
//        DatabaseReference ref=null ;
//
//        if (vehicletype.equals("car"))
//            ref= FirebaseDatabase.getInstance().getReference("DriversWorking/Car");
//        else if (vehicletype.equals("auto"))
//            ref= FirebaseDatabase.getInstance().getReference("DriversWorking/Auto");
//        else if (vehicletype.equals("bike"))
//            ref= FirebaseDatabase.getInstance().getReference("DriversWorking/Bike");
//        else if (vehicletype.equals("rickshaw"))
//            ref= FirebaseDatabase.getInstance().getReference("DriversWorking/Rickshaw");
//
//        for (int k = 0; k < driver_markers_share.size(); k++) {
//            driver_markers_share.get(k).remove();
//        }
//        driver_markers_share.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_share = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_share.removeAllListeners();
//
//        find_driver_share.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list_share.add(key);
//
//                if (driver_list_share.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_share.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list_share.size();i++) {
//                    if (key.equals(driver_list_share.get(i))) {
//                        Marker mrk = driver_markers_share.get(i);
//                        mrk.remove();
//                        driver_list_share.remove(i);
//                        driver_markers_share.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list_share.size();i++){
//                    if (key.equals(driver_list_share.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_share.get(i);
//                        mrk.remove();
//                        driver_markers_share.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }

//    public void place_drivers_bike() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Bike");
//
//        for (int k = 0; k < driver_markers_bike.size(); k++) {
//            driver_markers_bike.get(k).remove();
//        }
//        driver_markers_bike.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_bike = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_bike.removeAllListeners();
//
//        find_driver_bike.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_bike.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_bike.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_bike.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_bike.get(i);
//                        mrk.remove();
//                        driver_markers_bike.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_car() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
//
//        for (int k = 0; k < driver_markers_car.size(); k++) {
//            driver_markers_car.get(k).remove();
//        }
//        driver_markers_car.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_car = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_car.removeAllListeners();
//
//        find_driver_car.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_car.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_car.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_car.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_car.get(i);
//                        mrk.remove();
//                        driver_markers_car.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_rickshaw() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");
//
//        for (int k = 0; k < driver_markers_rickshaw.size(); k++) {
//            driver_markers_rickshaw.get(k).remove();
//        }
//        driver_markers_rickshaw.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_rickshaw = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_rickshaw.removeAllListeners();
//
//        find_driver_rickshaw.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_rickshaw.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_rickshaw.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_rickshaw.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_rickshaw.get(i);
//                        mrk.remove();
//                        driver_markers_rickshaw.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_auto() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Auto");
//
//        for (int k = 0; k < driver_markers_auto.size(); k++) {
//            driver_markers_auto.get(k).remove();
//        }
//        driver_markers_auto.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_auto = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_auto.removeAllListeners();
//
//        find_driver_auto.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_auto.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_auto.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_auto.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_auto.get(i);
//                        mrk.remove();
//                        driver_markers_auto.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_share_car() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
//
//        for (int k = 0; k < driver_markers_car.size(); k++) {
//            driver_markers_car.get(k).remove();
//        }
//        driver_markers_car.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_car = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_car.removeAllListeners();
//
//        find_driver_car.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_car.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_car.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_car.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_car.get(i);
//                        mrk.remove();
//                        driver_markers_car.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_share_rickshaw() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");
//
//        for (int k = 0; k < driver_markers_rickshaw.size(); k++) {
//            driver_markers_rickshaw.get(k).remove();
//        }
//        driver_markers_rickshaw.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_rickshaw = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_rickshaw.removeAllListeners();
//
//        find_driver_rickshaw.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_rickshaw.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_rickshaw.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_rickshaw.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_rickshaw.get(i);
//                        mrk.remove();
//                        driver_markers_rickshaw.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void place_drivers_share_auto() {
//        driver_list.clear();
//        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Auto");
//
//        for (int k = 0; k < driver_markers_auto.size(); k++) {
//            driver_markers_auto.get(k).remove();
//        }
//        driver_markers_auto.clear();
//
//        GeoFire geoFire = new GeoFire(ref);
//        find_driver_auto = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver_auto.removeAllListeners();
//
//        find_driver_auto.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                driver_list.add(key);
//
//                if (driver_list.size()==1){
//                    loc=location;
//                }
//                MarkerOptions options = new MarkerOptions()
//                        .title("Bike")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                        .position(new LatLng(location.latitude, location.longitude));
//
//                driver_markers_auto.add(mMap.addMarker(options));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                for (int i=0;i<driver_list.size();i++) {
//                    if (key.equals(driver_list.get(i))) {
//                        Marker mrk = driver_markers_auto.get(i);
//                        mrk.remove();
//                        driver_list.remove(i);
//                        driver_markers_auto.remove(i);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                for (int i=0;i<driver_list.size();i++){
//                    if (key.equals(driver_list.get(i))){
//                        MarkerOptions options = new MarkerOptions()
//                                .title("Bike")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//                                .position(new LatLng(location.latitude, location.longitude));
//
//                        Marker mrk=driver_markers_auto.get(i);
//                        mrk.remove();
//                        driver_markers_auto.set(i,mMap.addMarker(options));
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//                if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
//                    Object[] dataTransfer = new Object[9];
//                    String url = getDirectionsUrl(loc);
//                    GetDirectionsData getDirectionsData = new GetDirectionsData();
//                    dataTransfer[0] = mMap;
//                    dataTransfer[1] = url;
//                    dataTransfer[2] = time_bike;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_shareAuto;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
//                    getDirectionsData.execute(dataTransfer);
//                }
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//        });
//    }
//
//    public void remove_drivers_bike(){
//        driver_list_bike.clear();
//        for (int k = 0; k < driver_markers_bike.size(); k++) {
//            driver_markers_bike.get(k).remove();
//        }
//        driver_markers_bike.clear();
//    }
//
//    public void remove_drivers_car(){
//        driver_list_car.clear();
//        for (int k = 0; k < driver_markers_car.size(); k++) {
//            driver_markers_car.get(k).remove();
//        }
//        driver_markers_car.clear();
//    }
//
//    public void remove_drivers_auto(){
//        driver_list_auto.clear();
//        for (int k = 0; k < driver_markers_auto.size(); k++) {
//            driver_markers_auto.get(k).remove();
//        }
//        driver_markers_auto.clear();
//    }
//
//    public void remove_drivers_rickshaw(){
//        driver_list_rickshaw.clear();
//        for (int k = 0; k < driver_markers_rickshaw.size(); k++) {
//            driver_markers_rickshaw.get(k).remove();
//        }
//        driver_markers_rickshaw.clear();
//    }

    private String getDirectionsUrl(GeoLocation locate) {
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+locate.latitude+","+locate.longitude);
        googleDirectionsUrl.append("&destination="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyAicFor08br3-Jl-xwUc0bZHC2KMdcGRNo");
        return googleDirectionsUrl.toString();
    }

    private List<Polyline> polylines = new ArrayList<>();

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
           // Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
           // Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        //for (int i = 0; i <route.size(); i++) {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(7);
        polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);

        pricebike(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        priceauto(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        pricecar(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        pricerickshaw(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        priceshareauto(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        pricesharecar(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());
        pricesharerickshaw(route.get(shortestRouteIndex).getDistanceValue(),route.get(shortestRouteIndex).getDurationValue());

//            time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
        //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}
    }

    private void pricebike(int distanceValue,int time) {
        price_bike.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
        //time_bike.setText(String.valueOf(time/60)+" min");
    }

    private void priceauto(int distanceValue,int time) {
        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
        //time_auto.setText(String.valueOf(time/60)+" min");
    }

    private void pricecar(int distanceValue,int time) {
        price_car.setText("Rs. " + String.valueOf(distanceValue * 6 / 1000));
        //time_car.setText(String.valueOf(time/60)+" min");
    }

    private void pricerickshaw(int distanceValue,int time) {
        price_rickshaw.setText("Rs. " + String.valueOf(distanceValue * 4.5 / 1000));
        //time_rickshaw.setText(String.valueOf(time/60)+" min");
    }

    private void priceshareauto(int distanceValue,int time) {
        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
        //time_shareAuto.setText(String.valueOf(time/60)+" min");
    }

    private void pricesharecar(int distanceValue,int time) {
        price_shareCar.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
        //time_shareCar.setText(String.valueOf(time/60)+" min");
    }

    private void pricesharerickshaw(int distanceValue,int time) {
        price_shareRickshaw.setText("Rs. " + String.valueOf(distanceValue * 3.5 / 1000));
        //time_shareRickshaw.setText(String.valueOf(time/60)+" min");
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteToMarker(LatLng pickupLatLng, LatLng destnLatLng) {
        if (pickupLatLng != null && destnLatLng != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void erasePolylines() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_rides) {
            intent = new Intent(Home.this, CustomerRides.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(Home.this,Settings.class));
        } else if (id == R.id.nav_drive_with_us) {
            startActivity(new Intent(Home.this,DriveWithUs.class));
        } else if (id == R.id.nav_emergency_contact) {
            startActivity(new Intent(Home.this,EmergencyContact.class));
//            Intent callIntent = new Intent(Intent.ACTION_CALL);
//            callIntent.setData(Uri.parse("tel:0000000000"));
//
//            if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return false;
//            }
//            startActivity(callIntent);
        } else if (id == R.id.nav_offers) {
            intent = new Intent(Home.this, OffersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_payment) {
            intent = new Intent(Home.this, Payment.class);
            startActivity(intent);
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(Home.this,Support.class));
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                    "mailto","qiklift@gmail.com", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Message : ");
//            startActivity(Intent.createChooser(emailIntent, "Requesting Support !"));
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==1 && resultCode==RESULT_OK){
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);

            pickup_address.setText(name);
            goToLocationZoom(latitude, longitude, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            MarkerOptions options=new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc))
                    .position(new LatLng(latitude,longitude))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();
            cur_loc=marker_pick.getPosition();

            data.setSt_lat(latitude);
            data.setSt_lng(longitude);

            find_driver.removeAllListeners();
//            find_driver_share.removeAllListeners();
            place_drivers();
//            place_drivers_share();
            getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());

        } else if (requestCode==2 && resultCode==RESULT_OK){
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);

            findViewById(R.id.rating_bar).setVisibility(View.GONE);
            destn_address.setText(name);

            screen_status=1;

            goToLocationZoom(latitude,longitude, 15);

            if (marker_drop!=null){
                marker_drop.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
                    .position(new LatLng(latitude,longitude))
                    .snippet("Destination");
            marker_drop=mMap.addMarker(options);

            data.setEn_lat(marker_drop.getPosition().latitude);
            data.setEn_lng(marker_drop.getPosition().longitude);
            findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
            getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());
            findViewById(R.id.layout3).setVisibility(View.GONE);
            findViewById(R.id.layout4).setVisibility(View.GONE);
            findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);

        } else if (requestCode==3 && resultCode==RESULT_OK){
            Double pr=Double.parseDouble(final_price.getText().toString().substring(4));
            Double disc=Double.parseDouble(intent.getStringExtra("discount"));
            Double upto=Double.parseDouble(intent.getStringExtra("upto"));

            Double less=(pr*disc)/100;
            if (less>upto)
                less=upto;

            if (pr<less)
                pr=0.0;
            else
                pr=pr-less;

            final_price.setText("Rs. "+pr);

            offer.setText(intent.getStringExtra("offer"));
        }
    }

    public void ratingbarclick(View v){
        Intent intent=new Intent(Home.this,RatingActivity.class);
        intent.putExtra("rating",0);
        startActivity(intent);
    }

    public void closelocationsave(View view){
        findViewById(R.id.location_card_view).setVisibility(View.GONE);
        findViewById(R.id.rating_bar).setVisibility(View.GONE);
    }

    int showoption=0;
    public void showoptiondetail(View view){
        if (showoption==0) {
            findViewById(R.id.ridedetails).setVisibility(View.VISIBLE);
            showoption = 1;
        }
        else {
            findViewById(R.id.ridedetails).setVisibility(View.GONE);
            showoption = 0;
        }
    }

    public void seatfull(){
        seats_list.clear();
        seats_list.add("full");

        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
        adapter1.setDropDownViewResource(R.layout.payment_list);
        seats.setAdapter(adapter1);
    }

    public void seatshare(){
        seats_list.clear();
        seats_list.add("1");
        seats_list.add("2");
        seats_list.add("3");

        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
        adapter1.setDropDownViewResource(R.layout.payment_list);
        seats.setAdapter(adapter1);
    }
}

