package com.cs550.teama.spotflickr.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.hotspot.SaveHotspotActivity;
import com.cs550.teama.spotflickr.adapter.ImageListAdapter;
import com.cs550.teama.spotflickr.adapter.PhotoAdapter;
import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.model.Photo;
import com.cs550.teama.spotflickr.model.Photos;
import com.cs550.teama.spotflickr.network.RetrofitInstance;
import com.cs550.teama.spotflickr.services.FlickrApiUrlService;
import com.cs550.teama.spotflickr.services.OAuthService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PhotoListActivity";
    private static final String API_KEY = "c78af6829b82ef76418e7563ee33fe85";
    private Context context;
    private String lat;
    private String lon;
    private String content;

    private PhotoAdapter adapter;
    private RecyclerView recyclerView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        context = this;

        /* Get latitude and longitude coordinates from map*/
        Bundle extras = getIntent().getExtras();
        content = extras.getString("content");
        lat = extras.getString("latitude");
        lon = extras.getString("longitude");

        findViewById(R.id.adding_hotspot).setOnClickListener(this);
        /* Send request to get photos at this location*/
        sendRequest();
    }

    private void sendRequest() {
        FlickrApiUrlService urlService = prepareUrlService();
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Photos> call = service.getPhotosForLocation(urlService.getRequestUrl());
        call.enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                Log.d(TAG, "Successful sendRequest");
                generatePhotoList(response.body().getPhotos().getPhoto());
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Toast.makeText(PhotoListActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private FlickrApiUrlService prepareUrlService() {
        FlickrApiUrlService urlService = new FlickrApiUrlService(OAuthService.INSTANCE);
        urlService.addParam("method", "flickr.photos.geo.photosForLocation");
        urlService.addParam("lat", lat);
        urlService.addParam("lon", lon);
        return urlService;
    }


    /** Method to generate List of photos using RecyclerView with custom adapter*/
    private void generatePhotoList(ArrayList<Photo> photoArrayList) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < photoArrayList.size(); i++) {
            Photo photo = photoArrayList.get(i);
            urls.add(buildPhotoUrl(
                    String.valueOf(photo.getFarm()),
                    photo.getServer(),
                    photo.getId(),
                    photo.getSecret()
            ));
        }
        listView = findViewById(R.id.list_view);
        listView.setAdapter(new ImageListAdapter(PhotoListActivity.this, urls));
    }

    // Build URL in the form: https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg
    private String buildPhotoUrl(String farmId, String serverId, String id, String secret) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://farm");
        sb.append(farmId);
        sb.append(".staticflickr.com/");
        sb.append(serverId);
        sb.append("/");
        sb.append(id);
        sb.append("_");
        sb.append(secret);
        sb.append(".jpg");
        return sb.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adding_hotspot:
                Intent intent = new Intent(this, SaveHotspotActivity.class);
                intent.putExtra("content", content);
                intent.putExtra("latitude", lat);
                intent.putExtra("longitude", lon);
                startActivity(intent);
                break;
        }
    }
}
