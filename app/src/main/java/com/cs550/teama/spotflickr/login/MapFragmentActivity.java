package com.cs550.teama.spotflickr.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.User;
import com.cs550.teama.spotflickr.api.PhotoListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private LocationManager lm;
    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).readTimeout(300, TimeUnit.SECONDS).build();
    private JSONArray placeList = new JSONArray();
    private DrawerLayout drawer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DocumentReference documentReference = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Map");
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

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                    user_name.setText(current_user.getUsername());
                    user_email.setText(current_user.getEmail());
                }
                else
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
            }
        });

//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        TextView user_name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_name);
//        user_name.setText(current_user.getUsername());
//        TextView user_email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_email);
//        user_email.setText("");

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        mapFragment.getMapAsync(this);

        LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
        FlickerHttpTask task = new FlickerHttpTask();
        task.execute(current_location);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(this, UserProfileFragmentActivity.class));
                break;
            case R.id.nav_map:
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        naverMap.setLocationSource(locationSource);
        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(current_location);
        naverMap.moveCamera(cameraUpdate);
        locationOverlay.setPosition(current_location);

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);



    }



    private class FlickerHttpTask extends AsyncTask<LatLng, Void, Void> implements OnMapReadyCallback {
        JSONArray placeLIst;
        @Override
        protected Void doInBackground(LatLng... params) {
            LatLng CurrentLocation = params[0];
            double minLat = CurrentLocation.latitude-0.01;
            double maxLat = CurrentLocation.latitude+0.01;
            double minLng = CurrentLocation.longitude-0.01;
            double maxLng = CurrentLocation.longitude+0.01;

            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            //it return with format as json.
            try {
                String target_url = "https://secure.flickr.com/services/rest/?method=flickr.places.placesForBoundingBox&bbox="+minLng+","+minLat+","+maxLng+","+maxLat+"&api_key=c78af6829b82ef76418e7563ee33fe85&place_type_id=22&format=json";
                Request request = new Request.Builder()
                        .url(target_url)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                String json_body = body.substring(14, body.length()-1);
                JSONArray placeList = new JSONObject(json_body).getJSONObject("places").getJSONArray("place");
                this.placeLIst = placeList;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }


        @Override
        @UiThread
        public void onMapReady(@NonNull NaverMap naverMap) {
            System.out.println(this.placeLIst);

            try{
                for (int i = 0; i < this.placeLIst.length(); i++) {
                    try {
                        final JSONObject place= this.placeLIst.getJSONObject(i);
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(place.getDouble("latitude"),place.getDouble("longitude")));
                        marker.setOnClickListener(o -> {
                            Intent intent = new Intent(MapFragmentActivity.this, PhotoListActivity.class);
                            try {
                                intent.putExtra("content", place.getString("_content"));
                                intent.putExtra("latitude", Double.toString(place.getDouble("latitude")));
                                intent.putExtra("longitude", Double.toString(place.getDouble("longitude")));
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            return true;
                        });
                        marker.setMap(naverMap);

                        InfoWindow infoWindow = new InfoWindow();
                        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
                            @NonNull
                            @Override
                            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                try {
                                    return place.getString("_content");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });
                        infoWindow.open(marker);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                } catch(NullPointerException e){
                e.printStackTrace();
            }
        }
        }
    }

