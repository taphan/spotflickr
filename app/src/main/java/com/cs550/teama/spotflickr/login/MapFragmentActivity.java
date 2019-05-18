package com.cs550.teama.spotflickr.login;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;


import com.cs550.teama.spotflickr.R;
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
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapFragmentActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private LocationManager lm;
    private OkHttpClient client = new OkHttpClient();
    private JSONArray placeList = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

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



//    private ArrayList<LatLng> getPositionsOfHotSpot() throws IOException {
//        Request request = new Request.Builder()
//                .url("https://secure.flickr.com/services/rest/?method=flickr.places.getTopPlacesList&api_key=c78af6829b82ef76418e7563ee33fe85&place_type_id=22&format=json")
//                .build();
//        //it return with format as json.
//        Response response = client.newCall(request).execute();
//        System.out.println(response.toString());
//
//        return new ArrayList<LatLng>();
//    }

    private class FlickerHttpTask extends AsyncTask<LatLng, Void, Void> implements OnMapReadyCallback {
        JSONArray placeLIst;
        @Override
        protected Void doInBackground(LatLng... params) {
            LatLng CurrentLocation = params[0];
            double minLat = CurrentLocation.latitude-0.01;
            double maxLat = CurrentLocation.latitude+0.01;
            double minLng = CurrentLocation.longitude-0.01;
            double maxLng = CurrentLocation.longitude+0.01;

            //it return with format as json.
            try {
                Request request = new Request.Builder()
                        .url("https://secure.flickr.com/services/rest/?method=flickr.places.placesForBoundingBox&bbox="+minLng+","+minLat+","+maxLng+","+maxLat+"&api_key=c78af6829b82ef76418e7563ee33fe85&place_type_id=22&format=json")
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
            for (int i = 0; i < this.placeLIst.length(); i++) {
                try {
                    final JSONObject place= this.placeLIst.getJSONObject(i);
                    Marker marker = new Marker();
                    marker.setPosition(new LatLng(place.getDouble("latitude"),place.getDouble("longitude")));
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
            }
        }
    }

