package com.cs550.teama.spotflickr.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.hotspot.SaveHotspotActivity;
import com.cs550.teama.spotflickr.adapter.ImageListAdapter;
import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.model.photo.Photo;
import com.cs550.teama.spotflickr.model.photo.Photos;
import com.cs550.teama.spotflickr.model.User;
import com.cs550.teama.spotflickr.network.RetrofitInstance;
import com.cs550.teama.spotflickr.services.FlickrApiUrlService;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PhotoListActivity";
    private Context context;
    private String place_id;
    private String lat;
    private String lon;

    private ListView listView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());

    private User current_user;
    private String oauth_token;
    private String oauth_token_secret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        context = this;

        /* Get place_id, latitude and longitude coordinates from map*/
        Bundle extras = getIntent().getExtras();
        place_id = extras.getString("place_id");
        lat = extras.getString("latitude");
        lon = extras.getString("longitude");

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    /* Check if user is logged in and has valid oauth token*/
                    current_user = documentSnapshot.toObject(User.class);
                    oauth_token = current_user.getOauth_token();
                    oauth_token_secret = current_user.getOauth_token_secret();
                    /* Send request to get photos at this location*/
                    sendRequest();
                }
            }
        });

        findViewById(R.id.adding_hotspot).setOnClickListener(this);
    }

    private void sendRequest() {
        FlickrApiUrlService urlService = prepareUrlService();
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Log.d(TAG, urlService.getRequestUrl());
        Call<Photos> call = service.getPhotosForLocation(urlService.getRequestUrl());
        call.enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                Log.d(TAG, "Successful sendRequest");
                if (response.body() != null) {
                    generatePhotoList(response.body().getPhotos().getPhoto());
                } else {
                    Toast.makeText(PhotoListActivity.this, "There is no photo here" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Toast.makeText(PhotoListActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private FlickrApiUrlService prepareUrlService() {
        FlickrApiUrlService urlService = new FlickrApiUrlService(OAuthService.INSTANCE, oauth_token, oauth_token_secret);
//        urlService.addParam("method", "flickr.photos.geo.photosForLocation");
        urlService.addParam("method", "flickr.photos.search");
        urlService.addParam("place_id", place_id);
        urlService.addParam("lat", lat);
        urlService.addParam("lon", lon);
        urlService.addParam("radius", String.valueOf(1));
        return urlService;
    }


    /** Method to generate List of photos using RecyclerView with custom adapter*/
    private void generatePhotoList(ArrayList<Photo> photoArrayList) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < photoArrayList.size(); i++) {
            Photo photo = photoArrayList.get(i);
            Log.d(TAG, photo.getId());
            urls.add(buildPhotoUrl(
                    String.valueOf(photo.getFarm()),
                    photo.getServer(),
                    photo.getId(),
                    photo.getSecret()
            ));
        }
        listView = findViewById(R.id.list_view);
        listView.setAdapter(new ImageListAdapter(PhotoListActivity.this, urls));
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            /* Start new page to get more detailed view of photo*/
            Intent intent = new Intent(PhotoListActivity.this, PhotoDetailActivity.class);
            Photo photo = photoArrayList.get(position);
            intent.putExtra("id", photo.getId());
            intent.putExtra("farm", String.valueOf(photo.getFarm()));
            intent.putExtra("server", photo.getServer());
            intent.putExtra("secret", photo.getSecret());
            startActivity(intent);
        });
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
        if (v.getId() == R.id.adding_hotspot) {
            Intent intent = new Intent(this, SaveHotspotActivity.class);
            intent.putExtra("place_id", place_id);
            intent.putExtra("latitude", lat);
            intent.putExtra("longitude", lon);
            startActivity(intent);
        }
    }
}
