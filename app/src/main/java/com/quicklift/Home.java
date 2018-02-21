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
    ArrayList<String> payment_mode = new ArrayList<>();
    Spinner payment;
    Button pickup_go, destn_go, confirm;
    //Button ride;
    Data data = new Data();
    Polyline line;
    LatLng pickup, cur_loc;
    GeoLocation driver_loc;
    PlaceAutocompleteFragment autocompleteFragment, autocompleteFragment_pickup;
    ProgressDialog pdialog;
    int i = 0, found = 0;
    ArrayList<String> driver_list = new ArrayList<String>();
    private GeoQuery find_driver;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //// getSupportActionBar().setTitle("Home ");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);

        sqlQueries = new SQLQueries(this);

        pdialog = new ProgressDialog(this);
        pdialog.setMessage("Searching for driver...");
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(true);

        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_home_screen);
            initMap();
        } else {
            Toast.makeText(this, "Unable to load map !", Toast.LENGTH_SHORT).show();
        }

    /*    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        */

    }

    private void initMap() {
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);

        pickup_address = (EditText) findViewById(R.id.pickup);
        destn_address = (EditText) findViewById(R.id.destination);
        //time=(TextView)findViewById(R.id.time);
        //pickup_go=(Button)findViewById(R.id.pick_go);
        //destn_go=(Button)findViewById(R.id.dest_go);
        // ride=(Button)findViewById(R.id.ride);
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
        final_price = (TextView) findViewById(R.id.price);
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
        //sqlQueries.lastride("HQMFVDHAu8WwjLqLW0qWLHKbm183","2017-02-20","Huskur Gate");
        Cursor cursor = sqlQueries.retrievelastride();
        if (cursor != null && cursor.getCount() > 0) {
            // Toast.makeText(this, String.valueOf(cursor.getCount()), Toast.LENGTH_SHORT).show();
            displayrideinfo(cursor);

        } else {
            displayoffers();
        }
/*
        destn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                if (TextUtils.isEmpty(destn_address.getText().toString())){
                    destn_address.setError("Required.");
                    valid = false;
                }
                else {
                    try {
                        geoLocate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        pickup_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                if (TextUtils.isEmpty(pickup_address.getText().toString())){
                    pickup_address.setError("Required.");
                    valid = false;
                }
                else {
                    try {
                        geoLocate_pickup();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                if (TextUtils.isEmpty(pickup_address.getText().toString())){
                    pickup_address.setError("Required.");
                    valid = false;
                }
                if (TextUtils.isEmpty(destn_address.getText().toString())){
                    destn_address.setError("Required.");
                    valid = false;
                }
                else {
                    destn_address.setInputType(0);
                    pickup_address.setInputType(0);
                    ride.setVisibility(View.GONE);
                    findViewById(R.id.layout4).setVisibility(View.VISIBLE);
                }
            }
        });
*/
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = 0;
                findViewById(R.id.layout4).setVisibility(View.GONE);
/*
                String userId= log_id.getString("id",null);
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("CustomerRequest");

                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation());
*/

                // pdialog.show();
                data.setCustomer_id(log_id.getString("id", null));
                find_driver.removeAllListeners();
                findViewById(R.id.layout3).setVisibility(View.VISIBLE);

                findViewById(R.id.pickup_layout).setVisibility(View.INVISIBLE);
                findViewById(R.id.destn_layout).setVisibility(View.INVISIBLE);
                finddriver();
            }
        });
/*
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(12.864162, 77.438610),
                new LatLng(13.139807, 77.711895)));
        autocompleteFragment.setHint("Destination");
        autocompleteFragment.getView().setVisibility(View.GONE);
        destn_address=(EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.BLACK);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(Color.DKGRAY);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: " + place.getName());
                //Toast.makeText(Home.this, place.getName(), Toast.LENGTH_SHORT).show();
                goToLocationZoom(place.getLatLng().latitude, place.getLatLng().longitude, 15);

                if (marker_drop!=null){
                    marker_drop.remove();
                }

                MarkerOptions options = new MarkerOptions()
                        .title(place.getName().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .position(place.getLatLng())
                        .snippet("Destination");
                marker_drop=mMap.addMarker(options);

                data.setEn_lat(marker_drop.getPosition().latitude);
                data.setEn_lng(marker_drop.getPosition().longitude);
                //destn_address.setText(locality);
/*
                if (line!=null)
                    line.remove();
                drawline();

                float result[]=new float[10];
                Location.distanceBetween(marker_pick.getPosition().latitude,marker_pick.getPosition().longitude,marker_drop.getPosition().latitude,marker_drop.getPosition().longitude,result);
                // EditText dt = (EditText) findViewById(R.id.fare);

                Object[] dataTransfer=new Object[4];
                String url= getDirectionsUrl();
                GetDirectionsData getDirectionsData=new GetDirectionsData();
                dataTransfer[0]=mMap;
                dataTransfer[1]=url;
                dataTransfer[2]=time;
                dataTransfer[3]=data;

                getDirectionsData.execute(dataTransfer);

                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);

                getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());

                findViewById(R.id.layout3).setVisibility(View.GONE);
                findViewById(R.id.layout4).setVisibility(View.GONE);
                ride.setVisibility(View.VISIBLE);
/*
                Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude+"&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude+"&travelmode=driving");
                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(Home.this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }

                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", marker_drop.getPosition().latitude, marker_drop.getPosition().longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                // Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(Home.this, status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        autocompleteFragment_pickup = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pickup);
        autocompleteFragment_pickup.setBoundsBias(new LatLngBounds(
                new LatLng(12.864162, 77.438610),
                new LatLng(13.139807, 77.711895)));
        autocompleteFragment_pickup.setHint("Pickup");

        autocompleteFragment.getView().setVisibility(View.GONE);
        pickup_address=(EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input);
        ((EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.BLACK);
        ((EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(Color.DKGRAY);

        autocompleteFragment_pickup.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: " + place.getName());
                //Toast.makeText(Home.this, place.getName(), Toast.LENGTH_SHORT).show();

                goToLocationZoom(place.getLatLng().latitude, place.getLatLng().longitude, 15);

                if (marker_pick!=null){
                    marker_pick.remove();
                }

                MarkerOptions options=new MarkerOptions()
                        .title(place.getName().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .position(place.getLatLng())
                        .snippet("Pick up");
                marker_pick=mMap.addMarker(options);
                pickup=marker_pick.getPosition();

                data.setSt_lat(marker_pick.getPosition().latitude);
                data.setSt_lng(marker_pick.getPosition().longitude);
/*
                if (line!=null)
                    line.remove();
                drawline();

                place_drivers();
                getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());

                findViewById(R.id.layout3).setVisibility(View.GONE);
                findViewById(R.id.layout4).setVisibility(View.GONE);
                ride.setVisibility(View.VISIBLE);
              /*
                //float result[]=new float[10];
                //Location.distanceBetween(marker_pick.getPosition().latitude,marker_pick.getPosition().longitude,marker_drop.getPosition().latitude,marker_drop.getPosition().longitude,result);
                // EditText dt = (EditText) findViewById(R.id.fare);

                Object[] dataTransfer=new Object[4];
                String url= getDirectionsUrl();
                GetDirectionsData getDirectionsData=new GetDirectionsData();
                dataTransfer[0]=mMap;
                dataTransfer[1]=url;
                dataTransfer[2]=time;

                getDirectionsData.execute(dataTransfer);


            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                // Log.i(TAG, "An error occurred: " + status);
                //Toast.makeText(Home.this, status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        */
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

    private void displayoffers() {
        Toast.makeText(this, "Display Offers Here !", Toast.LENGTH_SHORT).show();
        findViewById(R.id.rating_bar).setVisibility(View.GONE);
    }

    private void displayrideinfo(Cursor cursor) {
        cursor.moveToNext();
        //Toast.makeText(this, "Ride Info", Toast.LENGTH_SHORT).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Drivers/" + cursor.getString(cursor.getColumnIndex("driver")));

        ((TextView) findViewById(R.id.timestamp)).setText(cursor.getString(cursor.getColumnIndex("date")));
        ((TextView) findViewById(R.id.location)).setText(cursor.getString(cursor.getColumnIndex("destination")));

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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void finddriver() {
        //Log.v("TAG",String.valueOf(i));
        if (found == 1)
            Toast.makeText(this, "Ride Found", Toast.LENGTH_SHORT).show();
        else if (found == 0 && i < driver_list.size())
            getClosestDriver();
        else {
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
            //ride.setVisibility(View.GONE);
            if (v == findViewById(R.id.bike)) {
                // Toast.makeText(this, "bike", Toast.LENGTH_SHORT).show();
                final_price.setText(price_bike.getText());
                final_image.setImageResource(R.drawable.ic_bike);
            } else if (v == findViewById(R.id.car)) {
                // Toast.makeText(this, "car", Toast.LENGTH_SHORT).show();
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

        resp = FirebaseDatabase.getInstance().getReference("Response/" + driver_list.get(i));
        resp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("resp").toString().equals("Accept")) {
                        Toast.makeText(Home.this, "Driver is on its way !", Toast.LENGTH_SHORT).show();
                        found = 1;
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
                         /*       TextView name=(TextView)findViewById(R.id.name);
                                TextView phone=(TextView)findViewById(R.id.phone);
                                TextView car_type=(TextView)findViewById(R.id.cartype);
                                TextView car_no=(TextView)findViewById(R.id.carno);
                                TextView rating=(TextView)findViewById(R.id.rating);
                                CircleImageView img=(CircleImageView)findViewById(R.id.img);

                                name.setText(cust.get("name").toString());
                                phone.setText(cust.get("phone").toString());
                                car_type.setText(cust.get("veh_type").toString());
                                car_no.setText(cust.get("veh_num").toString());

                                if (!cust.get("thumb").toString().equals("")) {
                                    byte[] dec = Base64.decode(cust.get("thumb").toString(), Base64.DEFAULT);
                                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                    ((CircleImageView)findViewById(R.id.pic)).setImageBitmap(decbyte);
                                }
                                findViewById(R.id.pickup_layout).setVisibility(View.GONE);
                                findViewById(R.id.destn_layout).setVisibility(View.GONE);
                                SharedPreferences.Editor editor=log_id.edit();
                                editor.putString("driver",driver_list.get(i));
                                editor.commit();

                                tracktripstatus();
                                //pdialog.dismiss();
*/
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
        //Toast.makeText(this, "i"+i, Toast.LENGTH_SHORT).show();
        //}
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
        //ride.setVisibility(View.GONE);
        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null) + "/l");
        tripstatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Double> map = (ArrayList<Double>) dataSnapshot.getValue();

                    if (driver != null) {
                        driver.remove();
                    }

                    MarkerOptions options = new MarkerOptions()
                            .title("Driver")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .position(new LatLng((Double) map.get(0), (Double) map.get(1)))
                            .snippet("Location");
                    driver = mMap.addMarker(options);

                    getRouteToMarker(marker_pick.getPosition(), driver.getPosition());
                } else {
                    Toast.makeText(Home.this, "Trip Cancelled", Toast.LENGTH_SHORT).show();
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
                    findViewById(R.id.pickup_layout).setVisibility(View.GONE);
                    findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.canceltrip).setVisibility(View.GONE);
                    destn_address.setText("");
                    DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
                    cr.removeValue();
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
                Map<String, Object> cust = (Map<String, Object>) dataSnapshot.getValue();
  /*              TextView name=(TextView)findViewById(R.id.name);
                TextView phone=(TextView)findViewById(R.id.phone);
                TextView car_type=(TextView)findViewById(R.id.cartype);
                TextView car_no=(TextView)findViewById(R.id.carno);
                TextView rating=(TextView)findViewById(R.id.rating);
                CircleImageView img=(CircleImageView)findViewById(R.id.img);

                name.setText(cust.get("name").toString());
                phone.setText(cust.get("phone").toString());
                car_type.setText(cust.get("veh_type").toString());
                car_no.setText(cust.get("veh_num").toString());

                if (!cust.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(cust.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                    img.setImageBitmap(decbyte);
                }
                findViewById(R.id.pickup_layout).setVisibility(View.GONE);
                findViewById(R.id.destn_layout).setVisibility(View.GONE);
                //SharedPreferences.Editor editor=log_id.edit();
                //editor.putString("driver",driver_list.get(i));
                //editor.commit();

                //tracktripstatus();
                //pdialog.dismiss();
*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void cancel_trip(View v) {
        DatabaseReference tripstatus = FirebaseDatabase.getInstance().getReference("Status/" + log_id.getString("driver", null));
        tripstatus.removeValue();
        resp.removeValue();
        SharedPreferences.Editor editor = log_id.edit();
        editor.putString("driver", "");
        editor.commit();

        DatabaseReference cr = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + log_id.getString("driver", null));
        cr.removeValue();
        found = 0;

        if (driver != null) {
            driver.remove();
        }
        if (marker_drop != null) {
            marker_drop.remove();
        }
        erasePolylines();
        //ride.setVisibility(View.VISIBLE);
        findViewById(R.id.layout4).setVisibility(View.GONE);
        findViewById(R.id.layout3).setVisibility(View.GONE);
        findViewById(R.id.pickup_layout).setVisibility(View.GONE);
        findViewById(R.id.destn_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.canceltrip).setVisibility(View.GONE);
        destn_address.setText("");
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

    /*
        public void geoLocate() throws IOException {
            String location = destn_address.getText().toString();

            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocationName(location, 1);


            if (list.size()==0){
                Toast.makeText(this, "Cannot Locate Address !", Toast.LENGTH_SHORT).show();
                destn_address.setText("");
            } else {
                Address address = list.get(0);
                String locality = address.getAddressLine(0);

                //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

                double lat = address.getLatitude();
                double lng = address.getLongitude();


                goToLocationZoom(lat, lng, 15);

                if (marker_drop!=null){
                    marker_drop.remove();
                }

                MarkerOptions options = new MarkerOptions()
                        .title(locality)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .position(new LatLng(lat, lng))
                        .snippet("Destination");
                marker_drop=mMap.addMarker(options);

                data.setEn_lat(marker_drop.getPosition().latitude);
                data.setEn_lng(marker_drop.getPosition().longitude);
                //destn_address.setText(locality);

                if (line!=null)
                    line.remove();
                drawline();

                float result[]=new float[10];
                Location.distanceBetween(marker_pick.getPosition().latitude,marker_pick.getPosition().longitude,marker_drop.getPosition().latitude,marker_drop.getPosition().longitude,result);
               // EditText dt = (EditText) findViewById(R.id.fare);

                Object[] dataTransfer=new Object[4];
                String url= getDirectionsUrl();
                GetDirectionsData getDirectionsData=new GetDirectionsData();
                dataTransfer[0]=mMap;
                dataTransfer[1]=url;
                dataTransfer[2]=time;
                dataTransfer[3]=data;

                getDirectionsData.execute(dataTransfer);

                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);
                //dt.setText(String.valueOf(result[0]/1000) + " Km");
            }
        }

        public void geoLocate_pickup() throws IOException {
            String location = destn_address.getText().toString();

            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocationName(location, 1);

            if (list.size()==0){
                Toast.makeText(this, "Cannot Locate Address !", Toast.LENGTH_SHORT).show();
                pickup_address.setText("");
            } else {
                Address address = list.get(0);
                String locality = address.getAddressLine(0);

                //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

                double lat = address.getLatitude();
                double lng = address.getLongitude();

                goToLocationZoom(lat, lng, 15);

                if (marker_pick!=null){
                    marker_pick.remove();
                }

                MarkerOptions options = new MarkerOptions()
                        .title(locality)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                        .position(new LatLng(lat, lng))
                        .snippet("Pick Up");
                marker_pick=mMap.addMarker(options);
                pickup=marker_pick.getPosition();

                data.setSt_lat(marker_pick.getPosition().latitude);
                data.setSt_lng(marker_pick.getPosition().longitude);
                pickup_address.setText(locality);

                if (line!=null)
                    line.remove();
                drawline();

                float result[]=new float[10];
                Location.distanceBetween(marker_pick.getPosition().latitude,marker_pick.getPosition().longitude,marker_drop.getPosition().latitude,marker_drop.getPosition().longitude,result);
                // EditText dt = (EditText) findViewById(R.id.fare);

                Object[] dataTransfer=new Object[4];
                String url= getDirectionsUrl();
                GetDirectionsData getDirectionsData=new GetDirectionsData();
                dataTransfer[0]=mMap;
                dataTransfer[1]=url;
                dataTransfer[2]=time;

                getDirectionsData.execute(dataTransfer);

                //dt.setText(String.valueOf(result[0]/1000) + " Km");
            }
        }

        private String getDirectionsUrl() {
            StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            googleDirectionsUrl.append("origin="+marker_pick.getPosition().latitude+","+marker_pick.getPosition().longitude);
            googleDirectionsUrl.append("&destination="+marker_drop.getPosition().latitude+","+marker_drop.getPosition().longitude);
            googleDirectionsUrl.append("&key="+"AIzaSyAicFor08br3-Jl-xwUc0bZHC2KMdcGRNo");

            return googleDirectionsUrl.toString();
        }

        private void drawline() {
            PolylineOptions options=new PolylineOptions()
                    .add(marker_pick.getPosition())
                    .add(marker_drop.getPosition())
                    .color(Color.BLUE)
                    .width(5);

            line=mMap.addPolyline(options);
        }
    */
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
            //LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            // CameraUpdate update=CameraUpdateFactory.newLatLngZoom(ll,15);
            //mmap.animateCamera(update);
            // MarkerOptions options=new MarkerOptions()
            //         .title("Address")
            //         .position(ll);
            // mmap.addMarker(options);

            Geocoder gc = new Geocoder(this);
            List<Address> list = null;
            try {
                list = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = list.get(0);
            String locality = address.getAddressLine(0);

            //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

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
            //Toast.makeText(this, address.getLocality(), Toast.LENGTH_SHORT).show();
            // autocompleteFragment_pickup.setText(address.getFeatureName());
            place_drivers();
        }
    }
    /*
        public void getClosestDriver() {

            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable/"+driver_list.get(0));

            final GeoFire geoFire=new GeoFire(ref);
            GeoQuery gqw=geoFire.queryAtLocation(new GeoLocation(pickup.latitude,pickup.longitude),radius);
            //geoFire.removeLocation(key);
            while (found==0 || i==driver_list.size()) {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                            found=1;
                            geoFire.removeLocation(driver_list.get(i));
                            DatabaseReference cus_ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+driver_list.get(i));
                            cus_ref.child("Info").setValue(data);

                            cus_ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getChildrenCount()==0){
                                        //pdialog.dismiss();
                                        Toast.makeText(Home.this, "Ride Cancelled \n Finding new ride", Toast.LENGTH_SHORT).show();
                                        getClosestDriver();
                                    }
                                    else{
                                        for (DataSnapshot dsp:dataSnapshot.getChildren()){
                                            Data dt=dsp.getValue(Data.class);
                                            if (dt.getAccept()==1){
                                                if (driver_drop!=null){
                                                    driver_drop.remove();
                                                }
                                                marker_drop.setVisible(false);

                                                DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+driver_list.get(i)+"/location");

                                                GeoFire gefr=new GeoFire(ref);
                                                final GeoQuery gq=gefr.queryAtLocation(new GeoLocation(pickup.latitude,pickup.longitude),radius);
                                                gq.removeAllListeners();

                                                gq.addGeoQueryEventListener(new GeoQueryEventListener() {
                                                    @Override
                                                    public void onKeyEntered(String key, GeoLocation location) {
                                                        MarkerOptions options = new MarkerOptions()
                                                                .title("Driver")
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                                .position(new LatLng(location.latitude,location.longitude))
                                                                .snippet("Pick up");
                                                        mMap.addMarker(options);
                                                        getRouteToMarker(pickup,new LatLng(location.latitude,location.longitude));
                                                    }

                                                    @Override
                                                    public void onKeyExited(String key) {

                                                    }

                                                    @Override
                                                    public void onKeyMoved(String key, GeoLocation location) {

                                                    }

                                                    @Override
                                                    public void onGeoQueryReady() {

                                                    }

                                                    @Override
                                                    public void onGeoQueryError(DatabaseError error) {

                                                    }
                                                });
    /*
                                                if (line!=null)
                                                    line.remove();
                                                drawline();

                                                DatabaseReference db=FirebaseDatabase.getInstance().getReference("Drivers/"+driver_list.get(i));
                                                db.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Map<String,Object> cust=(Map<String, Object>) dataSnapshot.getValue();
                                                        TextView name=(TextView)findViewById(R.id.name);
                                                        TextView phone=(TextView)findViewById(R.id.phone);
                                                        TextView car_type=(TextView)findViewById(R.id.cartype);
                                                        TextView car_no=(TextView)findViewById(R.id.carno);
                                                        TextView rating=(TextView)findViewById(R.id.rating);
                                                        CircleImageView img=(CircleImageView)findViewById(R.id.img);

                                                        name.setText(cust.get("name").toString());
                                                        phone.setText(cust.get("phone").toString());
                                                        car_type.setText(cust.get("veh_type").toString());
                                                        car_no.setText(cust.get("veh_num").toString());

                                                        if (!cust.get("thumb").toString().equals("")) {
                                                            byte[] dec = Base64.decode(cust.get("thumb").toString(), Base64.DEFAULT);
                                                            Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                                            img.setImageBitmap(decbyte);
                                                        }
                                                        pdialog.dismiss();

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                                Toast.makeText(Home.this, "Driver is on its way.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            found = 0;
                            i++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            if (found==0 && i==driver_list.size()){
                Toast.makeText(this, "Sorry ! No ride found !", Toast.LENGTH_SHORT).show();
            }

            //final DatabaseReference cus_ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+key);

            //cus_ref.push().setValue(data);

        }

        public void getClosestDriver() {
            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

            final GeoFire geoFire=new GeoFire(ref);
            GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(pickup.latitude,pickup.longitude),radius);
            geoQuery.removeAllListeners();

            data.setCustomer_id(log_id.getString("id",null));

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!driverfound) {
                        driverfound = true;
                        driverid=key;

                        geoFire.removeLocation(key);

                        final DatabaseReference cus_ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+key);

                        cus_ref.push().setValue(data);
                        cus_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount()==0){
                                    pdialog.dismiss();
                                    Toast.makeText(Home.this, "Ride Cancelled \n Finding new ride", Toast.LENGTH_SHORT).show();
                                    getClosestDriver();
                                }
                                else{
                                    for (DataSnapshot dsp:dataSnapshot.getChildren()){
                                        Data dt=dsp.getValue(Data.class);
                                        if (dt.getAccept()==1){
                                            if (marker_drop!=null){
                                                marker_drop.remove();
                                            }

                                            MarkerOptions options = new MarkerOptions()
                                                    .title("Driver")
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                    .position(new LatLng(dt.getD_lat(), dt.getD_lng()))
                                                    .snippet("Pick up");
                                            marker_drop=mMap.addMarker(options);

                                            if (line!=null)
                                                line.remove();
                                            drawline();

                                            DatabaseReference db=FirebaseDatabase.getInstance().getReference("Drivers");
                                            db.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                        if (ds.getKey().equals(driverid)){
                                                            Driver cust=ds.getValue(Driver.class);
                                                            TextView name=(TextView)findViewById(R.id.name);
                                                            TextView phone=(TextView)findViewById(R.id.phone);
                                                            TextView car_type=(TextView)findViewById(R.id.cartype);
                                                            TextView car_no=(TextView)findViewById(R.id.carno);
                                                            TextView rating=(TextView)findViewById(R.id.rating);
                                                            CircleImageView img=(CircleImageView)findViewById(R.id.img);

                                                            name.setText(cust.getName());
                                                            phone.setText(cust.getPhone());
                                                            car_type.setText(cust.getVeh_type());
                                                            car_no.setText(cust.getVeh_num());

                                                            if (!cust.getThumb().equals("")) {
                                                                byte[] dec = Base64.decode(cust.getThumb(), Base64.DEFAULT);
                                                                Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                                                img.setImageBitmap(decbyte);
                                                            }
                                                            pdialog.dismiss();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            Toast.makeText(Home.this, "Driver is on its way.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if (!driverfound){
                        Toast.makeText(Home.this, "No ride found", Toast.LENGTH_SHORT).show();
                        pdialog.dismiss();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }
    */
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

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

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

        pricebike(route.get(shortestRouteIndex).getDistanceValue());
        priceauto(route.get(shortestRouteIndex).getDistanceValue());
        pricecar(route.get(shortestRouteIndex).getDistanceValue());
        pricerickshaw(route.get(shortestRouteIndex).getDistanceValue());
        priceshareauto(route.get(shortestRouteIndex).getDistanceValue());
        pricesharecar(route.get(shortestRouteIndex).getDistanceValue());
        pricesharerickshaw(route.get(shortestRouteIndex).getDistanceValue());

//            time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
        //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}
    }

    private void pricebike(int distanceValue) {
        price_bike.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
    }

    private void priceauto(int distanceValue) {
        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
    }

    private void pricecar(int distanceValue) {
        price_car.setText("Rs. " + String.valueOf(distanceValue * 6 / 1000));
    }

    private void pricerickshaw(int distanceValue) {
        price_rickshaw.setText("Rs. " + String.valueOf(distanceValue * 4.5 / 1000));
    }

    private void priceshareauto(int distanceValue) {
        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
    }

    private void pricesharecar(int distanceValue) {
        price_shareCar.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
    }

    private void pricesharerickshaw(int distanceValue) {
        price_shareRickshaw.setText("Rs. " + String.valueOf(distanceValue * 3.5 / 1000));
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

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_rides) {
            intent = new Intent(Home.this, CustomerRides.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(Home.this,EditProfile.class));
        } else if (id == R.id.nav_drive_with_us) {

        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:7896858413"));

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

        } else if (id == R.id.nav_payment) {

        } else if (id == R.id.nav_share) {

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
            //ride.setVisibility(View.VISIBLE);

            //calculatePrice();
        }
    }

    public void ratingbarclick(View v){
        startActivity(new Intent(Home.this,RatingActivity.class));
    }
}

