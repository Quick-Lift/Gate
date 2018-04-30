package com.quicklift;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhoneAuthActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    SharedPreferences log_id;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        getSupportActionBar().setTitle("Phone Verification");
        // ...
        log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
        editor=log_id.edit();
// Choose authentication providers
        if (!log_id.contains("id")) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }
        else {
            startActivity(new Intent(PhoneAuthActivity.this,Home.class));
            finish();
        }
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
                            progress.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAuthActivity.this);
                            builder.setMessage("This number belongs to "+dataSnapshot.child("name").getValue(String.class))
                                    .setCancelable(true)
                                    .setPositiveButton("Continue ...", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final SQLQueries sqlQueries=new SQLQueries(PhoneAuthActivity.this);
                                            sqlQueries.deletefare();
                                            sqlQueries.deletelocation();
                                            DatabaseReference db= FirebaseDatabase.getInstance().getReference("Fare/Patna");
                                            db.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
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
                                    })
                                    .setNeutralButton("New User !", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //  Action for 'NO' Button
                                            Intent intent=new Intent(PhoneAuthActivity.this,CustomerRegistration.class);
                                            intent.putExtra("phone",String.valueOf(user.getPhoneNumber()));
                                            intent.putExtra("key",user.getUid());
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                            //Creating dialog box
                            AlertDialog alert = builder.create();
                            //Setting the title manually
                            alert.setTitle("Account Action !");
                            alert.show();
                            alert.getButton(alert.BUTTON_POSITIVE).setTextColor(Color.parseColor("red"));
                            alert.getButton(alert.BUTTON_NEGATIVE).setTextColor(Color.parseColor("red"));
                        }
                        else {
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
}
