package com.quicklift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static com.quicklift.Home.home;
import static com.quicklift.Home.network_status;
import static com.quicklift.PlaceSelector.place;
import static com.quicklift.PlaceSelector.place_network_status;

/**
 * Created by adarsh on 5/5/18.
 */

public class CheckConnectivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "hi", Toast.LENGTH_SHORT).show();
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(isConnected){
            if (home!=null) {
                if (network_status != null)
                    network_status.setVisibility(View.VISIBLE);
                home.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if (place!=null){
                if (place_network_status != null)
                    place_network_status.setVisibility(View.VISIBLE);
                place.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
        else{
            if (home!=null) {
                if (network_status!=null)
                    network_status.setVisibility(View.GONE);
                home.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if (place!=null){
                if (place_network_status!=null)
                    place_network_status.setVisibility(View.GONE);
                place.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        }
    }
}
