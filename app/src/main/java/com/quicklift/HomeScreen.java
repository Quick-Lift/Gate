package com.quicklift;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class HomeScreen extends AppCompatActivity
        implements OnMapReadyCallback,NavigationView.OnNavigationItemSelectedListener,View.OnClickListener{

    private GoogleMap mMap;
    private FrameLayout button,vehicle_list;
    private EditText source,destination;
    private TextView fare,timestamp,name;
    private CardView confirm0,confirm1,confirm2,confirm3,location,fav_dest;
    private Spinner user_list,seat_num,coupon_list,payment;
    String[] user_item,seat_item,coupon_item,payment_item;
    private DisplayMetrics displayMetrics;
    private Button bike,car,shareCar,auto,shareAuto,rickshaw,
            shareRickshaw,ride,confirm,home,work,marathahalli,kadubisnahalli,extra;
    private FirebaseDatabase database;
    private DatabaseReference reference,rate_ref,driver_info;
    private ListView location_list;
    private ArrayList<places> places_list;
    private ArrayList<rides> rate_list;
    private String did;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng currentLocation = new LatLng(12.831385, 77.700493);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        //mMap.getMaxZoomLevel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        rate_list = new ArrayList<>();
        places_list = new ArrayList<>();
        //location_list = (ListView)findViewById(R.id.location_list);
        user_item = new String[]{"Personal","Add Other"};
        seat_item = new String[]{"1","2","3","4","5","6"};
        coupon_item = new String[]{"QUICK_LIFT_WLC","QUICK_LIFT_RIDE","QUICK_LIFT_FIRST"};
        payment_item = new String[]{"Cash Mode","Paytm","PayUMoney"};

        timestamp  = (TextView)findViewById(R.id.text_timestamp);

        name = (TextView)findViewById(R.id.text_name);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("address/jQEZNBgHsjOQcEM1jnO5yaf9PDq1");
        rate_ref = database.getReference("ride");
        Query query = rate_ref.child("PhHGcX8jdkb26N6zlVzWb5JgaZr2").orderByKey().limitToLast(1);


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Log.e("TAG", "" + dataSnapshot.child("000001").getValue());

                for (DataSnapshot data:dataSnapshot.getChildren()) {
                    //Map<String, Object> map = (Map<String, Object>) data.getValue();
                    // Log.e("TAG", "" + data.child("rated").getValue());
                    //Log.e("TAG", "" + dataSnapshot.getChildrenCount());

                    if (data.child("rated").getValue().toString().equals("false")) {
                        //Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
                        did = new String(data.child("did").getValue().toString());
                        rides ride = new rides();
                        ride.setDestination(data.child("destination").getValue().toString());
                        ride.setSource(data.child("source").getValue().toString());
                        ride.setTimestamp(data.child("timestamp").getValue().toString());
                        ride.setDid(data.child("did").getValue().toString());

                        rate_list.add(ride);
                        StringTokenizer st = new StringTokenizer(ride.getTimestamp(), " ");
                        String times = st.nextToken();
                        timestamp.setText("on " + times + " ");

                        driver_info = database.getReference("driver/lkkaHaQalTWCTMETFcSgL1ysd7G3");
                        driver_info.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Log.e("TAG",dataSnapshot.child("name").getValue().toString());
                                name.setText(dataSnapshot.child("name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            String key = "recent", key1 = "saved";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    places_list.clear();
                    if (places_list.size()<4 && dataSnapshot.child(key).getChildrenCount() > 0){
                        for (DataSnapshot data:dataSnapshot.child(key).getChildren()){
                            Log.i("TAG",""+data);
                            if (places_list.size() < 4){
                                places place = new places();
                                place.setName(data.getKey());
                                place.setAddress(data.getValue().toString());
                                places_list.add(place);
                            }else{
                                break;
                            }
                        }
                    }

                    if (dataSnapshot.child(key1).getChildrenCount() > 0){
                        for (DataSnapshot data:dataSnapshot.child(key1).getChildren()){
                            places place = new places();
                            place.setName(data.getKey());
                            place.setAddress(data.getValue().toString());
                            places_list.add(place);
                        }
                    }

                    CustomAdapter customAdapter = new CustomAdapter();
                    location_list.setAdapter(customAdapter);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        user_list = (Spinner) findViewById(R.id.user);
        seat_num = (Spinner)findViewById(R.id.seat);
        coupon_list = (Spinner)findViewById(R.id.apply);
        payment = (Spinner)findViewById(R.id.payment_method);

        ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, user_item);
        ArrayAdapter<String> seatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, seat_item);
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, payment_item);
        ArrayAdapter<String> couponAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, coupon_item);

        user_list.setAdapter(userAdapter);
        seat_num.setAdapter(seatAdapter);
        coupon_list.setAdapter(couponAdapter);
        payment.setAdapter(paymentAdapter);
*/
    //    fav_dest = (CardView) findViewById(R.id.fav_destination);
        button = (FrameLayout)findViewById(R.id.button);
        vehicle_list = (FrameLayout)findViewById(R.id.vehicles_list);
        source =  (EditText)findViewById(R.id.source_main);
        destination = (EditText)findViewById(R.id.destination_main);
        destination.setFocusable(false);
        destination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
                    showFrame(fav_dest);
                }else {
                    String dest = destination.getText().toString().trim();
                    if (TextUtils.isEmpty(dest)){
                        showFrame(fav_dest);
                    }else {
                        String pickup = source.getText().toString().trim();
                        if (TextUtils.isEmpty(pickup)){
                            source.setFocusable(true);
                        }else {
                            showFrame(fav_dest);
                            showFrame(vehicle_list);
                        }
                    }
                }
            }
        });

        fare = (TextView)findViewById(R.id.fare_main);
        fare.setOnClickListener(this);

        bike = (Button)findViewById(R.id.bike);
        bike.setOnClickListener(this);

        car = (Button)findViewById(R.id.car);
        car.setOnClickListener(this);

        shareCar = (Button)findViewById(R.id.shareCar);
        shareCar.setOnClickListener(this);

        auto = (Button)findViewById(R.id.auto);
        auto.setOnClickListener(this);

        shareAuto = (Button)findViewById(R.id.shareAuto);
        shareAuto.setOnClickListener(this);

        rickshaw = (Button)findViewById(R.id.rickshaw);
        rickshaw.setOnClickListener(this);

        shareRickshaw = (Button)findViewById(R.id.shareRickshaw);
        shareRickshaw.setOnClickListener(this);

        ride = (Button)findViewById(R.id.ride_button_main);
        ride.setOnClickListener(this);

        confirm = (Button)findViewById(R.id.confirm_main);
        confirm.setOnClickListener(this);

        location = (CardView)findViewById(R.id.location_card);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(0);
        toolbar.setBackgroundColor(0x00ffffff);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Upcoming Offers !!!", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch(id){
            case R.id.home:
                destination.setText(home.getText());
                hideFrame(fav_dest);
                showFrame(vehicle_list);
                break;
            case R.id.bike :
                selectVehicle(bike);
                break;
            case R.id.car :
                selectVehicle(car);
                break;
            case R.id.shareCar :
                selectVehicle(shareCar);
                break;
            case R.id.auto :
                selectVehicle(auto);
                break;
            case R.id.shareAuto :
                selectVehicle(shareAuto);
                break;
            case R.id.rickshaw :
                selectVehicle(rickshaw);
                break;
            case R.id.shareRickshaw :
                selectVehicle(shareRickshaw);
                break;
            case R.id.ride_button_main:
                Log.i("ride button","ride button clicked");
                hideFrame(vehicle_list);
                showFrame(button);
                location.setVisibility(View.GONE);
                break;
            case R.id.confirm_main:
                hideFrame(button);
                Toast.makeText(this, "Show Driver Information", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void selectVehicle(Button button){
        bike.setEnabled(true);
        car.setEnabled(true);
        shareCar.setEnabled(true);
        auto.setEnabled(true);
        shareAuto.setEnabled(true);
        rickshaw.setEnabled(true);
        shareRickshaw.setEnabled(true);
        button.setEnabled(false);
        ride.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //showFrame(fav_dest);
        hideFrame(button);
        hideFrame(vehicle_list);
        ride.setVisibility(View.GONE);
        source.setVisibility(View.GONE);

        int width = displayMetrics.widthPixels/2;
    }

    private void showFrame(FrameLayout frameLayout){
        frameLayout.setVisibility(View.VISIBLE);
    }

    private void hideFrame(FrameLayout frameLayout){
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return places_list.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            //Toast.makeText(MainActivity.this, ""+places_list.size(), Toast.LENGTH_SHORT).show();
            convertView=getLayoutInflater().inflate(R.layout.location_list_activity,null);

            Button loc=(Button)convertView.findViewById(R.id.address);
            loc.setText(places_list.get(position).getName());
            return convertView;
        }
    }
}
