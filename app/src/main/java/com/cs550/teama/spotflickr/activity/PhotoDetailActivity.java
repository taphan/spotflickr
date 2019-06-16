package com.cs550.teama.spotflickr.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.App;
import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.model.photo.PhotoDetail;
import com.cs550.teama.spotflickr.model.photo.PhotoInfo;
import com.cs550.teama.spotflickr.network.RetrofitInstance;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class  PhotoDetailActivity extends AppCompatActivity {
    private static final String TAG = "PhotoDetailActivity";
    private final static String API_KEY = App.getContext().getString(R.string.flickr_api_key);
    private String photo_id;
    private String photo_farm;
    private String photo_server;
    private String photo_secret;
    private ImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        /* Get photo id of chosen photo from photo list*/
        Bundle extras = getIntent().getExtras();
        photo_id = extras.getString("id");
        photo_farm = extras.getString("farm");
        photo_server = extras.getString("server");
        photo_secret = extras.getString("secret");

        imgView = findViewById(R.id.imageView);
        sendPhotoDetailRequest();
    }

    private void sendPhotoDetailRequest() {
        Map<String, String> params = prepareParams();
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<PhotoDetail> call = service.getPhotoDetails(params);
        call.enqueue(new Callback<PhotoDetail>() {
            @Override
            public void onResponse(Call<PhotoDetail> call, Response<PhotoDetail> response) {
                Log.d(TAG, "Successful sendRequest");
                if (response.body() != null) {
                    generatePhotoDetails(response.body().getPhoto());
                } else {
                    Toast.makeText(PhotoDetailActivity.this, "This query is empty" ,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PhotoDetail> call, Throwable t) {
                Toast.makeText(PhotoDetailActivity.this,
                        "Something went wrong...Error message: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private Map<String, String> prepareParams() {
        Map<String, String> params = new HashMap<>();
        params.put("method", "flickr.photos.getInfo");
        params.put("api_key", API_KEY);
        params.put("photo_id", photo_id);
        params.put("nojsoncallback", "1");
        params.put("format", "json");
        return params;
    }

    private void generatePhotoDetails(PhotoInfo photo) {
        Log.d(TAG, "PhotoDetail is: " + photo.getFarm() + photo.getServer());
        String url = buildPhotoUrl(photo_farm,
                photo_server,
                photo_id,
                photo_secret);
        Picasso
                .with(this)
                .load(url)
                .into(imgView);
    }

    /** Build URL in the form: https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}.jpg*/
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
