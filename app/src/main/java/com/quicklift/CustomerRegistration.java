package com.quicklift;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerRegistration extends AppCompatActivity {
    EditText name,email,phone,password,address,refcode;
    CircleImageView pic;
    ProgressDialog pdialog;
    String upload_img="";
    Uri selectedImage=null;
    private StorageReference mStorageRef;
    String key,phone_no;
    DatabaseReference user_db= FirebaseDatabase.getInstance().getReference("Users");

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
        setContentView(R.layout.activity_customer_registration);

        getSupportActionBar().setTitle("Registration");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pdialog=new ProgressDialog(this);
        key=getIntent().getStringExtra("key");
        phone_no=getIntent().getStringExtra("phone");

        // [START initialize_auth]
        mStorageRef = FirebaseStorage.getInstance().getReference();

        name=(EditText)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        password=(EditText)findViewById(R.id.password);
        address=(EditText)findViewById(R.id.address);
        refcode=(EditText)findViewById(R.id.refcode);
        pic=(CircleImageView) findViewById(R.id.image);

        phone.setText(phone_no.substring(3));
        phone.setInputType(0);
        phone.setVisibility(View.GONE);

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photo = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                final int ACTIVITY_SELECT_IMAGE = 1234;
                startActivityForResult(photo, ACTIVITY_SELECT_IMAGE);
            }
        });
    }

//    public void signup(View v){
//        if (!validateForm()){
//
//        }
//
//        else {
//            showProgressDialog();
//            // [START create_user_with_email]
//            String em=email.getText().toString();
//            String pass=password.getText().toString();
//            mAuth.createUserWithEmailAndPassword(em,pass )
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                // Sign in success, update UI with the signed-in user's information
//                                //Log.d(TAG, "createUserWithEmail:success");
//                                FirebaseUser user = mAuth.getCurrentUser();
//                                sendEmailVerification();
//                                updateUI(user);
//                            } else {
//                                // If sign in fails, display a message to the user.
//                                //Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                Toast.makeText(CustomerRegistration.this, "Authentication failed."+task.getException(), Toast.LENGTH_SHORT).show();
//                                updateUI(null);
//                            }
//                            // [START_EXCLUDE]
//                            hideProgressDialog();
//                            // [END_EXCLUDE]
//                        }
//                    });
//            // [END create_user_with_email]
//        }
//    }

    public void updateUI(View view) {
//        hideProgressDialog();
        if (validateForm()) {
            Customer customer=new Customer();
            customer.setName(name.getText().toString());
            customer.setEmail(email.getText().toString());
            customer.setPhone(phone.getText().toString());
            customer.setAddress(address.getText().toString());
            customer.setThumb(upload_img);

            if (selectedImage!=null){
                StorageReference riversRef = mStorageRef.child("Users/"+key);

                UploadTask uploadTask = riversRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(CustomerRegistration.this, "Failed to upload image !", Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(CustomerRegistration.this, "Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                //Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }

            user_db.child(key).setValue(customer);
            final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("ReferalCode");
            ref.child(key+"/code").setValue(phone.getText().toString()+"@qik");
            DatabaseReference newref=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+key);
            newref.child("100").setValue("1");
            if (!refcode.getText().toString().equals("")){
                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("ReferalCode");
                reference.orderByChild("code").equalTo(refcode.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot dt:dataSnapshot.getChildren()){
//                            DatabaseReference dref=FirebaseDatabase.getInstance().getReference("CustomerOffers/"+dt.getKey());
//                            dref.child("101").setValue("true");
                            ref.child(key+"/referredby").setValue(dt.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            SharedPreferences log_id=getApplicationContext().getSharedPreferences("Login",MODE_PRIVATE);
            final SharedPreferences.Editor editor=log_id.edit();
            //Toast.makeText(CustomerRegistration.this, ""+user.getUid(), Toast.LENGTH_SHORT).show();
            //Log.v("TAG",user.getUid());
            editor.putString("id",key);
            editor.putString("driver","");
            editor.commit();
            Toast.makeText(this, "Successfully Registered !", Toast.LENGTH_SHORT).show();

            final SQLQueries sqlQueries=new SQLQueries(CustomerRegistration.this);
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
            //mAuth.signOut();
            startActivity(new Intent(CustomerRegistration.this,Home.class));
            finish();

        }
    }

//    private void sendEmailVerification() {
//        // Send verification email
//        // [START send_email_verification]
//        final FirebaseUser user = mAuth.getCurrentUser();
//        user.sendEmailVerification()
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        // [START_EXCLUDE]
//                        if (task.isSuccessful()) {
//                            Toast.makeText(CustomerRegistration.this, "Verification email sent to " + user.getEmail(),
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(CustomerRegistration.this, "Failed to send verification email.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        // [END_EXCLUDE]
//                    }
//                });
//        // [END send_email_verification]
//    }

    private void hideProgressDialog() {
        pdialog.dismiss();
    }

    private void showProgressDialog() {
        pdialog.setCanceledOnTouchOutside(false);
        pdialog.setIndeterminate(false);
        pdialog.setMessage("Please Wait  ...");
        pdialog.show();
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required.");
            valid = false;
        } else {
            name.setError(null);
        }

        if (TextUtils.isEmpty(phone.getText().toString())) {
            phone.setError("Required.");
            valid = false;
        } else {
            phone.setError(null);
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        if (TextUtils.isEmpty(address.getText().toString())) {
            address.setError("Required.");
            valid = false;
        } else {
            address.setError(null);
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1234:
                if(resultCode == RESULT_OK){
                    showProgressDialog();
                    if (data.getData()!=null) {
                        Uri uri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            //image.setImageBitmap(bitmap);
                            pic.setImageURI(uri);
                            selectedImage=uri;
                            Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] byteFormat = stream.toByteArray();
                            upload_img = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(this, "Unable to get image. Please try again !!", Toast.LENGTH_SHORT).show();
                    }

                    hideProgressDialog();
                }
                break;
        }
    }


    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.NO_WRAP);
        return temp;
    }
}
