package com.quicklift;

import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by adarsh on 24/4/18.
 */

public class GetDuration extends AsyncTask<Object,String,String> {
    GoogleMap mmap;
    String url,googleDirectionsData,duration,distance;
    Marker marker;
    Data data;

    @Override
    protected String doInBackground(Object... objects) {
        mmap=(GoogleMap)objects[0];
        url=(String)objects[1];
        marker=(Marker) objects[2];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl=new DownloadUrl();

        try {
            googleDirectionsData=downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String,String> directionsList=null;
        DataParser parser=new DataParser();
        directionsList=parser.parseDirections(s);
        duration=directionsList.get("duration");
        //distance=directionsList.get("distance");

        //editText.setText(distance + " : "+duration);
        marker.setTitle("Time Remaining");
        marker.setSnippet(duration);
        marker.showInfoWindow();
    }
}

