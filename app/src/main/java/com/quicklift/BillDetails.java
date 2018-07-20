package com.quicklift;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quicklift.mail.Config;
import com.quicklift.mail.SendMail;

import java.util.ResourceBundle;

public class BillDetails extends AppCompatActivity {
    String rideid;
    DatabaseReference db;
    private SharedPreferences log_id;
    private ProgressDialog progressDialog;
    float total=0,cancel=0,parking=0,timing=0,waiting=0,tax=0,offer=0;

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
        setContentView(R.layout.activity_bill_details);

        getSupportActionBar().setTitle("Trip Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        rideid=getIntent().getStringExtra("rideid");
        db= FirebaseDatabase.getInstance().getReference("Rides/"+rideid);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.timestamp)).setText(dataSnapshot.child("time").getValue().toString());
                    ((TextView)findViewById(R.id.source)).setText(dataSnapshot.child("source").getValue().toString());
                    ((TextView)findViewById(R.id.destination)).setText(dataSnapshot.child("destination").getValue().toString());
                    if (dataSnapshot.hasChild("parking") && !dataSnapshot.child("parking").getValue().toString().equals("0")) {
                        findViewById(R.id.parkingLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.parking)).setText("Rs. "+dataSnapshot.child("parking").getValue().toString());
                        parking=(float) Float.parseFloat(dataSnapshot.child("parking").getValue().toString());
                        total=total+(float) Float.parseFloat(dataSnapshot.child("parking").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("waiting") && !dataSnapshot.child("waiting").getValue().toString().equals("0")) {
                        findViewById(R.id.waitingLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.waiting)).setText("Rs. "+dataSnapshot.child("waiting").getValue().toString());
                        waiting=(float) Float.parseFloat(dataSnapshot.child("waiting").getValue().toString());
                        total=total+(float) Float.parseFloat(dataSnapshot.child("waiting").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("timing") && !dataSnapshot.child("timing").getValue().toString().equals("0")) {
                        findViewById(R.id.timingLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.timing)).setText("Rs. "+dataSnapshot.child("timing").getValue().toString());
                        timing=(float) Float.parseFloat(dataSnapshot.child("timing").getValue().toString());
                        total=total+(float) Float.parseFloat(dataSnapshot.child("timing").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("cancel_charge") && !dataSnapshot.child("cancel_charge").getValue().toString().equals("0")) {
                        findViewById(R.id.cancelLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.cancel_charge)).setText("Rs. "+dataSnapshot.child("cancel_charge").getValue().toString());
                        cancel=(float) Float.parseFloat(dataSnapshot.child("cancel_charge").getValue().toString());
//                        total=total+(float) Float.parseFloat(dataSnapshot.child("cancel_charge").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("discount") && !dataSnapshot.child("discount").getValue().toString().equals("0")) {
                        findViewById(R.id.discountLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.discount)).setText("Rs. "+dataSnapshot.child("discount").getValue().toString());
                        offer=(float) Float.parseFloat(dataSnapshot.child("discount").getValue().toString());
//                        total=total+(float) Float.parseFloat(dataSnapshot.child("cancel_charge").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("tax") && !dataSnapshot.child("tax").getValue().toString().equals("0")) {
                        findViewById(R.id.taxLayout).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.tax)).setText("Rs. "+dataSnapshot.child("tax").getValue().toString());
                        tax=(float) Float.parseFloat(dataSnapshot.child("tax").getValue().toString());
                        total=total+(float) Float.parseFloat(dataSnapshot.child("tax").getValue().toString());
                    }
                    if (dataSnapshot.hasChild("status")) {
                        if (dataSnapshot.child("status").getValue().toString().equals("Cancelled")) {
                            ((TextView) findViewById(R.id.status)).setText(dataSnapshot.child("status").getValue().toString());
                        } else if (dataSnapshot.child("status").getValue().toString().equals("Canceled By Driver")) {
                            ((TextView) findViewById(R.id.status)).setText("CANCELLED");
                        }
                    }
                    ((TextView)findViewById(R.id.paymode)).setText(dataSnapshot.child("paymode").getValue().toString());
                    float base=(float) Float.parseFloat(dataSnapshot.child("amount").getValue().toString()) - total +Float.parseFloat(dataSnapshot.child("discount").getValue().toString());
                    ((TextView)findViewById(R.id.basefare)).setText("Rs. "+String.format("%.2f",(base)));
                    ((TextView)findViewById(R.id.total)).setText("Rs. "+String.valueOf(total+base+cancel));

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Drivers/"+dataSnapshot.child("driver").getValue().toString());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                ((TextView)findViewById(R.id.name)).setText(dataSnapshot.child("name").getValue().toString());
                                ((TextView)findViewById(R.id.rating)).setText(String.format("%.2f", (float)Float.parseFloat(dataSnapshot.child("rate").getValue().toString())));
                                if (!dataSnapshot.child("thumb").getValue().toString().equals("")) {
                                    byte[] dec = Base64.decode(dataSnapshot.child("thumb").getValue().toString(), Base64.DEFAULT);
                                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
                                    ((ImageView)findViewById(R.id.profile_pic)).setImageBitmap(decbyte);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    DatabaseReference dref=FirebaseDatabase.getInstance().getReference("VehicleDetails/Patna/"+dataSnapshot.child("driver").getValue().toString());
                    dref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                ((TextView)findViewById(R.id.vehiclemodel)).setText(dataSnapshot.child("model").getValue().toString());
                                ((TextView)findViewById(R.id.vehicleno)).setText(dataSnapshot.child("number").getValue().toString());
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
    }

    public void sendinvoice(View view){
        progressDialog=new ProgressDialog(this,ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setMessage("Please Wait !!!");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null)+"/email");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.getValue().toString().equals("")){
                        progressDialog.dismiss();
                        Toast.makeText(BillDetails.this, "No email id found ! \n Please enter email id in profile !", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        sendEmail(dataSnapshot.getValue().toString());
                    }
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(BillDetails.this, "No email id found !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean sendEmail(String emailID){
        //Getting content for email
        String subject = "Invoice for trip.";
        String message = "Dear Customer,\n" +
                "Thankyou for using \"QuickLift\" Service. Here is your invoice for the trip.\n\n\n" +
                "Time : "+((TextView)findViewById(R.id.timestamp)).getText().toString()+"\n" +
                "Source : "+((TextView)findViewById(R.id.source)).getText().toString()+"\n" +
                "Destination : "+((TextView)findViewById(R.id.destination)).getText().toString()+"\n" +
                "Driver : "+((TextView)findViewById(R.id.name)).getText().toString()+" ("+((TextView)findViewById(R.id.rating)).getText().toString()+"/5)"+"\n" +
                "Vehicle : "+((TextView)findViewById(R.id.vehiclemodel)).getText().toString()+" - "+((TextView)findViewById(R.id.vehiclemodel)).getText().toString()+"\n\n" +
                "Base Fare : "+((TextView)findViewById(R.id.basefare)).getText().toString()+"\n";

        if (parking!=0)
            message=message+"Tolls/Parking : "+((TextView)findViewById(R.id.parking)).getText().toString()+"\n";
        if (timing!=0)
            message=message+"Timing Charge : "+((TextView)findViewById(R.id.timing)).getText().toString()+"\n";
        if (waiting!=0)
            message=message+"Waiting Charge : "+((TextView)findViewById(R.id.waiting)).getText().toString()+"\n";
        if (cancel!=0)
            message=message+"Cancellation Charge : "+((TextView)findViewById(R.id.cancel_charge)).getText().toString()+"\n";
        if (offer!=0)
            message=message+"Offer : "+((TextView)findViewById(R.id.discount)).getText().toString()+"\n";

                message=message+"Tax : "+((TextView)findViewById(R.id.tax)).getText().toString()+"\n\n" +
                "Total : "+((TextView)findViewById(R.id.total)).getText().toString()+"\n\n" +
                "Payment Mode : "+((TextView)findViewById(R.id.paymode)).getText().toString()+"\n\n\n\n" +
                "Regards,\nQuickLift Team";
        //Creating SendMail object
        SendMail sm = new SendMail(this, emailID, subject, message);
        //Executing sendmail to send email
        progressDialog.dismiss();
        sm.execute();
        return true;
    }
}
