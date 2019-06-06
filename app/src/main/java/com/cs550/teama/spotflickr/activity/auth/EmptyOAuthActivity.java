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
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

import java.util.Map;
// TODO: Remove
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == flickrLoginActivityRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                Map<String, String> loginInfo = OAuthService.INSTANCE.getAccessTokenResponse();
                Intent intent = new Intent(EmptyOAuthActivity.this, RegisterPasswordActivity.class);
                intent.putExtra("name", Utils.oauthDecode(loginInfo.get("fullname")));
                intent.putExtra("username", Utils.oauthDecode(loginInfo.get("username")));
                startActivity(intent);

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
