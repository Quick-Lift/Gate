package com.quicklift;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;

public class WelcomeScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int RequestPermissionCode = 1;
    SharedPreferences log_id;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        editor=log_id.edit();
        //Toast.makeText(Login.this, ""+user.getUid(), Toast.LENGTH_SHORT).show();
        //Log.v("TAG",user.getUid());

//        StringBuilder googleDirectionsUrl=new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
//        googleDirectionsUrl.append("origin=12.8284773,77.70238379999999");
//        googleDirectionsUrl.append("&destination=12.950288500000001,77.69906259999999");
//      //  googleDirectionsUrl.append("&waypoints=optimize:true|12.835483999999997,77.679803|12.950288500000001,77.69906259999999");
//        googleDirectionsUrl.append("&waypoints=optimize:true|12.835483999999997,77.679803|12.934533,77.626579");
//        googleDirectionsUrl.append("&key="+"AIzaSyAicFor08br3-Jl-xwUc0bZHC2KMdcGRNo");
//
//
////        googleDirectionsUrl.append("&destination=12.950288500000001,77.69906259999999");
////        googleDirectionsUrl.append("&waypoints=optimize:true|12.835483999999997,77.679803");
//
//        Log.v("TAG",googleDirectionsUrl.toString());
//        Object[] dataTransfer = new Object[9];
//        String url = googleDirectionsUrl.toString();
//        BestRoute getDirectionsData = new BestRoute();
//        dataTransfer[0] = url;
//        getDirectionsData.execute(dataTransfer);

        final SQLQueries sqlQueries=new SQLQueries(this);
        sqlQueries.deletefare();
        sqlQueries.deletelocation();
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Fare/Patna");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editor.putString("cancelcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge").getValue(Integer.class)));
                editor.commit();
                for (DataSnapshot data:dataSnapshot.child("Package").getChildren()){
                    ArrayList<String> price=new ArrayList<String>();
                    price.add(data.child("Latitude").getValue(String.class));
                    price.add(data.child("Longitude").getValue(String.class));
                    price.add(data.child("Amount").getValue(String.class));
                    price.add(data.child("Distance").getValue(String.class));

                    sqlQueries.savelocation(price);
                }
                for (DataSnapshot data:dataSnapshot.child("Price").getChildren()){
                    ArrayList<String> price=new ArrayList<String>();
                    price.add(data.child("NormalTime/BaseFare/Amount").getValue(String.class));
                    price.add(data.child("NormalTime/BaseFare/Distance").getValue(String.class));
                    price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                    price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                    price.add(data.child("NormalTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                    price.add(data.child("NormalTime/Time").getValue(String.class));

                    sqlQueries.savefare(price);
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");

                    price.clear();
                    price.add(data.child("PeakTime/BaseFare/Amount").getValue(String.class));
                    price.add(data.child("PeakTime/BaseFare/Distance").getValue(String.class));
                    price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
                    price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
                    price.add(data.child("PeakTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
                    price.add(data.child("PeakTime/Time").getValue(String.class));

                    sqlQueries.savefare(price);
//                    Toast.makeText(WelcomeScreen.this, ""+"hi", Toast.LENGTH_SHORT).show();
//                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setMessage("This app requires your location to be turned On. Press ok to turn it On.");
//            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
////                    Intent myIntent = new Intent(ACTION_LOCATION_SOURCE_SETTINGS);
////                    startActivity(myIntent);
//                    //get gps
//                }
//            });
//            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    // TODO Auto-generated method stub
//
//                }
//            });
//            dialog.show();
            EnableGPSAutoMatically();
        }
//         variables storing function values returned by network connection functions
        boolean status1 = haveNetworkConnection();
        boolean status2 = hasActiveInternetConnection();

        if (status1 && status2){
            if (!checkPermission()){
                requestPermission();
            }
            else {
                Intent i = new Intent(this,PhoneAuthActivity.class);
                startActivity(i);
                finish();
            }
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this,R.style.myBackgroundStyle);
            builder.setMessage("Turn on your internet connection and try again.")
                    .setCancelable(false)
                    .setTitle("No Internet !")
                    .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            startActivity(getIntent());
                        }
                    });

            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            alert.show();
            alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
        }

        // checking user permission
//        if(!checkPermission())
//        {
//            appendLog(getCurrentTime()+"Gathering permissions status:0");
//            //requesting permission to access mobile resources
//            if(status1 && status2)
//            {
//                appendLog(getCurrentTime()+"Gathering network information status:1");
//                requestPermission();
//            }
//            else{
//                requestPermission();
//                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
//                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this);
//                builder.setMessage("No internet connection")
//                        .setCancelable(true)
//                        .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        })
//                        .setNeutralButton("Cancel !", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //  Action for 'NO' Button
//                                dialog.cancel();
//                            }
//                        });
//
//                //Creating dialog box
//                AlertDialog alert = builder.create();
//                //Setting the title manually
//                alert.setTitle("Account Action !");
//                alert.show();
//                alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("red"));
//                alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("red"));
//            }
//        }
//        else {
//            appendLog(getCurrentTime() + "Gathered permissions status:1");
//
//            if(status1 && status2)
//            {
//                appendLog(getCurrentTime()+"Gathering network information status:1");
//                Intent i = new Intent(this,PhoneAuthActivity.class);
//                startActivity(i);
//                finish();
//            }
//            else{
//                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
//                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this);
//                builder.setMessage("No internet connection")
//                        .setCancelable(true)
//                        .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        })
//                        .setNeutralButton("Cancel !", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //  Action for 'NO' Button
//                                dialog.cancel();
//                            }
//                        });
//
//                //Creating dialog box
//                AlertDialog alert = builder.create();
//                //Setting the title manually
//                alert.setTitle("Account Action !");
//                alert.show();
//                alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("red"));
//                alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("red"));
//            }
//        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public boolean hasActiveInternetConnection()
    {
        // TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(WelcomeScreen.this, new String[]
                {
                        WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        LOCATION_SERVICE,
                        CALL_PHONE

                }, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {
                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStorgaePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean LocationService = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean CallPhone = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && ReadStorgaePermission && LocationService && CallPhone) {
                        Intent i = new Intent(this,PhoneAuthActivity.class);
                        startActivity(i);
                        finish();
                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        //Toast.makeText(WelcomeScreen.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        appendLog(getCurrentTime()+"Few permissions denied status:0");
                        Intent i = new Intent(this,PhoneAuthActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), LOCATION_SERVICE);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int SixthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult ==PackageManager.PERMISSION_GRANTED &&
                SixthPermissionResult ==PackageManager.PERMISSION_GRANTED;
    }

    static public void appendLog(String text)
    {
        File logFile = new File("sdcard/log.txt");
        if (!logFile.exists())

        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        //date output format
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        return dateFormat.format(cal.getTime())+"\t";
    }

    private void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//            locationRequest.setInterval(30 * 1000);
//            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
//                            toast("Success");
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            toast("GPS is not on");
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(WelcomeScreen.this, 1000);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            toast("Setting change not allowed");
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
