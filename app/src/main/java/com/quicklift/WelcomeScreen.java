package com.quicklift;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeScreen extends AppCompatActivity {
    ImageView i1,i2,i3;
    TextView t;
    Animation animation_plus,animation_heading,animation_appear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("");

        //DatabaseReference db=FirebaseDatabase.getInstance().getReference();
        //db.push().setValue("hi");

        Intent intent = new Intent(WelcomeScreen.this, Login.class);
        Bundle bundle= ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.animation_slide1,R.anim.animation_slide2).toBundle();
        startActivity(intent,bundle);
        finish();

        i1=(ImageView)findViewById(R.id.img1);
        i2=(ImageView)findViewById(R.id.img2);
        i3=(ImageView)findViewById(R.id.img3);
        t=(TextView)findViewById(R.id.app_name);

        animation_heading= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.welcome_animation);
        animation_plus= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.welcome_animation_right_to_left);
        animation_appear= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.animation_appear);
        //heading.setAnimation(animation_heading);
        //imageView.setAnimation(animation_plus);

        i1.setAnimation(animation_heading);

        animation_plus.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //login.setVisibility(View.VISIBLE);
                /*
                pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                if (!TextUtils.isEmpty(pref.getString("id",null))){
                    Intent intent = new Intent(Welcome.this, Dr_login.class);
                    intent.putExtra("id", pref.getString("id",null));
                    startActivity(intent);
                }  */
                i1.setVisibility(View.INVISIBLE);
                i2.setVisibility(View.INVISIBLE);

                i3.setVisibility(View.VISIBLE);
                t.setVisibility(View.VISIBLE);

                i3.setAnimation(animation_appear);
                t.setAnimation(animation_appear);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_heading.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //login.setVisibility(View.VISIBLE);
                /*
                pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                if (!TextUtils.isEmpty(pref.getString("id",null))){
                    Intent intent = new Intent(Welcome.this, Dr_login.class);
                    intent.putExtra("id", pref.getString("id",null));
                    startActivity(intent);
                }  */
                //slogan.setAnimation(animation_appear);

                i1.setAnimation(animation_plus);
                i2.setAnimation(animation_plus);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animation_appear.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //login.setVisibility(View.VISIBLE);
                /*
                pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                if (!TextUtils.isEmpty(pref.getString("id",null))){
                    Intent intent = new Intent(Welcome.this, Dr_login.class);
                    intent.putExtra("id", pref.getString("id",null));
                    startActivity(intent);
                }  */
                //slogan.setAnimation(animation_appear);
                Intent intent = new Intent(WelcomeScreen.this, Login.class);
                Bundle bundle= ActivityOptions.makeCustomAnimation(getApplicationContext(),R.anim.animation_slide1,R.anim.animation_slide2).toBundle();
                startActivity(intent,bundle);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }
}
