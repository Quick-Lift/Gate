package com.quicklift;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhoneAuthActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    SharedPreferences log_id;
    SharedPreferences.Editor editor;
    Receiver receiver=new Receiver();
    int load=0;

    @Override
    public void onBackPressed() {
        System.exit(0);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        getSupportActionBar().setTitle("Phone Verification");
        // ...
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        editor=log_id.edit();

//        boolean status1 = haveNetworkConnection();
//        boolean status2 = hasActiveInternetConnection();
//        if (status1 && status2) {
//
//        }
//        else {
//            Toast.makeText(this, "No internet access !", Toast.LENGTH_LONG).show();
//        }
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, intentFilter);

//        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//        registerReceiver(receiver, intentFilter);
// Choose authentication providers
//        if (!log_id.contains("id")) {
//            List<AuthUI.IdpConfig> providers = Arrays.asList(
//                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
//
//// Create and launch sign-in intent
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setAvailableProviders(providers)
//                            .build(),
//                    RC_SIGN_IN);
//        }
//        else {
//            startActivity(new Intent(PhoneAuthActivity.this,Home.class));
//            finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final ProgressDialog progress=new ProgressDialog(PhoneAuthActivity.this);
                progress.setCancelable(false);
                progress.setCanceledOnTouchOutside(false);
                progress.setIndeterminate(true);
                progress.setMessage("Phone Verified Successfully !");
                progress.show();
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                Toast.makeText(this, user.getUid()+" "+user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users/"+user.getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (progress.isShowing())
                            progress.dismiss();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAuthActivity.this,R.style.myBackgroundStyle);
//                            builder.setMessage("This number belongs to "+dataSnapshot.child("name").getValue(String.class))
//                                    .setCancelable(true)
//                                    .setTitle("Account Action !")
//                                    .setPositiveButton("Continue ...", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            final SQLQueries sqlQueries=new SQLQueries(PhoneAuthActivity.this);
//                                            sqlQueries.deletefare();
//                                            sqlQueries.deletelocation();
//                                            DatabaseReference db= FirebaseDatabase.getInstance().getReference("Fare/Patna");
//                                            db.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                                    for (DataSnapshot data:dataSnapshot.child("Package").getChildren()){
//                                                        ArrayList<String> price=new ArrayList<String>();
//                                                        price.add(data.child("Latitude").getValue(String.class));
//                                                        price.add(data.child("Longitude").getValue(String.class));
//                                                        price.add(data.child("Amount").getValue(String.class));
//                                                        price.add(data.child("Distance").getValue(String.class));
//
//                                                        sqlQueries.savelocation(price);
//                                                    }
//                                                    for (DataSnapshot data:dataSnapshot.child("Price").getChildren()){
//                                                        ArrayList<String> price=new ArrayList<String>();
//                                                        price.add(data.child("NormalTime/BaseFare/Amount").getValue(String.class));
//                                                        price.add(data.child("NormalTime/BaseFare/Distance").getValue(String.class));
//                                                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
//                                                        price.add(data.child("NormalTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
//                                                        price.add(data.child("NormalTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
//                                                        price.add(data.child("NormalTime/Time").getValue(String.class));
//
//                                                        sqlQueries.savefare(price);
////                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");
//
//                                                        price.clear();
//                                                        price.add(data.child("PeakTime/BaseFare/Amount").getValue(String.class));
//                                                        price.add(data.child("PeakTime/BaseFare/Distance").getValue(String.class));
//                                                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Amount").getValue(String.class));
//                                                        price.add(data.child("PeakTime/BeyondLimit/FirstLimit/Distance").getValue(String.class));
//                                                        price.add(data.child("PeakTime/BeyondLimit/SecondLimit/Amount").getValue(String.class));
//                                                        price.add(data.child("PeakTime/Time").getValue(String.class));
//
//                                                        sqlQueries.savefare(price);
////                    Toast.makeText(WelcomeScreen.this, ""+"hi", Toast.LENGTH_SHORT).show();
////                    Log.v("TAG",price.get(0)+" "+price.get(1)+" "+price.get(2)+" "+price.get(3)+" "+price.get(4)+" "+price.get(5)+" ");
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onCancelled(DatabaseError databaseError) {
//
//                                                }
//                                            });
//
//                                            editor.putString("id",user.getUid());
//                                            editor.putString("driver","");
//                                            editor.commit();
//                                            startActivity(new Intent(PhoneAuthActivity.this,Home.class));
//                                            finish();
//                                        }
//                                    })
//                                    .setNeutralButton("New User !", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            //  Action for 'NO' Button
//                                            Intent intent=new Intent(PhoneAuthActivity.this,CustomerRegistration.class);
//                                            intent.putExtra("phone",String.valueOf(user.getPhoneNumber()));
//                                            intent.putExtra("key",user.getUid());
//                                            startActivity(intent);
//                                            finish();
//                                        }
//                                    });
//
//                            //Creating dialog box
//                            AlertDialog alert = builder.create();
//                            //Setting the title manually
//                            alert.show();
//                            alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("#000000"));
//                            alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#000000"));
                            View view=getLayoutInflater().inflate(R.layout.confirm_existing_layout,null);
//                            TextView title=(TextView)view.findViewById(R.id.title);
                            TextView message=(TextView)view.findViewById(R.id.message);
                            TextView left=(TextView) view.findViewById(R.id.left_btn);
                            Button right=(Button) view.findViewById(R.id.right_btn);

//                            title.setText("Account Action !");
                            message.setText("Are you "+dataSnapshot.child("name").getValue(String.class)+" ?");
//                            left.setText("No");
                            right.setText("Yes");

                            left.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent=new Intent(PhoneAuthActivity.this,CustomerRegistration.class);
                                    intent.putExtra("phone",String.valueOf(user.getPhoneNumber()));
                                    intent.putExtra("key",user.getUid());
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            right.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final SQLQueries sqlQueries=new SQLQueries(PhoneAuthActivity.this);
                                    sqlQueries.deletefare();
                                    sqlQueries.deletelocation();
                                    DatabaseReference db= FirebaseDatabase.getInstance().getReference("Fare/Patna");
                                    db.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
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
                                            editor.putString("normaltimeradius",String.valueOf(dataSnapshot.child("NormalTimeSearchRadius").getValue().toString()));
                                            editor.putString("peaktimeradius",String.valueOf(dataSnapshot.child("PeakTimeSearchRadius").getValue().toString()));
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

                                    editor.putString("id",user.getUid());
                                    editor.putString("driver","");
                                    editor.commit();
                                    startActivity(new Intent(PhoneAuthActivity.this,Home.class));
                                    finish();
                                }
                            });

                            AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAuthActivity.this);
                            builder .setView(view)
                                    .setCancelable(false);

                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        else {
                            if (progress.isShowing())
                            progress.dismiss();
                            Intent intent=new Intent(PhoneAuthActivity.this,CustomerRegistration.class);
                            intent.putExtra("phone",String.valueOf(user.getPhoneNumber()));
                            intent.putExtra("key",user.getUid());
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
                Toast.makeText(this, "Signin failed ! Please try again later !", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
            }
        }
    }

    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (isConnected) {
                load=1;
                findViewById(R.id.msg).setVisibility(View.VISIBLE);
//                Toast.makeText(PhoneAuthActivity.this, "No internet access !", Toast.LENGTH_LONG).show();
            }
            else {
                load=0;
                findViewById(R.id.msg).setVisibility(View.GONE);
//                if (!log_id.contains("id")) {
//                    List<AuthUI.IdpConfig> providers = Arrays.asList(
//                            new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
//
//// Create and launch sign-in intent
//                    startActivityForResult(
//                            AuthUI.getInstance()
//                                    .createSignInIntentBuilder()
//                                    .setAvailableProviders(providers)
//                                    .build(),
//                            RC_SIGN_IN);
//                } else {
//                    startActivity(new Intent(PhoneAuthActivity.this, Home.class));
//                    finish();
//                }
                if (!log_id.contains("id")) {
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.PhoneBuilder().build());

// Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                } else {
                    startActivity(new Intent(PhoneAuthActivity.this, Home.class));
                    finish();
                }
            }
        }
    }
}
