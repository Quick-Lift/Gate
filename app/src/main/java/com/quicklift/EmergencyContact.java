package com.quicklift;

import android.*;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class EmergencyContact extends AppCompatActivity {
    ArrayList<Map<String,String>> contact=new ArrayList<>();
    ImageView image;
    ListView list;
    TextView address;

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
        setContentView(R.layout.activity_emergency_contact);

        getSupportActionBar().setTitle("Emergency Contacts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        address=(TextView)findViewById(R.id.address);
        list=(ListView) findViewById(R.id.list);
        image=(ImageView)findViewById(R.id.image_location);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("EmergencyContacts");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                address.setText(dataSnapshot.child("Address").getValue(String.class));
                for (DataSnapshot data:dataSnapshot.child("Contacts").getChildren()){
                    Map<String,String> map=(Map<String, String>) data.getValue();
                    contact.add(map);
                }
                list.setAdapter(new CustomAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contact.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            view=getLayoutInflater().inflate(R.layout.contact_layout,null);
            TextView name=(TextView)view.findViewById(R.id.name);
            TextView designation=(TextView)view.findViewById(R.id.designation);
            TextView phone=(TextView)view.findViewById(R.id.phone);
            LinearLayout call=(LinearLayout) view.findViewById(R.id.call);

            name.setText(contact.get(position).get("name").toString());
            designation.setText(contact.get(position).get("designation").toString());
            phone.setText(contact.get(position).get("phone").toString());

            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + contact.get(position).get("phone").toString()));

                    if (ActivityCompat.checkSelfPermission(EmergencyContact.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(callIntent);
                }
            });

            return view;
        }
    }
}
