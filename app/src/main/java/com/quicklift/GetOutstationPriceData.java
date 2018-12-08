package com.quicklift;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by adarsh on 27/4/18.
 */

public class GetOutstationPriceData extends AsyncTask<Object,String,String> {
    GoogleMap mmap;
    String url,distance,duration;
    TextView price_van,price_sedan,price_suv;
    Context context;
    String googleDirectionsData;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected String doInBackground(Object... objects) {
        mmap=(GoogleMap)objects[0];
        url=(String)objects[1];
        price_van=(TextView) objects[2];
        price_sedan=(TextView) objects[3];
        price_suv=(TextView) objects[4];
        context=(Context) objects[5];

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
        PriceParser parser=new PriceParser();
        directionsList=parser.parseDirections(s);
        duration=directionsList.get("duration");
        distance=directionsList.get("distance");

        pref = context.getSharedPreferences("Login",MODE_PRIVATE);
        editor=pref.edit();

        float van=Float.parseFloat(pref.getString("outstationvan",null));
        float sedan=Float.parseFloat(pref.getString("outstationsedan",null));
        float suv=Float.parseFloat(pref.getString("outstationsuv",null));
        float multiplier=Float.parseFloat(pref.getString("outstationmultiplier",null));
        float timing=Float.parseFloat(pref.getString("outstationtimingcharge",null));
        float dis=Float.valueOf(distance);
        float dur=Float.valueOf(duration);

        price_van.setText("\u20B9 "+(int)(((van*dis)+(dur*timing))*multiplier));
        price_sedan.setText("\u20B9 "+(int)(((sedan*dis)+(dur*timing))*multiplier));
        price_suv.setText("\u20B9 "+(int)(((suv*dis)+(dur*timing))*multiplier));
    }
}
