package com.cs550.teama.spotflickr.activity.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.activity.auth.CurrentUserPasswordActivity;
import com.cs550.teama.spotflickr.activity.auth.DeleteAccountActivity;
import com.cs550.teama.spotflickr.activity.hotspot.HotspotListActivity;
import com.cs550.teama.spotflickr.model.hotspot.HotspotList;
import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragmentActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private EditText editTextName;
    private EditText editTextDesc;
    private List<String> hotspotListNames;

    TextView user_name, user_email;
    ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        hotspotListNames = new ArrayList<String>();

        db.collection("hotspot lists").whereEqualTo("user_id", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot d : list) {
                            HotspotList hList = d.toObject(HotspotList.class);
                            hotspotListNames.add(hList.getName());
                        }
                    }
                });

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

        final TextView nav_user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView nav_user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);

        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        progressBar = findViewById(R.id.progressbar);

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                    nav_user_name.setText(current_user.getUsername());
                    nav_user_email.setText(current_user.getEmail());
                    user_name.setText("Welcome, " + current_user.getUsername() + "!");
                    user_email.setText("E-mail: " + current_user.getEmail());
                }
                else
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
            }
        });

        editTextName = findViewById(R.id.edittext_name);
        editTextDesc = findViewById(R.id.edittext_desc);

        findViewById(R.id.change_password).setOnClickListener(this);
        findViewById(R.id.delete_account).setOnClickListener(this);
        findViewById(R.id.hotspot_list).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
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

    private boolean hasValidationErrors(String name, String desc) {
        if (name.isEmpty()) {
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return true;
        }

        for (int i = 0; i < hotspotListNames.size(); i++) {
            if (name.equals(hotspotListNames.get(i))) {
                editTextName.setError("Same name already exists");
                editTextName.requestFocus();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.change_password:
                startActivity(new Intent(this, CurrentUserPasswordActivity.class));
                break;

            case R.id.delete_account:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Deletion is permanent...");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(UserProfileFragmentActivity.this, DeleteAccountActivity.class);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog ad = builder.create();
                ad.show();
                break;
            case R.id.hotspot_list:
                startActivity(new Intent(this, HotspotListActivity.class));
                break;
            case R.id.button_save:
                final String name = editTextName.getText().toString().trim();
                final String desc = editTextDesc.getText().toString().trim();

                if (!hasValidationErrors(name, desc)) {
                    CollectionReference dbProducts = db.collection("hotspot lists");

                    List<String> hotspot = new ArrayList<String>();
                    HotspotList hotspotList = new HotspotList(
                            name,
                            desc,
                            mAuth.getCurrentUser().getUid(),
                            hotspot
                    );

                    dbProducts.add(hotspotList)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference listDocRef) {
                                    Toast.makeText(UserProfileFragmentActivity.this, "Hotspot List Added", Toast.LENGTH_LONG).show();
                                    current_user.addHotspotList(listDocRef.getId());
                                    userDocRef.set(current_user);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserProfileFragmentActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
                break;
        }
    }
}
