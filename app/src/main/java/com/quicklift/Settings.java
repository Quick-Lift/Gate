package com.quicklift;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    TextView name,phone,nm,ph;
    EditText location,location_name;
    CircleImageView image,photo;
    private SharedPreferences log_id;
    private PlaceAutocompleteFragment autocompleteFragment;
    private EditText address;
//    LinearLayout home,work;
    String latitude,longitude;
    ListView list;
    ArrayList<String> loc_name=new ArrayList<>();
    ArrayList<String> loc_key=new ArrayList<>();
    int item_id;

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
        setContentView(R.layout.content_settings);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        location=(EditText)findViewById(R.id.location);
        location_name=(EditText)findViewById(R.id.location_name);
        list=(ListView) findViewById(R.id.list);
//        home=(LinearLayout)findViewById(R.id.home);
//        work=(LinearLayout)findViewById(R.id.work);
        autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.location_autocomplete);
        address=(EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);

        autocompleteFragment.setHint("Search location ...");
        address.setTextColor(Color.parseColor("#05affc"));
        address.setHintTextColor(Color.parseColor("#9005affc"));
        LatLngBounds latLngBounds = new LatLngBounds(
                new LatLng(25.612677,85.158875),
                new LatLng(25.612677,85.158875));
        autocompleteFragment.setBoundsBias(latLngBounds);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Settings");
//
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
//        phone = (TextView) navigationView.getHeaderView(0).findViewById(R.id.phone);
//        image = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.image);
        nm = (TextView) findViewById(R.id.name);
        ph = (TextView) findViewById(R.id.phone);
        photo = (CircleImageView) findViewById(R.id.photo);
//        image.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(Settings.this,EditProfile.class));
//            }
//        });
//
        updatenavbar();

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null)+"/saved");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    loc_key.clear();
                    loc_name.clear();
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        loc_key.add(data.getKey());
                        loc_name.add(data.child("name").getValue().toString()+" ("+data.child("locname").getValue().toString()+")");
                    }
                    list.setAdapter(new CustomAdapter());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        home.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                location_name.setText("Home");
//                address.performClick();
//            }
//        });
//
//        work.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                location_name.setText("Work");
//                address.performClick();
//            }
//        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.i(TAG, "Place: " + place.getName());
                //Toast.makeText(Home.this, place.getName(), Toast.LENGTH_SHORT).show();
                latitude=String.valueOf(place.getLatLng().latitude);
                longitude=String.valueOf(place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                // Log.i(TAG, "An error occurred: " + status);
//                Toast.makeText(Home.this, status.toString(), Toast.LENGTH_SHORT).show();
            }
        });

//        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                item_id=position;
//                registerForContextMenu(list);
//                return false;
//            }
//        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Menu");
            String[] menuItems = getResources().getStringArray(R.array.delete_list);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.delete_list);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = "Menu";

        if(menuItemName.equals("Delete")) {
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null)+"/saved");
            ref.child(loc_key.get(item_id)).removeValue();
            Toast.makeText(this, "Location Removed !!", Toast.LENGTH_LONG).show();
        }
        //text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
        //Toast.makeText(this, String.format("Selected %s for item %s",search_array.get(item_no).getId() , listItemName), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void updatenavbar() {
        DatabaseReference db= FirebaseDatabase.getInstance().getReference("Users/"+log_id.getString("id",null));
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> map=(Map<String, Object>) dataSnapshot.getValue();
//                name.setText(map.get("name").toString());
//                phone.setText(map.get("phone").toString());
                nm.setText(map.get("name").toString());
                ph.setText(map.get("phone").toString());
                if (!map.get("thumb").toString().equals("")) {
                    byte[] dec = Base64.decode(map.get("thumb").toString(), Base64.DEFAULT);
                    Bitmap decbyte = BitmapFactory.decodeByteArray(dec, 0, dec.length);
//                    image.setImageBitmap(decbyte);
                    photo.setImageBitmap(decbyte);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void save_location(View view){
        if (TextUtils.isEmpty(address.getText().toString()) || TextUtils.isEmpty(location_name.getText().toString())){
            Toast.makeText(this, "Fill all the fields !", Toast.LENGTH_LONG).show();
        }
        else {
            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null)+"/saved");
            String key=ref.push().getKey();
            HashMap<String,String> map=new HashMap<>();
            map.put("name",location_name.getText().toString());
            map.put("locname",address.getText().toString());
            map.put("lat",latitude);
            map.put("lng",longitude);
//            ref.child(key+"/name").setValue(location_name.getText().toString());
//            ref.child(key+"/locname").setValue(address.getText().toString());
//            ref.child(key+"/lat").setValue(latitude);
//            ref.child(key+"/lng").setValue(longitude);
            ref.child(key).setValue(map);

//            Toast.makeText(this, "Location Saved !", Toast.LENGTH_SHORT).show();
            address.setText("");
            location_name.setText("");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_rides) {
            intent = new Intent(Settings.this, CustomerRides.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_drive_with_us) {
            startActivity(new Intent(Settings.this,DriveWithUs.class));
            finish();
        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(Settings.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            startActivity(callIntent);
        } else if (id == R.id.nav_offers) {
            startActivity(new Intent(Settings.this,OffersActivity.class));
            finish();
        } else if (id == R.id.nav_payment) {
            startActivity(new Intent(Settings.this,Payment.class));
            finish();
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(Settings.this,Support.class));
            finish();
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
//                    "mailto","qiklift@gmail.com", null));
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support");
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "Message : ");
//            startActivity(Intent.createChooser(emailIntent, "Requesting Support !"));
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return loc_name.size();
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
            view=getLayoutInflater().inflate(R.layout.settings_place_layout,null);
            TextView name=(TextView)view.findViewById(R.id.name);
            ImageView del=(ImageView)view.findViewById(R.id.delete);

            name.setTextColor(Color.parseColor("#05affc"));
            name.setText(loc_name.get(position));

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item_id=position;
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("SavedLocations/"+log_id.getString("id",null)+"/saved");
                    ref.child(loc_key.get(item_id)).removeValue();
                    Toast.makeText(Settings.this, "Location Removed !!", Toast.LENGTH_LONG).show();
                }
            });

            return view;
        }
    }
}
