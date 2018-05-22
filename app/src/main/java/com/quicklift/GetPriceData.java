package com.quicklift;

import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by adarsh on 27/4/18.
 */

public class GetPriceData extends AsyncTask<Object,String,String> {
    GoogleMap mmap;
    String url,googleDirectionsData,duration,distance;
    TextView price_bike,price_excel, price_car, price_auto, price_rickshaw, price_shareAuto, price_shareCar, price_shareRickshaw;
    Data data;
    Marker marker_drop;
    Cursor cursor,spec_location;
    int spec_package=-1,vehicle_case;

    @Override
    protected String doInBackground(Object... objects) {
        mmap=(GoogleMap)objects[0];
        url=(String)objects[1];
        price_excel=(TextView) objects[2];
        price_car=(TextView) objects[3];
        price_rickshaw=(TextView) objects[4];
        price_shareCar=(TextView) objects[5];
        price_shareRickshaw=(TextView) objects[6];
        marker_drop=(Marker) objects[7];
        cursor=(Cursor) objects[8];
        spec_location=(Cursor) objects[9];
        spec_package=(int) objects[10];
        vehicle_case=(int) objects[11];
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
        PriceParser parser=new PriceParser();
        directionsList=parser.parseDirections(s);
        duration=directionsList.get("duration");
        distance=directionsList.get("distance");

        //editText.setText(distance + " : "+duration);
//        Toast.makeText(this, ""+cursor.getCount()+" , "+cursor.getColumnCount(), Toast.LENGTH_SHORT).show();

        SimpleDateFormat dt=new SimpleDateFormat("HH:mm");
        int index=1;
        try {
            if ((dt.parse("11:00").after(dt.parse(dt.format(new Date()))) && dt.parse("08:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("23:59").after(dt.parse(dt.format(new Date()))) && dt.parse("17:00").before(dt.parse(dt.format(new Date())))) ||
                    (dt.parse("05:00").after(dt.parse(dt.format(new Date()))) && dt.parse("00:00").before(dt.parse(dt.format(new Date()))))){
                index=2;
            }
            else {
                index=1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        pricebike(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
        priceexcel(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
//        priceauto(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
        pricecar(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
        pricerickshaw(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
//        priceshareauto(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
        pricesharecar(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);
        pricesharerickshaw(spec_package,spec_location,index, cursor,Integer.valueOf(distance)/1000,Integer.valueOf(duration)/60);

//        editText.setText(duration);
//        editText1.setText(duration);
//        editText2.setText(duration);
//        editText3.setText(duration);
//        editText4.setText(duration);
//        editText5.setText(duration);
//        editText6.setText(duration);
    }

    private void priceexcel(int pckg, Cursor sloc, int index, Cursor cursor, int distanceValue, int time) {
//        Toast.makeText(this, ""+distanceValue+" "+time, Toast.LENGTH_SHORT).show();

        cursor.moveToFirst();
        for (int x=0;x<8;x++){
            cursor.moveToNext();
        }
//            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
        }
//            Log.v("TAG",""+fare);

        if (vehicle_case==1) {
            int val=(int)fare;
            price_excel.setText("Rs. " + val);
        }
        else if (vehicle_case==2) {
            int val=(int)fare;
            price_excel.setText("Rs. " + 2*val);
        }
    }
//    private void pricebike(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_bike.setText("Rs. " + String.valueOf(distanceValue * 5 / 1000));
//        //time_bike.setText(String.valueOf(time/60)+" min");
//    }
//    private void priceauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_auto.setText("Rs. " + String.valueOf(distanceValue * 4 / 1000));
//        //time_auto.setText(String.valueOf(time/60)+" min");
//    }
    private void pricecar(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
        cursor.moveToFirst();
//            for (int x=0;x<1;x++){
//                cursor.moveToNext();
//            }

//            Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                    +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
        }

        if (vehicle_case==1){
            int val=(int)fare;
            price_car.setText("Rs. " + val);
        }
        else if (vehicle_case==2) {
            int val = (int) fare;
            price_car.setText("Rs. " + 2 * val);
        }
        //time_car.setText(String.valueOf(time/60)+" min");
    }
    private void pricerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
        cursor.moveToFirst();
        for (int x=0;x<2;x++){
            cursor.moveToNext();
        }

//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
        }
        int val=(int)fare;
        price_rickshaw.setText("Rs. " + val);
        //time_rickshaw.setText(String.valueOf(time/60)+" min");
    }
//    private void priceshareauto(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
//        price_shareAuto.setText("Rs. " + String.valueOf(distanceValue * 3 / 1000));
//        //time_shareAuto.setText(String.valueOf(time/60)+" min");
//    }
    private void pricesharecar(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
        cursor.moveToFirst();
        for (int x=0;x<4;x++){
            cursor.moveToNext();
        }
//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
        }
        int val=(int)fare;
        price_shareCar.setText("Rs. " + val);
        //time_shareCar.setText(String.valueOf(time/60)+" min");
    }
    private void pricesharerickshaw(int pckg,Cursor sloc, int index, Cursor cursor, int distanceValue,int time) {
        cursor.moveToFirst();
        for (int x=0;x<6;x++){
            cursor.moveToNext();
        }
//        Log.v("TAG",cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2)
//                +" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
        float fare=0;
        if (index==2)
            cursor.moveToNext();

        if (pckg==-1) {
            if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
            } else {
                fare = Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_base")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_base")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
            }
            fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
        }
        else {
            if (distanceValue <= Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
            } else if (distanceValue <= Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))) {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (distanceValue - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            } else {
                fare = Float.valueOf(sloc.getString(sloc.getColumnIndex("amount")));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_first"))) * (Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first"))) - Integer.parseInt(sloc.getString(sloc.getColumnIndex("distance")))));
//                Log.v("TAG",""+fare);
                fare = fare + (Float.valueOf(cursor.getString(cursor.getColumnIndex("amount_second"))) * (distanceValue - Integer.parseInt(cursor.getString(cursor.getColumnIndex("dist_first")))));
//                Log.v("TAG",""+fare);
                fare = fare + (time * Float.valueOf(cursor.getString(cursor.getColumnIndex("time"))));
            }
        }
        int val=(int)fare;
        price_shareRickshaw.setText("Rs. " + val);
        //time_shareRickshaw.setText(String.valueOf(time/60)+" min");
    }
}