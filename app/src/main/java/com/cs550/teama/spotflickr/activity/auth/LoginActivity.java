package com.cs550.teama.spotflickr.activity.auth;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "LoginActivity";
    private static final int flickrLoginActivityRequestCode = 1;
    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;
    ProgressBar progressBar;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getAppKeyHash();
        setContentView(R.layout.activity_login);

        int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int accessFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if ( internetPermission == PackageManager.PERMISSION_GRANTED
                && accessFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }

            mAuth = FirebaseAuth.getInstance();

            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            progressBar = findViewById(R.id.progressbar);

            findViewById(R.id.textViewSignup).setOnClickListener(this);
            findViewById(R.id.buttonLogin).setOnClickListener(this);


        } else {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 10) {
            editTextPassword.setError("Minimum length of password should be 10");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);


        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Intent intent = new Intent(LoginActivity.this, MapFragmentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.textViewSignup:
                Intent myIntent = new Intent(LoginActivity.this, FlickrLoginActivity.class);
                startActivityForResult(myIntent, flickrLoginActivityRequestCode);
                break;
            case R.id.buttonLogin:
                userLogin();
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == flickrLoginActivityRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                Map<String, String> loginInfo = OAuthService.INSTANCE.getAccessTokenResponse();
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                for (String key : loginInfo.keySet()) {
                    Log.d(TAG, key + " " + Utils.oauthDecode(loginInfo.get(key)));
                    intent.putExtra(key, Utils.oauthDecode(loginInfo.get(key)));
                }
                startActivity(intent);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                this.setTheme(R.style.AlertDialogCustom);
                new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Failed to login")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }
    }



}
