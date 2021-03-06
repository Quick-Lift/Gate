package com.quicklift;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceSelector extends AppCompatActivity {
    private GooglePlacesAutocompleteAdapter dataAdapter;
    private ListView listView,list_places;
    private EditText destination;
    private ArrayList<String> keys;
    private ArrayList<SavePlace> name=new ArrayList<SavePlace>();
    private GeoDataClient mGeoDataClient;
    SQLQueries sqlQueries;
    private SharedPreferences log_id;
    TextView outofbound;
    static Activity place=null;
    static TextView place_network_status=null;
    CheckConnectivity con=new CheckConnectivity();
    Intent check;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void back (View view){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);
        place=this;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        destination=(EditText)findViewById(R.id.destination);
        check=getIntent();
        if (check.hasExtra("type")){
            if (check.getStringExtra("type").equals("pickup")) {
                destination.setHint("Pick me from ");
            } else {
                destination.setHint("Drop me at");
            }
        }

        outofbound=(TextView)findViewById(R.id.outofbound);
        place_network_status=(TextView)findViewById(R.id.network_status);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        sqlQueries=new SQLQueries(this);

        dataAdapter = new   GooglePlacesAutocompleteAdapter(PlaceSelector.this, R.layout.list_text_view);

        listView = (ListView)findViewById(R.id.list);
        list_places= (ListView)findViewById(R.id.list_places);
            // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        name.clear();

        //sqlQueries.saveplace("Ittina Neela Apartment","24","33","Home");
        //sqlQueries.saveplace("Huskur Gate","24","34","Work");
        //sqlQueries.recentplace("Salarpuria info zone","24","33","Other");

//        Cursor cursor=sqlQueries.retrieve_save_place();
//        if (cursor!=null && cursor.getCount()>0){
//            while (cursor.moveToNext()){
//                SavePlace sp=new SavePlace();
//                sp.setName(cursor.getString(cursor.getColumnIndex("name")));
//                sp.setPlace(cursor.getString(cursor.getColumnIndex("locname")));
//                sp.setLat(cursor.getString(cursor.getColumnIndex("lat")));
//                sp.setLng(cursor.getString(cursor.getColumnIndex("lng")));
//
//                //Toast.makeText(this, sp.getName(), Toast.LENGTH_SHORT).show();
//                name.add(sp);
//            }
//            //Toast.makeText(this, String.valueOf(name.size()), Toast.LENGTH_SHORT).show();
//        }
//
//        if (name.size()<4){
//            cursor=sqlQueries.retrieve_recent_place();
//            if (cursor!=null && cursor.getCount()>0){
//
//                while (cursor.moveToNext() && name.size()<4){
//                    SavePlace sp=new SavePlace();
//                    sp.setName(cursor.getString(cursor.getColumnIndex("name")));
//                    sp.setPlace(cursor.getString(cursor.getColumnIndex("locname")));
//                    sp.setLat(cursor.getString(cursor.getColumnIndex("lat")));
//                    sp.setLng(cursor.getString(cursor.getColumnIndex("lng")));
//
//                    name.add(sp);
//                }
//               // Toast.makeText(this, String.valueOf(name.size()), Toast.LENGTH_SHORT).show();
//            }
//        }
//        CustomAdapter customAdapter=new CustomAdapter();

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null));
        ref.child("saved").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    SavePlace sp=new SavePlace();
                    sp.setName(data.child("name").getValue(String.class));
                    sp.setPlace(data.child("locname").getValue(String.class));
                    sp.setLat(data.child("lat").getValue(String.class));
                    sp.setLng(data.child("lng").getValue(String.class));

                    name.add(sp);
                }
                list_places.setAdapter(new CustomAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        destination.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //Toast.makeText(PlaceSelector.this, String.valueOf(dataAdapter.getCount()), Toast.LENGTH_SHORT).show();

                list_places.setVisibility(View.GONE);
                outofbound.setVisibility(View.GONE);
                Handler handle=new Handler();
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataAdapter.getFilter().filter(s.toString());
                        keys=dataAdapter.getkeys();
                    }
                },2000);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(PlaceSelector.this, keys.get(position), Toast.LENGTH_SHORT).show();
                //GeoDataClient mGeoDataClient=new GeoDataClient();
                mGeoDataClient.getPlaceById(keys.get(position)).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                        if (task.isSuccessful()) {
                            PlaceBufferResponse places = task.getResult();
                            Place myPlace = places.get(0);

                            location_bangalore_search(myPlace);
//                            location_patna_search(myPlace);

//                            Log.v("Address",myPlace.getAddress().toString());

//                            if (myPlace.getLatLng().latitude >= 25.548596 && myPlace.getLatLng().latitude <= 25.701826
//                                    && myPlace.getLatLng().longitude >= 84.854858 && myPlace.getLatLng().longitude <= 85.278055 ) {

//                            if (myPlace.getAddress().toString().contains("Bihar")){


//                                destination.setText(myPlace.getAddress());
//                            Log.v("Address",myPlace.getAddress().toString());
//                            Toast.makeText(PlaceSelector.this, ""+myPlace.getLatLng().latitude, Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent();
//                                intent.putExtra("place", myPlace.getAddress());
//                                intent.putExtra("lat", myPlace.getLatLng().latitude);
//                                intent.putExtra("lng", myPlace.getLatLng().longitude);
//                                intent.putExtra("case","1");
//                                Log.v("Tag",""+myPlace.getLatLng().latitude+" "+myPlace.getLatLng().longitude);



//                                if (myPlace.getLatLng().latitude >= 25.561272 && myPlace.getLatLng().latitude <= 25.654152
//                                        && myPlace.getLatLng().longitude >= 85.020262 && myPlace.getLatLng().longitude <= 85.278055){
//                                    intent.putExtra("case","1");
//                                }
//                                else {
//                                    intent.putExtra("case","2");
//                                }


//                                setResult(RESULT_OK, intent);
//                                finish();

//                            } else {
////                                Toast.makeText(PlaceSelector.this, "Location is out of our service area !", Toast.LENGTH_SHORT).show();
//                                outofbound.setVisibility(View.VISIBLE);
//                            }
//
//
//
                            //Toast.makeText(PlaceSelector.this, myPlace.getName(), Toast.LENGTH_SHORT).show();
                            //Log.i(TAG, "Place found: " + myPlace.getName());
                            places.release();
                        } else {
                            //Log.e(TAG, "Place not found.");
                        }
                    }
                });
            }
        });

        list_places.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(PlaceSelector.this, ""+name.get(position).getLat(), Toast.LENGTH_SHORT).show();

                location_bangalore_list(position);
//                location_patna_list(position);

//                Intent intent=new Intent();
//                intent.putExtra("place", name.get(position).getPlace());
//                intent.putExtra("lat", Double.parseDouble(name.get(position).getLat()));
//                intent.putExtra("lng", Double.parseDouble(name.get(position).getLng()));
//                intent.putExtra("case","1");

//                double latitude = Double.parseDouble(name.get(position).getLat());
//                double longitude = Double.parseDouble(name.get(position).getLng());
//                Geocoder geocoder = new Geocoder(PlaceSelector.this, Locale.getDefault());
////                //List<Address> addresses =geocoder.getFromLocation(latitude, longitude, 1);
////Log.v("Tag",""+Double.parseDouble(name.get(position).getLat())+" "+Double.parseDouble(name.get(position).getLng()));
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                    String stateName = addresses.get(0).getAdminArea();
//
////                    Log.v("Address",addresses.get(0).getAdminArea());
//                    if (Double.parseDouble(name.get(position).getLat()) >= 25.561272 && Double.parseDouble(name.get(position).getLat()) <= 25.654152
//                            && Double.parseDouble(name.get(position).getLng()) >= 85.020262 && Double.parseDouble(name.get(position).getLng()) <= 85.278055){
//                        intent.putExtra("case","1");
//                        setResult(RESULT_OK,intent);
//                        finish();
//                    }
//                    else if (stateName.equals("Bihar")){
//                        intent.putExtra("case","2");
//                        setResult(RESULT_OK,intent);
//                        finish();
//                    }
//                    else {
////                      Log.v("TAG","outside");
////                      Toast.makeText(PlaceSelector.this, "Location is out of our service area !", Toast.LENGTH_SHORT).show();
//                        outofbound.setVisibility(View.VISIBLE);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(PlaceSelector.this, "Failed to retrieve address ! Please try again !", Toast.LENGTH_SHORT).show();
//                }





//                if (Double.parseDouble(name.get(position).getLat()) >= 25.561272 && Double.parseDouble(name.get(position).getLat()) <= 25.654152
//                        && Double.parseDouble(name.get(position).getLng()) >= 85.020262 && Double.parseDouble(name.get(position).getLng()) <= 85.278055){
//                    intent.putExtra("case","1");
//                    setResult(RESULT_OK,intent);
//                    finish();
//                }
//                else if (Double.parseDouble(name.get(position).getLat()) >= 25.548596 && Double.parseDouble(name.get(position).getLat()) <= 25.701826
//                        && Double.parseDouble(name.get(position).getLng()) >= 84.854858 && Double.parseDouble(name.get(position).getLng()) <= 85.278055 ) {
//                    intent.putExtra("case","2");
//                    setResult(RESULT_OK,intent);
//                    finish();
//                }
//                else {
//                    Log.v("TAG","outside");
////                    Toast.makeText(PlaceSelector.this, "Location is out of our service area !", Toast.LENGTH_SHORT).show();
//                    outofbound.setVisibility(View.VISIBLE);
//                }



//                setResult(RESULT_OK,intent);
//                finish();
            }
        });

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(con, intentFilter);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return name.size();
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

            txt.setText(name.get(position).getName());
            //Toast.makeText(PlaceSelector.this, name.get(position).getName(), Toast.LENGTH_SHORT).show();

            return view;
        }
    }

    @Override
    protected void onDestroy() {
        if (this.isFinishing())
            unregisterReceiver(con);
        super.onDestroy();
    }

    public void location_patna_search(Place myPlace){
        if (myPlace.getAddress().toString().contains("Bihar")){
            destination.setText(myPlace.getAddress());
//                            Log.v("Address",myPlace.getAddress().toString());
//                            Toast.makeText(PlaceSelector.this, ""+myPlace.getLatLng().latitude, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("place", myPlace.getAddress());
            intent.putExtra("lat", myPlace.getLatLng().latitude);
            intent.putExtra("lng", myPlace.getLatLng().longitude);

            if (myPlace.getLatLng().latitude >= 25.561272 && myPlace.getLatLng().latitude <= 25.654152
                    && myPlace.getLatLng().longitude >= 85.020262 && myPlace.getLatLng().longitude <= 85.278055){
                intent.putExtra("case","1");
            }
            else {
                intent.putExtra("case","2");
            }

            setResult(RESULT_OK, intent);
            finish();
        } else {
//                                Toast.makeText(PlaceSelector.this, "Location is out of our service area !", Toast.LENGTH_SHORT).show();
            outofbound.setVisibility(View.VISIBLE);
        }
    }

    public void location_bangalore_search(Place myPlace){
        destination.setText(myPlace.getAddress());

        Intent intent = new Intent();
        intent.putExtra("place", myPlace.getAddress());
        intent.putExtra("lat", myPlace.getLatLng().latitude);
        intent.putExtra("lng", myPlace.getLatLng().longitude);
        intent.putExtra("case","1");

        setResult(RESULT_OK, intent);
        finish();
    }

    public void location_bangalore_list(int position){
        Intent intent=new Intent();
        intent.putExtra("place", name.get(position).getPlace());
        intent.putExtra("lat", Double.parseDouble(name.get(position).getLat()));
        intent.putExtra("lng", Double.parseDouble(name.get(position).getLng()));
        intent.putExtra("case","1");

        setResult(RESULT_OK,intent);
        finish();
    }

    public void location_patna_list(int position){
        Intent intent=new Intent();
        intent.putExtra("place", name.get(position).getPlace());
        intent.putExtra("lat", Double.parseDouble(name.get(position).getLat()));
        intent.putExtra("lng", Double.parseDouble(name.get(position).getLng()));

        double latitude = Double.parseDouble(name.get(position).getLat());
        double longitude = Double.parseDouble(name.get(position).getLng());
        Geocoder geocoder = new Geocoder(PlaceSelector.this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String stateName = addresses.get(0).getAdminArea();

            if (Double.parseDouble(name.get(position).getLat()) >= 25.561272 && Double.parseDouble(name.get(position).getLat()) <= 25.654152
                    && Double.parseDouble(name.get(position).getLng()) >= 85.020262 && Double.parseDouble(name.get(position).getLng()) <= 85.278055){
                intent.putExtra("case","1");
                setResult(RESULT_OK,intent);
                finish();
            }
            else if (stateName.equals("Bihar")){
                intent.putExtra("case","2");
                setResult(RESULT_OK,intent);
                finish();
            }
            else {
                outofbound.setVisibility(View.VISIBLE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(PlaceSelector.this, "Failed to retrieve address ! Please try again !", Toast.LENGTH_SHORT).show();
        }
    }
}
