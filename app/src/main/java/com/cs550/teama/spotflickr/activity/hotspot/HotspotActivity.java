package com.cs550.teama.spotflickr.activity.hotspot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.MapFragmentActivity;
import com.cs550.teama.spotflickr.activity.user.UserProfileFragmentActivity;
import com.cs550.teama.spotflickr.adapter.HotspotAdapter;
import com.cs550.teama.spotflickr.model.Hotspot;
import com.cs550.teama.spotflickr.model.HotspotList;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HotspotActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private RecyclerView recyclerView;
    private HotspotAdapter adapter;
    private List<Hotspot> hotspotList;
    private ProgressBar progressBar;

    private HotspotList hotspotListList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot);

        hotspotListList = (HotspotList) getIntent().getSerializableExtra("hotspotList");

        progressBar = findViewById(R.id.progressbar);

        recyclerView = findViewById(R.id.recyclerview_hotspots);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hotspotList = new ArrayList<>();
        adapter = new HotspotAdapter(this, hotspotList);

        recyclerView.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Hotspots");
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

        db.collection("hotspots").whereEqualTo("list_id", hotspotListList.getListId()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressBar.setVisibility(View.GONE);
                        if (!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for (DocumentSnapshot d : list) {
                                Hotspot h = d.toObject(Hotspot.class);
                                h.setId(d.getId());
                                hotspotList.add(h);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
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
