package com.quicklift;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DatabaseReference db,write_db;
    SharedPreferences log_id;
    EditText email,phone,address;
    TextView name;
    RatingBar rating;
    Map<String,Object> user;
    CircleImageView pic;
    private String upload_img="";
    private Uri selectedImage=null;
    private StorageReference mStorageRef;

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
        setContentView(R.layout.content_edit_profile);

        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        log_id = getApplicationContext().getSharedPreferences("Login", MODE_PRIVATE);
        db= FirebaseDatabase.getInstance().getReference("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        name=(TextView)findViewById(R.id.name);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        address=(EditText)findViewById(R.id.address);
        rating=(RatingBar) findViewById(R.id.rating);
        pic=(CircleImageView)findViewById(R.id.image);

        email.setInputType(0);
        phone.setInputType(0);
        address.setInputType(0);
        pic.setEnabled(false);
        rating.setEnabled(false);

        (findViewById(R.id.edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (findViewById(R.id.edit1)).setVisibility(View.VISIBLE);
                (findViewById(R.id.edit2)).setVisibility(View.VISIBLE);
                (findViewById(R.id.edit3)).setVisibility(View.VISIBLE);
                (findViewById(R.id.layoutsave)).setVisibility(View.VISIBLE);
                (findViewById(R.id.layoutcancel)).setVisibility(View.VISIBLE);
                email.setInputType(1);
                phone.setInputType(1);
                address.setInputType(1);
                pic.setEnabled(true);
            }
        });

        db.child(log_id.getString("id",null)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user=(Map<String, Object>) dataSnapshot.getValue();
                name.setText(user.get("name").toString());
                email.setText(user.get("email").toString());
                phone.setText(user.get("phone").toString());
                if (user.containsKey("address")){
                    address.setText(user.get("address").toString());
                }
                upload_img=user.get("thumb").toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStorageRef.child("Users/"+log_id.getString("id",null)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(pic);
            }
        });

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Profile");
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_rides) {
            startActivity(new Intent(EditProfile.this,CustomerRides.class));
            finish();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(EditProfile.this,Settings.class));
            finish();
        } else if (id == R.id.nav_drive_with_us) {
            startActivity(new Intent(EditProfile.this,DriveWithUs.class));
            finish();
        } else if (id == R.id.nav_emergency_contact) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:0000000000"));

            if (ActivityCompat.checkSelfPermission(EditProfile.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
            intent = new Intent(EditProfile.this, OffersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_payment) {
            startActivity(new Intent(EditProfile.this,Payment.class));
            finish();
        } else if (id == R.id.nav_support) {
            startActivity(new Intent(EditProfile.this,Support.class));
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

    public void cancel (View view){
        finish();
    }

    public void save (View view){
        //user.remove("name");
        //user.put("name",name.getText().toString());
        if (!TextUtils.isEmpty(phone.getText().toString()) && !TextUtils.isEmpty(address.getText().toString())) {
            user.replace("name", name.getText().toString());
            user.replace("phone", phone.getText().toString());
            user.replace("email", email.getText().toString());
            if (user.containsKey("address")) {
                user.replace("address", address.getText().toString());
            } else {
                user.put("address", address.getText().toString());
            }
            user.replace("thumb", upload_img);
            db.child(log_id.getString("id", null)).setValue(user);

            if (selectedImage != null) {
                StorageReference riversRef = mStorageRef.child("Users/" + log_id.getString("id", null));

                UploadTask uploadTask = riversRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(EditProfile.this, "Failed to upload image !" + exception.toString(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(EditProfile.this, "Successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Toast.makeText(this, "Profile Successfully Updated !", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            Toast.makeText(this, "Please fill the details", Toast.LENGTH_LONG).show();
        }
    }

    public void updatepic(View v){
        Intent photo = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        final int ACTIVITY_SELECT_IMAGE = 1234;
        startActivityForResult(photo, ACTIVITY_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 1234:
                if(resultCode == RESULT_OK){
                    selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                    Bitmap imageThumbnail= ThumbnailUtils.extractThumbnail(yourSelectedImage,150,150);
                    upload_img = BitMapToString(imageThumbnail);
                    Glide.with(EditProfile.this).load(selectedImage).into(pic);
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
