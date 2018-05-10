package com.quicklift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by adarsh on 5/5/18.
 */

public class CheckConnectivity extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(isConnected){
//            Intent i = new Intent(context, DisplayAlertbox.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);

        }
        else{
//            Toast.makeText(context, "Internet Connected", Toast.LENGTH_LONG).show();
        }

//        if (!wifi.isAvailable() && !mobile.isAvailable()) {
//            // Do something
//            AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.myBackgroundStyle);
//            builder.setMessage("Turn on your internet connection and try again.")
//                    .setCancelable(false)
//                    .setTitle("No Internet !")
//                    .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
//
//            //Creating dialog box
//            AlertDialog alert = builder.create();
//            //Setting the title manually
//            alert.show();
//            alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
////            Log.d("Network Available ", "Flag No 1");
//        }
    }
}
