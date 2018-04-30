package com.quicklift;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class PhoneAuthentication extends AppCompatActivity implements
        View.OnClickListener {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mSignedInViews;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;

    private Button sendotp;
    private Button resendotp;
    private Button verify;
    private Button signup;

    EditText name,email,phone,password,otp;
    CircleImageView pic;
    ProgressDialog pdialog;
    String upload_img="";
    Uri selectedImage=null;
    private StorageReference mStorageRef;
    DatabaseReference user_db= FirebaseDatabase.getInstance().getReference("Users");
    private String mVerificationId;

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
        setContentView(R.layout.activity_phone_authentication);

        getSupportActionBar().setTitle("User Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        sendotp = findViewById(R.id.send_otp);
        resendotp = findViewById(R.id.resend_otp);
        verify = findViewById(R.id.verify);
        signup = findViewById(R.id.sign_up);

        pdialog=new ProgressDialog(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        name=(EditText)findViewById(R.id.name);
        otp=(EditText)findViewById(R.id.code);
        email=(EditText)findViewById(R.id.email);
        phone=(EditText)findViewById(R.id.phone);
        password=(EditText)findViewById(R.id.password);
        pic=(CircleImageView) findViewById(R.id.image);

        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photo = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                final int ACTIVITY_SELECT_IMAGE = 1234;
                startActivityForResult(photo, ACTIVITY_SELECT_IMAGE);
            }
        });

        // Assign click listeners
        sendotp.setOnClickListener(this);
        resendotp.setOnClickListener(this);
        verify.setOnClickListener(this);
        signup.setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

//                Toast.makeText(PhoneAuthentication.this, "Verification Success...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
               // Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
//                    Toast.makeText(PhoneAuthentication.this, "Invalid Phone Number !", Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Toast.makeText(PhoneAuthentication.this, "Quota Exceeded !", Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                //updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
               // Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
//                Toast.makeText(PhoneAuthentication.this, verificationId, Toast.LENGTH_SHORT).show();

                // [START_EXCLUDE]
                // Update UI
               // updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
       // mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError("Required.");
            valid = false;
        } else {
            name.setError(null);
        }

        if (TextUtils.isEmpty(otp.getText().toString())) {
            otp.setError("Required.");
            valid = false;
        } else {
            otp.setError(null);
        }

        if (TextUtils.isEmpty(phone.getText().toString())) {
            phone.setError("Required.");
            valid = false;
        } else {
            phone.setError(null);
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]
        phone.setVisibility(View.GONE);
        sendotp.setVisibility(View.GONE);
        otp.setVisibility(View.VISIBLE);
        verify.setVisibility(View.VISIBLE);
        resendotp.setVisibility(View.VISIBLE);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           // Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            pic.setVisibility(View.VISIBLE);
                            name.setVisibility(View.VISIBLE);
                            email.setVisibility(View.VISIBLE);
                            password.setVisibility(View.VISIBLE);
                            signup.setVisibility(View.VISIBLE);
                            otp.setVisibility(View.GONE);
                            verify.setVisibility(View.GONE);
                            resendotp.setVisibility(View.GONE);
                            // [START_EXCLUDE]
                            //updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                Toast.makeText(PhoneAuthentication.this, "Wrong OTP Code !", Toast.LENGTH_SHORT).show();
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                           // updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]

    private void signOut() {
        mAuth.signOut();
        //updateUI(STATE_INITIALIZED);
    }
    /*
       // private void updateUI(int uiState) {
       //     updateUI(uiState, mAuth.getCurrentUser(), null);
       // }

        private void updateUI(FirebaseUser user) {
            if (user != null) {
             //   updateUI(STATE_SIGNIN_SUCCESS, user);
            } else {
              //  updateUI(STATE_INITIALIZED);
            }
        }

        private void updateUI(int uiState, FirebaseUser user) {
            updateUI(uiState, user, null);
        }

        private void updateUI(int uiState, PhoneAuthCredential cred) {
            updateUI(uiState, null, cred);
        }

        private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
            switch (uiState) {
                case STATE_INITIALIZED:
                    // Initialized state, show only the phone number field and start button
                    enableViews(mStartButton, mPhoneNumberField);
                    disableViews(mVerifyButton, mResendButton, mVerificationField);
                    mDetailText.setText(null);
                    break;
                case STATE_CODE_SENT:
                    // Code sent state, show the verification field, the
                    enableViews(mVerifyButton, mResendButton, mPhoneNumberField, mVerificationField);
                    disableViews(mStartButton);
                    mDetailText.setText(R.string.status_code_sent);
                    break;
                case STATE_VERIFY_FAILED:
                    // Verification has failed, show all options
                    enableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                            mVerificationField);
                    mDetailText.setText(R.string.status_verification_failed);
                    break;
                case STATE_VERIFY_SUCCESS:
                    // Verification has succeeded, proceed to firebase sign in
                    disableViews(mStartButton, mVerifyButton, mResendButton, mPhoneNumberField,
                            mVerificationField);
                    mDetailText.setText(R.string.status_verification_succeeded);

                    // Set the verification text based on the credential
                    if (cred != null) {
                        if (cred.getSmsCode() != null) {
                            mVerificationField.setText(cred.getSmsCode());
                        } else {
                            mVerificationField.setText(R.string.instant_validation);
                        }
                    }

                    break;
                case STATE_SIGNIN_FAILED:
                    // No-op, handled by sign-in check
                    mDetailText.setText(R.string.status_sign_in_failed);
                    break;
                case STATE_SIGNIN_SUCCESS:
                    // Np-op, handled by sign-in check
                    break;
            }

            if (user == null) {
                // Signed out
                mPhoneNumberViews.setVisibility(View.VISIBLE);
                mSignedInViews.setVisibility(View.GONE);

                mStatusText.setText(R.string.signed_out);
            } else {
                // Signed in
                mPhoneNumberViews.setVisibility(View.GONE);
                mSignedInViews.setVisibility(View.VISIBLE);

                enableViews(mPhoneNumberField, mVerificationField);
                mPhoneNumberField.setText(null);
                mVerificationField.setText(null);

                mStatusText.setText(R.string.signed_in);
                mDetailText.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            }
        }
    */
    private boolean validatePhoneNumber() {
        String phoneNumber = mPhoneNumberField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_otp:
                /*
                if (!validatePhoneNumber()) {
                    return;
                }
*/
                if (TextUtils.isEmpty(phone.getText().toString())){
                    phone.setError("Cannot be empty.");
                }
                else {
                    startPhoneNumberVerification(phone.getText().toString());
                }
                break;

            case R.id.verify:
                String code = otp.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    otp.setError("Cannot be empty.");
                    return;
                }else {
                    verifyPhoneNumberWithCode(mVerificationId, code);
                }
                break;

            case R.id.resend_otp:
                resendVerificationCode(phone.getText().toString(), mResendToken);
                break;

            case R.id.sign_up:
                signOut();
                break;
        }
    }
}
