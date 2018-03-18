package com.quicklift;

import android.*;
import android.Manifest;
import android.app.Dialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
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
    GeoQuery find_driver;
    ArrayList<String> payment_mode = new ArrayList<>();
    Spinner payment;
    Button confirm;
    Data data = new Data();
    Polyline line;
    LatLng pickup, cur_loc;
    GeoLocation driver_loc;
    ProgressDialog pdialog;
    int i = 0, found = 0;
    ArrayList<String> driver_list = new ArrayList<String>();
    SharedPreferences log_id;
    Integer no_of_drivers = 0;
    HorizontalScrollView hsv;
    private CircleImageView final_image;
    private TextView final_price;
    SQLQueries sqlQueries;
    int radius = 5;
    boolean driverfound = false;
    String driverid;
    private TextView name;
    private TextView phone;
    private CircleImageView image;
    Integer screen_status=0;
    DatabaseReference lastride;
    TextView time_bike,time_car,time_auto,time_rickshaw,time_shareAuto,time_shareCar,time_shareRickshaw,final_time;

    @Override
    public void onBackPressed() {
       // super.onBackPressed();

        //Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
        if (screen_status==1) {
            screen_status = 0;

            destn_address.setVisibility(View.VISIBLE);
            pickup_address.setVisibility(View.VISIBLE);

            findViewById(R.id.fragmentTwo).setVisibility(View.VISIBLE);
            findViewById(R.id.layout3).setVisibility(View.GONE);
            findViewById(R.id.layout4).setVisibility(View.GONE);
            findViewById(R.id.rating_bar).setVisibility(View.GONE);
        }
        else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
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
            Toast.makeText(this, "Unable to load map !", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap() {
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);

        pickup_address = (EditText) findViewById(R.id.pickup);
        destn_address = (EditText) findViewById(R.id.destination);
        confirm = (Button) findViewById(R.id.confirm);
        payment = (Spinner) findViewById(R.id.pay_mode);
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
        final_image = (CircleImageView) findViewById(R.id.final_image);

        payment_mode.add("Cash");
        payment_mode.add("QuickLift Money");
        payment_mode.add("Bhim Upi");
        payment_mode.add("Paytm");

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        phone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.phone);
        image = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.image);

        updatenavbar();

        if (!log_id.getString("driver", null).equals("")) {
            tracktripstatus();
        }
        else {
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
                            findViewById(R.id.rating_bar).setVisibility(View.GONE);
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
        }
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
                findViewById(R.id.layout4).setVisibility(View.GONE);

                data.setCustomer_id(log_id.getString("id", null));
                //find_driver.removeAllListeners();
                //findViewById(R.id.layout3).setVisibility(View.VISIBLE);
               // Toast.makeText(Home.this, "2", Toast.LENGTH_SHORT).show();
                screen_status=0;
                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                finddriver();
            }
        });
    }

    private void updatenavbar() {
        DatabaseReference db=FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null));
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
        find_driver.removeAllListeners();
        if (found == 1)
            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
        else if (found == 0 && i < driver_list.size())
            getClosestDriver();
        else {
             screen_status=1;
             findViewById(R.id.layout4).setVisibility(View.VISIBLE);
             findViewById(R.id.layout3).setVisibility(View.GONE);
             Toast.makeText(this, "No Ride Found", Toast.LENGTH_SHORT).show();
             findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
             findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
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

                final_price.setText(price_bike.getText());
                final_image.setImageResource(R.drawable.ic_bike);
            } else if (v == findViewById(R.id.car)) {

                final_price.setText(price_car.getText());
                final_image.setImageResource(R.drawable.ic_car);
            } else if (v == findViewById(R.id.auto)) {
                // Toast.makeText(this, "auto", Toast.LENGTH_SHORT).show();
                final_price.setText(price_auto.getText());
                final_image.setImageResource(R.drawable.ic_car);
            } else if (v == findViewById(R.id.rickshaw)) {
                // Toast.makeText(this, "rickshaw", Toast.LENGTH_SHORT).show();
                final_price.setText(price_rickshaw.getText());
                final_image.setImageResource(R.drawable.ic_car);
            }

            findViewById(R.id.layout4).setVisibility(View.VISIBLE);
            findViewById(R.id.fragmentTwo).setVisibility(View.GONE);
        }
    }

    DatabaseReference cust_req;
    DatabaseReference resp;

    private void getClosestDriver() {
        cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + driver_list.get(i));
        cust_req.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    cust_req.child("Info").setValue(data);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(driver_list.get(i));
                } else {
                    ++i;
                    finddriver();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.v("TAG",log_id.getString("driver",null)+"\njihij "+ driver_list.get(i));
        resp = FirebaseDatabase.getInstance().getReference("Response/" + driver_list.get(i));
        resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        //((Button)findViewById(R.id.canceltrip)).setVisibility(View.VISIBLE);
                        found = 1;
                        //Toast.makeText(Home.this, ""+log_id.getString("driver",null), Toast.LENGTH_SHORT).show();
                        Log.v("TAG",log_id.getString("driver",null));
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Drivers/" + driver_list.get(i));
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
                                editor.putString("driver",driver_list.get(i));
                                editor.commit();
                                tracktripstatus();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        if (log_id.getString("driver", null).equals("")) {
                            Toast.makeText(Home.this, "Rejected", Toast.LENGTH_SHORT).show();
                            found = 0;
                            cust_req.removeValue();
                            resp.removeValue();
                            ++i;
                            if (i <= driver_list.size())
                                finddriver();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Handler handle = new Handler();
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (found == 0 && ++i <= driver_list.size()) {
                    cust_req.removeValue();
                    resp.removeValue();
                    resp.onDisconnect();
                    finddriver();
                    Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                }
            }
        }, 8000);
    }

    private void tracktripstatus() {
        findViewById(R.id.canceltrip).setVisibility(View.VISIBLE);
        findViewById(R.id.rating_bar).setVisibility(View.INVISIBLE);
        findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
        //ride.setVisibility(View.GONE);
        Log.v("TAG","enter");
        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
        tripstatus.child("/l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    ArrayList<Double> map = (ArrayList<Double>) dataSnapshot.getValue();

                    if (driver != null) {
                        driver.remove();
                    }
                    mMap.clear();
                    cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver",null));
                    cust_req.child("Info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String,Object> map1=(Map<String, Object>) dataSnapshot.getValue();
                            MarkerOptions options = new MarkerOptions()
                                    .title("Pickup")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    .position(new LatLng((Double)(map1.get("st_lat")), (Double)(map1.get("st_lng"))))
                                    .snippet("Pickup Location");
                            marker_pick = mMap.addMarker(options);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    MarkerOptions options = new MarkerOptions()
                            .title("Driver")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(new LatLng(Double.parseDouble(map.get(0).toString()), Double.parseDouble(map.get(1).toString())))
                            .snippet("Driver Location");
                    driver = mMap.addMarker(options);

                    getRouteToMarker(marker_pick.getPosition(), driver.getPosition());

                } else {
                    Toast.makeText(Home.this, "Trip Cancelled", Toast.LENGTH_SHORT).show();
                    Log.v("TAG","cancel");

                    if (driver != null) {
                        driver.remove();
                    }
                    if (marker_drop != null) {
                        marker_drop.remove();
                    }
                    found = 0;
                    erasePolylines();
                    //ride.setVisibility(View.GONE);
                    findViewById(R.id.layout4).setVisibility(View.GONE);
                    findViewById(R.id.layout3).setVisibility(View.GONE);
                    findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.canceltrip).setVisibility(View.GONE);
                    destn_address.setText("");
                    //DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
                    //cr.removeValue();
                    mMap.clear();
                    if (marker_pick != null) {
                        marker_pick.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(cur_loc)
                            .snippet("Pick up");
                    marker_pick = mMap.addMarker(options);
                    pickup = cur_loc;

                    resp = FirebaseDatabase.getInstance().getReference("Response/" + log_id.getString("driver",null));
                    resp.removeValue();

                    DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
                    cr.removeValue();

                    SharedPreferences.Editor editor = log_id.edit();
                    editor.putString("driver", "");
                    editor.commit();
                    found = 0;
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
        Toast.makeText(this, "cancelling", Toast.LENGTH_SHORT).show();
        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
        tripstatus.removeValue();
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

            Address address = list.get(0);
            String locality = address.getAddressLine(0);

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            goToLocationZoom(lat, lng, 15);

            if (marker_pick != null) {
                marker_pick.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(address.getLocality())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(new LatLng(lat, lng))
                    .snippet("Pick up");
            marker_pick = mMap.addMarker(options);

            pickup = marker_pick.getPosition();
            cur_loc = marker_pick.getPosition();

            data.setSt_lat(marker_pick.getPosition().latitude);
            data.setSt_lng(marker_pick.getPosition().longitude);

            pickup_address.setText(address.getLocality());
            place_drivers();
        }
    }

    public void place_drivers() {
        driver_list.clear();
        final List<Marker> driver_markers = new ArrayList<>();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        for (int k = 0; k < driver_markers.size(); k++) {
            driver_markers.get(k).remove();
        }
        driver_markers.clear();

        GeoFire geoFire = new GeoFire(ref);

        find_driver = geoFire.queryAtLocation(new GeoLocation(pickup.latitude, pickup.longitude), radius);
        find_driver.removeAllListeners();

        find_driver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                driver_list.add(key);

                MarkerOptions options = new MarkerOptions()
                        .title("Bike")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
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
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
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
    }

    private List<Polyline> polylines = new ArrayList<>();

    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
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
        time_bike.setText(String.valueOf(time/60)+" min");
    }

    private void priceauto(int distanceValue,int time) {
        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
        time_auto.setText(String.valueOf(time/60)+" min");
    }

    private void pricecar(int distanceValue,int time) {
        price_car.setText("Rs. " + String.valueOf(distanceValue * 6 / 1000));
        time_car.setText(String.valueOf(time/60)+" min");
    }

    private void pricerickshaw(int distanceValue,int time) {
        price_rickshaw.setText("Rs. " + String.valueOf(distanceValue * 4.5 / 1000));
        time_rickshaw.setText(String.valueOf(time/60)+" min");
    }

    private void priceshareauto(int distanceValue,int time) {
        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
        time_shareAuto.setText(String.valueOf(time/60)+" min");
    }

    private void pricesharecar(int distanceValue,int time) {
        price_shareCar.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
        time_shareCar.setText(String.valueOf(time/60)+" min");
    }

    private void pricesharerickshaw(int distanceValue,int time) {
        price_shareRickshaw.setText("Rs. " + String.valueOf(distanceValue * 3.5 / 1000));
        time_shareRickshaw.setText(String.valueOf(time/60)+" min");
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
            startActivity(new Intent(Home.this,EditProfile.class));
        } else if (id == R.id.nav_drive_with_us) {

        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(Home.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
            intent = new Intent(Home.this, OffersActivity.class);
            startActivity(intent);
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(new LatLng(latitude,longitude))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();

            data.setSt_lat(marker_pick.getPosition().latitude);
            data.setSt_lng(marker_pick.getPosition().longitude);

            place_drivers();
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
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
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
        }
    }

    public void ratingbarclick(View v){
        Intent intent=new Intent(Home.this,RatingActivity.class);
        intent.putExtra("rating",0);
        startActivity(intent);
    }
}

