package com.quicklift;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class ConfirmLocation extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    private Marker marker_pick;
    BitmapDescriptor icon;
    private Bitmap b;
    Double lat,lng;
    String addr;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent();
        intent.putExtra("result","0");
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent=new Intent();
                intent.putExtra("result","0");
                setResult(RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_location);

        getSupportActionBar().setTitle("Confirm Location");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        lat=Double.parseDouble(getIntent().getStringExtra("lat"));
        lng=Double.parseDouble(getIntent().getStringExtra("lng"));
        addr=getIntent().getStringExtra("address");

        ((TextView)findViewById(R.id.location_name)).setText(addr);
        MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        mapfragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                String address=getCompleteAddressString(cameraPosition.target.latitude,cameraPosition.target.longitude);
                if (!address.equals("")){
                    lat=cameraPosition.target.latitude;
                    lng=cameraPosition.target.longitude;
                    addr=address;
                    ((TextView)findViewById(R.id.location_name)).setText(address);
                }
            }
        });
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

        goToLocationZoom(lat,lng,15);
    }

    public void confirm (View view){
        Intent intent=new Intent();
        intent.putExtra("result","1");
        intent.putExtra("lat",String.valueOf(lat));
        intent.putExtra("lng",String.valueOf(lng));
        intent.putExtra("address",addr);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void goToLocationZoom(double v, double v1, float zoom) {
        LatLng ll = new LatLng(v, v1);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
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
}
