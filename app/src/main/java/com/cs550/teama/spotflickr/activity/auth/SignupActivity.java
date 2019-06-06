package com.cs550.teama.spotflickr.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cs550.teama.spotflickr.model.HotspotList;
import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextUserName, editTextEmail, editTextPassword;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUserName = findViewById(R.id.editTextUserName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        progressBar = findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonSignUp).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    private void registerUser() {
        final String username = editTextUserName.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty()) {
            editTextUserName.setError("User name is required.");
            editTextUserName.requestFocus();
            return;
        }

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

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    //User user = new User(username, email, password, null);
                    List<String> hotspot_list = new ArrayList<String>();
                    hotspot_list.add(mAuth.getCurrentUser().getUid());

                    User user = new User(username, email, password, hotspot_list);
                    db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user);

                    HotspotList hotspotList = new HotspotList("default", "", mAuth.getCurrentUser().getUid());
                    db.collection("hotspot lists").document(mAuth.getCurrentUser().getUid()).set(hotspotList);

                    Toast.makeText(getApplicationContext(), "User Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonSignUp:
                registerUser();
                break;

            case R.id.textViewLogin:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}
