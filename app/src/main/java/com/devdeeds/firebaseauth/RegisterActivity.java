package com.devdeeds.firebaseauth;



import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.*;

import org.json.JSONObject;
import java.io.File;



public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    // UI references.
    private NestedScrollView nestedScrollView;
    private TextInputEditText mEmailView;
    private TextInputEditText mNameView;
    private TextInputEditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean mAllowNavigation = true;

    FirebaseDatabase db;

    private AppCompatButton ButtonTakePhoto;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        // Set up the login form.
        mEmailView = (TextInputEditText) findViewById(R.id.email);
        mNameView = (TextInputEditText) findViewById(R.id.name);
        db=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mPasswordView = (TextInputEditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        ButtonTakePhoto = (AppCompatButton) findViewById(R.id.ButtonTakePhoto);
        if (ButtonTakePhoto != null) {
            ButtonTakePhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Take_Photo();
                }
            });
        }

        Button btnRegisterButton = (Button) findViewById(R.id.email_sign_up_button);
        if (btnRegisterButton != null) {
            btnRegisterButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptRegister();
                }
            });
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                    if (mAllowNavigation) {
                        mAllowNavigation = false;

                        //  Section checks email verified or not /////


                        if (user.isEmailVerified()) {

                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            //user.sendEmailVerification();
                            Toast.makeText(getApplicationContext(), R.string.msg_email_address_not_verified, Toast.LENGTH_LONG).show();
                        }


                        //  end of checking....... /////


                    }


                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");


                }
                // ...
            }
        };
    }

    private void attemptRegister() {


        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String name = mNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }


        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);

            // hashing passwords
            password = MD5.getMD5(password);
            //Toast.makeText(getApplicationContext(), "pass:"+password, Toast.LENGTH_LONG).show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Authentication failed." ,
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });


        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void Take_Photo(){
        final String path = "/mnt/sdcard/Pictures/face.jpg";
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"face.jpg");
        Uri tempUri = Uri.fromFile(imageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        //wait for uploading the picture to server
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                facepp_detect(path);
            }
        }, 10000);
    }

    private void facepp_detect(final String path) {
        try {
            String url = "https://api-us.faceplusplus.com/facepp/v3/detect";
            RequestParams Params = new RequestParams();
            Params.put("api_key", "5rBvJiv_t4NrUyyU5ITAHQzmbJVSJyPL");
            Params.put("api_secret", "xdfOlg9MKz5F0S-RZZJUOxe3Q5_8EfEa");
            File myFile = new File(path);
            Params.put("image_file", myFile);
            AsyncHttpClient client = new AsyncHttpClient();
            client.post(url, Params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    try {
                        String successStr = new String(responseBody);
                        JSONObject jObject = new JSONObject(successStr).getJSONArray("faces").getJSONObject(0);
                        String token = jObject.getString("face_token");

                        DatabaseReference dbRef = db.getReference("Token");
                        String key = dbRef.push().getKey();
                        DatabaseReference dbRefKeyli = db.getReference("Token/" + key);

                        String email=mEmailView.getText().toString().trim();

                        dbRefKeyli.setValue(new Token(email, token));
                        Toast.makeText(getApplicationContext(), "Face is saved to database! Now, Sign Up!", Toast.LENGTH_LONG).show();


                    } catch (Exception e1){
                        e1.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                    if (responseBody != null) {
                        Log.w("ceshi", "responseBody===" + new String(responseBody));
                        Snackbar.make(nestedScrollView, "No Token", Snackbar.LENGTH_LONG).show();

                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


}

