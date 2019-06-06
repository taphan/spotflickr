package com.cs550.teama.spotflickr.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.model.User;
import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.activity.auth.LoginActivity;
import com.cs550.teama.spotflickr.login.ChangePassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserProfileFragmentActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    TextView user_name, user_email;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DocumentReference documentReference = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("User Profile");
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        user_name = (TextView) findViewById(R.id.user_name);
        user_email = (TextView) findViewById(R.id.user_email);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                    user_name.setText("Welcome, " + current_user.getUsername() + "!");
                    user_email.setText("E-mail: " + current_user.getEmail());
                }
                else
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.change_password).setOnClickListener(this);
        findViewById(R.id.delete_account).setOnClickListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                break;
            case R.id.nav_map:
                startActivity(new Intent(this, MapFragmentActivity.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.change_password:
                startActivity(new Intent(this, ChangePassword.class));
                break;

            case R.id.delete_account:
                progressBar.setVisibility(View.VISIBLE);
                mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            List<String> hotspotIDList = current_user.getHotspot_id_list();
                            for (int i = 0; i < hotspotIDList.size(); i++) {
                                db.collection("hotspot lists").document(hotspotIDList.get(i)).delete();
                            }
                            documentReference.delete();
                            Toast.makeText(getApplicationContext(), "Account Successfully Deleted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(UserProfileFragmentActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                });
                break;
        }
    }
}
