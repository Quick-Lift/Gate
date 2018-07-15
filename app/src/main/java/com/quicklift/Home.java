package com.quicklift;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
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
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener, NavigationView.OnNavigationItemSelectedListener {
    private GoogleMap mMap;
    GoogleApiClient gpc;
    Marker marker_pick, marker_drop, marker, driver_drop, driver;
    EditText pickup_address, destn_address;
    TextView price_bike,price_excel, price_car, price_auto, price_rickshaw, price_shareAuto, price_shareCar, price_shareRickshaw;
    TextView time;
    Float estimated_time;
    GeoQuery find_driver;
    ArrayList<String> seats_list = new ArrayList<>();
    ArrayList<String> offer_list = new ArrayList<>();
    TextView payment;
    Button confirm;
    Data data = new Data();
    Polyline line;
    GeoLocation loc;
    LatLng pickup, cur_loc;
    GeoLocation driver_loc;
    ProgressDialog pdialog;
    int i = 0, found = 0;
    ArrayList<String> driver_list = new ArrayList<String>();
    ArrayList<String> share_driver_list = new ArrayList<String>();
    SharedPreferences log_id;
    Integer no_of_drivers = 0;
    HorizontalScrollView hsv;
    private ImageView final_image;
    private TextView final_price;
    SQLQueries sqlQueries;
    float radius=3;
    boolean driverfound = false;
    String driverid,arrival_time;
    private TextView name,otp;
    private TextView phone;
    private CircleImageView image;
    Integer screen_status=0;
    ValueEventListener resplistener;
    DatabaseReference lastride;
    SharedPreferences.Editor editor;
    TextView time_bike,time_excel,time_car,time_auto,time_rickshaw,time_shareAuto,time_shareCar,time_shareRickshaw,final_time;
    boolean doubleBackToExitPressedOnce = false;
    DrawerLayout drawer;
    Spinner seats;
    TextView offer;
    String offercode="";
    Integer offervalue=0,realprice=0;
    List<Marker> driver_markers = new ArrayList<>();
    private LatLng pick_loc;
    int show=0;
    String ridetype=null,vehicletype="car",prev_ride_case="";
    TextView findingridemsg,ridedetails;
    Dialog dialog;
    int vehicle_case=0;
    boolean isrunning=false;
    Handler handler_time;
    Handler handle_display=new Handler();
    Runnable runnable,disp_detail;
    View view;
    TextView title,message;
    Button left,right;
    AlertDialog alert;
    GeoQueryEventListener gqel=null;
    int original_seats=1;
    Handler handle=new Handler();
    Runnable find_runnable;
    Handler sharehandle=new Handler();
    Runnable sharerunnable;
    static TextView network_status;
    static Activity home=null;
    int repeatcounter=0;
    int vehcasepick=0,vehcasedrop=0;
    int park_pick=0,park_drop=0;
    String parking_priceshare="0",parking_pricefull="0",parking_priceexcel="0";
    CheckConnectivity connectivity=new CheckConnectivity();

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

                showoption = 0;
                findViewById(R.id.ridedetails).setVisibility(View.GONE);
                findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);
                findViewById(R.id.layout3).setVisibility(View.GONE);
                findViewById(R.id.layout4).setVisibility(View.GONE);
                findViewById(R.id.rating_bar).setVisibility(View.GONE);

                offer.setText("");
                offercode="";
                offervalue=0;
                original_seats=1;

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

        home=this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        editor=log_id.edit();
        sqlQueries = new SQLQueries(this);

        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Searching for driver...");
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(true);

        view=getLayoutInflater().inflate(R.layout.notification_layout,null);
        title=(TextView)view.findViewById(R.id.title);
        message=(TextView)view.findViewById(R.id.message);
        left=(Button) view.findViewById(R.id.left_btn);
        right=(Button) view.findViewById(R.id.right_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder .setView(view)
                .setCancelable(false);

        alert = builder.create();

        if (googleServicesAvailable()) {
            //Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_home_screen);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date strDate = null;
            try {
                strDate = sdf.parse("04/05/2018");
                if ((new Date()).after(strDate)) {
                    initMap();
                }
                else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//                    builder.setMessage("This app is coming soon for your area .")
//                            .setTitle("Coming Soon !")
//                            .setCancelable(false);
//
//                    //Creating dialog box
//                    AlertDialog alert = builder.create();
//                    //Setting the title manually
//                    alert.show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
//            Toast.makeText(this, "Unable to load map ! Please turn on location !", Toast.LENGTH_SHORT).show();
        }

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivity, intentFilter);
    }

    private void initMap() {
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);

        pickup_address = (EditText) findViewById(R.id.pickup);
        destn_address = (EditText) findViewById(R.id.destination);
        confirm = (Button) findViewById(R.id.confirm);
        payment = (TextView) findViewById(R.id.pay_mode);
        seats = (Spinner) findViewById(R.id.seats);
        offer = (TextView) findViewById(R.id.offer);
        hsv = (HorizontalScrollView) findViewById(R.id.fragmentTwo);
        price_auto = (TextView) findViewById(R.id.price_auto);
        price_bike = (TextView) findViewById(R.id.price_bike);
        price_excel = (TextView) findViewById(R.id.price_excel);
        price_car = (TextView) findViewById(R.id.price_car);
        price_rickshaw = (TextView) findViewById(R.id.price_rickshaw);
        price_shareAuto = (TextView) findViewById(R.id.price_shareAuto);
        price_shareCar = (TextView) findViewById(R.id.price_shareCar);
        price_shareRickshaw = (TextView) findViewById(R.id.price_shareRickshaw);
        time_auto = (TextView) findViewById(R.id.time_auto);
        time_bike = (TextView) findViewById(R.id.time_bike);
        time_excel = (TextView) findViewById(R.id.time_excel);
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
        network_status=(TextView) findViewById(R.id.network_status);

        getTime();
//        checkinternetconnection();
//        handler_time.postDelayed(runnable,0);
//        seats_list.add("full");
//        seats_list.add("1");
//        seats_list.add("2");
//        seats_list.add("3");
//        seats_list.add("4");

//        ArrayAdapter<String> adapter1 =
//                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
//        adapter1.setDropDownViewResource(R.layout.payment_list);
//        seats.setAdapter(adapter1);

//        new CheckConnection();

        data.setWaitcharge(log_id.getString("waitingcharge",null));
        data.setWaittime(log_id.getString("waittime",null));
        SimpleDateFormat dt=new SimpleDateFormat("HH:mm");
        try {
            if ((dt.parse("11:00").after(dt.parse(dt.format(new Date()))) && dt.parse("08:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("23:59").after(dt.parse(dt.format(new Date()))) && dt.parse("17:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("05:00").after(dt.parse(dt.format(new Date()))) && dt.parse("00:00").before(dt.parse(dt.format(new Date()))))){
                if (log_id.contains("peaktimeradius"))
                    radius=(float)Float.valueOf(log_id.getString("peaktimeradius",null));
            }
            else {
                if (log_id.contains("normaltimeradius"))
                    radius=(float)Float.valueOf(log_id.getString("normaltimeradius",null));
//                Toast.makeText(home, ""+log_id.getString("normaltimeradius",null), Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);
        toolbar.setBackgroundColor(0x00ffffff);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        pickup_address.setInputType(0);
        destn_address.setInputType(0);

        seats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    if (original_seats==2){
                        original_seats=1;
                        int val=Integer.parseInt(final_price.getText().toString().substring(4));
                        val=(int) ((val*100)/115);
                        final_price.setText("Rs. "+val);
                    }
                }
                else if (position==1){
                    if (original_seats==1){
                        original_seats=2;
                        int val=Integer.parseInt(final_price.getText().toString().substring(4));
                        val=val+(int) ((val*15)/100);
                        final_price.setText("Rs. "+val);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pickup_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Home.this, PlaceSelector.class);
                intent.putExtra("type","pickup");
                startActivityForResult(intent, 1);
            }
        });

        destn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Home.this, PlaceSelector.class);
                intent.putExtra("type","destination");
                startActivityForResult(intent, 2);
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

        find_runnable=new Runnable() {
            @Override
            public void run() {
                isrunning = true;
                if (found == 0 && i<=driver_list.size()) {
                    i=i+1;
                    cust_req.child(log_id.getString("id", null)).removeValue();
                    resp.removeValue();
                    resp.removeEventListener(resplistener);

                    Log.v("TAG","Find driver by handler"+i);
//                        Toast.makeText(Home.this, "Find driver by handler", Toast.LENGTH_SHORT).show();

                    finddriver();
//                        Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                }
            }
        };
        sharerunnable=new Runnable() {
            @Override
            public void run() {
                if (found == 0 && sharedriver<=share_driver_list.size()) {
                    sharedriver=sharedriver+1;
                    cust_req.child(log_id.getString("id", null)).removeValue();
                    resp.removeValue();
                    resp.removeEventListener(resplistener);

//                    Log.v("TAG","Find driver by handler"+i);
//                        Toast.makeText(Home.this, "Find driver by handler", Toast.LENGTH_SHORT).show();

                    sendsharerequest();
//                        Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                }
            }
        };

        disp_detail=new Runnable() {
            @Override
            public void run() {
                found=1;
                SharedPreferences.Editor ed=log_id.edit();
                ed.putString("found","1");
                ed.commit();
                startService(new Intent(Home.this,NotificationService.class));
                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/"+driverid);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Map<String,Object> drive=(Map<String, Object>) dataSnapshot.getValue();
                            ((TextView)findViewById(R.id.bike_name)).setText(drive.get("model").toString());
                            ((TextView)findViewById(R.id.bike_no)).setText(drive.get("number").toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + driverid);
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                        final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                        //Glide.with(Home.this).load(drive.get("thumb").toString()).into((CircleImageView)findViewById(R.id.pic));
//                                ((TextView)findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
//                                ((TextView)findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                        int iend = drive.get("name").toString().indexOf(" ");
                        if (iend!=-1)
                            ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString().substring(0 , iend));
                        else
                            ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString());
//                                if (drive.containsKey("rate"))
//                                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(Float.valueOf(drive.get("rate").toString()));
//                                else
//                                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(0);
                        if (drive.containsKey("rate"))
                            ((TextView)findViewById(R.id.rating_value)).setText(String.format("%.1f",Float.parseFloat(drive.get("rate").toString())));
                        else
                            ((TextView)findViewById(R.id.rating_value)).setText("5.0");
                        ((TextView)findViewById(R.id.amount)).setText(final_price.getText().toString());

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
                        handler_time.removeCallbacks(runnable);
                        handler_time.postDelayed(runnable,1000);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

//        if (!log_id.getString("driver", null).equals("")) {
//            check_status();
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
            show_ride_current_details();
        }

        DatabaseReference dataref=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
        dataref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("resp").getValue().toString().equals("Accept") ||
                            dataSnapshot.child("resp").getValue().toString().equals("Located")){
                        editor.putString("show","ride");
                        editor.commit();
                        show_ride_current_details();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        data.setCancel_charge("0");
        DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerPendingCharges/"+log_id.getString("id",null));
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int val=0;
                    if (dataSnapshot.hasChild("cancel_req/excel")){
                        val=val+(Integer.parseInt(dataSnapshot.child("cancel_req/excel").getValue().toString())*Integer.parseInt(log_id.getString("excelcharge",null)));
                    }
                    if (dataSnapshot.hasChild("cancel_req/full")){
                        val=val+(Integer.parseInt(dataSnapshot.child("cancel_req/full").getValue().toString())*Integer.parseInt(log_id.getString("fullcharge",null)));
                    }
                    if (dataSnapshot.hasChild("cancel_req/share")){
                        val=val+(Integer.parseInt(dataSnapshot.child("cancel_req/share").getValue().toString())*Integer.parseInt(log_id.getString("sharecharge",null)));
                    }
                    data.setCancel_charge(String.valueOf(val));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                Intent intent=new Intent(Home.this, ConfirmLocation.class);
                intent.putExtra("lat",String.valueOf(marker_pick.getPosition().latitude));
                intent.putExtra("lng",String.valueOf(marker_pick.getPosition().longitude));
                intent.putExtra("address",pickup_address.getText().toString());
                startActivityForResult(intent, 6);
            }
        });

        offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Home.this, SelectOffers.class), 3);
            }
        });
    }

    private void show_ride_current_details() {
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
                    driverid=log_id.getString("driver",null);
                    otp.setText("OTP\n"+dataSnapshot.child("otp").getValue(String.class));
                    destn_address.setText(dataSnapshot.child("destination").getValue(String.class));
                    pickup_address.setText(dataSnapshot.child("source").getValue(String.class));
                    final_price.setText("Rs. "+dataSnapshot.child("price").getValue(String.class));
                    pick_loc=new LatLng(dataSnapshot.child("st_lat").getValue(Double.class),dataSnapshot.child("st_lng").getValue(Double.class));
                    editor.remove("show");
                    editor.putString("amount",String.valueOf((int) (Float.parseFloat(dataSnapshot.child("price").getValue(String.class)
                            + Float.parseFloat(dataSnapshot.child("cancel_charge").getValue(String.class))))));
                    editor.commit();
                    check_status();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

//        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog=new Dialog(this);
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    private void findsharedriver() {
        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        data.setOtp(id);
//        data.setParking_price(parking_price);
        otp.setText("OTP\n"+id);
        editor.putString("otp",id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(4));
        data.setOffer(String.valueOf(offervalue));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        data.setSeat(seats.getSelectedItem().toString().substring(0,1));

        final DatabaseReference share=FirebaseDatabase.getInstance().getReference("Share");
        ShareClass shareClass=new ShareClass();
        shareClass.setSt_lat(marker_pick.getPosition().latitude);
        shareClass.setSt_lng(marker_pick.getPosition().longitude);
        shareClass.setEn_lat(marker_drop.getPosition().latitude);
        shareClass.setEn_lng(marker_drop.getPosition().longitude);
        shareClass.setSeats(seats.getSelectedItem().toString().substring(0,1));
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
//            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
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
//                        Toast.makeText(Home.this, "hello", Toast.LENGTH_SHORT).show();
                        String seat = dataSnapshot.getValue(String.class);
                        if (!seat.equals("full") && (Integer.parseInt(seat) + Integer.parseInt(seats.getSelectedItem().toString().substring(0,1))) <= 4) {
                            cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + share_driver_list.get(sharedriver));
                            cust_req.child(log_id.getString("id", null)).setValue(data);
                            found=0;
                            driverid=share_driver_list.get(sharedriver);
//                            sharehandle.postDelayed(sharerunnable,60000);
//                            response();
                            found=1;
                            response();
                        } else {
                            sharedriver++;
                            sharehandle.removeCallbacks(sharerunnable);
                            sendsharerequest();
                        }
                    }
                    else {
                        sharedriver++;
                        sharehandle.removeCallbacks(sharerunnable);
                        sendsharerequest();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

//            sharehandle.postDelayed(sharerunnable,60000);
//                            response();

//            resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
//            resplistener=resp.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.exists()){
//                        if (dataSnapshot.child("resp").getValue().toString().equals("Accept")){
//                            sharehandle.removeCallbacks(sharerunnable);
//                            resp.removeEventListener(resplistener);
//                            found=1;
//                            response();
//                        }
//                        else if (dataSnapshot.child("resp").getValue().toString().equals("Reject")){
//                            resp.removeValue();
//                            resp.removeEventListener(resplistener);
//                            sharedriver=sharedriver+1;
//                            sharehandle.removeCallbacks(sharerunnable);
//                            sendsharerequest();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
        }
        else if (found!=1 && sharedriver>=share_driver_list.size()){
            if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                repeatsearching();
            }
            else {
                DatabaseReference share = FirebaseDatabase.getInstance().getReference("Share");
                share.child(log_id.getString("id", null)).removeValue();
                finddriver();
            }
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
        findViewById(R.id.location_card_view).setEnabled(false);
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

        find_driver.removeAllListeners();
        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        data.setOtp(id);
//        data.setParking_price(parking_price);
        otp.setText("OTP\n"+id);
        editor.putString("otp",id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(4));
        data.setOffer(String.valueOf(offervalue));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        if (seats.getSelectedItem().toString().equals("full"))
            data.setSeat(seats.getSelectedItem().toString());
        else
            data.setSeat(seats.getSelectedItem().toString().substring(0,1));
        //Toast.makeText(this, ""+String.valueOf(i), Toast.LENGTH_SHORT).show();
        if (found == 1) {
//            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing())
                dialog.dismiss();
        }
        else if (found == 0 && i < driver_list.size()) {
//            Log.v("TAG","CD"+i+" "+driver_list.size());
//            Toast.makeText(this, "close driver", Toast.LENGTH_SHORT).show();
            getClosestDriver();
        }
        else {
            if (vehicletype == "car" && ridetype == "full" && prev_ride_case=="car") {
//                Log.v("TAG","if "+String.valueOf(isrunning));
                found = 0;
                i = 0;
                driverid = "";
                vehicletype = "excel";
                place_drivers_excel();
            } else if (vehicletype.equals("excel") && prev_ride_case.equals("car")) {
//                Log.v("TAG","else if "+String.valueOf(isrunning));
//                Toast.makeText(this, "else if condition", Toast.LENGTH_SHORT).show();
                found = 0;
                place_drivers();
                prev_ride_case = "";
                vehicletype = "car";

//                if (isrunning) {
//                    isrunning = false;

                    driverid = "";
                    Handler hd=new Handler();
                    hd.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Handler hd=new Handler();
                            hd.postDelayed(new Runnable() {
                                @Override
                                public void run() {

//             if (vehicletype.equals("bike"))
//                place_drivers_bike();
//             else if (vehicletype.equals("car"))
//                 place_drivers_car();
//             else if (vehicletype.equals("auto"))
//                 place_drivers_auto();
//             else if (vehicletype.equals("rickshaw"))
//                 place_drivers_rickshaw();
//            place_drivers_share();
                                    if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                                        repeatsearching();
                                    } else {
                                        if (dialog.isShowing())
                                            dialog.dismiss();
////                                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
////                                    builder.setMessage("Sorry we are facing congestion ! Please try again later ! ")
////                                            .setCancelable(false)
////                                            .setTitle("Try Again !")
////                                            .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
////                                                public void onClick(DialogInterface dialog, int id) {
////                                                    screen_status = 1;
////                                                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
////                                                    findViewById(R.id.layout3).setVisibility(View.GONE);
//////                                                    Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
////                                                    DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
////                                                    data.child(log_id.getString("id", null)).removeValue();
////                                                    findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
////                                                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
////                                                }
////                                            });
//
////                                    //Creating dialog box
////                                    AlertDialog alert = builder.create();
////                                    //Setting the title manually
////                                    alert.show();
////                                    alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
////                                    alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));
//
//                                        title.setText("No Ride Found !");
//                                        message.setText("Sorry we are facing congestion ! Please try again later ! ");
//                                        left.setVisibility(View.GONE);
//                                        right.setText("Try Again");
//
//                                        alert.show();
//
//                                        right.setOnClickListener(null);
//                                        right.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                screen_status = 1;
//                                                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
//                                                findViewById(R.id.layout3).setVisibility(View.GONE);
////                                                    Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
//                                                DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
//                                                data.child(log_id.getString("id", null)).removeValue();
//                                                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
//                                                findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
//                                                alert.dismiss();
//
//                                                driverid = "";
//                                                place_drivers();
//                                            }
//                                        });
                                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                                        ((TextView)findViewById(R.id.msg_text)).setText("Sorry we are facing congestion ! Please try again later ! ");
                                        pickup_address.setEnabled(false);
                                        destn_address.setEnabled(false);
                                        no_ride_found();
                                        findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                screen_status = 1;
                                                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                                                findViewById(R.id.layout3).setVisibility(View.GONE);
//                                                    Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
                                                DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
                                                data.child(log_id.getString("id", null)).removeValue();
                                                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                                                findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                                                alert.dismiss();

                                                driverid = "";
                                                place_drivers();

                                                pickup_address.setEnabled(true);
                                                destn_address.setEnabled(true);
                                                findViewById(R.id.no_ride).setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }
                            }, 20000);
                        }
                    }, 5000);
//                }
            } else {
                Log.v("TAG","else "+String.valueOf(isrunning));
//                if (isrunning) {
//                    isrunning = false;
//                    Toast.makeText(this, "else condition", Toast.LENGTH_SHORT).show();

                Handler hd=new Handler();
                hd.postDelayed(new Runnable() {
                    @Override
                    public void run() {

//            place_drivers_share();
                        if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                            repeatsearching();
                        } else {
                            if (dialog.isShowing())
                                dialog.dismiss();

//                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//                        builder.setMessage("Sorry we are facing congestion ! Please try again later ! ")
//                                .setCancelable(false)
//                                .setTitle("Try Again !")
//                                .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        prev_ride_case = "";
//                                        screen_status = 1;
//                                        found = 0;
//                                        findViewById(R.id.layout4).setVisibility(View.VISIBLE);
//                                        findViewById(R.id.layout3).setVisibility(View.GONE);
////                                        Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
//                                        DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
//                                        data.child(log_id.getString("id", null)).removeValue();
//                                        findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
//                                        findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
////             if (vehicletype.equals("bike"))
////                place_drivers_bike();
////             else if (vehicletype.equals("car"))
////                 place_drivers_car();
////             else if (vehicletype.equals("auto"))
////                 place_drivers_auto();
////             else if (vehicletype.equals("rickshaw"))
////                 place_drivers_rickshaw();
//                                        driverid = "";
//                                        place_drivers();
//                                    }
//                                });
//
//                        //Creating dialog box
//                        AlertDialog alert = builder.create();
//                        //Setting the title manually
//                        alert.show();
//                        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//                        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));

//                            title.setText("No Ride Found !");
//                            message.setText("Sorry we are facing congestion ! Please try again later ! ");
//                            left.setVisibility(View.GONE);
//                            right.setText("Try Again");
//
//                            alert.show();
//
//                            right.setOnClickListener(null);
//                            right.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    prev_ride_case = "";
//                                    screen_status = 1;
//                                    found = 0;
//                                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
//                                    findViewById(R.id.layout3).setVisibility(View.GONE);
////                                        Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
//                                    DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
//                                    data.child(log_id.getString("id", null)).removeValue();
//                                    findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
//                                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
////             if (vehicletype.equals("bike"))
////                place_drivers_bike();
////             else if (vehicletype.equals("car"))
////                 place_drivers_car();
////             else if (vehicletype.equals("auto"))
////                 place_drivers_auto();
////             else if (vehicletype.equals("rickshaw"))
////                 place_drivers_rickshaw();
//                                    driverid = "";
//                                    place_drivers();
//                                    alert.dismiss();
//                                }
//                            });
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.msg_text)).setText("Sorry we are facing congestion ! Please try again later ! ");
                            pickup_address.setEnabled(false);
                            destn_address.setEnabled(false);
                            no_ride_found();
                            findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    screen_status = 1;
                                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                                    findViewById(R.id.layout3).setVisibility(View.GONE);
//                                                    Toast.makeText(Home.this, "No Ride Found", Toast.LENGTH_SHORT).show();
                                    DatabaseReference data = FirebaseDatabase.getInstance().getReference("Share");
                                    data.child(log_id.getString("id", null)).removeValue();
                                    findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                                    alert.dismiss();

                                    driverid = "";
                                    place_drivers();

                                    pickup_address.setEnabled(true);
                                    destn_address.setEnabled(true);
                                    findViewById(R.id.no_ride).setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }, 20000);
//                }
            }
        }
    }

    private void repeatsearching() {
        repeatcounter++;
        i=0;
        sharedriver=0;
        place_drivers();
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                find_driver.removeAllListeners();
                if (ridetype.equals("full"))
                    finddriver();
                else
                    findsharedriver();
            }
        },6000);
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
            if (v == findViewById(R.id.bike)) {
                screen_status=1;
                seatfull();
                vehicletype="bike";
                final_price.setText(price_bike.getText());
//                final_time.setText(time_bike.getText());
                final_image.setImageResource(R.drawable.bike1);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.car)) {
                screen_status=1;
                seatfull();
                vehicletype="car";
                data.setVeh_type("full");
//                if (log_id.contains("parkfull"))
//                    data.setParking_price(log_id.getString("parkfull",null));
                final_price.setText(price_car.getText());
//                final_time.setText(time_car.getText());
                final_image.setImageResource(R.drawable.niji);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();
                ridedetails.setText("This option is for booking car. It allows you to book complete car for yourself.");

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.type)).setText("Van");
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.excel)) {
                screen_status=1;
                seatfull();
                vehicletype="excel";
//                if (log_id.contains("parkexcel"))
//                    data.setParking_price(log_id.getString("parkexcel",null));
                data.setVeh_type("excel");
                final_price.setText(price_excel.getText());
//                final_time.setText(time_excel.getText());
                final_image.setImageResource(R.drawable.carfinal);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();
                ridedetails.setText("This option is for booking excel car. It allows you to book complete car for yourself with passenger capacity more than 4.");

                ((TextView)findViewById(R.id.type)).setText("SUV");
                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.auto)) {
                screen_status=1;
                seatfull();
                vehicletype="auto";
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_auto.getText());
//                final_time.setText(time_auto.getText());
                final_image.setImageResource(R.drawable.erickshaw1);
                seats.setSelection(0);
                seats.setEnabled(false);
                ridetype="full";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
//                remove_drivers_bike();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.rickshaw)) {
//                seatfull();
//                vehicletype="rickshaw";
//                data.setVeh_type("full");
//                // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
//                final_price.setText(price_rickshaw.getText());
////                final_time.setText(time_rickshaw.getText());
//                final_image.setImageResource(R.drawable.rickshawfinal);
//                seats.setSelection(0);
//                seats.setEnabled(false);
//                ridetype="full";
//                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
//                place_drivers();
//                ridedetails.setText("This option is for booking rickshaw. It allows you to book full rickshaw for yourself.");
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_bike();

                comingsoonnotification();
            } else if (v == findViewById(R.id.shareCar)) {
                screen_status=1;
                seatshare();
                vehicletype="car";
//                if (log_id.contains("parkshare"))
//                    data.setParking_price(log_id.getString("parkshare",null));
                data.setVeh_type("share");
                final_price.setText(price_shareCar.getText());
//                final_time.setText(time_shareCar.getText());
                final_image.setImageResource(R.drawable.share);
                seats.setSelection(0);
                seats.setEnabled(true);
                ridetype="share";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();
                ridedetails.setText("This option is for booking share car. It allows you to book only required number of seats for yourself.");

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.type)).setText("Taxi");
                //                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.shareAuto)) {
                screen_status=1;
                seatshare();
                vehicletype="auto";
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_shareAuto.getText());
//                final_time.setText(time_shareAuto.getText());
                final_image.setImageResource(R.drawable.erickshaw1);
                seats.setSelection(0);
                seats.setEnabled(true);
                ridetype="share";
                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
                place_drivers();

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
//                remove_drivers_bike();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.shareRickshaw)) {
//                seatshare();
//                vehicletype="rickshaw";
//                data.setVeh_type("share");
//                // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
//                final_price.setText(price_shareRickshaw.getText());
////                final_time.setText(time_shareRickshaw.getText());
//                final_image.setImageResource(R.drawable.rickshawfinal);
//                seats.setSelection(0);
//                seats.setEnabled(true);
//                ridetype="share";
//                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(4));
//                place_drivers();
//                ridedetails.setText("This option is for booking share rickshaw. It allows you to book only required number of seats for yourself.");
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_bike();
                comingsoonnotification();
            }
        }
    }

    private void comingsoonnotification() {
        findViewById(R.id.comingsoon).setVisibility(View.VISIBLE);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.comingsoon).setVisibility(View.GONE);
            }
        },2000);
    }

    DatabaseReference cust_req;
    DatabaseReference resp;

    private void getClosestDriver() {
        if (i<driver_list.size()) {
            Log.v("TAG","Closest driver"+i);
            driverid = driver_list.get(i);
            cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + driver_list.get(i));
            cust_req.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
//                        Toast.makeText(Home.this, "Sent", Toast.LENGTH_SHORT).show();
                        DatabaseReference ref = null;
                        if (vehicletype.equals("bike"))
                            ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Bike");
                        if (vehicletype.equals("excel"))
                            ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Excel");
                        else if (vehicletype.equals("car"))
                            ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
                        else if (vehicletype.equals("auto"))
                            ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Auto");
                        else if (vehicletype.equals("rickshaw"))
                            ref = FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.removeLocation(driverid);
                        cust_req.child(log_id.getString("id", null)).setValue(data);
                    } else {
                        i=i+1;
                        Log.v("TAG","Find driver by customer req");
                        handle.removeCallbacks(find_runnable);
//                        Toast.makeText(Home.this, "Find driver by customer request", Toast.LENGTH_SHORT).show();
                        finddriver();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //Log.v("TAG",log_id.getString("driver",null)+"\njihij "+ driver_list.get(i));

            response();
        }
//        handle.postDelayed(find_runnable,60000);
    }

    private void check_status(){
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/"+driverid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String,Object> drive=(Map<String, Object>) dataSnapshot.getValue();
                    ((TextView)findViewById(R.id.bike_name)).setText(drive.get("model").toString());
                    ((TextView)findViewById(R.id.bike_no)).setText(drive.get("number").toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + log_id.getString("driver",null));
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                //Glide.with(Home.this).load(drive.get("thumb").toString()).into((CircleImageView)findViewById(R.id.pic));
//                ((TextView)findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
//                ((TextView)findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                int iend = drive.get("name").toString().indexOf(" ");
                if (iend!=-1)
                    ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString().substring(0 , iend));
                else
                    ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString());
//                if (drive.containsKey("rate"))
//                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(Float.valueOf(drive.get("rate").toString()));
//                else
//                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(0);
                if (drive.containsKey("rate"))
                    ((TextView)findViewById(R.id.rating_value)).setText(String.format("%.1f",Float.parseFloat(drive.get("rate").toString())));
                else
                    ((TextView)findViewById(R.id.rating_value)).setText("5.0");
                ((TextView)findViewById(R.id.amount)).setText(final_price.getText().toString());

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
                        editor.remove("offer");
                        if (!offercode.equals(""))
                            editor.putString("offer",offercode);
                        editor.commit();

//                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        tracktripstatus();
                        handler_time.removeCallbacks(runnable);
                        handler_time.postDelayed(runnable,1000);
                        //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
                        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();
                    }
                    else if (map.get("resp").toString().equals("Located")){
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status","located");
                        editor.commit();
//                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    }
                    else if (map.get("resp").toString().equals("Trip Started")){
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.layout_ride_status).setVisibility(View.VISIBLE);
//                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
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
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
                        editor.putString("status","ended");
                        editor.commit();
//                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(Home.this,TripCompleted.class);
                        intent.putExtra("id",log_id.getString("driver",null));
                        finish();
                        resp.removeEventListener(resplistener);
                    }
                    else if (map.get("resp").toString().equals("Cancel")){
                        SharedPreferences.Editor ed=log_id.edit();
                        ed.putString("found","0");
                        ed.commit();
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
//                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//                        builder.setMessage("Sorry the driver is currently unable to serve you ! Please try again !!")
//                                .setCancelable(false)
//                                .setTitle("Trip Cancelled !")
//                                .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
////                                        Toast.makeText(Home.this, "Trip Cancelled by driver", Toast.LENGTH_SHORT).show();
//                                        cancel_current_trip();
//                                    }
//                                });

//                        //Creating dialog box
//                        AlertDialog alert = builder.create();
//                        //Setting the title manually
//                        alert.show();
//                        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//                        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));

//                        title.setText("Trip Cancelled !");
//                        message.setText("Sorry the driver is currently unable to serve you ! Please try again !");
//                        left.setVisibility(View.GONE);
//                        right.setText("Try Again");
//
//                        alert.show();
//
//
//                        right.setOnClickListener(null);
//                        right.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                cancel_current_trip();
//                                alert.dismiss();
//                            }
//                        });
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                        findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancel_current_trip();
                            }
                        });
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
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
            handle.removeCallbacks(find_runnable);

//                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
            //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
//            found = 1;
            final SharedPreferences.Editor editor = log_id.edit();
            editor.putString("amount", String.valueOf(Float.parseFloat(data.getPrice()) + Float.parseFloat(data.getCancel_charge())));
            editor.remove("offer");
            if (!offercode.equals(""))
                editor.putString("offer", offercode);
            editor.commit();

        if (ridetype.equals("full"))
            handle_display.postDelayed(disp_detail, 5000);
        else
            handle_display.postDelayed(disp_detail, 10);
        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();

        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id",null));
        resplistener=resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        SharedPreferences.Editor ed=log_id.edit();
                        ed.putString("found","1");
                        ed.commit();
                        found=1;
                        handle_display.removeCallbacks(disp_detail);
                        handle_display.postDelayed(disp_detail,10);
                    }
                    else if (map.get("resp").toString().equals("Located")){
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status","located");
                        editor.commit();
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
//                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    }
                    else if (map.get("resp").toString().equals("Trip Started")){
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.layout_ride_status).setVisibility(View.VISIBLE);
//                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
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
//                        stopService(new Intent(Home.this,NotificationService.class));
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
//                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent=new Intent(Home.this,TripCompleted.class);
                        intent.putExtra("id",log_id.getString("driver",null));
                        startActivity(intent);
//                        stopService(new Intent(Home.this, NotificationService.class));
                        finish();
                        resp.removeValue();
                        resp.removeEventListener(resplistener);
                    }
                    else if (map.get("resp").toString().equals("Reject")) {
                        //Toast.makeText(Home.this, "Rejected"+driver_list.size(), Toast.LENGTH_SHORT).show();
//                        handle.removeCallbacks(find_runnable);
//                        handler_time.removeCallbacks(runnable);
//                        marker_pick.hideInfoWindow();
//                        found = 0;
//                        cust_req.child(log_id.getString("id",null)).removeValue();
//                        resp.removeValue();
//                        resp.removeEventListener(resplistener);
////                        stopService(new Intent(Home.this, NotificationService.class));
//                        driverid="";
//                        i=i+1;
//                        //if (i <= driver_list.size())
//                        Log.v("TAG","Find driver by reject"+i);
////                        Toast.makeText(Home.this, "Find driver by reject", Toast.LENGTH_SHORT).show();
//                            finddriver();
                        SharedPreferences.Editor ed=log_id.edit();
                        ed.putString("found","0");
                        ed.commit();
                        if (found==0){
                            handle.removeCallbacks(find_runnable);
                            handler_time.removeCallbacks(runnable);
                            if (marker_pick!=null)
                                marker_pick.hideInfoWindow();
                            handle_display.removeCallbacks(disp_detail);
                            driverid="";
                            i=i+1;cust_req.child(log_id.getString("id",null)).removeValue();
                            resp.removeValue();
                            resp.removeEventListener(resplistener);
                            finddriver();
                        }
                        else {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            handle_display.removeCallbacks(disp_detail);
                            handler_time.removeCallbacks(runnable);
                            if (marker_pick!=null)
                                marker_pick.hideInfoWindow();
                            findViewById(R.id.layout3).setVisibility(View.GONE);
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                            findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancel_current_trip();
                                }
                            });
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                        }
//                        if (found==0) {
//                            finddriver();
//                        }
//                        else {
//                            if (dialog.isShowing())
//                                dialog.dismiss();
//                        }
                    }
                    else if (map.get("resp").toString().equals("Cancel")){
                        SharedPreferences.Editor ed=log_id.edit();
                        ed.putString("found","0");
                        ed.commit();
                        if (dialog.isShowing())
                            dialog.dismiss();
                        handle_display.removeCallbacks(disp_detail);
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick!=null)
                            marker_pick.hideInfoWindow();
//                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//                        builder.setMessage("Sorry the driver is currently unable to serve you ! Please try again !!")
//                                .setCancelable(false)
//                                .setTitle("Trip Cancelled !")
//                                .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
////                                        Toast.makeText(Home.this, "Trip Cancelled by driver", Toast.LENGTH_SHORT).show();
//                                        cancel_current_trip();
//                                    }
//                                });
//
//                        //Creating dialog box
//                        AlertDialog alert = builder.create();
//                        //Setting the title manually
//                        alert.show();
//                        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//                        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));


//                        title.setText("Trip Cancelled !");
//                        message.setText("Sorry the driver is currently unable to serve you ! Please try again !");
//                        left.setVisibility(View.GONE);
//                        right.setText("Try Again");
//
//                        alert.show();
//
//                        right.setOnClickListener(null);
//                        right.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                cancel_current_trip();
//                                alert.dismiss();
//                            }
//                        });
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                        findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancel_current_trip();
                            }
                        });
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);

//                        stopService(new Intent(Home.this, NotificationService.class));
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
//        Toast.makeText(this, "Trip Cancelled !", Toast.LENGTH_SHORT).show();
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
                    if (dataSnapshot.exists()) {
                        if (marker_pick != null) {
                            marker_pick.remove();
                        }
                        MarkerOptions opt = new MarkerOptions()
                                .title(dataSnapshot.child("destination").getValue(String.class))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
                                .position(new LatLng(dataSnapshot.child("en_lat").getValue(Double.class), dataSnapshot.child("en_lng").getValue(Double.class)))
                                .snippet("Destination");
                        marker_pick = mMap.addMarker(opt);
                    }
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
                    if (Double.valueOf(map.get(0).toString())!=0 && Double.valueOf(map.get(1).toString())!=0) {
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        Bitmap b = null;
                        if (vehicletype.equals("rickshaw"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rickshawfinal), 100,60, false);
                        else if (vehicletype.equals("car"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.carfinal), 100,60, false);
                        else if (vehicletype.equals("excel"))
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
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/"+driverid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
//                    Map<String,Object> drive=(Map<String, Object>) dataSnapshot.getValue();
//                    Log.v("STATUS",drive.get("model").toString());
                    ((TextView)findViewById(R.id.bike_name)).setText(dataSnapshot.child("model").getValue().toString());
                    ((TextView)findViewById(R.id.bike_no)).setText(dataSnapshot.child("number").getValue().toString());
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
                    if (log_id.getString("status",null).equals("started"))
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                    else
                        findViewById(R.id.layout3).setVisibility(View.VISIBLE);

                    final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

//                    ((TextView) findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
//                    ((TextView) findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                    int iend = drive.get("name").toString().indexOf(" ");
                    if (iend!=-1)
                        ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString().substring(0 , iend));
                    else
                        ((TextView)findViewById(R.id.dname)).setText(drive.get("name").toString());
//                    if (drive.containsKey("rate"))
//                        ((RatingBar)findViewById(R.id.driver_rating)).setRating(Float.valueOf(drive.get("rate").toString()));
//                    else
//                        ((RatingBar)findViewById(R.id.driver_rating)).setRating(0);
                    if (drive.containsKey("rate"))
                        ((TextView)findViewById(R.id.rating_value)).setText(String.format("%.1f",Float.parseFloat(drive.get("rate").toString())));
                    else
                        ((TextView)findViewById(R.id.rating_value)).setText("5.0");
                    ((TextView)findViewById(R.id.amount)).setText(final_price.getText().toString());

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
//        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//        builder.setMessage("Are you sure to cancel the ride ??")
//                .setCancelable(false)
//                .setTitle("Cancel Trip !")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        final ProgressDialog progress=new ProgressDialog(Home.this);
//                        progress.setMessage("Cancelling Trip !");
//                        progress.setIndeterminate(true);
//                        progress.setCanceledOnTouchOutside(false);
//                        progress.setCancelable(false);
//                        progress.show();
//
//                        handler_time.removeCallbacks(runnable);
//                        marker_pick.hideInfoWindow();
//
//                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null)+"/"+log_id.getString("id",null));
//                        ref.child("accept").setValue(2);
//                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                final Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
//                                Ride ride=new Ride();
//                                ride.setDestination(map.get("destination").toString());
//                                ride.setSource(map.get("source").toString());
//                                ride.setAmount(map.get("price").toString());
//                                ride.setCustomerid(map.get("customer_id").toString());
//                                ride.setDriver(log_id.getString("driver",null));
//                                ride.setCancel_charge(map.get("cancel_charge").toString());
//                                ride.setPaymode(map.get("paymode").toString());
//                                ride.setDiscount(map.get("offer").toString());
//                                Date dt=new Date();
//                                ride.setTime(dt.toString());
//                                ride.setStatus("Cancelled");
//                                ride.setCancelledby("Customer");
//
//                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Rides");
//                                String key=ref.push().getKey();
//                                ref.child(key).setValue(ride);
//
//                                final DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerPendingCharges/"+log_id.getString("id",null)+"/cancel_req");
//                                dref.child(map.get("veh_type").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        if (dataSnapshot.exists()){
//                                            int val=Integer.parseInt(dataSnapshot.getValue().toString())+1;
//                                            dref.child(map.get("veh_type").toString()).setValue(String.valueOf(val));
//                                        }
//                                        else {
//                                            dref.child(map.get("veh_type").toString()).setValue(String.valueOf(1));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });
//
//                                progress.dismiss();
////                                Toast.makeText(Home.this, "Trip Cancelled !", Toast.LENGTH_SHORT).show();
//                                resp.removeValue();
//                                resp.removeEventListener(resplistener);
//                                cust_req.child(log_id.getString("id",null)).removeValue();
//                                SharedPreferences.Editor editor=log_id.edit();
//                                editor.putString("driver","");
//                                editor.remove("ride");
//                                editor.remove("status");
//                                editor.commit();
//                                finish();
//                                startActivity(getIntent());
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//                })
//                .setNeutralButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });

//        //Creating dialog box
//        AlertDialog alert = builder.create();
//        //Setting the title manually
//        alert.show();
//        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//        alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));

        title.setText("Cancel Trip !");
        message.setText("Are you sure to cancel the ride ??");
        left.setVisibility(View.VISIBLE);
        left.setText("No");
        right.setText("Yes");

        alert.show();

        left.setOnClickListener(null);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        right.setOnClickListener(null);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progress=new ProgressDialog(Home.this);
                progress.setMessage("Cancelling Trip !");
                progress.setIndeterminate(true);
                progress.setCanceledOnTouchOutside(false);
                progress.setCancelable(false);
                progress.show();

                handler_time.removeCallbacks(runnable);
                if (marker_pick!=null)
                    marker_pick.hideInfoWindow();

                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null)+"/"+log_id.getString("id",null));
                ref.child("accept").setValue(2);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            Ride ride = new Ride();
                            ride.setDestination(map.get("destination").toString());
                            ride.setSource(map.get("source").toString());
                            ride.setAmount(map.get("price").toString());
                            ride.setCustomerid(map.get("customer_id").toString());
                            ride.setDriver(log_id.getString("driver", null));
                            ride.setCancel_charge(map.get("cancel_charge").toString());
                            ride.setPaymode(map.get("paymode").toString());
                            if (map.containsKey("parking_price"))
                                ride.setParking(map.get("parking_price").toString());
                            else
                                ride.setParking("0");
                            ride.setSeat(map.get("seat").toString());
                            ride.setDiscount(map.get("offer").toString());
                            Date dt = new Date();
                            ride.setTime(dt.toString());
                            ride.setStatus("Cancelled");
                            ride.setCancelledby("Customer");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Rides");
                            String key = ref.push().getKey();
                            ref.child(key).setValue(ride);

                            final DatabaseReference dref = FirebaseDatabase.getInstance().getReference("CustomerPendingCharges/" + log_id.getString("id", null) + "/cancel_req");
                            dref.child(map.get("veh_type").toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        int val = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;
                                        dref.child(map.get("veh_type").toString()).setValue(String.valueOf(val));
                                    } else {
                                        dref.child(map.get("veh_type").toString()).setValue(String.valueOf(1));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            progress.dismiss();
//                                Toast.makeText(Home.this, "Trip Cancelled !", Toast.LENGTH_SHORT).show();
                            resp.removeValue();
                            resp.removeEventListener(resplistener);
                            cust_req.child(log_id.getString("id", null)).removeValue();
                            SharedPreferences.Editor editor = log_id.edit();
                            editor.putString("driver", "");
                            editor.remove("ride");
                            editor.remove("status");
                            editor.commit();
                            finish();
                            startActivity(getIntent());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

//        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
//        tripstatus.removeValue();
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
//            Toast.makeText(this, "Cannot get current location !", Toast.LENGTH_SHORT).show();
        } else {
//            Geocoder gc = new Geocoder(this);
//            List<Address> list = null;
//            try {
//                list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            if (list!=null) {
//                Address address = list.get(0);
                String locality = getCompleteAddressString(location.getLatitude(),location.getLongitude());


                double lat = location.getLatitude();
                double lng = location.getLongitude();

                if (lat >= 25.561272 && lat <= 25.654152 && lng >= 85.020262 && lng <= 85.278055){
                    vehcasepick=1;
                }
                else {
                    vehcasepick=2;
                }

                goToLocationZoom(lat, lng, 15);

                if (show==0) {
                   // Toast.makeText(this, "hi2", Toast.LENGTH_SHORT).show();
                    if (marker_pick != null) {
                        marker_pick.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title(locality)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc))
                            .position(new LatLng(lat, lng))
                            .snippet("Pick up");
                    marker_pick = mMap.addMarker(options);
                }

                pickup = new LatLng(lat, lng);
                cur_loc = new LatLng(lat, lng);

                data.setSt_lat(lat);
                data.setSt_lng(lng);

                pickup_address.setText(locality);
                place_drivers();
//                place_drivers_share();
//            }
        }
    }

    public void place_drivers() {
        driver_list.clear();
        if (find_driver!=null)
            find_driver.removeAllListeners();
        DatabaseReference ref=null ;
        if (vehicletype.equals("bike"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Bike");
        if (vehicletype.equals("excel"))
            ref= FirebaseDatabase.getInstance().getReference("DriversAvailable/Excel");
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
        else if (vehicletype.equals("excel"))
            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.carfinal),100,60,false);

        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);

        GeoFire geoFire = new GeoFire(ref);
        find_driver = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver.removeAllListeners();

        final_time.setText("NA");
        loc=null;

        find_driver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                driver_list.add(key);

                if (driver_list.size()==1){
                    loc=location;
                    if (!pickup_address.getText().toString().equals("") && !destn_address.getText().toString().equals("") && loc!=null) {
                        Object[] dataTransfer = new Object[9];
                        String url = getDirectionsUrl(loc);
                        GetDirectionsData getDirectionsData = new GetDirectionsData();
                        dataTransfer[0] = mMap;
                        dataTransfer[1] = url;
                        dataTransfer[2] = final_time;
//                    dataTransfer[3] = time_car;
//                    dataTransfer[4] = time_auto;
//                    dataTransfer[5] = time_rickshaw;
//                    dataTransfer[6] = time_excel;
//                    dataTransfer[7] = time_shareCar;
//                    dataTransfer[8] = time_shareRickshaw;
                        getDirectionsData.execute(dataTransfer);
                    }
                }

                MarkerOptions options = new MarkerOptions()
                        .title(vehicletype)
                        .icon(icon)
                        .position(new LatLng(location.latitude, location.longitude));

                driver_markers.add(mMap.addMarker(options));
            }

            @Override
            public void onKeyExited(String key) {
                if (driver_markers.size()!=0) {
//                    Toast.makeText(Home.this, "" + "exited", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < driver_list.size(); i++) {
                        if (key.equals(driver_list.get(i))) {
                            Marker mrk = driver_markers.get(i);
                            mrk.remove();
                            driver_list.remove(i);
                            driver_markers.remove(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (int i=0;i<driver_list.size();i++){
                    if (key.equals(driver_list.get(i))){
                        MarkerOptions options = new MarkerOptions()
                                .title(vehicletype)
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

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

//        if (!vehicletype.equals("bike"))
//            place_drivers_share();
    }

    public void place_drivers_excel() {
        for (int k = 0; k < driver_markers.size(); k++) {
            driver_markers.get(k).remove();
        }
        driver_markers.clear();
        driver_list.clear();
        if (find_driver!=null)
            find_driver.removeAllListeners();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable/Excel");

        GeoFire geoFire = new GeoFire(ref);
        find_driver = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
//        find_driver.removeAllListeners();

        find_driver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                driver_list.add(key);
            }

            @Override
            public void onKeyExited(String key) {
                for (int i=0;i<driver_list.size();i++) {
                    if (key.equals(driver_list.get(i))) {
                        driver_list.remove(i);
                        break;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//                Toast.makeText(Home.this, ""+driver_list.size(), Toast.LENGTH_SHORT).show();
                finddriver();
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
        googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
        return googleDirectionsUrl.toString();
    }

    private String getDirectionsUrltwoplaces() {
        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude);
        googleDirectionsUrl.append("&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");
        return googleDirectionsUrl.toString();
    }

    private List<Polyline> polylines = new ArrayList<>();
    ProgressDialog prgdlg;

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
           // Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
           // Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
        prgdlg.dismiss();
    }

    @Override
    public void onRoutingStart() {
        prgdlg=new ProgressDialog(this);
        prgdlg.setMessage("Finding Route !");
        prgdlg.setIndeterminate(true);
        prgdlg.setCanceledOnTouchOutside(false);
        prgdlg.setCancelable(false);
        prgdlg.show();
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

//        SQLQueries sqlQueries=new SQLQueries(this);
//        int spec_package=-1;
//
//        Cursor spec_location=sqlQueries.retrievelocation();
//        Cursor cursor=sqlQueries.retrievefare();
//        Location lc=new Location(LocationManager.GPS_PROVIDER);
//        lc.setLatitude(marker_drop.getPosition().latitude);
//        lc.setLongitude(marker_drop.getPosition().longitude);
//        while(spec_location.moveToNext()){
//            Location l=new Location(LocationManager.GPS_PROVIDER);
//            l.setLatitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));
//            l.setLongitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("longitude"))));
////            GeoLocation l=new GeoLocation(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))),
////                    Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));
//
//            if (l.distanceTo(lc)<800){
//                spec_package++;
//                if (spec_package==2){
//                    hide_share_vehicles();
//                }
//                break;
//            }
//        }
////        Toast.makeText(this, ""+cursor.getCount()+" , "+cursor.getColumnCount(), Toast.LENGTH_SHORT).show();
//
//        SimpleDateFormat dt=new SimpleDateFormat("HH:mm");
//        int index=1;
//        try {
//            if ((dt.parse("11:00").after(dt.parse(dt.format(new Date()))) && dt.parse("08:00").before(dt.parse(dt.format(new Date())))) ||
//                    (dt.parse("23:59").after(dt.parse(dt.format(new Date()))) && dt.parse("17:00").before(dt.parse(dt.format(new Date())))) ||
//                    (dt.parse("05:00").after(dt.parse(dt.format(new Date()))) && dt.parse("00:00").before(dt.parse(dt.format(new Date()))))){
//                index=2;
//            }
//            else {
//                index=1;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        pricebike(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        priceexcel(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        priceauto(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        pricecar(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        pricerickshaw(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        priceshareauto(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        pricesharecar(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);
//        pricesharerickshaw(spec_package,spec_location,index, cursor,route.get(shortestRouteIndex).getDistanceValue()/1000,route.get(shortestRouteIndex).getDurationValue()/60);

//            time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
        //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}
        prgdlg.dismiss();
    }

//    private void priceexcel(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue, int time) {
////        Toast.makeText(this, ""+distanceValue+" "+time, Toast.LENGTH_SHORT).show();
//
//            cursor.moveToFirst();
//            for (int x=0;x<8;x++){
//                cursor.moveToNext();
//            }
////            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
////                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
//            float fare=0;
//            if (index==2)
//                cursor.moveToNext();
//
//        if (pckg==-1) {
//            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        else {
//            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
////            Log.v("TAG",""+fare);
//
//        if (vehicle_case==1) {
//            price_excel.setText("Rs. " + fare);
//        }
//        else if (vehicle_case==2) {
//            price_excel.setText("Rs. " + 2*fare);
//        }
//    }
//
//    private void pricebike(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_bike.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
//        //time_bike.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void priceauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
//        //time_auto.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void pricecar(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//            cursor.moveToFirst();
////            for (int x=0;x<1;x++){
////                cursor.moveToNext();
////            }
//
////            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
////                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
//            float fare=0;
//            if (index==2)
//                cursor.moveToNext();
//
//        if (pckg==-1) {
//            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        else {
//            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//
//        if (vehicle_case==1){
//            price_car.setText("Rs. " + fare);
//        }
//        else if (vehicle_case==2)
//            price_car.setText("Rs. " + 2*fare);
//        //time_car.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void pricerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        cursor.moveToFirst();
//        for (int x=0;x<2;x++){
//            cursor.moveToNext();
//        }
//
////        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
////                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
//        float fare=0;
//        if (index==2)
//            cursor.moveToNext();
//
//        if (pckg==-1) {
//            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        else {
//            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        price_rickshaw.setText("Rs. " + fare);
//        //time_rickshaw.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void priceshareauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
//        //time_shareAuto.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void pricesharecar(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        cursor.moveToFirst();
//        for (int x=0;x<4;x++){
//            cursor.moveToNext();
//        }
////        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
////                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
//        float fare=0;
//        if (index==2)
//            cursor.moveToNext();
//
//        if (pckg==-1) {
//            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        else {
//            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        price_shareCar.setText("Rs. " + fare);
//        //time_shareCar.setText(String.valueOf(time/60)+" min");
//    }
//
//    private void pricesharerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        cursor.moveToFirst();
//        for (int x=0;x<6;x++){
//            cursor.moveToNext();
//        }
////        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
////                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
//        float fare=0;
//        if (index==2)
//            cursor.moveToNext();
//
//        if (pckg==-1) {
//            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        else {
//            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//            } else {
//                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
////                Log.v("TAG",""+fare);
//                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
////                Log.v("TAG",""+fare);
//            }
//            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
//        }
//        price_shareRickshaw.setText("Rs. " + fare);
//        //time_shareRickshaw.setText(String.valueOf(time/60)+" min");
//    }

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
            startActivityForResult(intent,4);
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

//            if (intent.getStringExtra("case").equals("1")) {
//                vehcasepick=1;
//                if (vehcasedrop==2) {
//                    vehicle_case = 2;
//                    hide_share_vehicles();
//                }
//                else {
//                    vehicle_case = 1;
//                    show_share_vehicles();
//                }
//            }
//            else {
//                vehcasepick=2;
//                vehicle_case = 2;
//                hide_share_vehicles();
//            }

            pickup_address.setText(name);
//            goToLocationZoom(latitude, longitude, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            data.setSt_lat(latitude);
            data.setSt_lng(longitude);
            MarkerOptions options=new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc))
                    .position(new LatLng(latitude,longitude))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();
            cur_loc=marker_pick.getPosition();

            SQLQueries sqlQueries = new SQLQueries(this);
            int spec_package = -1;

            Cursor spec_location = sqlQueries.retrievelocation();
            Cursor cursor = sqlQueries.retrievefare();
            Location lc = new Location(LocationManager.GPS_PROVIDER);
            lc.setLatitude(marker_pick.getPosition().latitude);
            lc.setLongitude(marker_pick.getPosition().longitude);
            park_pick=0;
            while (spec_location.moveToNext()) {
                Location l = new Location(LocationManager.GPS_PROVIDER);
                l.setLatitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));
                l.setLongitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("longitude"))));
//            GeoLocation l=new GeoLocation(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))),
//                    Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));

                if (l.distanceTo(lc) < 300) {
                    spec_package++;
                    if (spec_package == 2) {
                        hide_share_vehicles();
                    }
                    park_pick=1;
                    Log.v("Park","pick park");
                    break;
                }
            }
            Log.v("DISTANCE",""+park_pick);
            if (marker_drop!=null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

                Log.v("Park"," "+(park_drop+park_pick));

                Object[] dataTransfer = new Object[18];
                String url = getDirectionsUrltwoplaces();
                GetPriceData getDirectionsData = new GetPriceData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = price_excel;
                dataTransfer[3] = price_car;
                dataTransfer[4] = price_rickshaw;
                dataTransfer[5] = price_shareCar;
                dataTransfer[6] = price_shareRickshaw;
                dataTransfer[7] = marker_drop;
                dataTransfer[8] = cursor;
                dataTransfer[9] = spec_location;
                dataTransfer[10] = spec_package;
                dataTransfer[11] = vehicle_case;
                dataTransfer[12] = Home.this;
                dataTransfer[13] = (park_pick+park_drop);
                dataTransfer[14] = parking_priceshare;
                dataTransfer[15] = parking_pricefull;
                dataTransfer[16] = parking_priceexcel;
                dataTransfer[17] = data;
                getDirectionsData.execute(dataTransfer);

//            place_drivers_share();
                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
            }
            else {
                goToLocationZoom(latitude, longitude, 15);
            }
                find_driver.removeAllListeners();
//            find_driver_share.removeAllListeners();
                place_drivers();

        } else if (requestCode==2 && resultCode==RESULT_OK) {
            String name;
            Double latitude, longitude;
            name = intent.getStringExtra("place");
            latitude = intent.getDoubleExtra("lat", 0);
            longitude = intent.getDoubleExtra("lng", 0);

//            if (intent.getStringExtra("case").equals("1")) {
//                vehcasedrop = 1;
//                if (vehcasepick == 2) {
//                    vehicle_case = 2;
//                    hide_share_vehicles();
//                } else {
//                    vehicle_case = 1;
//                    show_share_vehicles();
//                }
//            } else {
//                vehcasedrop = 2;
//                vehicle_case = 2;
//                hide_share_vehicles();
//            }

            if (intent.getStringExtra("case").equals("1")) {
                    vehicle_case = 1;
                    show_share_vehicles();
            }
            else {
                vehicle_case = 2;
                hide_share_vehicles();
            }

            findViewById(R.id.rating_bar).setVisibility(View.GONE);
            destn_address.setText(name);

            screen_status = 1;

            goToLocationZoom(latitude, longitude, 15);

            if (marker_drop != null) {
                marker_drop.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
                    .position(new LatLng(latitude, longitude))
                    .snippet("Destination");
            marker_drop = mMap.addMarker(options);

            SQLQueries sqlQueries = new SQLQueries(this);
            int spec_package = -1;

            Cursor spec_location = sqlQueries.retrievelocation();
            Cursor cursor = sqlQueries.retrievefare();
            Location lc = new Location(LocationManager.GPS_PROVIDER);
            lc.setLatitude(marker_drop.getPosition().latitude);
            lc.setLongitude(marker_drop.getPosition().longitude);
            park_drop=0;
            while (spec_location.moveToNext()) {
                Location l = new Location(LocationManager.GPS_PROVIDER);
                l.setLatitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));
                l.setLongitude(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("longitude"))));
//            GeoLocation l=new GeoLocation(Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))),
//                    Double.valueOf(spec_location.getString(spec_location.getColumnIndex("latitude"))));

                if (l.distanceTo(lc) < 300) {
                    spec_package++;
                    if (spec_package == 2) {
                        hide_share_vehicles();
                    }
                    park_drop = 1;
                    Log.v("Park","dest park");
                    break;
                }
            }
            Log.v("DISTANCE",""+park_drop);
            if (marker_pick != null) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

//            Log.v("URL",getDirectionsUrltwoplaces());
                Log.v("Park"," "+(park_drop+park_pick));

                Object[] dataTransfer = new Object[18];
                String url = getDirectionsUrltwoplaces();
                GetPriceData getDirectionsData = new GetPriceData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = price_excel;
                dataTransfer[3] = price_car;
                dataTransfer[4] = price_rickshaw;
                dataTransfer[5] = price_shareCar;
                dataTransfer[6] = price_shareRickshaw;
                dataTransfer[7] = marker_drop;
                dataTransfer[8] = cursor;
                dataTransfer[9] = spec_location;
                dataTransfer[10] = spec_package;
                dataTransfer[11] = vehicle_case;
                dataTransfer[12] = Home.this;
                dataTransfer[13] = (park_pick + park_drop);
                dataTransfer[14] = parking_priceshare;
                dataTransfer[15] = parking_pricefull;
                dataTransfer[16] = parking_priceexcel;
                dataTransfer[17] = data;
                getDirectionsData.execute(dataTransfer);

                data.setEn_lat(marker_drop.getPosition().latitude);
                data.setEn_lng(marker_drop.getPosition().longitude);
                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
                findViewById(R.id.layout3).setVisibility(View.GONE);
                findViewById(R.id.layout4).setVisibility(View.GONE);
                findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);
            }

        } else if (requestCode==3 && resultCode==RESULT_OK){
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(4));
            offercode=intent.getStringExtra("offer_code");
            double pr=realprice;
            double disc=(double) Double.parseDouble(intent.getStringExtra("discount"));
            double upto=(double) Double.parseDouble(intent.getStringExtra("upto"));

            double less=(pr*disc)/100;
            if (less>upto)
                less=upto;

            offervalue=(int)less;
            if (pr<less)
                pr=0;
            else
                pr=pr-less;

            final_price.setText("Rs. "+(int)pr);
            offer.setText(intent.getStringExtra("offer"));

        } else if (requestCode==4 && resultCode==RESULT_OK){
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(4));
            if (intent.getStringExtra("currentride").equals("true")){
                finish();
                startActivity(getIntent());
            }

        } else if (requestCode==5 && resultCode==RESULT_OK){
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(4));
            payment.setText(intent.getStringExtra("mode"));
            data.setPaymode(intent.getStringExtra("mode"));

        } else if (requestCode==6 && resultCode==RESULT_OK){
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(4));
//            payment.setText(intent.getStringExtra("mode"));
//            data.setPaymode(intent.getStringExtra("mode"));
            if (intent.getStringExtra("result").equals("1")){
                data.setSt_lat(Double.parseDouble(intent.getStringExtra("lat")));
                data.setSt_lng(Double.parseDouble(intent.getStringExtra("lng")));
                pickup_address.setText(intent.getStringExtra("address"));
                data.setSource(intent.getStringExtra("address"));

                marker_pick.setPosition(new LatLng(Double.parseDouble(intent.getStringExtra("lat")),Double.parseDouble(intent.getStringExtra("lng"))));
                marker_pick.setTitle(intent.getStringExtra("address"));

                pickup=marker_pick.getPosition();
                cur_loc=marker_pick.getPosition();
                SharedPreferences.Editor ed=log_id.edit();
                ed.putString("found","0");
                ed.commit();

                repeatcounter=1;
                if (log_id.getString("driver",null).equals("")) {
                    if (!data.getCancel_charge().equals("0")) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
//                    builder.setMessage("You have pending cancellation charge Rs. "+data.getCancel_charge()+
//                            "\nThe amount will be collected by the driver on trip completion.")
//                            .setCancelable(false)
//                            .setTitle("Pending Charge")
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    i = 0;
//                                    dialogdisplay();
//                                    find_driver.removeAllListeners();
//                                    findViewById(R.id.layout4).setVisibility(View.GONE);
//                                    if (vehicletype == "car" && ridetype == "full")
//                                        prev_ride_case = "car";
//
//                                    data.setCustomer_id(log_id.getString("id", null));
//                                    screen_status = 0;
//                                    findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
//                                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
//                                    if (ridetype.equals("full"))
//                                        finddriver();
//                                    else
//                                        findsharedriver();
//                                }
//                            });
//
//                    //Creating dialog box
//                    AlertDialog alert = builder.create();
//                    //Setting the title manually
//                    alert.show();
//                    alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));

                        title.setText("Pending Charge !");
                        message.setText("You have pending cancellation charge Rs. " + data.getCancel_charge() +
                                "\nThe amount will be collected by the driver on trip completion.");
                        left.setVisibility(View.GONE);
                        right.setText("Ok");

                        alert.show();

                        right.setOnClickListener(null);
                        right.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                i = 0;
                                dialogdisplay();
                                find_driver.removeAllListeners();
                                findViewById(R.id.layout4).setVisibility(View.GONE);
                                if (vehicletype == "car" && ridetype == "full")
                                    prev_ride_case = "car";

                                data.setCustomer_id(log_id.getString("id", null));
                                screen_status = 0;
                                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                                findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                                alert.dismiss();
                                if (ridetype.equals("full"))
                                    finddriver();
                                else
                                    findsharedriver();
                            }
                        });
                    } else {
                        i = 0;
                        dialogdisplay();
                        find_driver.removeAllListeners();
                        findViewById(R.id.layout4).setVisibility(View.GONE);
                        if (vehicletype == "car" && ridetype == "full")
                            prev_ride_case = "car";

                        data.setCustomer_id(log_id.getString("id", null));
                        screen_status = 0;
                        findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                        if (ridetype.equals("full"))
                            finddriver();
                        else
                            findsharedriver();
                    }
                }
                else {
                    title.setText("Already on Trip !");
                    message.setText("You are already on trip. Please try again after its completion.");
                    left.setVisibility(View.GONE);
                    right.setText("Ok");

                    alert.show();

                    right.setOnClickListener(null);
                    right.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alert.dismiss();
                        }
                    });
                }
            }
        }
    }

    public void ratingbarclick(View v){
        if ( findViewById(R.id.location_card_view).isEnabled()) {
            Intent intent = new Intent(Home.this, RatingActivity.class);
            intent.putExtra("rating", 0);
            startActivity(intent);
        }
    }

    public void closelocationsave(View view){
        findViewById(R.id.location_card_view).setVisibility(View.GONE);
        findViewById(R.id.rating_bar).setVisibility(View.GONE);
        lastride.removeValue();
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

        findViewById(R.id.layoutseat).setVisibility(View.GONE);
        findViewById(R.id.layoutpaymode).getLayoutParams().width= LinearLayout.LayoutParams.MATCH_PARENT;
    }

    public void seatshare(){
        findViewById(R.id.layoutseat).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutpaymode).getLayoutParams().width= LinearLayout.LayoutParams.MATCH_PARENT/2;
        seats_list.clear();
        seats_list.add("1 seat");
        seats_list.add("2 seat");

        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
        adapter1.setDropDownViewResource(R.layout.payment_list);
        seats.setAdapter(adapter1);
    }

    public void getTime(){
        handler_time = new Handler();
        runnable = new Runnable() {
            public void run() {
                StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
                googleDirectionsUrl.append("origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude);
                googleDirectionsUrl.append("&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude);
                googleDirectionsUrl.append("&key="+"AIzaSyAexys7sg7A0OSyEk1uBmryDXFzCmY0068");

                Object[] dataTransfer = new Object[9];
                String url = googleDirectionsUrl.toString();
                GetDuration getDirectionsData = new GetDuration();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = marker_pick;
                getDirectionsData.execute(dataTransfer);

                handler_time.postDelayed(runnable,30000);
            }
        };
    }

    public void hide_share_vehicles(){
//        findViewById(R.id.rickshaw).setVisibility(View.GONE);
//        findViewById(R.id.shareRickshaw).setVisibility(View.GONE);
//        findViewById(R.id.shareCar).setVisibility(View.GONE);
//        findViewById(R.id.sanjha).setVisibility(View.GONE);

//        findViewById(R.id.rickshaw).setBackgroundResource(R.drawable.round_button_disabled);
//        findViewById(R.id.rickshaw).setClickable(false);
//        findViewById(R.id.shareRickshaw).setBackgroundResource(R.drawable.round_button_disabled);
//        findViewById(R.id.shareRickshaw).setClickable(false);
//        findViewById(R.id.shareCar).setBackgroundResource(R.drawable.round_button_disabled);
        findViewById(R.id.shareCar).setAlpha((float)0.7);
        findViewById(R.id.shareCar).setClickable(false);
    }

    public void show_share_vehicles(){
//        findViewById(R.id.rickshaw).setVisibility(View.VISIBLE);
//        findViewById(R.id.shareRickshaw).setVisibility(View.VISIBLE);
//        findViewById(R.id.shareCar).setVisibility(View.VISIBLE);
//        findViewById(R.id.sanjha).setVisibility(View.VISIBLE);

//        findViewById(R.id.rickshaw).setBackgroundResource(R.drawable.round_button);
//        findViewById(R.id.rickshaw).setClickable(true);
//        findViewById(R.id.shareRickshaw).setBackgroundResource(R.drawable.round_button);
//        findViewById(R.id.shareRickshaw).setClickable(true);
        findViewById(R.id.shareCar).setAlpha((float)1);
        findViewById(R.id.shareCar).setClickable(true);
    }

//    public void checkinternetconnection(){
//        handler_time = new Handler();
//        runnable = new Runnable() {
//            public void run() {
////                Toast.makeText(Home.this, "checking connection...", Toast.LENGTH_SHORT).show();
//                boolean status1 = haveNetworkConnection();
//                boolean status2 = hasActiveInternetConnection();
//
//                if (!status1 || !status2){
////                    AlertDialog.Builder builder = new AlertDialog.Builder(Home.this,R.style.myBackgroundStyle);
////                    builder.setMessage("Turn on your internet connection and try again.")
////                            .setCancelable(false)
////                            .setTitle("No Internet !")
////                            .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
////                                public void onClick(DialogInterface dialog, int id) {
////                                    finish();
////                                    startActivity(getIntent());
////                                }
////                            });
////
////                    //Creating dialog box
////                    AlertDialog alert = builder.create();
////                    //Setting the title manually
////                    alert.show();
////                    alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//
//                    title.setText("No Internet !");
//                    message.setText("Turn on your internet connection and try again.");
//                    left.setVisibility(View.GONE);
//                    right.setText("Try Again");
//
//                    right.setOnClickListener(null);
//                    right.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            finish();
//                            startActivity(getIntent());
//                        }
//                    });
//
//                    alert.show();
//                }
//                else {
//                    handler_time.postDelayed(runnable,15000);
//                }
//            }
//        };
//    }

//    private boolean haveNetworkConnection() {
//        boolean haveConnectedWifi = false;
//        boolean haveConnectedMobile = false;
//
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
//        for (NetworkInfo ni : netInfo) {
//            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
//                if (ni.isConnected())
//                    haveConnectedWifi = true;
//            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
//                if (ni.isConnected())
//                    haveConnectedMobile = true;
//        }
//        return haveConnectedWifi || haveConnectedMobile;
//    }

//    public boolean hasActiveInternetConnection() {
//        // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
//            int exitValue = ipProcess.waitFor();
//            return (exitValue == 0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public void selectpayment(View view) {
        startActivityForResult(new Intent(Home.this,SelectPayment.class),5);
    }

    @Override
    protected void onDestroy() {
        if (this.isFinishing())
            unregisterReceiver(connectivity);
        super.onDestroy();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strAdd;
    }

    //    public static class CheckConnection extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//            if(isConnected){
//                if (network_status!=null)
//                    network_status.setVisibility(View.VISIBLE);
//                    home.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            }
//            else{
//                if (network_status!=null)
//                network_status.setVisibility(View.GONE);
//                home.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            }
//        }
//    }

    public void no_ride_found (){
        DatabaseReference rej_ride=FirebaseDatabase.getInstance().getReference("NotFoundRides");
        HashMap<String,Object> map=new HashMap<>();
        map.put("customer_id",log_id.getString("id",null));
        map.put("source",data.getSource());
        map.put("destination",data.getDestination());
        map.put("time",(new Date()).toString());
        rej_ride.push().setValue(map);
    }
}

