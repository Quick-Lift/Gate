package com.quicklift;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
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
    private ListView listView;
    private EditText destination;
    private ArrayList<String> keys;
    private GeoDataClient mGeoDataClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selector);

        destination=(EditText)findViewById(R.id.destination);
        mGeoDataClient = Places.getGeoDataClient(this, null);

        dataAdapter = new   GooglePlacesAutocompleteAdapter(PlaceSelector.this, R.layout.list_text_view);

            listView = (ListView)findViewById(R.id.list);
            // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

            //enables filtering for the contents of the given ListView
    listView.setTextFilterEnabled(true);

    destination.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(PlaceSelector.this, String.valueOf(dataAdapter.getCount()), Toast.LENGTH_SHORT).show();
                dataAdapter.getFilter().filter(s.toString());
                keys=dataAdapter.getkeys();
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
    }
}
