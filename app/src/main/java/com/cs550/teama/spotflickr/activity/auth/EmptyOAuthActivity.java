package com.cs550.teama.spotflickr.activity.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

public class EmptyOAuthActivity extends AppCompatActivity {
    private static final int flickrLoginActivityRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent myIntent = new Intent(EmptyOAuthActivity.this, FlickrLoginActivity.class);
                startActivityForResult(myIntent, flickrLoginActivityRequestCode);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == flickrLoginActivityRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                String name = OAuthService.INSTANCE.getAccessTokenResponse().get("fullname");
                name = Utils.oauthDecode(name);
                new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Successfully logged in!\nWelcome " + name)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Failed to login
                new AlertDialog.Builder(this)
                        .setTitle("Login Status")
                        .setMessage("Failed to login")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }
}
