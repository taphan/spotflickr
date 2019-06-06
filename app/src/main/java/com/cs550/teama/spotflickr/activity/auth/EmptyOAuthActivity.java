package com.cs550.teama.spotflickr.activity.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.model.Photo;
import com.cs550.teama.spotflickr.model.Photos;
import com.cs550.teama.spotflickr.network.RetrofitInstance;
import com.cs550.teama.spotflickr.services.FlickrApiUrlService;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmptyOAuthActivity extends AppCompatActivity {
    private final static String TAG = "EmptyOAuthActivity";
    private static final int flickrLoginActivityRequestCode = 1;
    private TextView textview;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        textview = findViewById(R.id.text_view);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(EmptyOAuthActivity.this, FlickrLoginActivity.class);
                startActivityForResult(myIntent, flickrLoginActivityRequestCode);
            }
        });
    }
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

    private void sendRequest() {
        FlickrApiUrlService urlService = new FlickrApiUrlService(OAuthService.INSTANCE);
        urlService.addParam("method", "flickr.photos.geo.photosForLocation");
        urlService.addParam("lat", "36.376");
        urlService.addParam("lon", "127.336");
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<Photos> call = service.getPhotosForLocation(urlService.getRequestUrl());
        call.enqueue(new Callback<Photos>() {
            @Override
            public void onResponse(Call<Photos> call, Response<Photos> response) {
                Log.d(TAG, "Successful sendRequest");
                ArrayList<Photo> photoArrayList = response.body().getPhotos().getPhoto();
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
            }

            @Override
            public void onFailure(Call<Photos> call, Throwable t) {
                Toast.makeText(EmptyOAuthActivity.this, "Something went wrong...Error message: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == flickrLoginActivityRequestCode) {
            sendRequest();
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Redirect to new activity");
                Map<String, String> loginInfo = OAuthService.INSTANCE.getAccessTokenResponse();
                Intent intent = new Intent(EmptyOAuthActivity.this, RegisterPasswordActivity.class);
                intent.putExtra("name", Utils.oauthDecode(loginInfo.get("fullname")));
                intent.putExtra("username", Utils.oauthDecode(loginInfo.get("username")));
                startActivity(intent);
                /*new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Successfully logged in!\nWelcome " + name)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                textview.setText("You are now logged in as " + name + ".\n" +
                        "You username is " + Utils.oauthDecode(loginInfo.get("username")) + ".");
                button.setText("Logged in");
                button.setEnabled(false);*/
            } else if (resultCode == Activity.RESULT_CANCELED) {
                new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Failed to login")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
}
