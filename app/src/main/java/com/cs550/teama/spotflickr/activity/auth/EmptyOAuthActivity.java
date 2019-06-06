package com.cs550.teama.spotflickr.activity.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.services.FlickrApiUrlService;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

import java.util.Map;

public class EmptyOAuthActivity extends AppCompatActivity {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == flickrLoginActivityRequestCode) {
            FlickrApiUrlService urlService = new FlickrApiUrlService(OAuthService.INSTANCE);
            if (resultCode == Activity.RESULT_OK) {
                Map<String, String> loginInfo = OAuthService.INSTANCE.getAccessTokenResponse();
                String name = loginInfo.get("fullname");
                name = Utils.oauthDecode(name);
                new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Successfully logged in!\nWelcome " + name)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                textview.setText("You are now logged in as " + name + ".\n" +
                        "You username is " + Utils.oauthDecode(loginInfo.get("username")) + ".");
                button.setText("Logged in");
                button.setEnabled(false);
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
