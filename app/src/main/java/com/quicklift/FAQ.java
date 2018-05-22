package com.quicklift;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FAQ extends AppCompatActivity {
    int layout1=0,layout2=0,layout3=0,layout4=0,layout5=0,layout6=0,layout7=0;

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
        setContentView(R.layout.activity_faq);

        getSupportActionBar().setTitle("Frequently Asked Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void expand (View view){
        if (view==findViewById(R.id.image1)){
            if (layout1==0){
                layout1=1;
                findViewById(R.id.ans1).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout1=0;
                findViewById(R.id.ans1).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image2)){
            if (layout2==0){
                layout2=1;
                findViewById(R.id.ans2).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout2=0;
                findViewById(R.id.ans2).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image3)){
            if (layout3==0){
                layout3=1;
                findViewById(R.id.ans3).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout3=0;
                findViewById(R.id.ans3).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image4)){
            if (layout4==0){
                layout4=1;
                findViewById(R.id.ans4).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout4=0;
                findViewById(R.id.ans4).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image5)){
            if (layout5==0){
                layout5=1;
                findViewById(R.id.ans5).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout5=0;
                findViewById(R.id.ans5).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image6)){
            if (layout6==0){
                layout6=1;
                findViewById(R.id.ans6).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout6=0;
                findViewById(R.id.ans6).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
        if (view==findViewById(R.id.image7)){
            if (layout7==0){
                layout7=1;
                findViewById(R.id.ans7).setVisibility(View.VISIBLE);
                ((ImageView)view).setImageResource(R.drawable.ic_up);
            }
            else {
                layout7=0;
                findViewById(R.id.ans7).setVisibility(View.GONE);
                ((ImageView)view).setImageResource(R.drawable.ic_down);
            }
        }
    }
}
