package com.quicklift;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quicklift.mail.Config;
import com.quicklift.mail.SendMail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BillDetails extends AppCompatActivity {
    String rideid;
    DatabaseReference db;
    private SharedPreferences log_id;
    private ProgressDialog progressDialog;
    float total=0,cancel=0,parking=0,timing=0,waiting=0,tax=0,offer=0;
    private ArrayList<String> charges=new ArrayList<>();
    private ArrayList<String> price=new ArrayList<>();
    TextView src,dest,time,driver,veh,mode,vehno;

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

        src=(TextView)findViewById(R.id.source);
        dest=(TextView)findViewById(R.id.destination);
        time=(TextView)findViewById(R.id.timestamp);
        driver=(TextView)findViewById(R.id.name);
        veh=(TextView)findViewById(R.id.vehiclemodel);
        vehno=(TextView)findViewById(R.id.vehicleno);
        mode=(TextView)findViewById(R.id.paymode);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    charges.clear();
                    price.clear();
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
                            findViewById(R.id.invoice_layout).setVisibility(View.GONE);
                            ((TextView) findViewById(R.id.status)).setText(dataSnapshot.child("status").getValue().toString());
                        } else if (dataSnapshot.child("status").getValue().toString().equals("Canceled By Driver")) {
                            ((TextView) findViewById(R.id.status)).setText("CANCELLED");
                            findViewById(R.id.invoice_layout).setVisibility(View.GONE);
                        }
                    }
                    ((TextView)findViewById(R.id.paymode)).setText(dataSnapshot.child("paymode").getValue().toString());
                    float base=(float) Float.parseFloat(dataSnapshot.child("amount").getValue().toString()) - total +Float.parseFloat(dataSnapshot.child("discount").getValue().toString());
                    ((TextView)findViewById(R.id.basefare)).setText("Rs. "+String.format("%.2f",(base)));
                    ((TextView)findViewById(R.id.total)).setText("Rs. "+String.valueOf(total+base+cancel));

                    charges.add("Base Fare");
                    price.add("Rs. "+String.format("%.2f",(base)));

                    if (parking!=0){
                        charges.add("Parking");
                        price.add("Rs. "+String.valueOf(parking));
                    }
                    if (waiting!=0){
                        charges.add("Waiting Charge");
                        price.add("Rs. "+String.valueOf(waiting));
                    }
                    if (timing!=0){
                        charges.add("Timing Charge");
                        price.add("Rs. "+String.valueOf(timing));
                    }
                    if (cancel!=0){
                        charges.add("Cancel Charge");
                        price.add("Rs. "+String.valueOf(cancel));
                    }
                    if (tax!=0){
                        charges.add("Tax");
                        price.add("Rs. "+String.valueOf(tax));
                    }
                    if (offer!=0){
                        charges.add("Offer");
                        price.add("Rs. "+String.valueOf(offer));
                    }
                    charges.add("Total");
                    price.add("Rs. "+String.valueOf(total+base+cancel));

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

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("email") && dataSnapshot.child("email").getValue().toString().equals("")){
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
//        charges.add("Base Fare");
//        charges.add("Parking");
//        charges.add("Waiting Charge");
//        charges.add("Timing Charge");
//        charges.add("Cancel Charge");
//        charges.add("Tax");
//        charges.add("Offer");
//        charges.add("Total");
//        price.add("Rs. 100");
//        price.add("Rs. 30");
//        price.add("Rs. 70");
//        price.add("Rs. 20");
//        price.add("Rs. 10");
//        price.add("Rs. 20");
//        price.add("Rs. 10");
//        price.add("Rs. 260");
        String subject = "Invoice for trip.";
        String message="Thank you for using our service . \nThe invoice is attached with this mail.";
        invoice(emailID,subject,message);
//        String message = "Dear Customer,\n" +
//                "Thankyou for using \"QuickLift\" Service. Here is your invoice for the trip.\n\n\n" +
//                "Time : "+((TextView)findViewById(R.id.timestamp)).getText().toString()+"\n" +
//                "Source : "+((TextView)findViewById(R.id.source)).getText().toString()+"\n" +
//                "Destination : "+((TextView)findViewById(R.id.destination)).getText().toString()+"\n" +
//                "Driver : "+((TextView)findViewById(R.id.name)).getText().toString()+" ("+((TextView)findViewById(R.id.rating)).getText().toString()+"/5)"+"\n" +
//                "Vehicle : "+((TextView)findViewById(R.id.vehiclemodel)).getText().toString()+" - "+((TextView)findViewById(R.id.vehiclemodel)).getText().toString()+"\n\n" +
//                "Base Fare : "+((TextView)findViewById(R.id.basefare)).getText().toString()+"\n";
//
//        if (parking!=0)
//            message=message+"Tolls/Parking : "+((TextView)findViewById(R.id.parking)).getText().toString()+"\n";
//        if (timing!=0)
//            message=message+"Timing Charge : "+((TextView)findViewById(R.id.timing)).getText().toString()+"\n";
//        if (waiting!=0)
//            message=message+"Waiting Charge : "+((TextView)findViewById(R.id.waiting)).getText().toString()+"\n";
//        if (cancel!=0)
//            message=message+"Cancellation Charge : "+((TextView)findViewById(R.id.cancel_charge)).getText().toString()+"\n";
//        if (offer!=0)
//            message=message+"Offer : "+((TextView)findViewById(R.id.discount)).getText().toString()+"\n";
//
//                message=message+"Tax : "+((TextView)findViewById(R.id.tax)).getText().toString()+"\n\n" +
//                "Total : "+((TextView)findViewById(R.id.total)).getText().toString()+"\n\n" +
//                "Payment Mode : "+((TextView)findViewById(R.id.paymode)).getText().toString()+"\n\n\n\n" +
//                "Regards,\nQuickLift Team";
//        //Creating SendMail object
//        SendMail sm = new SendMail(this, emailID, subject, message);
//        //Executing sendmail to send email
//        progressDialog.dismiss();
//        sm.execute();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void invoice(String emailID,String subject,String message){
//        if (isStoragePermissionGranted()){
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(576, 792, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int titleBaseLine = 30;
        int source_title = 30;
        int leftMargin = 30;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(10);
        paint.setStrokeWidth((float) 1.2);

        TextPaint txt = new TextPaint();
        txt.setTextSize(10);
        txt.setStrokeWidth((float) 1.2);
        canvas.drawLine(0,1,pageInfo.getPageWidth(),1,paint);
        canvas.drawLine(0, pageInfo.getPageHeight(), pageInfo.getPageWidth(), pageInfo.getPageHeight(), paint);
        canvas.drawLine(1, 1, 1, pageInfo.getPageHeight(), paint);
        canvas.drawLine(pageInfo.getPageWidth(), 0, pageInfo.getPageWidth(), pageInfo.getPageHeight(), paint);

//            byte[] decodedString = Base64.decode(pref.getString("h_img", null), Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        //Bitmap icon = BitmapFactory.decodeResource(Display_descActivity.this.getResources(),
        //      R.drawable.doctor);
        Bitmap decodedByte = BitmapFactory.decodeResource(getResources(),R.drawable.logo);
        Bitmap newBitmap = Bitmap.createScaledBitmap(decodedByte, 50, 50, true);
        canvas.drawBitmap(newBitmap, leftMargin+20, titleBaseLine+10, txt);

        txt.setColor(Color.parseColor("#05affc"));
        txt.setTextSize(18);
        StaticLayout staticLayout = new StaticLayout("QuickLift Solution Private Limited", txt, pageInfo.getPageWidth()-230, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(105, titleBaseLine+33);
        staticLayout.draw(canvas);
        canvas.restore();

        titleBaseLine=titleBaseLine+85;

        txt.setTextSize(26);
        staticLayout = new StaticLayout(price.get(price.size()-1), txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);
        canvas.save();
        canvas.translate(0, titleBaseLine);
        staticLayout.draw(canvas);
        canvas.restore();

        titleBaseLine+=35;
        txt.setTextSize(16);
        staticLayout = new StaticLayout(rideid, txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);
        canvas.save();
        canvas.translate(0, titleBaseLine);
        staticLayout.draw(canvas);
        canvas.restore();
        paint.setColor(Color.parseColor("#05affc"));
        canvas.drawLine(80, titleBaseLine+10,190, titleBaseLine+10 , paint);
        canvas.drawLine(385, titleBaseLine+10,pageInfo.getPageWidth()-80, titleBaseLine+10 , paint);

        titleBaseLine+=28;
        txt.setTextSize(14);
        staticLayout = new StaticLayout("Thank you Adarsh Verma for using Doorbell. Have a nice day !", txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, false);
        canvas.save();
        canvas.translate(0, titleBaseLine);
        staticLayout.draw(canvas);
        canvas.restore();

        titleBaseLine+=25;
        paint.setColor(Color.parseColor("#05affc"));
        canvas.drawLine(40, titleBaseLine,pageInfo.getPageWidth()-40, titleBaseLine , paint);

        titleBaseLine+=15;
        txt.setTextSize(16);
        txt.setStrokeWidth((float)3.5);
        staticLayout = new StaticLayout("Ride Details", txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(80, titleBaseLine);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout("Bill Details", txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(395, titleBaseLine);
        staticLayout.draw(canvas);
        canvas.restore();

        titleBaseLine+=23;
        paint.setStrokeWidth((float)2);
        canvas.drawLine(75, titleBaseLine,160, titleBaseLine , paint);
        canvas.drawLine(390, titleBaseLine,475, titleBaseLine , paint);

        source_title=titleBaseLine+10;
        titleBaseLine+=20;
        paint.setStrokeWidth((float)1.5);
        canvas.drawLine(320, titleBaseLine,pageInfo.getPageWidth()-40, titleBaseLine , paint);
        staticLayout = new StaticLayout(charges.get(0), txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(325, titleBaseLine+3);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout(price.get(0), txt, pageInfo.getPageWidth()-45, Layout.Alignment.ALIGN_OPPOSITE, 1, 1, false);
        canvas.save();
        canvas.translate(0, titleBaseLine+3);
        staticLayout.draw(canvas);
        canvas.restore();
        canvas.drawLine(320, titleBaseLine+=25,pageInfo.getPageWidth()-40, titleBaseLine , paint);

        paint.setColor(Color.parseColor("#2205affc"));
        canvas.drawRect(320,titleBaseLine,pageInfo.getPageWidth()-40,titleBaseLine+25,paint);

        int draw=1;
        for (int i=1;i<charges.size();i++){
            staticLayout = new StaticLayout(charges.get(i), txt, pageInfo.getPageWidth(), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
            canvas.save();
            canvas.translate(325, titleBaseLine+2);
            staticLayout.draw(canvas);
            canvas.restore();
            staticLayout = new StaticLayout(price.get(i), txt, pageInfo.getPageWidth()-45, Layout.Alignment.ALIGN_OPPOSITE, 1, 1, false);
            canvas.save();
            canvas.translate(0, titleBaseLine+2);
            staticLayout.draw(canvas);
            canvas.restore();
            titleBaseLine+=25;
            if (draw==1){
                draw=0;
            }
            else if (i!=charges.size()-1){
                canvas.drawRect(320,titleBaseLine,pageInfo.getPageWidth()-40,titleBaseLine+25,paint);
                draw=1;
            }
        }
        paint.setColor(Color.parseColor("#05affc"));
        canvas.drawLine(320, titleBaseLine,pageInfo.getPageWidth()-40, titleBaseLine , paint);

        txt.setTextSize(14);
        txt.setStrokeWidth(5);
        staticLayout = new StaticLayout("Thu Jul 19 16:01:01 GMT+05:30 2018", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=2);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout("Ittina neela Apartment, Electronic City Phase II, Bangalore, Karnataka - 560100", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=25);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout("Huskur Gate, Electronic City Phase II, Bangalore, Karnataka - 560100", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=60);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout("Puspendra Pandey", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=60);
        staticLayout.draw(canvas);
        canvas.restore();
        staticLayout = new StaticLayout("BR123456", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=20);
        staticLayout.draw(canvas);
        canvas.restore();

        paint.setStrokeWidth(1);
        canvas.drawLine(40, source_title+=100,pageInfo.getPageWidth()-40, source_title , paint);

        txt.setTextSize(16);
        staticLayout = new StaticLayout("Payment Mode : Cash", txt, 250, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        canvas.save();
        canvas.translate(45, source_title+=10);
        staticLayout.draw(canvas);
        canvas.restore();

        // My canvas code…
        canvas.save();

        document.finishPage(page);
        // write the document content
        String targetPdf = Environment.getExternalStorageDirectory().getPath() + "/invoice.pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(BillDetails.this, e.toString(), Toast.LENGTH_LONG).show();
        }
        // close the document
        document.close();
        SendMail sm = new SendMail(this, emailID, subject, message);
        //Executing sendmail to send email
        progressDialog.dismiss();
        sm.execute();
//        }
    }
}
