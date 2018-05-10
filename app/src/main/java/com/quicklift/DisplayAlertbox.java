package com.quicklift;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DisplayAlertbox extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_display_alertbox);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.myBackgroundStyle);
        builder.setMessage("Turn on your internet connection and try again.")
                .setCancelable(false)
                .setTitle("No Internet !")
                .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.show();
        alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
    }
}
