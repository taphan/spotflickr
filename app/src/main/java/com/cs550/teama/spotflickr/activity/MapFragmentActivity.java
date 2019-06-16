package com.cs550.teama.spotflickr.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.user.UserProfileFragmentActivity;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
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
    private LatLng current_location;
    private EditText search_text;
    private MapFragment mapFragment;
    private LatLng[] route;
    private LatLng dest_place;
    private Button routeButton;
    private Button shortButton;
    private ImageButton photoListButton;
    private PathOverlay[] pathes;
    private Marker[] markers;
    private String target_place_id;
    String currentImagePath = null;
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
        routeButton = findViewById(R.id.route_button);
        photoListButton = findViewById(R.id.photolist_button);
        shortButton = findViewById(R.id.short_route_button);
        photoListButton.setOnClickListener(this);
        routeButton.setOnClickListener(this);
        shortButton.setOnClickListener(this);
        findViewById(R.id.buttonCamera).setOnClickListener(this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.current_location = new LatLng(location.getLatitude(), location.getLongitude());
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final TextView user_name = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView user_email = navigationView.getHeaderView(0).findViewById(R.id.user_email);

        this.search_text = (EditText) findViewById(R.id.searchText);
        ImageButton search_button = (ImageButton) findViewById(R.id.searchButton);
        search_button.setOnClickListener(this);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);



        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                    user_name.setText(current_user.getUsername());
                    user_email.setText(current_user.getEmail());
                } else
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
            }
        });


        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }



        mapFragment.getMapAsync(this);

        LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
        FlickerHttpTask task = new FlickerHttpTask();
        task.execute(current_location);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile:
                startActivity(new Intent(MapFragmentActivity.this, UserProfileFragmentActivity.class));
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
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(this.current_location);
        naverMap.moveCamera(cameraUpdate);
        locationOverlay.setPosition(this.current_location);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        if(dest_place != null){
            if (pathes != null) {
                for (int i = 0; i < pathes.length; i++) {
                    pathes[i].setMap(null);
                }
            }
            if (markers != null) {
                for (int i = 0; i < markers.length; i++) {
                    markers[i].setMap(null);
                }
            }
            PathOverlay short_path = new PathOverlay();
            short_path.setCoords(Arrays.asList(current_location , dest_place));
            short_path.setMap(naverMap);
            pathes = new PathOverlay[]{short_path};

        }

    }

    public void captureImage(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File imageFile = null;

            try {
                imageFile = getImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, 1);
            }
        }
    }

    private File getImageFile() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "jpg_"+timestamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        currentImagePath = imageFile.getAbsolutePath();
        return imageFile;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchButton:
                String search_location = search_text.getText().toString();
                System.out.println(search_location);
                if (search_location == ""){

                } else {
                    LatLng target_location = this.current_location;
                    String url = "https://naveropenapi.apigw.ntruss.com/map-place/v1/search?query=" + search_location + "&coordinate=" + this.current_location.longitude + "," + this.current_location.latitude;
                    try{
                        Request search_request = new Request.Builder()
                            .addHeader("X-NCP-APIGW-API-KEY-ID", "vh8iieoys0")
                            .addHeader("X-NCP-APIGW-API-KEY", "4hc45eBq62ftAUkFh14bTiCahtfgxiMLGY7XgExk")
                            .url(url).build();
                        Response response = client.newCall(search_request).execute();
                        String body = response.body().string();
                        JSONObject jsonResponse = new JSONObject(body);
                        JSONArray places = jsonResponse.getJSONArray("places");
                        JSONObject current_place = places.getJSONObject(0);
                        this.current_location = new LatLng(Double.parseDouble(current_place.getString("x")),Double.parseDouble(current_place.getString("y")));
                        mapFragment.getMapAsync(this);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            case R.id.photolist_button:
                Intent intent = new Intent(MapFragmentActivity.this, PhotoListActivity.class);
                intent.putExtra("place_id",target_place_id);
                intent.putExtra("latitude",Double.toString(dest_place.latitude));
                intent.putExtra("longitude",Double.toString(dest_place.longitude));
                startActivity(intent);
                break;
            case R.id.route_button:
                RouteHttpTask task = new RouteHttpTask();
                task.execute(current_location);
                break;
            case R.id.short_route_button:
                mapFragment.getMapAsync(this);
                break;
            case R.id.buttonCamera:
                captureImage(view);
                break;

        }
    }

    private class FlickerHttpTask extends AsyncTask<LatLng, Void, Void> implements OnMapReadyCallback {
        JSONArray placeList;

        @Override
        protected Void doInBackground(LatLng... params) {
            LatLng CurrentLocation = params[0];
            double minLat = CurrentLocation.latitude - 0.01;
            double maxLat = CurrentLocation.latitude + 0.01;
            double minLng = CurrentLocation.longitude - 0.01;
            double maxLng = CurrentLocation.longitude + 0.01;

//            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            //it return with format as json.
            try {
                String target_url = "https://secure.flickr.com/services/rest/?method=flickr.places.placesForBoundingBox&bbox=" + minLng + "," + minLat + "," + maxLng + "," + maxLat + "&api_key=c78af6829b82ef76418e7563ee33fe85&place_type_id=22&format=json";
                Request request = new Request.Builder()
                        .url(target_url)
                        .build();
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                String json_body = body.substring(14, body.length() - 1);
                JSONArray placeList = new JSONObject(json_body).getJSONObject("places").getJSONArray("place");
                this.placeList = placeList;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
//            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }


        @Override
        @UiThread
        public void onMapReady(@NonNull NaverMap naverMap) {
            System.out.println(this.placeList);

            try {
                for (int i = 0; i < this.placeList.length(); i++) {
                    try {
                        final JSONObject place = this.placeList.getJSONObject(i);
                        Log.d("MapFragmentActivity: ", String.valueOf(place));
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(place.getDouble("latitude"), place.getDouble("longitude")));
                        marker.setOnClickListener(o -> {
                            try {
                                dest_place = new LatLng(place.getDouble("latitude"), place.getDouble("longitude"));
                                target_place_id = place.getString("place_id");
                                routeButton.setVisibility(View.VISIBLE);
                                photoListButton.setVisibility(View.VISIBLE);
                                shortButton.setVisibility(View.VISIBLE);
                                return true;
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
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private class RouteHttpTask extends AsyncTask<LatLng, Void, Void> implements OnMapReadyCallback {
        JSONArray transit_list;
        @Override
        protected Void doInBackground(LatLng... params){
            String target_url = "https://maps.googleapis.com/maps/api/directions/json?origin="+Double.toString(current_location.latitude)+","+Double.toString(current_location.longitude)+"&destination="+Double.toString(dest_place.latitude)+","+Double.toString(dest_place.longitude)+"&mode=transit&key=AIzaSyCmRam6VFK0HHYlxKPK5Rd9cskR-Q8j2m0";
            Request request = new Request.Builder()
                    .url(target_url)
                    .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String body = null;
            try {
                body = response.body().string();
                JSONArray routes = null;
                routes = new JSONObject(body).getJSONArray("routes");
                JSONArray steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                transit_list = new JSONArray();
                for (int i=0;i< steps.length(); i++){
                    JSONObject step = steps.getJSONObject(i);
                    transit_list.put(step);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            mapFragment.getMapAsync(this);

        }

        @Override
        @UiThread
        public void onMapReady(@NonNull NaverMap naverMap){
            PathOverlay[] new_pathes = new PathOverlay[transit_list.length()];
            Marker[] new_markers = new Marker[transit_list.length()];
            if (pathes != null) {
                for (int j = 0; j < pathes.length; j++) {
                    pathes[j].setMap(null);
                }
            }
            if (markers != null) {
                for (int i = 0; i < markers.length; i++) {
                    markers[i].setMap(null);
                }
            }
            for (int i=0; i<transit_list.length();i++){
                try {
                    JSONObject step =transit_list.getJSONObject(i);
                    Marker marker = new Marker();
                    JSONObject target_location = step.getJSONObject("start_location");
                    JSONObject end_location = step.getJSONObject("end_location");

                    marker.setPosition(new LatLng(target_location.getDouble("lat"),target_location.getDouble("lng")));
                    marker.setIcon(MarkerIcons.YELLOW);
                    marker.setMap(naverMap);
                    new_markers[i] = marker;

                    InfoWindow infoWindow = new InfoWindow();
                    infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
                        @NonNull
                        @Override
                        public CharSequence getText(@NonNull InfoWindow infoWindow) {
                            try {
                                return step.getString("html_instructions");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
                    infoWindow.open(marker);

                    PathOverlay path = new PathOverlay();
                    path.setCoords(Arrays.asList(
                            new LatLng(target_location.getDouble("lat"),target_location.getDouble("lng")),
                            new LatLng(end_location.getDouble("lat"),end_location.getDouble("lng"))
                    ));
                    path.setColor(Color.GRAY);
                    path.setMap(naverMap);
                    new_pathes[i] = path;

                } catch (JSONException e) {;
                    e.printStackTrace();
                }
            }
            pathes = new_pathes;
            markers = new_markers;
        }
    }
}