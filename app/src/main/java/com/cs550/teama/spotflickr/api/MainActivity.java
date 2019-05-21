package com.cs550.teama.spotflickr.api;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.adapter.PhotoAdapter;
import com.cs550.teama.spotflickr.model.Photo;
import com.cs550.teama.spotflickr.model.Photos;
import com.cs550.teama.spotflickr.network.RetrofitInstance;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String API_KEY = "c78af6829b82ef76418e7563ee33fe85";

    private PhotoAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "1st log");

        /** Create handle for the RetrofitInstance interface*/
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        /** Call the method with parameter in the interface to get the notice data*/
        Map<String, String> query = new HashMap<>();
        query.put("method", "flickr.photos.getRecent");
        query.put("api_key", API_KEY);
        query.put("format", "json");
        query.put("nojsoncallback", "1");
        Call<Photos> call = service.getRecentPhotos(query);
        final Context context = this;

        call.enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                generatePhotoList(response.body().getPhotos().getPhoto());
                Log.d(TAG, response.body().getPhotos().getPages().toString());
                ImageView image = (ImageView) findViewById(R.id.image);
                Picasso.with(context)
                        .load("https://farm66.staticflickr.com/65535/40934234293_9226cfebcf.jpg")
                        .into(image);
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    /** Method to generate List of photos using RecyclerView with custom adapter*/
    private void generatePhotoList(ArrayList<Photo> photoArrayList) {
        recyclerView = findViewById(R.id.recycler_view_notice_list);
        adapter = new PhotoAdapter(photoArrayList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

}
