package com.cs550.teama.spotflickr.activity.hotspot;

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
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.activity.user.UserProfileFragmentActivity;
import com.cs550.teama.spotflickr.model.hotspot.HotspotList;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UpdateHotspotListActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    private EditText editTextName;
    private EditText editTextDesc;

    private HotspotList hotspotList;
    private List<String> hotspotListNames;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_hotspot_list);

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

        hotspotList = (HotspotList) getIntent().getSerializableExtra("hotspotList");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Hotspot List Manager");
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final TextView user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);


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

        editTextName = findViewById(R.id.edittext_name_update);
        editTextDesc = findViewById(R.id.edittext_desc_update);

        editTextName.setText(hotspotList.getName());
        editTextDesc.setText(hotspotList.getDescription());

        findViewById(R.id.button_update).setOnClickListener(this);
        findViewById(R.id.button_delete).setOnClickListener(this);
    }

    private boolean hasValidationErrors(String name, String desc) {
        if (name.isEmpty()) {
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return true;
        }

        for (int i = 0; i < hotspotListNames.size(); i++) {
            if (name.equals(hotspotListNames.get(i)) && !hotspotList.getName().equals(name)) {
                editTextName.setError("Same name already exists");
                editTextName.requestFocus();
                return true;
            }
        }

        return false;
    }

    private void updateHotspotList() {
        String name = editTextName.getText().toString().trim();
        String desc = editTextDesc.getText().toString().trim();

        if (!hasValidationErrors(name, desc)) {

            CollectionReference dbProducts = db.collection("hotspot lists");

            dbProducts.document(hotspotList.getListId())
                    .update(
                            "name", name,
                            "description", desc
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(UpdateHotspotListActivity.this, "Hotspot List Updated", Toast.LENGTH_LONG).show();
                }
            });

            Intent intent = new Intent(this, HotspotListActivity.class);
            startActivity(intent);
        }
    }

    private void deleteHotspotList() {
        if (current_user.getHotspotIdListSize() == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cannot delete the list! You need at least one hotspot list in your account!");

            builder.setPositiveButton("Confirmed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog ad = builder.create();
            ad.show();
        } else {
            db.collection("hotspot lists").document(hotspotList.getListId()).delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                for (int i = 0; i < hotspotList.getHotspotIdSize(); i++) {
                                    db.collection("hotspots").document(hotspotList.getHotspot_id().get(i)).delete();
                                }
                                current_user.deleteHotspotList(hotspotList.getListId());
                                userDocRef.set(current_user);
                                Toast.makeText(UpdateHotspotListActivity.this, "Hotspot list deleted", Toast.LENGTH_LONG).show();
                                finish();
                                Intent intent = new Intent(UpdateHotspotListActivity.this, HotspotListActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_update:
                updateHotspotList();
                break;

            case R.id.button_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Deletion is permanent...");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteHotspotList();
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
}
