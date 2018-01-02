package com.devdeeds.firebaseauth;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;

public class LoginFace extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "LoginFace";

    String token_get=null;

    private Button foto, back;

    private NestedScrollView nestedScrollView;

    FirebaseDatabase db;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginface);
        baslangic();
    }
    private void baslangic(){

        nestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        db= FirebaseDatabase.getInstance();
        foto =(Button)findViewById(R.id.fotoBtn);
        back =(Button)findViewById(R.id.backBtn);


        foto.setOnClickListener(this);
        back.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fotoBtn:
                Take_Photo();
                break;

                // back to login page
            case R.id.backBtn:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class)); //Go back to home page
                finish();

        }
    }


    private void Take_Photo(){
        final String path = "/mnt/sdcard/Pictures/face.jpg";
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"face.jpg");
        Uri tempUri = Uri.fromFile(imageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        Handler handler = new Handler();

        //delay uploading face to server
        handler.postDelayed(new Runnable() {
            public void run() {
                facepp_compare(path);
            }
        }, 10000);
    }

    private void facepp_compare(final String path) {

        DatabaseReference dbGelenler = db.getReference("Token");
        dbGelenler.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String token_register = null;

                // getting user from previous page
                Bundle extras = getIntent().getExtras();
                String login_email = null;
                if (extras != null) {
                    login_email = extras.getString("email");
                }

                // getting token for spesific user from database
                for (DataSnapshot gelenler: dataSnapshot.getChildren()) {

                    String gelen_email = gelenler.getValue(Token.class).getEmail();
                    String gelen_token = gelenler.getValue(Token.class).getToken();
                    if(gelen_email.equals(login_email)){
                        token_register = gelen_token;
                    }

                }

                //posting variable to FacePlusPlus for comparing

                try {
                    String url = "https://api-us.faceplusplus.com/facepp/v3/compare";
                    RequestParams Params = new RequestParams();
                    Params.put("api_key", "5rBvJiv_t4NrUyyU5ITAHQzmbJVSJyPL");
                    Params.put("api_secret", "xdfOlg9MKz5F0S-RZZJUOxe3Q5_8EfEa");
                    File myFile = new File(path);
                    Params.put("image_file1", myFile);

                    Params.put("face_token2", token_register);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(url, Params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            try {
                                String successStr = new String(responseBody);
                                String confidence = new JSONObject(successStr).getString("confidence");

                                if(Float.parseFloat(confidence) > 80.0){
                                    Snackbar.make(nestedScrollView, confidence+" Confidence Level is successful!", Snackbar.LENGTH_LONG).show();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Do something after 5s = 5000ms
                                            Intent accountsIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                            startActivity(accountsIntent);
                                        }
                                    }, 5000);
                                }
                                else{
                                    Snackbar.make(nestedScrollView, confidence+" Confidence Level is low, Try Again!", Snackbar.LENGTH_LONG).show();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            Intent accountsIntent = new Intent(getApplicationContext(), LoginFace.class);
                                            //accountsIntent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
                                            startActivity(accountsIntent);

                                        }
                                    }, 5000);


                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                            if (responseBody != null) {
                                Log.w("ceshi", "responseBody===" + new String(responseBody));
                                Snackbar.make(nestedScrollView, "Post is unsuccessful!", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}