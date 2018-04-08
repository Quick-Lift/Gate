package com.quicklift;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by adarsh on 6/4/18.
 */

public class BestRoute extends AsyncTask<Object,String,String> {
    GoogleMap mmap;
    String url, googleDirectionsData, duration, distance;
    TextView editText, editText1, editText2, editText3, editText4, editText5, editText6;
    Data data;

    @Override
    protected String doInBackground(Object... objects) {
        //mmap = (GoogleMap) objects[0];
        url = (String) objects[0];
        //duration=(String) objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            googleDirectionsData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String> directionsList = null;

        Log.v("TAG",s);
        JSONArray jsonArray=null;
        JSONObject jsonObject = null;

        try {
            jsonObject=new JSONObject(s);
            jsonArray=jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i=0;i<jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").length();i++) {
                Log.v("TAG", String.valueOf(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order").getInt(i)));
            }
            for (int i=0;i<jsonArray.length();i++) {
                Log.v("TAG", jsonArray.getJSONObject(i).getJSONObject("distance").getString("text"));
               // Log.v("TAG", jsonArray.getJSONObject(1).getJSONObject("distance").getString("text"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        DataParser parser = new DataParser();
//        directionsList = parser.parseDirections(s);
//        duration = directionsList.get("duration");
//        //distance=directionsList.get("distance");
//
//        //editText.setText(distance + " : "+duration);
//        editText.setText(duration);
//        editText1.setText(duration);
//        editText2.setText(duration);
//        editText3.setText(duration);
//        editText4.setText(duration);
//        editText5.setText(duration);
//        editText6.setText(duration);
    }
}