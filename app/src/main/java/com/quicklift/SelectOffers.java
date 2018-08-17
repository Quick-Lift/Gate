package com.quicklift;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class SelectOffers extends AppCompatActivity {
    EditText promocode;
    ArrayList<String> select_offers=new ArrayList<>();
    ArrayList<String> select_discount=new ArrayList<>();
    ArrayList<String> select_upto=new ArrayList<>();
    ArrayList<String> select_offers_code=new ArrayList<>();
    ListView list;
    DatabaseReference db;
    private SharedPreferences log_id;

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
        setContentView(R.layout.activity_select_offers);

        getSupportActionBar().setTitle("Select Offer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("CustomerOffers/"+log_id.getString("id",null));
        list=(ListView)findViewById(R.id.list);
        promocode=(EditText) findViewById(R.id.promocode);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                select_offers.clear();
                select_offers_code.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
//                    Toast.makeText(SelectOffers.this, ""+data.getKey(), Toast.LENGTH_SHORT).show();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Offers");
                    ref.orderByChild("code").equalTo(data.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dt:dataSnapshot.getChildren()) {
                                Map<String, Object> map = (Map<String, Object>) dt.getValue();
                                String str = "Get " + map.get("discount").toString() + "% off upto Rs. " + map.get("upto").toString();
                                select_offers.add(str);
                                select_discount.add(map.get("discount").toString());
                                select_upto.add(map.get("upto").toString());
                                select_offers_code.add(map.get("code").toString());
                            }
                            if (select_offers.size()==0)
                                findViewById(R.id.nooffer).setVisibility(View.VISIBLE);
                            else
                                findViewById(R.id.nooffer).setVisibility(View.GONE);
                            list.setAdapter(new CustomAdapter());
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

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                intent.putExtra("offer", select_offers.get(position));
                intent.putExtra("discount", select_discount.get(position));
                intent.putExtra("upto", select_upto.get(position));
                intent.putExtra("offer_code", select_offers_code.get(position));
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        promocode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                findViewById(R.id.notfound).setVisibility(View.GONE);
                promocode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void apply(View view){
        if (TextUtils.isEmpty(promocode.getText().toString())){
//            Toast.makeText(this, "Please Enter Promocode.", Toast.LENGTH_SHORT).show();
            promocode.setError("Required.");
        }
        else {
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Please Wait ...");
            dialog.setIndeterminate(true);
            dialog.show();

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Promocode/"+promocode.getText().toString());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dialog.dismiss();
                    if (dataSnapshot.exists()){
                        String str = "Get " + dataSnapshot.child("discount").getValue().toString() + "% off upto Rs. " + dataSnapshot.child("upto").getValue().toString();
                        Intent intent=new Intent();
                        intent.putExtra("offer", str);
                        intent.putExtra("discount", dataSnapshot.child("discount").getValue().toString());
                        intent.putExtra("upto", dataSnapshot.child("upto").getValue().toString());
                        intent.putExtra("offer_code", dataSnapshot.child("code").getValue().toString());
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                    else {
                        findViewById(R.id.notfound).setVisibility(View.VISIBLE);
//                        Toast.makeText(SelectOffers.this, "Invalid Promocode !", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return select_offers.size();
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
        public View getView(int position, View view, ViewGroup parent) {
            view=getLayoutInflater().inflate(R.layout.place_text_view,null);
            TextView txt=(TextView)view.findViewById(R.id.name);

            txt.setText(select_offers.get(position));
            return view;
        }
    }
}
