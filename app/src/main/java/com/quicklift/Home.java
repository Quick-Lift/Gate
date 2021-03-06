package com.quicklift;

import android.*;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.graphics.drawable.RotateDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
import com.google.android.gms.maps.model.CameraPosition;
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
    TextView price_bike, price_excel, price_car, price_auto, price_rickshaw, price_shareAuto, price_shareCar, price_shareRickshaw;
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
    SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    int i = 0, found = 0;
    ArrayList<String> driver_list = new ArrayList<String>();
    ArrayList<String> driver_list_rick = new ArrayList<String>();
    ArrayList<String> share_driver_list = new ArrayList<String>();
    SharedPreferences log_id;
    Integer no_of_drivers = 0;
    HorizontalScrollView hsv;
    private ImageView final_image;
    private TextView final_price;
    SQLQueries sqlQueries;
    float radius = 3,rickradius=2;
    boolean driverfound = false;
    String driverid, arrival_time;
    private TextView name, otp;
    private TextView phone;
    private CircleImageView image;
    Integer screen_status = 0;
    ValueEventListener resplistener;
    DatabaseReference lastride;
    SharedPreferences.Editor editor;
    TextView time_bike, time_excel, time_car, time_auto, time_rickshaw, time_shareAuto, time_shareCar, time_shareRickshaw, final_time;
    boolean doubleBackToExitPressedOnce = false;
    DrawerLayout drawer;
    TextView seats;
    TextView offer;
    String offercode = "";
    Integer offervalue = 0, realprice = 0, offerdiscount = 0;
    List<Marker> driver_markers = new ArrayList<>();
    List<Marker> driver_markers_rick = new ArrayList<>();
    private LatLng pick_loc;
    int show = 0;
    String ridetype = null, vehicletype = "car", prev_ride_case = "";
    TextView findingridemsg, ridedetails;
    Dialog dialog;
    int vehicle_case = 0;
    boolean isrunning = false;
    Handler handler_time;
    Handler handle_display = new Handler();
    Runnable runnable, disp_detail;
    View view;
    TextView title, message;
    Button left, right;
    AlertDialog alert;
    GeoQueryEventListener gqel = null;
    int original_seats = 1;
    Handler handle = new Handler();
    Runnable find_runnable;
    Handler sharehandle = new Handler();
    Runnable sharerunnable;
    static TextView network_status;
    static Activity home = null;
    int repeatcounter = 0;
    int vehcasepick = 0, vehcasedrop = 0;
    int park_pick = 0, park_drop = 0;
    String parking_priceshare = "0", parking_pricefull = "0", parking_priceexcel = "0", parking_pricerickshaw = "0";
    CheckConnectivity connectivity = new CheckConnectivity();
    private int total=0;
    private ProgressBar bar;
    EditText promocode;
    ArrayList<String> select_offers=new ArrayList<>();
    ArrayList<String> select_discount=new ArrayList<>();
    ArrayList<String> select_upto=new ArrayList<>();
    ArrayList<String> select_offers_code=new ArrayList<>();
    ListView list;
    private DatabaseReference customer_offers;
    int offer_click=0;
    int seats_click=0;
    View conf_loc=null;
    int change_dest=0;
    int offer1=0,offer2=0;
    boolean specoffer=false,specpcode=false;
    int check_list=0;
    float v;
    String booking_type="local";
    static public boolean rickshawnotify=true;
    int dialogtime = 0;

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
                offercode = "";
                offervalue = 0;
                offerdiscount = 0;
                original_seats = 1;
                offer_click=0;
                data.setOffer_value("0");
                promocode.setText("");
                oneseat=0;
                twoseat=0;
                offer1=0;
                offer2=0;
                data.setOffer_type("");
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

        home = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        editor = log_id.edit();
        sqlQueries = new SQLQueries(this);

        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Searching for driver...");
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(true);

        view = getLayoutInflater().inflate(R.layout.notification_layout, null);
        title = (TextView) view.findViewById(R.id.title);
        message = (TextView) view.findViewById(R.id.message);
        left = (Button) view.findViewById(R.id.left_btn);
        right = (Button) view.findViewById(R.id.right_btn);

        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setView(view)
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
                } else {
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
        seats = (TextView) findViewById(R.id.seats);
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
        network_status = (TextView) findViewById(R.id.network_status);
        list=(ListView)findViewById(R.id.list_offers);
        promocode=(EditText) findViewById(R.id.promocode);
        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id", null));
        customer_offers= FirebaseDatabase.getInstance().getReference("CustomerOffers/"+log_id.getString("id",null));

        customer_offers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                select_offers.clear();
                select_offers_code.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
//                    Toast.makeText(SelectOffers.this, ""+data.getKey(), Toast.LENGTH_SHORT).show();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Offers");
                    ref.orderByChild("code").equalTo(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot dt : dataSnapshot.getChildren()) {
                                    Map<String, Object> map = (Map<String, Object>) dt.getValue();
                                    String str = "Get " + map.get("discount").toString() + "% off upto \u20B9 " + map.get("upto").toString();
                                    select_offers.add(str);
                                    select_discount.add(map.get("discount").toString());
                                    select_upto.add(map.get("upto").toString());
                                    select_offers_code.add(map.get("code").toString());
                                }
                                list.setVisibility(View.VISIBLE);
                                list.setAdapter(new CustomAdapter());
                            }
                            else {
                                list.setVisibility(View.GONE);
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

        promocode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    list.setVisibility(View.GONE);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                processoffer(select_offers.get(position),select_discount.get(position),select_upto.get(position),select_offers_code.get(position));
            }
        });

        getTime();

        data.setWaitcharge(log_id.getString("waitingcharge", null));
        data.setWaittime(log_id.getString("waittime", null));
        SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
        try {
            if ((dt.parse("11:00").after(dt.parse(dt.format(new Date()))) && dt.parse("08:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("23:59").after(dt.parse(dt.format(new Date()))) && dt.parse("17:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("05:00").after(dt.parse(dt.format(new Date()))) && dt.parse("00:00").before(dt.parse(dt.format(new Date()))))) {
                if (log_id.contains("peaktimeradius"))
                    radius = (float) Float.valueOf(log_id.getString("peaktimeradius", null));
            } else {
                if (log_id.contains("normaltimeradius"))
                    radius = (float) Float.valueOf(log_id.getString("normaltimeradius", null));
//                Toast.makeText(home, ""+log_id.getString("normaltimeradius",null), Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (log_id.contains("erickshawradius"))
            rickradius=(float) Float.valueOf(log_id.getString("erickshawradius", null));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);
        toolbar.setBackgroundColor(0x00ffffff);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        pickup_address.setInputType(0);
        destn_address.setInputType(0);
        destn_address.requestFocus();

        seats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seats_click==0){
                    seats_click=1;
                    findViewById(R.id.layout_seats).setVisibility(View.VISIBLE);
                } else {
                    seats_click=0;
                    findViewById(R.id.layout_seats).setVisibility(View.GONE);
                }
            }
        });

        pickup_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickup_address.hasFocus()) {
                    if (booking_type.equals("local")) {
                        Intent intent = new Intent(Home.this, PlaceSelector.class);
                        intent.putExtra("type", "pickup");
                        startActivityForResult(intent, 1);
                    } else {
                        Intent intent = new Intent(Home.this, PlaceSelector.class);
                        intent.putExtra("type", "pickup");
                        startActivityForResult(intent, 9);
                    }
                }
            }
        });

        destn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (destn_address.hasFocus()) {
                    if (booking_type.equals("local")) {
                        Intent intent = new Intent(Home.this, PlaceSelector.class);
                        intent.putExtra("type", "destination");
                        startActivityForResult(intent, 2);
                    } else {
                        Intent intent = new Intent(Home.this, PlaceSelector.class);
                        intent.putExtra("type", "destination");
                        startActivityForResult(intent, 10);
                    }
                }
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
                startActivity(new Intent(Home.this, EditProfile.class));
            }
        });
        updatenavbar();

        find_runnable = new Runnable() {
            @Override
            public void run() {
                isrunning = true;
                if (found == 0 && i <= driver_list.size()) {
                    i = i + 1;
                    cust_req.child(log_id.getString("id", null)).removeValue();
                    resp.removeValue();
                    resp.removeEventListener(resplistener);

                    Log.v("TAG", "Find driver by handler" + i);
//                        Toast.makeText(Home.this, "Find driver by handler", Toast.LENGTH_SHORT).show();

                    finddriver();
//                        Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                }
            }
        };
        sharerunnable = new Runnable() {
            @Override
            public void run() {
                if (found == 0 && sharedriver <= share_driver_list.size()) {
                    sharedriver = sharedriver + 1;
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

        disp_detail = new Runnable() {
            @Override
            public void run() {
                found = 1;
                SharedPreferences.Editor ed = log_id.edit();
                ed.putString("found", "1");
                ed.commit();
                for (int val = 0; val < driver_list.size(); val++) {
                    if (driver_list.get(val).equals(driverid)) {
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        Bitmap b = null;
                        if (vehicletype.equals("rickshaw"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rickshawfinal), 100, 60, false);
                        else if (vehicletype.equals("car"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55, 70, false);
                        else if (vehicletype.equals("excel"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55, 70, false);

                        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);
                        MarkerOptions opt = new MarkerOptions()
                                .title("Driver")
                                .icon(icon)
                                .position(new LatLng(driver_markers.get(val).getPosition().latitude, driver_markers.get(val).getPosition().longitude))
                                .snippet("Arriving");
                        marker_drop = mMap.addMarker(opt);
                        break;
                    }
                }
                startService(new Intent(Home.this, NotificationService.class));
                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                SharedPreferences.Editor editor = log_id.edit();
                editor.putString("driver", driverid);
                editor.putString("status", "accepted");
                editor.commit();
                if (dialog.isShowing())
                    dialog.dismiss();

                handler_time.removeCallbacks(runnable);
                handler_time.postDelayed(runnable, 1);
                tracktripstatus();
            }
        };

//        if (!log_id.getString("driver", null).equals("")) {
//            check_status();
//        }
//        else {
        // Toast.makeText(this, log_id.getString("driver", null), Toast.LENGTH_SHORT).show();
        lastride = FirebaseDatabase.getInstance().getReference("LastRide/" + log_id.getString("id", null));
        lastride.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("status").toString().equals("")) {
                        displayrideinfo(map);
                    } else if (map.get("status").toString().equals("rated")) {
                        //displayrideinfo(map);
                        if (map.containsKey("destination") && map.containsKey("lat") && map.containsKey("lng")) {
                            locationinfo(map);
                            findViewById(R.id.ride_card_view).setVisibility(View.GONE);
                        }
                        else {
                            lastride.removeValue();
                        }
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

//        if (!log_id.getString("driver", null).equals("")) {
//
//        }
//        show_ride_current_details();

        final DatabaseReference dataref = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id", null));
        dataref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("resp").getValue().toString().equals("Trip Ended")){
                        if (check_list==0) {
                            startActivity(new Intent(Home.this, WelcomeScreen.class));
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dataref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("resp").getValue().toString().equals("Accept") ||
                            dataSnapshot.child("resp").getValue().toString().equals("Located")||
                            dataSnapshot.child("resp").getValue().toString().equals("Waiting")) {
                        driverid=dataSnapshot.child("driver").getValue().toString();
                        editor.putString("show", "ride");
                        editor.putString("driver", driverid);
                        editor.commit();

                        show_ride_current_details();
                    }
                    else if (dataSnapshot.child("resp").getValue().toString().equals("Trip Started") && log_id.contains("show") && log_id.getString("show",null).equals("ride")){
                        driverid=dataSnapshot.child("driver").getValue().toString();
                        editor.putString("show", "ride");
                        editor.putString("driver", driverid);
                        editor.commit();

                        show_ride_current_details();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        show_ride_current_details();
//        data.setCancel_charge("0");
        DatabaseReference dref = FirebaseDatabase.getInstance().getReference("CustomerPendingCharges/" + log_id.getString("id", null));
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int val = 0;
                    if (dataSnapshot.hasChild("cancel_req/excel")) {
                        val = val + (Integer.parseInt(dataSnapshot.child("cancel_req/excel").getValue().toString()) * Integer.parseInt(log_id.getString("excelcharge", null)));
                    }
                    if (dataSnapshot.hasChild("cancel_req/full")) {
                        val = val + (Integer.parseInt(dataSnapshot.child("cancel_req/full").getValue().toString()) * Integer.parseInt(log_id.getString("fullcharge", null)));
                    }
                    if (dataSnapshot.hasChild("cancel_req/share")) {
                        val = val + (Integer.parseInt(dataSnapshot.child("cancel_req/share").getValue().toString()) * Integer.parseInt(log_id.getString("sharecharge", null)));
                    }
                    data.setCancel_charge(String.valueOf(val));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference erck=FirebaseDatabase.getInstance().getReference("Fare/Patna/ERickshaw");
        erck.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.getValue().toString().equals("true")){
                        rickshawnotify=false;
                    } else {
                        rickshawnotify=true;
                    }
                }
                if (receiveloc==1)
                    showanddisablevehicles();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destn_address.setVisibility(View.VISIBLE);
                pickup_address.setVisibility(View.VISIBLE);
                data.setRequest_time(sdf.format(new Date()));

                SharedPreferences.Editor ed=log_id.edit();
                ed.putString("found","0");
                ed.commit();

                repeatcounter=1;
                if (log_id.getString("driver",null).equals("")) {
                    if (!data.getCancel_charge().equals("0")) {
                        title.setText("Pending Charge !");
                        message.setText("You have pending cancellation charge \u20B9 " + data.getCancel_charge() +
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
        });

        offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivityForResult(new Intent(Home.this, SelectOffers.class), 3);
                list.setVisibility(View.VISIBLE);
                if(offer_click==0) {
                    findViewById(R.id.layout_offer).setVisibility(View.VISIBLE);
                    offer_click=1;
                }
                else {
                    findViewById(R.id.layout_offer).setVisibility(View.GONE);
                    offer_click=0;
                }
            }
        });
    }

    private void show_ride_current_details() {
//        editor.putString("show","ride");
//        editor.commit();
//        Toast.makeText(home, ""+log_id.getString("driver", null), Toast.LENGTH_SHORT).show();
        cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
        cust_req.child(log_id.getString("id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
//                    Toast.makeText(home, "hi", Toast.LENGTH_SHORT).show();
                    editor.putString("driver", "");
                    editor.remove("status");
//                    editor.remove("show");
                    editor.commit();
                } else if (dataSnapshot.exists() && log_id.contains("show") && dataSnapshot.getChildrenCount()>10) {
//                    Toast.makeText(home, "hi2", Toast.LENGTH_SHORT).show();
                    show = 1;
                    Log.v("CHILDRENCOUNT", "" + dataSnapshot.getChildrenCount());
                    driverid = log_id.getString("driver", null);

                    String str=dataSnapshot.child("veh_type").getValue(String.class);
                    if (str.equals("full") || str.equals("share"))
                        vehicletype="car";
                    else if (str.equals("full"))
                        vehicletype="excel";
                    else if (str.equals("rickshawfull"))
                        vehicletype="rickshaw";

                    otp.setText("OTP\n" + dataSnapshot.child("otp").getValue(String.class));
                    destn_address.setText(dataSnapshot.child("destination").getValue(String.class));
                    pickup_address.setText(dataSnapshot.child("source").getValue(String.class));
                    final_price.setText("\u20B9 " + dataSnapshot.child("price").getValue(String.class));
                    pick_loc = new LatLng(dataSnapshot.child("st_lat").getValue(Double.class), dataSnapshot.child("st_lng").getValue(Double.class));
                    editor.remove("show");
                    editor.putString("amount", String.valueOf((int) (Float.parseFloat(dataSnapshot.child("price").getValue(String.class)
                            + Float.parseFloat(dataSnapshot.child("cancel_charge").getValue(String.class))))));
                    editor.commit();
                    check_status();
                } else if (dataSnapshot.exists() && log_id.contains("show") && dataSnapshot.getChildrenCount()<10) {
//                    Toast.makeText(home, "hi2", Toast.LENGTH_SHORT).show();
                    cust_req.child(log_id.getString("id", null)).removeValue();
                    DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
                    dref.removeValue();
                    finish();
                    startActivity(getIntent());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    Handler dialoghandler=new Handler();
    Runnable dialogrunnable=null;

    public void dialogdisplay() {
        findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
        View view = getLayoutInflater().inflate(R.layout.waitingdialog, null);

        goToLocationZoom(marker_pick.getPosition().latitude, marker_pick.getPosition().longitude, 15);

//        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog = new Dialog(this);
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

        final int[] count = {250};
        final int[] times = {0};

        final ProgressBar pbar=(ProgressBar)view.findViewById(R.id.pbar);
        dialogrunnable=new Runnable() {
            @Override
            public void run() {
                count[0] = count[0] +10;
                RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(count[0], count[0]);
                pbar.setLayoutParams(layoutParams);
                times[0]=times[0]+1;
                if (times[0] < ((int)(dialogtime/2))+4)
                    dialoghandler.postDelayed(dialogrunnable,2000);
            }
        };
        dialoghandler.postDelayed(dialogrunnable,10);

        total=0;
        bar=(ProgressBar)view.findViewById(R.id.progress);
        bar.setProgress(total);
        int oneMin= 70 * 1000; // 1 minute in milli seconds

        /** CountDownTimer starts with 1 minutes and every onTick is 1 second */
        CountDownTimer cdt = new CountDownTimer(oneMin, 100) {
            public void onTick(long millisUntilFinished) {
                float tot=100/dialogtime;
                total=(int)((float)total+tot);
                bar.setProgress(total);
            }
            public void onFinish() {
                // DO something when 1 minute is up
            }
        }.start();
    }

    private void findsharedriver() {
        sharedriver=0;
        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        data.setOtp(id);
//        data.setParking_price(parking_price);
        otp.setText("OTP\n" + id);
        editor.putString("otp", id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(2));
        data.setOffer_upto(String.valueOf(offervalue));
        data.setOffer_disc(String.valueOf(offerdiscount));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        data.setSeat(seats.getText().toString().substring(0, 1));

        final DatabaseReference share = FirebaseDatabase.getInstance().getReference("Share");
        ShareClass shareClass = new ShareClass();
        shareClass.setSt_lat(marker_pick.getPosition().latitude);
        shareClass.setSt_lng(marker_pick.getPosition().longitude);
        shareClass.setEn_lat(marker_drop.getPosition().latitude);
        shareClass.setEn_lng(marker_drop.getPosition().longitude);
        shareClass.setSeats(seats.getText().toString().substring(0, 1));
        share.child(log_id.getString("id", null)).setValue(shareClass);

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {

                share.child(log_id.getString("id", null) + "/drivers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        share_driver_list.clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            share_driver_list.add(data.getKey());
                        }
                        sharedriver = 0;
                        share.child(log_id.getString("id", null)).removeValue();
                        sendsharerequest();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }, 20000);
    }

    int sharedriver = 0;

    public void sendsharerequest() {
        screen_status = 1;
        if (found == 1) {
//            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing())
                dialog.dismiss();
        } else if (found == 0 && sharedriver < share_driver_list.size()) {
//            Toast.makeText(Home.this, "hello "+String.valueOf(sharedriver)+" "+share_driver_list.get(sharedriver), Toast.LENGTH_SHORT).show();
            DatabaseReference dref = null;
            if (vehicletype.equals("car"))
                dref = FirebaseDatabase.getInstance().getReference("DriversWorking/Car/" + share_driver_list.get(sharedriver) + "/seat");
            else if (vehicletype.equals("rickshaw"))
                dref = FirebaseDatabase.getInstance().getReference("DriversWorking/Rickshaw/" + share_driver_list.get(sharedriver) + "/seat");

            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
//                        Toast.makeText(Home.this, "hello", Toast.LENGTH_SHORT).show();
                        String seat = dataSnapshot.getValue(String.class);
                        if (!seat.equals("full") && (Integer.parseInt(seat) + Integer.parseInt(seats.getText().toString().substring(0, 1))) <= 4) {
                            cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + share_driver_list.get(sharedriver));
                            cust_req.child(log_id.getString("id", null)).setValue(data);
                            found = 0;
                            driverid = share_driver_list.get(sharedriver);
                            resp.child("resp").setValue("Waiting");
                            resp.child("driver").setValue(driverid);
                            editor.putString("driver",driverid);
                            editor.commit();
//                            sharehandle.postDelayed(sharerunnable,60000);
//                            response();
                            found = 1;
                            response();
                        } else {
                            sharedriver++;
                            sharehandle.removeCallbacks(sharerunnable);
                            sendsharerequest();
                        }
                    } else {
                        sharedriver++;
                        sharehandle.removeCallbacks(sharerunnable);
                        sendsharerequest();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (found != 1 && sharedriver >= share_driver_list.size()) {
            if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                repeatsearching();
            } else {
                DatabaseReference share = FirebaseDatabase.getInstance().getReference("Share");
                share.child(log_id.getString("id", null)).removeValue();
                finddriver();
            }
        }
    }

    private void locationinfo(final Map<String, Object> map) {
        ((TextView) findViewById(R.id.ride_location)).setText(map.get("destination").toString());
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference save = FirebaseDatabase.getInstance().getReference("SavedLocations/" + log_id.getString("id", null) + "/saved");
                if (TextUtils.isEmpty(((EditText) findViewById(R.id.locname)).getText().toString())) {
                    Toast.makeText(Home.this, "Please provide location name ...", Toast.LENGTH_SHORT).show();
                } else {
                    String key = save.push().getKey();
                    save.child(key + "/name").setValue(((EditText) findViewById(R.id.locname)).getText().toString().toLowerCase());
                    save.child(key + "/locname").setValue(map.get("destination").toString());
                    save.child(key + "/lat").setValue(map.get("lat").toString());
                    save.child(key + "/lng").setValue(map.get("lng").toString());

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
        Log.v("TAG", "hi" + log_id.getString("id", null));
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users/" + log_id.getString("id", null));
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

    private void displayrideinfo(Map<String, Object> map) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers/" + map.get("driver").toString());
        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Intent intent = new Intent(Home.this, RatingActivity.class);
                intent.putExtra("rating", rating);
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
                    ((TextView) findViewById(R.id.driver_name)).setText("Rate " + map.get("name").toString());
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
        otp.setText("OTP\n" + id);
        editor.putString("otp", id);
        editor.commit();
        data.setPrice(final_price.getText().toString().substring(2));
        data.setOffer_upto(String.valueOf(offervalue));
        data.setOffer_disc(String.valueOf(offerdiscount));
        data.setSource(pickup_address.getText().toString());
        data.setDestination(destn_address.getText().toString());
        if (seats.getText().toString().equals("full"))
            data.setSeat(seats.getText().toString());
        else
            data.setSeat(seats.getText().toString().substring(0, 1));
        //Toast.makeText(this, ""+String.valueOf(i), Toast.LENGTH_SHORT).show();
        if (found == 1) {
//            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing())
                dialog.dismiss();
        } else if (found == 0 && i < driver_list.size()) {
//            Log.v("TAG","CD"+i+" "+driver_list.size());
//            Toast.makeText(this, "close driver", Toast.LENGTH_SHORT).show();
            getClosestDriver();
        } else {
            if (vehicletype == "car" && ridetype == "full" && prev_ride_case == "car") {
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
                Handler hd = new Handler();
                hd.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Handler hd = new Handler();
                        hd.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                                    repeatsearching();
                                } else {
                                    if (dialog.isShowing())
                                        dialog.dismiss();
                                    findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                                    findViewById(R.id.local).setVisibility(View.VISIBLE);
                                    ((TextView) findViewById(R.id.msg_text)).setText("Sorry we are facing congestion ! Please try again later ! ");
                                    pickup_address.setEnabled(false);
                                    destn_address.setEnabled(false);
                                    no_ride_found();
                                    findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            screen_status = 1;
                                            findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                                            findViewById(R.id.layout3).setVisibility(View.GONE);
                                            findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
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
                                            pickup_address.setVisibility(View.VISIBLE);
                                            destn_address.setVisibility(View.VISIBLE);
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
//                Log.v("TAG", "else " + String.valueOf(isrunning));
//                if (isrunning) {
//                    isrunning = false;
//                    Toast.makeText(this, "else condition", Toast.LENGTH_SHORT).show();
                Handler hd = new Handler();
                hd.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (repeatcounter < Integer.parseInt(log_id.getString("searchingtime", null))) {
                            repeatsearching();
                        } else {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                            findViewById(R.id.local).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.msg_text)).setText("Sorry we are facing congestion ! Please try again later ! ");
                            pickup_address.setEnabled(false);
                            destn_address.setEnabled(false);
                            no_ride_found();
                            findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    screen_status = 1;
                                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                                    findViewById(R.id.layout3).setVisibility(View.GONE);
                                    findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
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
                                    pickup_address.setVisibility(View.VISIBLE);
                                    destn_address.setVisibility(View.VISIBLE);
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
        i = 0;
        sharedriver = 0;
        place_drivers();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                find_driver.removeAllListeners();
                if (ridetype.equals("full"))
                    finddriver();
                else
                    findsharedriver();
            }
        }, 6000);
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
                screen_status = 1;
                seatfull();
                vehicletype = "bike";
                final_price.setText(price_bike.getText());
//                final_time.setText(time_bike.getText());
                final_image.setImageResource(R.drawable.bike1);
//                seats.setText("1 seat");
                original_seats=1;
                seats.setEnabled(false);
                ridetype = "full";
                realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                place_drivers();

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                findViewById(R.id.layout_offer).setVisibility(View.GONE);
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.car)) {
//                if (car!=0) {
                    dialogtime=57;
                    findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                    screen_status = 1;
                    seatfull();
                    vehicletype = "car";
                    data.setVeh_type("full");
//                if (log_id.contains("parkfull"))
//                    data.setParking_price(log_id.getString("parkfull",null));
                    final_price.setText(price_car.getText());
//                final_time.setText(time_car.getText());
                    final_image.setImageResource(R.drawable.niji);
//                seats.setText("1 seat");
                    original_seats = 1;
                    seats.setEnabled(false);
                    ridetype = "full";
                    realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                    place_drivers();
                    ridedetails.setText("Van includes all types of hatchback, compact sedan and sedan vehicles with capacity of 4 seats.");

                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                    findViewById(R.id.layout_offer).setVisibility(View.GONE);
                    ((TextView) findViewById(R.id.type)).setText("Van");
//                }
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.excel)) {
//                if (excel!=0) {
                    dialogtime=57;
                    findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                    screen_status = 1;
                    seatfull();
                    vehicletype = "excel";
//                if (log_id.contains("parkexcel"))
//                    data.setParking_price(log_id.getString("parkexcel",null));
                    data.setVeh_type("excel");
                    final_price.setText(price_excel.getText());
//                final_time.setText(time_excel.getText());
                    final_image.setImageResource(R.drawable.carfinal);
//                seats.setText("1 seat");
                    original_seats = 1;
                    seats.setEnabled(false);
                    ridetype = "full";
                    realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                    place_drivers();
                    ridedetails.setText("SUV includes all large vehicles with 6 and 7 seats capacity.");

                    ((TextView) findViewById(R.id.type)).setText("SUV");
                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                    findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                    findViewById(R.id.layout_offer).setVisibility(View.GONE);
//                }
//                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.auto)) {

            } else if (v == findViewById(R.id.rickshaw)) {
                if (rickshawnotify) {
                    findViewById(R.id.layout_offer).setVisibility(View.GONE);
                    comingsoonnotification();
                }
                else {
//                    if (rickshaw!=0) {
                        dialogtime=57;
                        findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                        screen_status = 1;
                        seatfull();
                        vehicletype = "rickshaw";
                        data.setVeh_type("rickshawfull");
                        // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
                        final_price.setText(price_rickshaw.getText());
                        //                final_time.setText(time_rickshaw.getText());
                        final_image.setImageResource(R.drawable.rickshawfinal);
                        original_seats=1;
                        seats.setEnabled(false);
                        ridetype = "full";
                        realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                        place_drivers();
                        ridedetails.setText("This option is for booking rickshaw. It allows you to book full rickshaw for yourself.");

                        ((TextView) findViewById(R.id.type)).setText("E-car");
                        findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                        findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                        findViewById(R.id.layout_offer).setVisibility(View.GONE);
//                    }
                }
            } else if (v == findViewById(R.id.shareCar)) {
                dialogtime=65;
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                screen_status = 1;
                seatshare();
                vehicletype = "car";
//                if (log_id.contains("parkshare"))
//                    data.setParking_price(log_id.getString("parkshare",null));
                data.setVeh_type("share");
                final_price.setText(price_shareCar.getText());
//                final_time.setText(time_shareCar.getText());
                final_image.setImageResource(R.drawable.share);
//                seats.setText("1 seat");
                original_seats=1;
                seats.setEnabled(true);
                ridetype = "share";
                realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                place_drivers();
                ridedetails.setText("Pool provides the facility to book the cab on sharing basis with maximum of 2 seats.");

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                findViewById(R.id.layout_offer).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.type)).setText("Pool");
                //                remove_drivers_auto();
//                remove_drivers_bike();
//                remove_drivers_rickshaw();
            } else if (v == findViewById(R.id.shareAuto)) {
                screen_status = 1;
                seatshare();
                vehicletype = "auto";
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_shareAuto.getText());
//                final_time.setText(time_shareAuto.getText());
                final_image.setImageResource(R.drawable.erickshaw1);
//                seats.setText("1 seat");
                original_seats=1;
                seats.setEnabled(true);
                ridetype = "share";
                realprice = (int) Double.parseDouble(final_price.getText().toString().substring(2));
                place_drivers();

                findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
                findViewById(R.id.layout_offer).setVisibility(View.GONE);
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
//                realprice=(int)Double.parseDouble(final_price.getText().toString().substring(2));
//                place_drivers();
//                ridedetails.setText("This option is for booking share rickshaw. It allows you to book only required number of seats for yourself.");
//                remove_drivers_auto();
//                remove_drivers_car();
//                remove_drivers_bike();
                comingsoonnotification();
                findViewById(R.id.layout_offer).setVisibility(View.GONE);
            }
        }
    }

    private void comingsoonnotification() {
        findViewById(R.id.comingsoon).setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.comingsoon).setVisibility(View.GONE);
            }
        }, 2000);
    }

    DatabaseReference cust_req;
    DatabaseReference resp;

    private void getClosestDriver() {
        if (i < driver_list.size()) {
            Log.v("TAG", "Closest driver" + i);
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
                        resp.child("resp").setValue("Waiting");
                        resp.child("driver").setValue(driverid);
                        editor.putString("driver",driverid);
                        editor.commit();
                    } else {
                        i = i + 1;
                        Log.v("TAG", "Find driver by customer req");
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

    private void check_status() {
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        driver_list.clear();
        if (find_driver!=null)
            find_driver.removeAllListeners();
        for (int k = 0; k < driver_markers.size(); k++) {
            driver_markers.get(k).remove();
        }
        driver_markers.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/" + driverid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();
                    ((TextView) findViewById(R.id.bike_name)).setText(drive.get("model").toString());
                    ((TextView) findViewById(R.id.bike_no)).setText(drive.get("number").toString());
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
                findViewById(R.id.layout3).setVisibility(View.VISIBLE);
                if (vehicletype.equals("rickshaw"))
                    ((ImageView)findViewById(R.id.vehtype)).setImageResource(R.drawable.rickshawfinal);
                final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

                //Glide.with(Home.this).load(drive.get("thumb").toString()).into((CircleImageView)findViewById(R.id.pic));
//                ((TextView)findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
//                ((TextView)findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                int iend = drive.get("name").toString().indexOf(" ");
                if (iend != -1)
                    ((TextView) findViewById(R.id.dname)).setText(drive.get("name").toString().substring(0, iend));
                else
                    ((TextView) findViewById(R.id.dname)).setText(drive.get("name").toString());
//                if (drive.containsKey("rate"))
//                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(Float.valueOf(drive.get("rate").toString()));
//                else
//                    ((RatingBar)findViewById(R.id.driver_rating)).setRating(0);
                if (drive.containsKey("rate"))
                    ((TextView) findViewById(R.id.rating_value)).setText(String.format("%.1f", Float.parseFloat(drive.get("rate").toString())));
                else
                    ((TextView) findViewById(R.id.rating_value)).setText("5.0");
                ((TextView) findViewById(R.id.amount)).setText(final_price.getText().toString());

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
                ((Button) findViewById(R.id.call_text)).setOnClickListener(new View.OnClickListener() {
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
                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.rating_bar).setVisibility(View.GONE);
                //SharedPreferences.Editor editor=log_id.edit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        resplistener = resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    check_list=1;
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        startService(new Intent(Home.this, NotificationService.class));
                        editor.putString("status", "accepted");
                        editor.remove("offer");
                        if (!offercode.equals(""))
                            editor.putString("offer", offercode);
                        editor.commit();

//                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        tracktripstatus();
                        handler_time.removeCallbacks(runnable);
                        handler_time.postDelayed(runnable, 1000);
                        //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
                        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();
                    } else if (map.get("resp").toString().equals("Located")) {
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status", "located");
                        editor.commit();
//                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    } else if (map.get("resp").toString().equals("Trip Started")) {
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.layout_ride_status).setVisibility(View.VISIBLE);
//                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = log_id.edit();
                        editor.putString("status", "started");
                        editor.commit();

                        mMap.clear();
                        if (marker_pick != null) {
                            marker_pick.remove();
                        }
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        tracktripstatus();
                    } else if (map.get("resp").toString().equals("Trip Ended")) {
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
                        editor.putString("status", "ended");
                        editor.commit();
//                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Home.this, TripCompleted.class);
                        intent.putExtra("id", log_id.getString("driver", null));
                        finish();
                        resp.removeEventListener(resplistener);
                    } else if (map.get("resp").toString().equals("Cancel")) {
                        SharedPreferences.Editor ed = log_id.edit();
                        ed.putString("found", "0");
                        ed.commit();
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                        findViewById(R.id.local).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                        findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancel_current_trip();
                            }
                        });
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void response() {
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

        resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("id", null));
        resplistener = resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    check_list=1;
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        SharedPreferences.Editor ed = log_id.edit();
                        ed.putString("found", "1");
                        ed.commit();
                        found = 1;
                        handle_display.removeCallbacks(disp_detail);
                        handle_display.postDelayed(disp_detail, 10);
                    } else if (map.get("resp").toString().equals("Located")) {
                        findViewById(R.id.waiting).setVisibility(View.VISIBLE);
                        editor.putString("status", "located");
                        editor.commit();
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
//                        Toast.makeText(Home.this, "Driver is waiting at the pickup location !", Toast.LENGTH_LONG).show();
                    } else if (map.get("resp").toString().equals("Trip Started")) {
                        findViewById(R.id.waiting).setVisibility(View.GONE);
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.layout_ride_status).setVisibility(View.VISIBLE);
//                        Toast.makeText(Home.this, "Trip Started !", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = log_id.edit();
                        editor.putString("status", "started");
                        editor.commit();

                        mMap.clear();
                        if (marker_pick != null) {
                            marker_pick.remove();
                        }
                        if (marker_drop != null) {
                            marker_drop.remove();
                        }
                        tracktripstatus();
                    } else if (map.get("resp").toString().equals("Trip Ended")) {
//                        stopService(new Intent(Home.this,NotificationService.class));
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();
//                        Toast.makeText(Home.this, "Trip Ended !", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Home.this, TripCompleted.class);
                        intent.putExtra("id", log_id.getString("driver", null));
                        startActivity(intent);
//                        stopService(new Intent(Home.this, NotificationService.class));
                        finish();
                        resp.removeValue();
                        resp.removeEventListener(resplistener);
                    } else if (map.get("resp").toString().equals("Reject")) {
                        SharedPreferences.Editor ed = log_id.edit();
                        ed.putString("found", "0");
                        ed.commit();
                        if (found == 0) {
                            handle.removeCallbacks(find_runnable);
                            handler_time.removeCallbacks(runnable);
                            if (marker_pick != null)
                                marker_pick.hideInfoWindow();
                            handle_display.removeCallbacks(disp_detail);
                            driverid = "";
                            i = i + 1;
                            cust_req.child(log_id.getString("id", null)).removeValue();
                            resp.removeValue();
                            resp.removeEventListener(resplistener);
                            finddriver();
                        } else {
                            if (dialog.isShowing())
                                dialog.dismiss();
                            handle_display.removeCallbacks(disp_detail);
                            handler_time.removeCallbacks(runnable);
                            if (marker_pick != null)
                                marker_pick.hideInfoWindow();
                            findViewById(R.id.layout3).setVisibility(View.GONE);
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                            findViewById(R.id.local).setVisibility(View.VISIBLE);
                            ((TextView) findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                            findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancel_current_trip();
                                }
                            });
                            findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                            findViewById(R.id.local).setVisibility(View.VISIBLE);
                        }
                    } else if (map.get("resp").toString().equals("Cancel")) {
                        SharedPreferences.Editor ed = log_id.edit();
                        ed.putString("found", "0");
                        ed.commit();
                        if (dialog.isShowing())
                            dialog.dismiss();
                        handle_display.removeCallbacks(disp_detail);
                        handler_time.removeCallbacks(runnable);
                        if (marker_pick != null)
                            marker_pick.hideInfoWindow();

                        findViewById(R.id.layout3).setVisibility(View.GONE);
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
//                        findViewById(R.id.local).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.msg_text)).setText("Sorry the driver is currently unable to serve you ! Please try again !");

                        findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancel_current_trip();
                            }
                        });
                        findViewById(R.id.no_ride).setVisibility(View.VISIBLE);
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
        cust_req.child(log_id.getString("id", null)).removeValue();
        SharedPreferences.Editor editor = log_id.edit();
        editor.putString("driver", "");
        editor.remove("ride");
        editor.remove("status");
        editor.commit();
        finish();
    }

    private void tracktripstatus() {
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        findViewById(R.id.rating_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
        //ride.setVisibility(View.GONE);
        //Log.v("TAG","enter");
        mMap.clear();
        if (log_id.getString("status", null).equals("accepted")) {
            if (marker_pick != null) {
                marker_pick.remove();
            }

            final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pin_location), 80, 80, false));
            if (show == 1) {
                MarkerOptions opt = new MarkerOptions()
                        .title(pickup_address.getText().toString())
                        .icon(icon)
                        .position(pick_loc)
                        .snippet("Pick Up");
                marker_pick = mMap.addMarker(opt);
            } else {
                MarkerOptions opt = new MarkerOptions()
                        .title(pickup_address.getText().toString())
                        .icon(icon)
                        .position(pickup)
                        .snippet("Pick Up");
                marker_pick = mMap.addMarker(opt);
            }
        } else {
            cust_req.child(log_id.getString("id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (marker_pick != null) {
                            marker_pick.remove();
                        }
                        MarkerOptions opt = new MarkerOptions()
                                .title(dataSnapshot.child("destination").getValue(String.class))
                                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_destination), 80, 80, false)))
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
        if (marker_drop!=null){
            marker_drop.remove();
            LatLng val=marker_drop.getPosition();
            String title=marker_drop.getTitle();
            String snippet=marker_drop.getSnippet();
            Bitmap b = null;
            if (vehicletype.equals("rickshaw"))
                b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rickshawfinal), 100, 60, false);
            else if (vehicletype.equals("car"))
                b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55,70, false);
            else if (vehicletype.equals("excel"))
                b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55,70, false);

            final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);

            if (vehicletype.equals("rickshaw"))
                BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pin_location), 80, 80, false));

            MarkerOptions opt = new MarkerOptions()
                    .title(title)
                    .icon(icon)
                    .position(val)
                    .snippet(snippet);
            marker_drop = mMap.addMarker(opt);
        }

//        updateCamera();

        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
        tripstatus.child("/l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //mMap.clear();
                    ArrayList<Double> map = (ArrayList<Double>) dataSnapshot.getValue();
                    if (Double.valueOf(map.get(0).toString()) != 0 && Double.valueOf(map.get(1).toString()) != 0) {
//                        if (marker_drop != null) {
//                            marker_drop.remove();
//                        }
                        Bitmap b = null;
                        if (vehicletype.equals("rickshaw"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rickshawfinal), 100, 60, false);
                        else if (vehicletype.equals("car"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55,70, false);
                        else if (vehicletype.equals("excel"))
                            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.trackingcar), 55,70, false);

                        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);
//                        final BitmapDescriptor icon=BitmapDescriptorFactory.fromResource(R.drawable.car_route);
                        if (marker_drop==null){
                            MarkerOptions opt = new MarkerOptions()
                                    .title("Driver")
                                    .icon(icon)
                                    .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
                                    .snippet("Driver Location");
                            marker_drop = mMap.addMarker(opt);
                            updateCamera();
                        }

                        final LatLng startPosition;
                        if (marker_drop!=null)
                            startPosition=marker_drop.getPosition();
                        else {
                            marker_drop = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
                                    .flat(true)
                                    .icon(icon));
                            startPosition = new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString()));
                        }
                        final LatLng endPosition=new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString()));
                        final float startRotation = marker_drop.getRotation();
                        final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();

                        if (startPosition.equals(endPosition)){
                            if (marker_drop != null) {
                                marker_drop.remove();
                            }
                            MarkerOptions opt = new MarkerOptions()
                                    .title("Driver")
                                    .icon(icon)
                                    .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
                                    .snippet("Driver Location");
                            marker_drop = mMap.addMarker(opt);
                            updateCamera();
                        }
                        else {
                            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                            valueAnimator.setDuration(8000);
                            valueAnimator.setInterpolator(new LinearInterpolator());
                            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    try {
                                        float v = valueAnimator.getAnimatedFraction();
                                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                                        marker_drop.setPosition(newPosition);
                                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                .target(newPosition)
                                                .zoom(mMap.getCameraPosition().zoom)
                                                .build()));
                                        marker_drop.setRotation(getBearing(startPosition, newPosition));
                                    } catch (Exception ex) {

                                    }
                                }
                            });
                            valueAnimator.start();
                        }
//
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/" + driverid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                    Map<String,Object> drive=(Map<String, Object>) dataSnapshot.getValue();
//                    Log.v("STATUS",drive.get("model").toString());
                    ((TextView) findViewById(R.id.bike_name)).setText(dataSnapshot.child("model").getValue().toString());
                    ((TextView) findViewById(R.id.bike_no)).setText(dataSnapshot.child("number").getValue().toString());
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
                    if (log_id.getString("status", null).equals("started"))
                        findViewById(R.id.layout3).setVisibility(View.GONE);
                    else
                        findViewById(R.id.layout3).setVisibility(View.VISIBLE);

                    if (vehicletype.equals("rickshaw"))
                        ((ImageView)findViewById(R.id.vehtype)).setImageResource(R.drawable.rickshawfinal);

                    final Map<String, Object> drive = (Map<String, Object>) dataSnapshot.getValue();

//                    ((TextView) findViewById(R.id.bike_name)).setText(drive.get("veh_type").toString());
//                    ((TextView) findViewById(R.id.bike_no)).setText(drive.get("veh_num").toString());
                    int iend = drive.get("name").toString().indexOf(" ");
                    if (iend != -1)
                        ((TextView) findViewById(R.id.dname)).setText(drive.get("name").toString().substring(0, iend));
                    else
                        ((TextView) findViewById(R.id.dname)).setText(drive.get("name").toString());
//                    if (drive.containsKey("rate"))
//                        ((RatingBar)findViewById(R.id.driver_rating)).setRating(Float.valueOf(drive.get("rate").toString()));
//                    else
//                        ((RatingBar)findViewById(R.id.driver_rating)).setRating(0);
                    if (drive.containsKey("rate"))
                        ((TextView) findViewById(R.id.rating_value)).setText(String.format("%.1f", Float.parseFloat(drive.get("rate").toString())));
                    else
                        ((TextView) findViewById(R.id.rating_value)).setText("5.0");
                    ((TextView) findViewById(R.id.amount)).setText(final_price.getText().toString());

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
                    ((Button) findViewById(R.id.call_text)).setOnClickListener(new View.OnClickListener() {
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
        startActivityForResult(new Intent(Home.this, CancelReason.class), 8);
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
//
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
        final LocationListener listener = this;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (gpc != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
//                            return ;
                        }
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(gpc, lct, listener);
                }
                return false;
            }
        });
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

    int receiveloc=0;
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
//            Toast.makeText(this, "Cannot get current location !", Toast.LENGTH_SHORT).show();
        } else if (receiveloc==0){
            receiveloc=1;
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
                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false)))
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

                showanddisablevehicles();
//                place_drivers_share();
//            }
        }
    }

    public void place_drivers() {
        driver_list.clear();
        if (find_driver!=null)
            find_driver.removeAllListeners();
        for (int k = 0; k < driver_markers.size(); k++) {
            driver_markers.get(k).remove();
        }
        driver_markers.clear();
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

        Bitmap b=null;
        if (vehicletype.equals("rickshaw"))
           b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rickshawfinal),100,60,false);
        else if (vehicletype.equals("car"))
            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.trackingcar),55,70,false);
        else if (vehicletype.equals("excel"))
            b = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.trackingcar),55,70,false);

        final BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);

        GeoFire geoFire = new GeoFire(ref);
        if (vehicletype.equals("rickshaw"))
            find_driver = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), rickradius);
        else
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

    int rickshaw=0,car=0,excel=0;
    public void showanddisablevehicles(){
        driver_list_rick.clear();
        for (int i=0;i<driver_markers_rick.size();i++){
            Marker mrk = driver_markers_rick.get(i);
            mrk.remove();
            driver_markers_rick.remove(i);
        }
        DatabaseReference refxl=FirebaseDatabase.getInstance().getReference("DriversAvailable/Excel");
        DatabaseReference refcar=FirebaseDatabase.getInstance().getReference("DriversAvailable/Car");
        DatabaseReference refrckw=FirebaseDatabase.getInstance().getReference("DriversAvailable/Rickshaw");

        GeoFire geoFirecar = new GeoFire(refcar);
        GeoFire geoFirexl = new GeoFire(refxl);
        GeoFire geoFirerckw = new GeoFire(refrckw);
        GeoQuery find_drivercar = geoFirecar.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
        GeoQuery find_driverxl = geoFirexl.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
        GeoQuery find_driverrckw = geoFirerckw.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), rickradius);

        find_drivercar.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (car==0)
                    showview(findViewById(R.id.car));
                car++;
            }

            @Override
            public void onKeyExited(String key) {
                car--;
                if (car==0)
                    hideview(findViewById(R.id.car));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//                Toast.makeText(Home.this, ""+driver_list.size(), Toast.LENGTH_SHORT).show();
                hideview(findViewById(R.id.car));
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        find_driverxl.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (excel==0)
                    showview(findViewById(R.id.excel));
                excel++;
            }

            @Override
            public void onKeyExited(String key) {
                excel--;
                if (excel==0)
                    hideview(findViewById(R.id.excel));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//                Toast.makeText(Home.this, ""+driver_list.size(), Toast.LENGTH_SHORT).show();
                hideview(findViewById(R.id.excel));
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        if (!rickshawnotify)
            find_driverrckw.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (rickshaw==0)
                    showview(findViewById(R.id.rickshaw));
                rickshaw++;
                driver_list_rick.add(key);

                MarkerOptions options = new MarkerOptions()
                        .title("Rickshaw")
                        .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rickshawfinal),100,60,false)))
                        .position(new LatLng(location.latitude, location.longitude));

                driver_markers_rick.add(mMap.addMarker(options));
            }

            @Override
            public void onKeyExited(String key) {
                rickshaw--;
                if (rickshaw==0)
                    hideview(findViewById(R.id.rickshaw));
                if (driver_markers_rick.size()!=0) {
    //                    Toast.makeText(Home.this, "" + "exited", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < driver_list_rick.size(); i++) {
                        if (key.equals(driver_list_rick.get(i))) {
                            Marker mrk = driver_markers_rick.get(i);
                            mrk.remove();
                            driver_list_rick.remove(i);
                            driver_markers_rick.remove(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (int i=0;i<driver_list_rick.size();i++){
                    if (key.equals(driver_list_rick.get(i))){
                        MarkerOptions options = new MarkerOptions()
                                .title("Rickshaw")
                                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.rickshawfinal),100,60,false)))
                                .position(new LatLng(location.latitude, location.longitude));

                        Marker mrk=driver_markers_rick.get(i);
                        mrk.remove();
                        driver_markers_rick.set(i,mMap.addMarker(options));
                        break;
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
//                Toast.makeText(Home.this, ""+driver_list.size(), Toast.LENGTH_SHORT).show();
                hideview(findViewById(R.id.rickshaw));
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

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

        prgdlg.dismiss();
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode==1 && resultCode==RESULT_OK){
            change_dest=0;
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);

            if (intent.getStringExtra("case").equals("1")) {
                vehcasepick=1;
                if (vehcasedrop==2) {
                    vehicle_case = 2;
                    hide_share_vehicles();
                }
                else {
                    vehicle_case = 1;
                    show_share_vehicles();
                }
            }
            else {
                vehcasepick=2;
                vehicle_case = 2;
                hide_share_vehicles();
            }

            pickup_address.setText(name);
//            goToLocationZoom(latitude, longitude, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            data.setSt_lat(latitude);
            data.setSt_lng(longitude);
            MarkerOptions options=new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false)))
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
//                Intent intnt = new Intent(Home.this, ConfirmLocation.class);
//                intnt.putExtra("lat", String.valueOf(marker_pick.getPosition().latitude));
//                intnt.putExtra("lng", String.valueOf(marker_pick.getPosition().longitude));
//                intnt.putExtra("address", pickup_address.getText().toString());
//                startActivityForResult(intnt, 6);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                mMap.setPadding(50,400,20,400);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

                Log.v("Park"," "+(park_drop+park_pick));

                Object[] dataTransfer = new Object[25];
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
                dataTransfer[17] = parking_pricerickshaw;
                dataTransfer[18] = data;
                dataTransfer[19] = final_price;
                dataTransfer[20] = null;
                dataTransfer[21] = 0;
                if (rickshawnotify)
                    dataTransfer[22] = 0;
                else
                    dataTransfer[22] = 1;
                dataTransfer[23] = findViewById(R.id.rickshaw);
                getDirectionsData.execute(dataTransfer);

//            place_drivers_share();
                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
                findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);
            }
            else {
                goToLocationZoom(latitude, longitude, 15);
            }
            if (find_driver!=null)
                find_driver.removeAllListeners();
//            find_driver_share.removeAllListeners();
                place_drivers();

        } else if (requestCode==2 && resultCode==RESULT_OK) {
            String name;
            change_dest=0;
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
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_destination),80,80,false)))
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
//                Intent intnt = new Intent(Home.this, ConfirmLocation.class);
//                intnt.putExtra("lat", String.valueOf(marker_pick.getPosition().latitude));
//                intnt.putExtra("lng", String.valueOf(marker_pick.getPosition().longitude));
//                intnt.putExtra("address", pickup_address.getText().toString());
//                startActivityForResult(intnt, 6);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                mMap.setPadding(50,400,20,400);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

//            Log.v("URL",getDirectionsUrltwoplaces());
                Log.v("Park"," "+(park_drop+park_pick));

                Object[] dataTransfer = new Object[25];
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
                dataTransfer[17] = parking_pricerickshaw;
                dataTransfer[18] = data;
                dataTransfer[19] = final_price;
                dataTransfer[20] = null;
                dataTransfer[21] = 0;
                if (rickshawnotify)
                    dataTransfer[22] = 0;
                else
                    dataTransfer[22] = 1;
                dataTransfer[23] = findViewById(R.id.rickshaw);
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
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(2));
            offercode=intent.getStringExtra("offer_code");
            double pr=realprice;
            double disc=(double) Double.parseDouble(intent.getStringExtra("discount"));
            double upto=(double) Double.parseDouble(intent.getStringExtra("upto"));

            double less=(pr*disc)/100;
            if (less>upto)
                less=upto;

            offervalue=(int)upto;
            offerdiscount=(int)disc;
            if (pr<less)
                pr=0;
            else
                pr=pr-less;

            data.setOffer_value(String.valueOf((int)less));
            final_price.setText("\u20B9 "+(int)pr);
            data.setOffer_code(offercode);
            offer.setText(intent.getStringExtra("offer"));

        } else if (requestCode==4 && resultCode==RESULT_OK){
//            Double pr=Double.parseDouble(final_price.getText().toString().substring(2));
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
                change_dest=1;
                if (!pickup_address.getText().toString().equals(intent.getStringExtra("address"))) {
                    data.setSt_lat(Double.parseDouble(intent.getStringExtra("lat")));
                    data.setSt_lng(Double.parseDouble(intent.getStringExtra("lng")));
                    pickup_address.setText(intent.getStringExtra("address"));
                    data.setSource(intent.getStringExtra("address"));

                    marker_pick.setPosition(new LatLng(Double.parseDouble(intent.getStringExtra("lat")), Double.parseDouble(intent.getStringExtra("lng"))));
                    marker_pick.setTitle(intent.getStringExtra("address"));

                    pickup = marker_pick.getPosition();
                    cur_loc = marker_pick.getPosition();
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
//                    if (marker_drop!=null) {
//                Intent intnt = new Intent(Home.this, ConfirmLocation.class);
//                intnt.putExtra("lat", String.valueOf(marker_pick.getPosition().latitude));
//                intnt.putExtra("lng", String.valueOf(marker_pick.getPosition().longitude));
//                intnt.putExtra("address", pickup_address.getText().toString());
//                startActivityForResult(intnt, 6);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(marker_pick.getPosition());
                        builder.include(marker_drop.getPosition());
                        LatLngBounds bounds = builder.build();

                        int padding = 40;
                        mMap.setPadding(50,400,20,400);
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);
                        mMap.animateCamera(cu);

                        Log.v("Park"," "+(park_drop+park_pick));

                        Object[] dataTransfer = new Object[25];
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
                        dataTransfer[17] = parking_pricerickshaw;
                        dataTransfer[18] = data;
                        dataTransfer[19] = final_price;
                        if (conf_loc==findViewById(R.id.car))
                            dataTransfer[20] = "car";
                        else if (conf_loc==findViewById(R.id.excel))
                            dataTransfer[20] = "excel";
                        else if (conf_loc==findViewById(R.id.shareCar))
                            dataTransfer[20] = "sharecar";
                        else if (conf_loc==findViewById(R.id.rickshaw))
                            dataTransfer[20] = "rickshaw";
                        else
                            dataTransfer[20] = null;
                        dataTransfer[21] = realprice;
                        if (rickshawnotify)
                            dataTransfer[22] = 0;
                        else
                            dataTransfer[22] = 1;
                        dataTransfer[23] = findViewById(R.id.rickshaw);
                        getDirectionsData.execute(dataTransfer);

//            place_drivers_share();
                        getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
//                    }
//                    else {
//                        goToLocationZoom(latitude, longitude, 15);
//                    }
                    if (find_driver!=null)
                        find_driver.removeAllListeners();
//            find_driver_share.removeAllListeners();
                    place_drivers();

                    select_vehicle(conf_loc);
                }
                else {
                    select_vehicle(conf_loc);
                }

            }
        }
        else if (requestCode==8 && resultCode==RESULT_OK){
            final ProgressDialog progress=new ProgressDialog(Home.this);
            progress.setMessage("Cancelling Trip !");
            progress.setIndeterminate(true);
            progress.setCanceledOnTouchOutside(false);
            progress.setCancelable(false);
            progress.show();

            handler_time.removeCallbacks(runnable);
            if (marker_pick!=null)
                marker_pick.hideInfoWindow();

            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null)+"/"+log_id.getString("id",null));
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ref.child("accept").setValue(2);
                        final Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        Ride ride = new Ride();
                        ride.setDestination(map.get("destination").toString());
                        ride.setSource(map.get("source").toString());
                        ride.setAmount(map.get("price").toString());
                        ride.setCustomerid(map.get("customer_id").toString());
                        ride.setDriver(log_id.getString("driver", null));
                        ride.setCancel_charge(map.get("cancel_charge").toString());
                        ride.setPaymode(map.get("paymode").toString());
                        ride.setReason(intent.getStringExtra("reason"));
                        if (map.containsKey("parking_price"))
                            ride.setParking(map.get("parking_price").toString());
                        else
                            ride.setParking("0");
                        ride.setSeat(map.get("seat").toString());
                        if (map.containsKey(offer))
                            ride.setDiscount(map.get("offer").toString());
                        else
                            ride.setDiscount("0");
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
                        editor.remove("offer");
                        editor.commit();
                        finish();
                        startActivity(getIntent());
                    }
                    else {
                        progress.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else if (requestCode==9 && resultCode==RESULT_OK){
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);
            pickup_address.setText(name);
//            goToLocationZoom(latitude, longitude, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            MarkerOptions options=new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.pin_location),80,80,false)))
                    .position(new LatLng(latitude,longitude))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();
            cur_loc=marker_pick.getPosition();

            if (marker_drop!=null){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                mMap.setPadding(50,400,20,400);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

                Object[] dataTransfer = new Object[22];
                String url = getDirectionsUrltwoplaces();
                GetOutstationPriceData getDirectionsData = new GetOutstationPriceData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = ((TextView)findViewById(R.id.i_price_van));
                dataTransfer[3] = ((TextView)findViewById(R.id.i_price_sedan));
                dataTransfer[4] = ((TextView)findViewById(R.id.i_price_suv));
                dataTransfer[5] = Home.this;
                getDirectionsData.execute(dataTransfer);

//            place_drivers_share();
                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
                findViewById(R.id.intercity).setVisibility(View.VISIBLE);
            }
            else {
                goToLocationZoom(latitude, longitude, 15);
            }
        }
        else if (requestCode==10 && resultCode==RESULT_OK){
            String name;
            Double latitude,longitude;
            name=intent.getStringExtra("place");
            latitude=intent.getDoubleExtra("lat",0);
            longitude=intent.getDoubleExtra("lng",0);
            findViewById(R.id.rating_bar).setVisibility(View.GONE);
            destn_address.setText(name);

            goToLocationZoom(latitude, longitude, 15);

            if (marker_drop != null) {
                marker_drop.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(name)
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.ic_destination),80,80,false)))
                    .position(new LatLng(latitude, longitude))
                    .snippet("Destination");
            marker_drop = mMap.addMarker(options);

            if (marker_pick!=null){
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(marker_pick.getPosition());
                builder.include(marker_drop.getPosition());
                LatLngBounds bounds = builder.build();

                int padding = 40;
                mMap.setPadding(50,400,20,400);
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

                Object[] dataTransfer = new Object[22];
                String url = getDirectionsUrltwoplaces();
                GetOutstationPriceData getDirectionsData = new GetOutstationPriceData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = ((TextView)findViewById(R.id.i_price_van));
                dataTransfer[3] = ((TextView)findViewById(R.id.i_price_sedan));
                dataTransfer[4] = ((TextView)findViewById(R.id.i_price_suv));
                dataTransfer[5] = Home.this;
                getDirectionsData.execute(dataTransfer);

//            place_drivers_share();
                getRouteToMarker(marker_pick.getPosition(), marker_drop.getPosition());
                findViewById(R.id.intercity).setVisibility(View.VISIBLE);
            }
            else {
                goToLocationZoom(latitude, longitude, 15);
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
        seats.setText("full");
//        seats_list.clear();
//        seats_list.add("full");
//
//        ArrayAdapter<String> adapter1 =
//                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
//        adapter1.setDropDownViewResource(R.layout.payment_list);
//        seats.setAdapter(adapter1);
//
        findViewById(R.id.layoutseat).setVisibility(View.GONE);
        findViewById(R.id.layout_seats).setVisibility(View.GONE);
        findViewById(R.id.layoutpaymode).getLayoutParams().width= LinearLayout.LayoutParams.MATCH_PARENT;
    }

    public void seatshare(){
        findViewById(R.id.layout_seats).setVisibility(View.GONE);
        findViewById(R.id.layoutseat).setVisibility(View.VISIBLE);
        seats.setText("1 seat");
        original_seats=1;
        findViewById(R.id.layoutpaymode).getLayoutParams().width= LinearLayout.LayoutParams.MATCH_PARENT/2;
//        seats_list.clear();
//        seats_list.add("1 seat");
//        seats_list.add("2 seat");
//
//        ArrayAdapter<String> adapter1 =
//                new ArrayAdapter<String>(getApplicationContext(), R.layout.payment_list, seats_list);
//        adapter1.setDropDownViewResource(R.layout.payment_list);
//        seats.setAdapter(adapter1);
    }

    public void getTime(){
        handler_time = new Handler();
        runnable = new Runnable() {
            public void run() {
//                Log.v("CHECKLOC",marker_pick.getPosition().toString()+" "+marker_drop.getPosition().toString());
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

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        //mMarker is the shown marker
                        if (marker_pick != null)
                            marker_pick.showInfoWindow();
                    }
                });

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

    public void hideview (View view){
        view.setAlpha((float)0.7);
        view.setClickable(false);
    }

    public void showview (View view){
        view.setAlpha((float)1);
        view.setClickable(true);
    }

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

    public void no_ride_found (){
        SharedPreferences.Editor ed = log_id.edit();
        ed.remove("offer");
        ed.commit();
        DatabaseReference rej_ride=FirebaseDatabase.getInstance().getReference("NotFoundRides");
        HashMap<String,Object> map=new HashMap<>();
        map.put("customer_id",log_id.getString("id",null));
        map.put("source",data.getSource());
        map.put("destination",data.getDestination());
        map.put("time",(new Date()).toString());
        rej_ride.push().setValue(map);
    }

    public class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return select_offers.size();
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
            view=getLayoutInflater().inflate(R.layout.place_text_view,null);
            TextView txt=(TextView)view.findViewById(R.id.name);

            txt.setText(select_offers.get(position));
            txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txt.setTextColor(Color.parseColor("#05affc"));
            return view;
        }
    }

    public void processoffer(String off, String off_disc, String off_upto, String off_code) {
        offercode=off_code;
        data.setOffer_type("");
        double pr=(double) Integer.parseInt(final_price.getText().toString().substring(2));
        double disc=(double) Double.parseDouble(off_disc);
        double upto=(double) Double.parseDouble(off_upto);

        if (!data.getOffer_value().equals("0"))
            pr=pr+Double.parseDouble(data.getOffer_value().toString());

        offer1=0;
        offer2=0;
        if (ridetype.equals("share")){
            if (oneseat==0) {
                double less = (pr * disc) / 100;
                if (less > upto)
                    less = upto;
                offer1 = (int) less;

                double nextpr = pr + ((pr * 15) / 100);
                less = (nextpr * disc) / 100;
                if (less > upto)
                    less = upto;
                offer2 = (int) less;
            }
            else {
                double less = (oneseat * disc) / 100;
                if (less > upto)
                    less = upto;
                offer1 = (int) less;

//                double nextpr = pr + ((pr * 15) / 100);
                less = (twoseat * disc) / 100;
                if (less > upto)
                    less = upto;
                offer2 = (int) less;
            }
        }
        double less=(pr*disc)/100;
        if (less>upto)
            less=upto;

        offervalue=(int)upto;
        offerdiscount=(int)disc;
        if (pr<less)
            pr=0;
        else
            pr=pr-less;

        final_price.setText("\u20B9 "+(int)pr);
        data.setOffer_code(offercode);
        data.setOffer_value(String.valueOf((int)less));
        offer.setText(off);
        promocode.setText("");
        findViewById(R.id.layout_offer).setVisibility(View.GONE);
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void processspecialoffer(String off, String off_disc, String off_upto, String off_code) {
        double pr=(double) Integer.parseInt(final_price.getText().toString().substring(2));
        if (pr>=Double.parseDouble(off_upto)) {
            offercode = off_code;
            data.setOffer_type("special");
            double disc = (double) Double.parseDouble(off_disc);
            double upto = (double) Double.parseDouble(off_upto);

            if (!data.getOffer_value().equals("0"))
                pr = pr + Double.parseDouble(data.getOffer_value().toString());

            offer1 = 0;
            offer2 = 0;
            if (ridetype.equals("share")) {
                if (oneseat == 0) {
                    double less = 0;
                    if (pr > upto)
                        less = disc;
                    else
                        less = 0;
                    offer1 = (int) less;

                    double nextpr = pr + ((pr * 15) / 100);
                    if (pr > upto)
                        less = disc;
                    else
                        less = 0;
                    offer2 = (int) less;
                } else {
                    double less = 0;
                    if ((oneseat+offer1) > upto)
                        less = disc;
                    else
                        less = 0;
                    offer1 = (int) less;

//                double nextpr = pr + ((pr * 15) / 100);
                    less = 0;
                    if ((twoseat+offer2) > upto)
                        less = disc;
                    else
                        less = 0;
                    offer2 = (int) less;
                }
            }
            double less = 0;
            if (pr > upto)
                less = disc;
            else
                less = 0;

            offervalue = (int) upto;
            offerdiscount = (int) disc;
            if (pr < less)
                pr = 0;
            else
                pr = pr - less;

            final_price.setText("\u20B9 " + (int) pr);
            data.setOffer_code(offercode);
            data.setOffer_value(String.valueOf((int) less));
            offer.setText(off);
            promocode.setText("");
            findViewById(R.id.layout_offer).setVisibility(View.GONE);
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        else {
            Toast.makeText(home, "Offer is not valid below \u20B9 "+Double.parseDouble(off_upto), Toast.LENGTH_SHORT).show();
        }
    }

    public void apply(View view){
        specpcode=false;
        specoffer=false;
        if (TextUtils.isEmpty(promocode.getText().toString())){
//            Toast.makeText(this, "Please Enter Promocode.", Toast.LENGTH_SHORT).show();
            promocode.setError("Required.");
        }
        else {
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Please Wait ...");
            dialog.setIndeterminate(true);
            dialog.show();

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Promocode/"+promocode.getText().toString());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("validity")) {
                            try {
                                Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dataSnapshot.child("validity").getValue().toString());
                                if (date.compareTo(new Date())==0){
                                    String str = "Get " + dataSnapshot.child("discount").getValue().toString() + "% off upto \u20B9 " + dataSnapshot.child("upto").getValue().toString();
                                    processoffer(str, dataSnapshot.child("discount").getValue().toString(), dataSnapshot.child("upto").getValue().toString(), dataSnapshot.child("code").getValue().toString());
                                } else if ((new Date()).after(date)) {
                                    Toast.makeText(Home.this, "Promocode Expired !", Toast.LENGTH_SHORT).show();
                                } else {
                                    String str = "Get " + dataSnapshot.child("discount").getValue().toString() + "% off upto \u20B9 " + dataSnapshot.child("upto").getValue().toString();
                                    processoffer(str, dataSnapshot.child("discount").getValue().toString(), dataSnapshot.child("upto").getValue().toString(), dataSnapshot.child("code").getValue().toString());
                                }
                            }catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            String str = "Get " + dataSnapshot.child("discount").getValue().toString() + "% off upto \u20B9 " + dataSnapshot.child("upto").getValue().toString();
                            processoffer(str, dataSnapshot.child("discount").getValue().toString(), dataSnapshot.child("upto").getValue().toString(), dataSnapshot.child("code").getValue().toString());
                        }
                    }
                    else {
                        specpcode=true;
                        check_offer();
//                        Toast.makeText(Home.this, "Invalid Promocode !", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            final DatabaseReference dref=FirebaseDatabase.getInstance().getReference("SpecialOffer");
            dref.child(promocode.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final String offername=dataSnapshot.getKey();
                        dref.child(dataSnapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("validity") && !dataSnapshot.child("limit").getValue().toString().equals("0")) {
//                            Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                                        try {
                                            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dataSnapshot.child("validity").getValue().toString());
                                            if (date.compareTo(new Date())==0){
                                                String str = "Get \u20B9" + dataSnapshot.child("value").getValue().toString() + " off on rides more than \u20B9" + dataSnapshot.child("minamount").getValue().toString();
                                                processspecialoffer(str, dataSnapshot.child("value").getValue().toString(), dataSnapshot.child("minamount").getValue().toString(), offername);
                                            } else if ((new Date()).after(date)) {
                                                Toast.makeText(Home.this, "Promocode Expired !", Toast.LENGTH_SHORT).show();
                                            } else {
                                                String str = "Get \u20B9" + dataSnapshot.child("value").getValue().toString() + " off on rides more than \u20B9" + dataSnapshot.child("minamount").getValue().toString();
                                                processspecialoffer(str, dataSnapshot.child("value").getValue().toString(), dataSnapshot.child("minamount").getValue().toString(), offername);
                                            }
                                        }catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if (dataSnapshot.hasChild("limit") && !dataSnapshot.child("limit").getValue().toString().equals("0")){
//                            Toast.makeText(Home.this, "bye" , Toast.LENGTH_SHORT).show();
                                        String str = "Get \u20B9" + dataSnapshot.child("value").getValue().toString() + " off on rides more than \u20B9" + dataSnapshot.child("minamount").getValue().toString();
                                        processspecialoffer(str, dataSnapshot.child("value").getValue().toString(), dataSnapshot.child("minamount").getValue().toString(), offername);
                                    }
                                    else {
                                        Toast.makeText(Home.this, "Invalid Promocode !", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    specoffer=true;
                                    check_offer();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        specoffer=true;
                        check_offer();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void check_offer() {
        if (specoffer && specpcode)
            Toast.makeText(Home.this, "Invalid Promocode !", Toast.LENGTH_LONG).show();
    }

    int oneseat=0,twoseat=0;
    public void seatone (View view){
        seats.setText("1 seat");
        if (original_seats == 2) {
            original_seats = 1;
            if (oneseat==0) {
                int val = Integer.parseInt(final_price.getText().toString().substring(2)) + 1;
                val = (int) ((val * 100) / 115);
                realprice = val;
                oneseat=val;
                data.setOffer_value(String.valueOf(offer1));
                final_price.setText("\u20B9 " + (val-offer1));
            }
            else {
                realprice = oneseat;
                data.setOffer_value(String.valueOf(offer1));
//                final_price.setText("\u20B9 " + (oneseat-Integer.parseInt(data.getOffer_value())));
                final_price.setText("\u20B9 " + (oneseat-offer1));
            }
        }
        findViewById(R.id.layout_seats).setVisibility(View.GONE);
//        seats.requestFocus();
    }

    public void seattwo (View view){
//        Toast.makeText(this, ""+offer1+" "+offer2, Toast.LENGTH_SHORT).show();
        seats.setText("2 seat");
        if (original_seats == 1) {
            original_seats = 2;
            if (twoseat==0) {
                oneseat=Integer.parseInt(final_price.getText().toString().substring(2))+Integer.parseInt(data.getOffer_value());
                int val = Integer.parseInt(final_price.getText().toString().substring(2)) + Integer.parseInt(data.getOffer_value()) + 1;
                val = val + (int) ((val * 15) / 100);
                realprice = val;
                twoseat=val;
                data.setOffer_value(String.valueOf(offer2));
                final_price.setText("\u20B9 " + (val-offer2));
            }
            else {
                realprice = twoseat;
                data.setOffer_value(String.valueOf(offer2));
//                final_price.setText("\u20B9 " + (twoseat-Integer.parseInt(data.getOffer_value())));
                final_price.setText("\u20B9 " + (twoseat-offer2));
            }
        }
        findViewById(R.id.layout_seats).setVisibility(View.GONE);
//        seats.requestFocus();
    }

    public void confirmLocation(View view){
        conf_loc=view;
        if (change_dest==0 && view!=findViewById(R.id.shareRickshaw) && view!=findViewById(R.id.rickshaw)) {
            Intent intnt = new Intent(Home.this, ConfirmLocation.class);
            intnt.putExtra("lat", String.valueOf(marker_pick.getPosition().latitude));
            intnt.putExtra("lng", String.valueOf(marker_pick.getPosition().longitude));
            intnt.putExtra("address", pickup_address.getText().toString());
            startActivityForResult(intnt, 6);
        }
        else if (change_dest==0 && view==findViewById(R.id.rickshaw) && !rickshawnotify){
            Intent intnt = new Intent(Home.this, ConfirmLocation.class);
            intnt.putExtra("lat", String.valueOf(marker_pick.getPosition().latitude));
            intnt.putExtra("lng", String.valueOf(marker_pick.getPosition().longitude));
            intnt.putExtra("address", pickup_address.getText().toString());
            startActivityForResult(intnt, 6);
        }
        else {
            select_vehicle(conf_loc);
        }
    }

    public void updateCamera(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marker_pick.getPosition());
        builder.include(marker_drop.getPosition());
        LatLngBounds bounds = builder.build();

        int padding = 40;
        mMap.setPadding(50,400,20,400);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public void ridetype (View view){
        if (view.getId()==R.id.lcl){
            booking_type="local";
            ((TextView)findViewById(R.id.lcl)).setBackgroundColor(Color.parseColor("#ffffff"));
            ((TextView)view).setTextColor(Color.parseColor("#05affc"));

            ((TextView)findViewById(R.id.rntl)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.rntl)).setTextColor(Color.parseColor("#ffffff"));

            ((TextView)findViewById(R.id.icty)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.icty)).setTextColor(Color.parseColor("#ffffff"));

            destn_address.setText("");
            findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.rental).setVisibility(View.GONE);
            findViewById(R.id.intercity).setVisibility(View.GONE);
            findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
            findViewById(R.id.layout2).setVisibility(View.VISIBLE);
            findViewById(R.id.layout3).setVisibility(View.GONE);
            findViewById(R.id.layout4).setVisibility(View.GONE);
            findViewById(R.id.no_ride).setVisibility(View.GONE);
        }
        else if (view.getId()==R.id.rntl){
            booking_type="rental";
            ((TextView)findViewById(R.id.rntl)).setBackgroundColor(Color.parseColor("#ffffff"));
            ((TextView)view).setTextColor(Color.parseColor("#05affc"));

            ((TextView)findViewById(R.id.lcl)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.lcl)).setTextColor(Color.parseColor("#ffffff"));

            ((TextView)findViewById(R.id.icty)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.icty)).setTextColor(Color.parseColor("#ffffff"));

            destn_address.setText("");
            findViewById(R.id.destn_layout).setVisibility(View.GONE);
            findViewById(R.id.rental).setVisibility(View.VISIBLE);
            findViewById(R.id.intercity).setVisibility(View.GONE);
            findViewById(R.id.layout2).setVisibility(View.GONE);
            findViewById(R.id.no_ride).setVisibility(View.GONE);
            setdefaultrentalprice();
        }
        else if (view.getId()==R.id.icty){
            booking_type="outstation";
            ((TextView)findViewById(R.id.icty)).setBackgroundColor(Color.parseColor("#ffffff"));
            ((TextView)view).setTextColor(Color.parseColor("#05affc"));

            ((TextView)findViewById(R.id.rntl)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.rntl)).setTextColor(Color.parseColor("#ffffff"));

            ((TextView)findViewById(R.id.lcl)).setBackgroundColor(Color.parseColor("#05affc"));
            ((TextView)findViewById(R.id.lcl)).setTextColor(Color.parseColor("#ffffff"));

            destn_address.setText("");
            findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.rental).setVisibility(View.GONE);
            findViewById(R.id.intercity).setVisibility(View.GONE);
            findViewById(R.id.layout2).setVisibility(View.GONE);
            findViewById(R.id.no_ride).setVisibility(View.GONE);
        }
    }

    private void setdefaultrentalprice() {
        ((RadioButton)findViewById(R.id.onehr)).setChecked(true);

        float rentalvan=Float.parseFloat(log_id.getString("rentalvan",null));
        float rentalsedan=Float.parseFloat(log_id.getString("rentalsedan",null));
        float rentalsuv=Float.parseFloat(log_id.getString("rentalsuv",null));

        ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(rentalvan)));
        ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(rentalsedan)));
        ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(rentalsuv)));
    }

    public void rental_vehicle_type (View view){
        if (view.getId()==R.id.r_van){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.r_sedan).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.r_suv).setBackgroundResource(R.drawable.round_button);
        }
        else if (view.getId()==R.id.r_sedan){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.r_van).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.r_suv).setBackgroundResource(R.drawable.round_button);
        }
        else if (view.getId()==R.id.r_suv){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.r_sedan).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.r_van).setBackgroundResource(R.drawable.round_button);
        }
    }

    public void intercity_vehicle_type (View view){
        if (view.getId()==R.id.i_van){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.i_sedan).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.i_suv).setBackgroundResource(R.drawable.round_button);
        }
        else if (view.getId()==R.id.i_sedan){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.i_van).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.i_suv).setBackgroundResource(R.drawable.round_button);
        }
        else if (view.getId()==R.id.i_suv){
            view.setBackgroundResource(R.drawable.round_button_blue);
            findViewById(R.id.i_sedan).setBackgroundResource(R.drawable.round_button);
            findViewById(R.id.i_van).setBackgroundResource(R.drawable.round_button);
        }
    }

    public void calculate_rental_price (View view){
        float rentalvan=Float.parseFloat(log_id.getString("rentalvan",null));
        float rentalsedan=Float.parseFloat(log_id.getString("rentalsedan",null));
        float rentalsuv=Float.parseFloat(log_id.getString("rentalsuv",null));
//        float rentalextra=Float.parseFloat(log_id.getString("rentalextra",null));
        if (view.getId()==R.id.onehr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(rentalsuv)));
        }else if (view.getId()==R.id.twohr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(2*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(2*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(2*rentalsuv)));
        }else if (view.getId()==R.id.threehr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(3*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(3*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(3*rentalsuv)));
        }else if (view.getId()==R.id.fourhr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(4*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(4*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(4*rentalsuv)));
        }else if (view.getId()==R.id.fivehr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(5*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(5*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(5*rentalsuv)));
        }else if (view.getId()==R.id.sixhr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(6*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(6*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(6*rentalsuv)));
        }else if (view.getId()==R.id.sevenhr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(7*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(7*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(7*rentalsuv)));
        }else if (view.getId()==R.id.eighthr){
            ((TextView)findViewById(R.id.r_price_van)).setText("\u20B9 "+String.valueOf((int)(8*rentalvan)));
            ((TextView)findViewById(R.id.r_price_sedan)).setText("\u20B9 "+String.valueOf((int)(8*rentalsedan)));
            ((TextView)findViewById(R.id.r_price_suv)).setText("\u20B9 "+String.valueOf((int)(8*rentalsuv)));
        }
//        Toast.makeText(this, ""+((RadioButton)view).getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
