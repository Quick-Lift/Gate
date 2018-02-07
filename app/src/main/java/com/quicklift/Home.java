package com.quicklift;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {
    private GoogleMap mMap;
    GoogleApiClient gpc;
    Marker marker_pick,marker_drop,marker,driver_drop;
    EditText pickup_address,destn_address;
    TextView time;
    Float estimated_time;
    Button pickup_go,destn_go,ride,confirm;
    Data data=new Data();
    Polyline line;
    LatLng pickup;
    GeoLocation driver_loc;
    PlaceAutocompleteFragment autocompleteFragment,autocompleteFragment_pickup;
    ProgressDialog pdialog;
    int i=0,found=0;
    ArrayList<String> driver_list=new ArrayList<String>();
    private GeoQuery find_driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Home ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pdialog=new ProgressDialog(this);
        pdialog.setMessage("Searching for driver...");
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(true);

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

        //pickup_address=(EditText)findViewById(R.id.pickup);
        //destn_address=(EditText)findViewById(R.id.destination);
        time=(TextView)findViewById(R.id.time);
        //pickup_go=(Button)findViewById(R.id.pick_go);
        //destn_go=(Button)findViewById(R.id.dest_go);
        ride=(Button)findViewById(R.id.ride);
        confirm=(Button)findViewById(R.id.confirm);
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
*/
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

               // pdialog.show();
                data.setCustomer_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                getClosestDriver();
            }
        });

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setHint("Destination");
        destn_address=(EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.WHITE);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(Color.WHITE);

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
*/
                findViewById(R.id.pickup_layout).setVisibility(View.VISIBLE);

                getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());
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
*/
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

        autocompleteFragment_pickup.setHint("Pickup");
        pickup_address=(EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input);
        ((EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(Color.WHITE);
        ((EditText)autocompleteFragment_pickup.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(Color.WHITE);

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
*/
                getRouteToMarker(marker_pick.getPosition(),marker_drop.getPosition());
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

                */
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                // Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(Home.this, status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    DatabaseReference cust_req;
    DatabaseReference resp;

    private void getClosestDriver() {
        find_driver.removeAllListeners();

        for (int t=0;t<5;t++){
            //Toast.makeText(Home.this, String.valueOf(i++), Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                        Toast.makeText(Home.this, String.valueOf(++i), Toast.LENGTH_SHORT).show();
                }
            }, 5000);
        }
        //Toast.makeText(Home.this, String.valueOf(++i), Toast.LENGTH_SHORT).show();

/*
            cust_req = FirebaseDatabase.getInstance().getReference("CustomerRequests/" + driver_list.get(i));
            cust_req.child("Info").setValue(data);

            resp = FirebaseDatabase.getInstance().getReference("Response/" + driver_list.get(i));
            resp.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(Home.this, "hi", Toast.LENGTH_SHORT).show();
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if (map.get("resp").toString().equals("Accept")) {
                            Toast.makeText(Home.this, "Accepted", Toast.LENGTH_SHORT).show();
                            found = 1;
                        } else {
                            Toast.makeText(Home.this, "Rejected", Toast.LENGTH_SHORT).show();
                            found = 0;
                            cust_req.removeValue();
                            resp.removeValue();
                            i++;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            i++;
            */
            //Toast.makeText(this, "i"+i, Toast.LENGTH_SHORT).show();
        //}
        if (i==driver_list.size() || found!=1){
            //Toast.makeText(this, "No Ride Found", Toast.LENGTH_SHORT).show();
        }

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
/*
    private void goToLocation(double v, double v1) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
    }
*/
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

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            goToLocationZoom(lat, lng, 15);

            if (marker_pick!=null){
                marker_pick.remove();
            }

            MarkerOptions options=new MarkerOptions()
                    .title(address.getPremises())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(new LatLng(lat,lng))
                    .snippet("Pick up");
            marker_pick=mMap.addMarker(options);
            pickup=marker_pick.getPosition();

            data.setSt_lat(marker_pick.getPosition().latitude);
            data.setSt_lng(marker_pick.getPosition().longitude);

            //pickup_address.setText(address.getFeatureName());
            autocompleteFragment_pickup.setText(address.getFeatureName());
            place_drivers();
        }
    }

    int radius=10;
    boolean driverfound=false;
    String driverid;
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
    public void place_drivers(){
        driver_list.clear();
        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire=new GeoFire(ref);
        find_driver=geoFire.queryAtLocation(new GeoLocation(pickup.latitude,pickup.longitude),radius);
        find_driver.removeAllListeners();

        find_driver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                driver_list.add(key);

                MarkerOptions options=new MarkerOptions()
                        .title("Bike")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .position(new LatLng(location.latitude,location.longitude));

                mMap.addMarker(options);
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

    private List<Polyline> polylines=new ArrayList<>();

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        //for (int i = 0; i <route.size(); i++) {
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.BLUE);
            polyOptions.width(10);
            polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            time.setText(String.valueOf(route.get(shortestRouteIndex).getDurationValue()/60));
            //Toast.makeText(getApplicationContext(),String.valueOf(shortestRouteIndex)+"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        //}

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRouteToMarker(LatLng pickupLatLng, LatLng destnLatLng) {
        if (pickupLatLng != null && destnLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(destnLatLng, pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}

