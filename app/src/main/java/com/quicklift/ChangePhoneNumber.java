package com.quicklift;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class ChangePhoneNumber extends AppCompatActivity {
    String vid;
    EditText phone,code;
    TextView message;
    FirebaseAuth mAuth;
    String message_phone="Please enter your phone number (10 digits only)";
    String message_code="Please enter the verification code sent to your mobile number";
    LinearLayout layout_phone,layout_code;
    ProgressDialog pdialog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone_number);

        getSupportActionBar().setTitle("Change PhoneNumber");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mAuth=FirebaseAuth.getInstance();

        phone=(EditText)findViewById(R.id.phone);
        code=(EditText)findViewById(R.id.code);
        message=(TextView) findViewById(R.id.message);
        layout_phone=(LinearLayout)findViewById(R.id.layout_phone);
        layout_code=(LinearLayout)findViewById(R.id.layout_code);
        message.setText(message_phone);
        layout_code.setVisibility(View.GONE);
        pdialog=new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
        pdialog.setMessage("Sending OTP ...");
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);
    }

    public void sendotp(View view){
        if (TextUtils.isEmpty(phone.getText().toString())){
            phone.setError("Required");
        } else if (phone.getText().toString().length()!=10){
            phone.setError("10 Digits only");
        } else {
            pdialog.show();
            String num="+91"+phone.getText().toString();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    num,        // Phone number to verify
                    120,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//                            Toast.makeText(ChangePhoneNumber.this, "Verification Comp", Toast.LENGTH_SHORT).show();
//                            FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(phoneAuthCredential)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()){
//                                                Toast.makeText(ChangePhoneNumber.this, "Success !", Toast.LENGTH_SHORT).show();
//                                            }
//                                            else {
//                                                Toast.makeText(ChangePhoneNumber.this, "Failed !", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
                            mAuth.getCurrentUser().updatePhoneNumber(phoneAuthCredential)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            if (pdialog.isShowing())
                                                pdialog.dismiss();
                                            if (e.toString().contains("com.google.firebase.auth.FirebaseAuthUserCollisionException")){
                                                Toast.makeText(ChangePhoneNumber.this, "This phone is already registered !", Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                Toast.makeText(ChangePhoneNumber.this, "Verification Failed ! Try Again Later ! "+e.toString(), Toast.LENGTH_LONG).show();
                                            }
// Log.v("MESSAGE","/"+e.toString()+"/");
                                            finish();
                                        }
                                    })
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            if (pdialog.isShowing())
//                                                pdialog.dismiss();
////                                            Toast.makeText(ChangePhoneNumber.this, ""+aVoid.toString(), Toast.LENGTH_SHORT).show();
//                                            SharedPreferences log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
//
//                                            DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null)+"/phone");
//                                            db.setValue(phone.getText().toString());
//
//                                            Toast.makeText(ChangePhoneNumber.this, "Phone Number Changed !", Toast.LENGTH_SHORT).show();
//                                            finish();
//                                        }
//                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                if (pdialog.isShowing())
                                                    pdialog.dismiss();
                                                SharedPreferences log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);

                                                DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null)+"/phone");
                                                db.setValue(phone.getText().toString());

                                                Toast.makeText(ChangePhoneNumber.this, "Phone Number Changed !", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
//                                            else {
//                                                if (pdialog.isShowing())
//                                                pdialog.dismiss();
//                                                Toast.makeText(ChangePhoneNumber.this, "Verification Failed ! Try Again Later ! "+task.getResult().toString(), Toast.LENGTH_SHORT).show();
//                                                finish();
//                                            }
                                        }
                                    });

                        }

                        @Override
                        public void onCodeAutoRetrievalTimeOut(String s) {
                            super.onCodeAutoRetrievalTimeOut(s);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            if (pdialog.isShowing())
                                pdialog.dismiss();
                            Toast.makeText(ChangePhoneNumber.this, "Verification Failed ! Please Try Again !", Toast.LENGTH_SHORT).show();
                            pdialog.setMessage("Sending OTP ...");
                            message.setText(message_phone);
                            code.setText("");
                            phone.setText("");
                            layout_phone.setVisibility(View.VISIBLE);
                            layout_code.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            if (pdialog.isShowing())
                                pdialog.dismiss();
                            pdialog.setMessage("Verifying OTP ...");
                            message.setText(message_code);
                            layout_phone.setVisibility(View.GONE);
                            layout_code.setVisibility(View.VISIBLE);
//                            Toast.makeText(ChangePhoneNumber.this, "Sent", Toast.LENGTH_SHORT).show();
                            vid=s;
                        }
                    });
        }
    }

    public void verifyotp(View view){
//        String code=((EditText)findViewById(R.id.code)).getText().toString();
        pdialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(vid, code.getText().toString());
        mAuth.getCurrentUser().updatePhoneNumber(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (pdialog.isShowing())
                            pdialog.dismiss();
                        SharedPreferences log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);

                        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null)+"/phone");
                        db.setValue(phone.getText().toString());

                        Toast.makeText(ChangePhoneNumber.this, "Phone Number Changed !", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pdialog.isShowing())
                            pdialog.dismiss();
                        code.setError("Wrong Verification Code");
//                        Toast.makeText(ChangePhoneNumber.this, "Wrong Verification Code ! "+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//                        finish();
                    }
                });
    }
}
