package com.cs550.teama.spotflickr.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.adapter.ImageListAdapter;
import com.cs550.teama.spotflickr.adapter.PhotoAdapter;
import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.model.Photo;
import com.cs550.teama.spotflickr.model.Photos;
import com.cs550.teama.spotflickr.network.RetrofitInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoListActivity extends AppCompatActivity {
    private static final String TAG = "PhotoListActivity";
    private static final String API_KEY = "c78af6829b82ef76418e7563ee33fe85";
    private Context context;

    private PhotoAdapter adapter;
    private RecyclerView recyclerView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        context = this;

        /* Create handle for the RetrofitInstance interface*/
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        /* Call the method with parameter in the interface to get the notice data*/
        Map<String, String> query = getQuery();
        Call<Photos> call = service.getRecentPhotos(query);

        call.enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                generatePhotoList(response.body().getPhotos().getPhoto());
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Toast.makeText(PhotoListActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    private Map<String, String> getQuery() {
        Map<String, String> query = new HashMap<>();
        query.put("method", "flickr.photos.getRecent");
        query.put("api_key", API_KEY);
        query.put("format", "json");
        query.put("nojsoncallback", "1");
        return query;
    }

    /** Method to generate List of photos using RecyclerView with custom adapter*/
    private void generatePhotoList(ArrayList<Photo> photoArrayList) {
//        recyclerView = findViewById(R.id.recycler_view_notice_list);
//        adapter = new PhotoAdapter(photoArrayList);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PhotoListActivity.this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(adapter);

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
}
