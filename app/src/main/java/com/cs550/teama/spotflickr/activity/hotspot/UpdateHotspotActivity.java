package com.cs550.teama.spotflickr.activity.hotspot;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.activity.user.UserProfileFragmentActivity;
import com.cs550.teama.spotflickr.model.Hotspot;
import com.cs550.teama.spotflickr.model.HotspotList;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UpdateHotspotActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    private EditText editTextDesc;

    private Hotspot hotspot;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_hotspot);

        hotspot = (Hotspot) getIntent().getSerializableExtra("hotspot");

        TextView textView_content = (TextView) findViewById(R.id.textview_content);
        TextView textView_latitude = (TextView) findViewById(R.id.textview_latitude);
        TextView textView_longitude = (TextView) findViewById(R.id.textview_longitude);

        textView_content.setText(hotspot.getName());
        textView_latitude.setText("Latitude: "+ Float.toString(hotspot.getLatitude()));
        textView_longitude.setText("Longitude: "+ Float.toString(hotspot.getLongitude()));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Hotspot Manager");
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final TextView user_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView user_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);


        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                    user_name.setText(current_user.getUsername());
                    user_email.setText(current_user.getEmail());
                } else {
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editTextDesc = findViewById(R.id.edittext_desc_update);

        editTextDesc.setText(hotspot.getDescription());

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, UserProfileFragmentActivity.class));
                break;
            case R.id.nav_map:
                startActivity(new Intent(this, MapFragmentActivity.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void updateHotspot() {
        String desc = editTextDesc.getText().toString().trim();

        CollectionReference hotspotCollection = db.collection("hotspots");

        hotspotCollection.document(hotspot.getId())
                .update(
                        "description", desc
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateHotspotActivity.this, "Hotspot Updated", Toast.LENGTH_LONG).show();
                    }
                });

        db.collection("hotspot lists").document(hotspot.getList_id()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        HotspotList hList = documentSnapshot.toObject(HotspotList.class);
                        Intent intent = new Intent(UpdateHotspotActivity.this, HotspotActivity.class);
                        intent.putExtra("hotspotList", hList);
                        startActivity(intent);
                    }
                });

    }

    private void deleteHotspot() {

        db.collection("hotspots").document(hotspot.getId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            db.collection("hotspot lists").document(hotspot.getList_id()).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            HotspotList hList = documentSnapshot.toObject(HotspotList.class);
                                            hList.deleteHotspot(hotspot.getId());
                                            hList.setListId(hotspot.getList_id());
                                            hList.setUser_id(mAuth.getCurrentUser().getUid());
                                            db.collection("hotspot lists").document(hotspot.getList_id()).set(hList);

                                            Toast.makeText(UpdateHotspotActivity.this, "Hotspot deleted", Toast.LENGTH_LONG).show();
                                            finish();
                                            Intent intent = new Intent(UpdateHotspotActivity.this, HotspotActivity.class);

                                            intent.putExtra("hotspotList", hList);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_update:
                updateHotspot();
                break;

            case R.id.button_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Deletion is permanent...");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteHotspot();
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
        }

    }
}
