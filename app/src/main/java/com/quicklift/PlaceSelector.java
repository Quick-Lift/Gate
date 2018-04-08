package com.quicklift;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.Serializable;
import java.util.ArrayList;

public class PlaceSelector extends AppCompatActivity {
    private GooglePlacesAutocompleteAdapter dataAdapter;
    private ListView listView,list_places;
    private EditText destination;
    private ArrayList<String> keys;
    private ArrayList<SavePlace> name=new ArrayList<SavePlace>();
    private GeoDataClient mGeoDataClient;
    SQLQueries sqlQueries;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);

        getSupportActionBar().setTitle("Select Place");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        destination=(EditText)findViewById(R.id.destination);
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

        Cursor cursor=sqlQueries.retrieve_save_place();
        if (cursor!=null && cursor.getCount()>0){
            while (cursor.moveToNext()){
                SavePlace sp=new SavePlace();
                sp.setName(cursor.getString(cursor.getColumnIndex("name")));
                sp.setPlace(cursor.getString(cursor.getColumnIndex("place")));
                sp.setLat(cursor.getString(cursor.getColumnIndex("lat")));
                sp.setLng(cursor.getString(cursor.getColumnIndex("lng")));

                //Toast.makeText(this, sp.getName(), Toast.LENGTH_SHORT).show();
                name.add(sp);
            }
            //Toast.makeText(this, String.valueOf(name.size()), Toast.LENGTH_SHORT).show();
        }

        if (name.size()<4){
            cursor=sqlQueries.retrieve_recent_place();
            if (cursor!=null && cursor.getCount()>0){

                while (cursor.moveToNext() && name.size()<4){
                    SavePlace sp=new SavePlace();
                    sp.setName(cursor.getString(cursor.getColumnIndex("name")));
                    sp.setPlace(cursor.getString(cursor.getColumnIndex("place")));
                    sp.setLat(cursor.getString(cursor.getColumnIndex("lat")));
                    sp.setLng(cursor.getString(cursor.getColumnIndex("lng")));

                    name.add(sp);
                }
               // Toast.makeText(this, String.valueOf(name.size()), Toast.LENGTH_SHORT).show();
            }
        }
        CustomAdapter customAdapter=new CustomAdapter();
        list_places.setAdapter(customAdapter);

        destination.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                //Toast.makeText(PlaceSelector.this, String.valueOf(dataAdapter.getCount()), Toast.LENGTH_SHORT).show();

                list_places.setVisibility(View.GONE);
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
                            destination.setText(myPlace.getName());
                            Intent intent=new Intent();
                            intent.putExtra("place", myPlace.getName());
                            intent.putExtra("lat",myPlace.getLatLng().latitude);
                            intent.putExtra("lng",myPlace.getLatLng().longitude);
                            setResult(RESULT_OK,intent);
                            finish();
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
                Intent intent=new Intent();
                intent.putExtra("place", name.get(position).getPlace());
                intent.putExtra("lat", name.get(position).getLat());
                intent.putExtra("lng", name.get(position).getLng());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
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
}
