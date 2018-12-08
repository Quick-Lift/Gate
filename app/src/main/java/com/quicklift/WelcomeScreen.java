package com.quicklift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.io.IOException;
import java.util.ArrayList;

import believe.cht.fadeintextview.TextViewListener;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class WelcomeScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int RequestPermissionCode = 1;
    SharedPreferences log_id;
    SharedPreferences.Editor editor;
    GenerateLog generateLog=new GenerateLog();
    String tag="Welcome";
    int load=0;
    boolean valid=false;
    boolean anim_handle=false,verscheck=false,permcheck=false;
    Receiver receiver=new Receiver();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        startActivity(new Intent(this,CancelReason.class));

//        DatabaseReference mail=FirebaseDatabase.getInstance().getReference("Mail_id");
//        mail.child("email").setValue("qiklift@gmail.com");
//        mail.child("password").setValue("PasswordForQuickLift");

//        ImageView imageView=findViewById(R.id.image);
//        Glide.with(this).load(R.drawable.welcomescreen).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade().into(imageView);
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        editor=log_id.edit();

        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoomin);
        ((ImageView)findViewById(R.id.image)).startAnimation(animation);

        generateLog.appendLog(tag,"Loading page !");

        final believe.cht.fadeintextview.TextView textView = findViewById(R.id.textView);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // setting a listener for tracking events (optional)
                textView.setListener(new TextViewListener() {
                    @Override
                    public void onTextStart() {
//                Toast.makeText(getBaseContext(), "onTextStart() fired!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTextFinish() {
                        anim_handle=true;
                        if (verscheck && permcheck) {
                            startActivity(new Intent(WelcomeScreen.this, PhoneAuthActivity.class));
                            overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                            finish();
                        }
//                Toast.makeText(getBaseContext(), "onTextFinish() fired!", Toast.LENGTH_SHORT).show();
                    }
                });

                textView.setLetterDuration(250); // sets letter duration programmatically
                textView.setText("QuickLift"); // sets the text with animation (Read "KNOWN BUGS" if it doesn't give desired results)
                textView.isAnimating(); // returns current boolean animation state (optional)

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

//        Handler handler=new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },10000);

        if (log_id.contains("id")) {
            DatabaseReference vers = FirebaseDatabase.getInstance().getReference("Version_customer");
            vers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            if (Integer.parseInt(dataSnapshot.getValue().toString()) >
                                    getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionCode) {
                                View view = getLayoutInflater().inflate(R.layout.notification_layout, null);
                                TextView title = (TextView) view.findViewById(R.id.title);
                                TextView message = (TextView) view.findViewById(R.id.message);
                                Button left = (Button) view.findViewById(R.id.left_btn);
                                Button right = (Button) view.findViewById(R.id.right_btn);

                                left.setVisibility(View.INVISIBLE);
                                right.setText("Ok");
                                title.setText("Update !");
                                message.setText("A new version of app is availabe !\n We request you to update the app to enjoy the uninterrupted services !!");
                                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this);
                                builder.setView(view)
                                        .setCancelable(false);

                                final AlertDialog alert = builder.create();
                                alert.show();

                                left.setOnClickListener(null);
                                right.setOnClickListener(null);
                                right.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=com.quickliftpilot"));
                                        try {
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.quickliftpilot"));
                                        }
                                        alert.dismiss();
                                    }
                                });
                            } else if (permcheck && anim_handle) {
                                startActivity(new Intent(WelcomeScreen.this, PhoneAuthActivity.class));
                                overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                                finish();
                            } else {
                                verscheck=true;
                                valid = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        verscheck=true;
                        valid = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else {
            verscheck=true;
            valid=true;
        }

        handleoffer();

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
        generateLog.appendLog(tag,"Fare Removed !");
        sqlQueries.deletelocation();
        generateLog.appendLog(tag,"Location Removed !");
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Fare/Patna");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                generateLog.appendLog(tag,"Saving Price Data!");
                editor.putString("excelcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/excel").getValue(Integer.class)));
                editor.putString("sharecharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/share").getValue(Integer.class)));
                editor.putString("fullcharge",String.valueOf(dataSnapshot.child("CustomerCancelCharge/full").getValue(Integer.class)));
                editor.putString("ratemultiplier",String.valueOf(dataSnapshot.child("RateMultiplier").getValue(Float.class)));
                editor.putString("searchingtime",String.valueOf(dataSnapshot.child("SearchingTime").getValue(Integer.class)));
                editor.putString("outsidetripextraamount",String.valueOf(dataSnapshot.child("OutsideTripExtraAmount").getValue(Integer.class)));
                editor.putString("twoseatprice",String.valueOf(dataSnapshot.child("Twoseatprice").getValue(Integer.class)));
                editor.putString("excel",String.valueOf(dataSnapshot.child("ParkingCharge/excel").getValue(Integer.class)));
                editor.putString("fullcar",String.valueOf(dataSnapshot.child("ParkingCharge/fullcar").getValue(Integer.class)));
                editor.putString("fullrickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/fullrickshaw").getValue(Integer.class)));
                editor.putString("sharecar",String.valueOf(dataSnapshot.child("ParkingCharge/sharecar").getValue(Integer.class)));
                editor.putString("sharerickshaw",String.valueOf(dataSnapshot.child("ParkingCharge/sharerickshaw").getValue(Integer.class)));
                editor.putString("normaltimeradius",dataSnapshot.child("NormalTimeSearchRadius").getValue(String.class));
                editor.putString("peaktimeradius",dataSnapshot.child("PeakTimeSearchRadius").getValue(String.class));
                editor.putString("waittime",String.valueOf(dataSnapshot.child("WaitingTime").getValue(Integer.class)));
                editor.putString("waitingcharge",String.valueOf(dataSnapshot.child("WaitingCharge").getValue(Integer.class)));
                editor.putString("tax",String.valueOf(dataSnapshot.child("Tax").getValue().toString()));
                editor.putString("rentalextra",String.valueOf(dataSnapshot.child("Rental/extra").getValue(String.class)));
                editor.putString("rentalvan",String.valueOf(dataSnapshot.child("Rental/van").getValue(String.class)));
                editor.putString("rentalsedan",String.valueOf(dataSnapshot.child("Rental/sedan").getValue(String.class)));
                editor.putString("rentalsuv",String.valueOf(dataSnapshot.child("Rental/suv").getValue(String.class)));
                editor.putString("outstationvan",String.valueOf(dataSnapshot.child("Outstation/Van").getValue(String.class)));
                editor.putString("outstationsedan",String.valueOf(dataSnapshot.child("Outstation/Sedan").getValue(String.class)));
                editor.putString("outstationsuv",String.valueOf(dataSnapshot.child("Outstation/Suv").getValue(String.class)));
                editor.putString("outstationmultiplier",String.valueOf(dataSnapshot.child("Outstation/Multiplier").getValue(String.class)));
                editor.putString("outstationtimingcharge",String.valueOf(dataSnapshot.child("Outstation/TimingCharge").getValue(String.class)));
                editor.putString("erickshawtimeratio",String.valueOf(dataSnapshot.child("ERickshawTimeRatio").getValue(String.class)));
                editor.putString("erickshawradius",String.valueOf(dataSnapshot.child("ERickshawSearchRadius").getValue(String.class)));
                editor.putString("erickshawpickupdist",String.valueOf(dataSnapshot.child("ERickshawPickupDistance").getValue(String.class)));
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

        final DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Response/"+log_id.getString("id",null));
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                generateLog.appendLog(tag,"Removing Available Response !");
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("resp").getValue().toString().equals("Trip Ended") || dataSnapshot.child("resp").getValue().toString().equals("Cancel") || dataSnapshot.child("resp").getValue().toString().equals("Reject")){
                        dref.removeValue();
                        editor.putString("driver","");
                        editor.commit();
                    }
                    else {
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("CustomerRequests/"+dataSnapshot.child("driver").getValue().toString()+"/"+log_id.getString("id",null));
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    dref.removeValue();
                                    editor.putString("driver","");
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
                else {
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Share/"+log_id.getString("id",null));
                    reference.removeValue();
                    editor.putString("driver","");
                    editor.remove("ride");
                    editor.commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        generateLog.appendLog(tag,"Checking Location !");
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            generateLog.appendLog(tag,"Checking gps !");
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            generateLog.appendLog(tag,"Checking network !");
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
//        boolean status1 = haveNetworkConnection();
//        boolean status2 = hasActiveInternetConnection();

//        if (status1 && status2){
//            generateLog.appendLog(tag,"Internet Available!");
//            Intent i = new Intent(this, PhoneAuthActivity.class);
//                    startActivity(i);
//                    finish();
            if (!checkPermission()){
                generateLog.appendLog(tag,"Permission Requesting !");
                requestPermission();
            }
            else {
                generateLog.appendLog(tag,"All permissions available!");
                if (verscheck && anim_handle) {
                    Intent i = new Intent(this, PhoneAuthActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                    finish();
                }
                else {
                    permcheck=true;
                    valid=true;
                }
            }
//        }
//        else {
//            generateLog.appendLog(tag,"No internet!");
//            findViewById(R.id.network_status).setVisibility(View.VISIBLE);
////            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this,R.style.myBackgroundStyle);
////            builder.setMessage("Turn on your internet connection and try again.")
////                    .setCancelable(false)
////                    .setTitle("No Internet !")
////                    .setPositiveButton("Try Again !", new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int id) {
////                            finish();
////                            startActivity(getIntent());
////                        }
////                    });
////
////            //Creating dialog box
////            AlertDialog alert = builder.create();
////            //Setting the title manually
////            alert.show();
////            alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//
////            View view=getLayoutInflater().inflate(R.layout.notification_layout,null);
////            TextView title=(TextView)view.findViewById(R.id.title);
////            TextView message=(TextView)view.findViewById(R.id.message);
////            Button left=(Button) view.findViewById(R.id.left_btn);
////            Button right=(Button) view.findViewById(R.id.right_btn);
////
////            title.setText("No Internet !");
////            message.setText("Turn on your internet connection and try again.");
////            left.setVisibility(View.GONE);
////            right.setText("Try Again");
////
////            right.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////                    finish();
////                    startActivity(getIntent());
////                }
////            });
////
////            AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeScreen.this);
////            builder .setView(view)
////                    .setCancelable(false);
////
////            AlertDialog alert = builder.create();
////            alert.show();
//        }

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

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);
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

    public boolean hasActiveInternetConnection(){
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
        generateLog.appendLog(tag,"Requesting Permission !");
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:
                generateLog.appendLog(tag,"Request Permission Result!");
                if (grantResults.length > 0) {
                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordAudioPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStorgaePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean LocationService = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean CallPhone = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && RecordAudioPermission && WriteStoragePermission && ReadStorgaePermission && LocationService && CallPhone) {
                        generateLog.appendLog(tag,"Request Permission Granted !");
                        if (verscheck && anim_handle) {
                            Intent i = new Intent(this, PhoneAuthActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                            finish();
                        }
                        else {
                            permcheck=true;
                            valid=true;
                        }
                        //Toast.makeText(AnimationActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        //Toast.makeText(WelcomeScreen.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        generateLog.appendLog(tag,"Request Permission Denied !");
                        if (verscheck && anim_handle){
                            Intent i = new Intent(this,PhoneAuthActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                            finish();
                        }
                        else {
                            permcheck=true;
                            valid=true;
                        }
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        generateLog.appendLog(tag,"Checking Permission !");
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

    private void EnableGPSAutoMatically() {
        generateLog.appendLog(tag,"Enable gps !");
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
                            generateLog.appendLog(tag,"Enabled gps !");
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

    @Override
    protected void onStop() {
        super.onStop();
//        unregisterReceiver(new Receiver());
    }

    public class Receiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (isConnected) {
                load=1;
                findViewById(R.id.network_status).setVisibility(View.VISIBLE);
            }
            else {
                findViewById(R.id.network_status).setVisibility(View.GONE);
                if (load==1 && anim_handle) {
                    Intent i = new Intent(WelcomeScreen.this, PhoneAuthActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.animation_slide1, R.anim.animation_slide2);
                    finish();
                }
            }
        }
    }

    private void handleoffer() {
        if (log_id.contains("offer")){
            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("ReferalCode/"+log_id.getString("id",null));
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("referredby")){
                        String str=dataSnapshot.child("referredby").getValue(String.class);
                        final DatabaseReference db=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+str);
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("101")) {
                                        Integer val=Integer.parseInt(dataSnapshot.child("101").getValue(String.class));
                                        db.child("101").setValue(String.valueOf(val+1));
                                        ref.child("referredby").removeValue();
                                    }
                                    else {
                                        db.child("101").setValue("1");
                                        ref.child("referredby").removeValue();
                                    }
                                }
                                else {
                                    db.child("101").setValue("1");
                                    ref.child("referredby").removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            final DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+log_id.getString("id",null));
            dref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (log_id.contains("offer")) {
                            if (dataSnapshot.hasChild(log_id.getString("offer", null))) {

                                Integer val = Integer.parseInt(dataSnapshot.child(log_id.getString("offer", null)).getValue(String.class));
                                if (val == 1)
                                    dref.child(log_id.getString("offer", null)).removeValue();
                                else
                                    dref.child(log_id.getString("offer", null)).setValue(String.valueOf(val - 1));
                                SharedPreferences.Editor ed = log_id.edit();
                                ed.remove("offer");
                                ed.commit();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            final DatabaseReference off_ref=FirebaseDatabase.getInstance().getReference("SpecialOffer/"+log_id.getString("offer", null));
            off_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        off_ref.removeValue();
                        SharedPreferences.Editor ed = log_id.edit();
                        ed.remove("offer");
                        ed.commit();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
