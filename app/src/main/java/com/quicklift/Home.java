package com.quicklift;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    GoogleApiClient gpc;
    Marker marker_pick,marker_drop;
    EditText pickup_address,destn_address;
    TextView time;
    Button pickup_go,destn_go,ride,confirm;
    Data data=new Data();
    Polyline line;
    LatLng pickup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Home ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (googleServicesAvailable()) {
            Toast.makeText(this, "Perfect !", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_home);
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

        pickup_address=(EditText)findViewById(R.id.pickup);
        destn_address=(EditText)findViewById(R.id.destination);
        time=(TextView)findViewById(R.id.time);
        pickup_go=(Button)findViewById(R.id.pick_go);
        destn_go=(Button)findViewById(R.id.dest_go);
        ride=(Button)findViewById(R.id.ride);
        confirm=(Button)findViewById(R.id.confirm);

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

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.layout4).setVisibility(View.GONE);
                findViewById(R.id.layout3).setVisibility(View.VISIBLE);
/*
                String userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("CustomerRequest");

                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userId,new GeoLocation());
*/

                getClosestDriver();
            }
        });
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
            destn_address.setText(locality);

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

    private void goToLocation(double v, double v1) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
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
    public void onLocationChanged(Location location) {
        if (location==null){
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
                list = gc.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = list.get(0);
            String locality = address.getAddressLine(0);

            //Toast.makeText(this, locality, Toast.LENGTH_SHORT).show();

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            goToLocationZoom(lat, lng, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            MarkerOptions options=new MarkerOptions()
                    .title(locality)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(new LatLng(lat,lng))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();

            data.setSt_lat(marker_pick.getPosition().latitude);
            data.setSt_lng(marker_pick.getPosition().longitude);

            pickup_address.setText(locality);
        }
    }

    int radius=1;
    boolean driverfound=false;
    String driverid;

    public void getClosestDriver() {
        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

        final GeoFire geoFire=new GeoFire(ref);
        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(pickup.latitude,pickup.longitude),radius);
        geoQuery.removeAllListeners();

        data.setCustomer_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

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
                                Toast.makeText(Home.this, "Ride Cancelled \n Finding new ride", Toast.LENGTH_SHORT).show();
                                getClosestDriver();
                            }
                            else{
                                for (DataSnapshot dsp:dataSnapshot.getChildren()){
                                    Data dt=dsp.getValue(Data.class);
                                    if (dt.getAccept()==1){
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
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}

