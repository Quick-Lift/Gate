package com.quicklift;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;

public class SpecialOffer extends AppCompatActivity {
    HashMap<String,String> map=new HashMap<>();
    EditText limit,validity,minamount,value,num_of_coupons,name;
    Button generate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_offer);

        limit=(EditText)findViewById(R.id.user_limit);
        validity=(EditText)findViewById(R.id.validity);
        minamount=(EditText)findViewById(R.id.min_amount);
        value=(EditText)findViewById(R.id.discount);
        num_of_coupons=(EditText)findViewById(R.id.num_of_offers);
        name=(EditText)findViewById(R.id.name);
        generate=(Button) findViewById(R.id.generate);

        final GenerateOffer offer=new GenerateOffer();

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate.setEnabled(false);
                ProgressDialog dialog=new ProgressDialog(SpecialOffer.this,ProgressDialog.THEME_HOLO_DARK);
                dialog.setMessage("Generating Coupons ....");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();

                if (TextUtils.isEmpty(limit.getText().toString()) || TextUtils.isEmpty(validity.getText().toString()) || TextUtils.isEmpty(minamount.getText().toString()) || TextUtils.isEmpty(value.getText().toString()) || TextUtils.isEmpty(num_of_coupons.getText().toString()) || TextUtils.isEmpty(name.getText().toString())){
                    Toast.makeText(SpecialOffer.this, "Please enter all the fields !", Toast.LENGTH_SHORT).show();
                    if (dialog.isShowing())
                        dialog.dismiss();
                    generate.setEnabled(true);
                }
                else {
                    int count = Integer.parseInt(num_of_coupons.getText().toString());
                    final int SHORT_ID_LENGTH = 8;
                    Map<String,Object> specialoffer = new HashMap<>();
                    specialoffer.put("limit",limit.getText().toString());
                    specialoffer.put("minamount",minamount.getText().toString());
                    specialoffer.put("validity",validity.getText().toString());
                    specialoffer.put("value",value.getText().toString());
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("SpecialOffer");
                    ref.child(name.getText().toString()).setValue(specialoffer);
                    for (int i = 0; i < count; i++) {
                        String shortId = RandomStringUtils.randomAlphanumeric(SHORT_ID_LENGTH);
                        if (!map.containsKey(shortId)) {
                            map.put(shortId, "true");
                            ref.child(shortId).setValue(name.getText().toString());
                            offer.appendLog(shortId);
                        } else {
                            i--;
                        }
                    }
                    if (dialog.isShowing())
                        dialog.dismiss();
                    Toast.makeText(SpecialOffer.this, "Coupons Generated successfully !!!", Toast.LENGTH_SHORT).show();
                    generate.setEnabled(true);
                    limit.setText("");
                    num_of_coupons.setText("");
                    value.setText("");
                    validity.setText("");
                    minamount.setText("");
                }
            }
        });
    }
}
