package com.quicklift;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Terms_and_Condition extends AppCompatActivity {
    ProgressDialog pdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and__condition);

        getSupportActionBar().setTitle("Terms and Conditions");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        pdialog=new ProgressDialog(this);
        pdialog.setMessage("Creating Account ...");
        pdialog.setIndeterminate(true);
        pdialog.setCancelable(false);

        ((TextView)findViewById(R.id.conditions)).setText("By clicking on the \"I ACCEPT\" button, You are consenting to be bound by these User Terms. PLEASE ENSURE THAT YOU READ AND UNDERSTAND ALL THESE USER TERMS BEFORE YOU USE THE SITE. If You do not accept any of the User Terms, then please do not use the Site or avail any of the services being provided therein.");
//        WebView simpleWebView=(WebView) findViewById(R.id.simpleWebView);
//// specify the url of the web page in loadUrl function
//        simpleWebView.loadUrl("http://quicklift.in/Quciklift-terms");
    }

    public void readmore(View view){
        Uri uri = Uri.parse("http://quicklift.in/Quciklift-terms");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
//        findViewById(R.id.terms).setVisibility(View.GONE);
//        findViewById(R.id.webview).setVisibility(View.VISIBLE);
    }

    public void accepted(View view){
        pdialog.show();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
