package com.cs550.teama.spotflickr.activity.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.interfaces.LoginObserver;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

import java.util.Map;

public class FlickrLoginActivity extends AppCompatActivity implements LoginObserver {
    final static String TAG = "FlickrLoginActivity";
    WebView webView;
    SwipeRefreshLayout swipe;
    OAuthService oauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_oauth);

        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!oauth.getAuthorizationURL().isEmpty())
                    WebAction(oauth.getAuthorizationURL());
            }
        });

        oauth = new OAuthService(this);
    }


    public void WebAction(String loginUrl){
        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.loadUrl(loginUrl);
        swipe.setRefreshing(true);
        webView.setWebViewClient(new WebViewClient(){

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                webView.loadUrl("file:///android_assets/error.html");
            }

            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                swipe.setRefreshing(false);
                // check if there is oauth_verifier in the url.
                Map<String, String> queries = Utils.getUrlParameters(url);
                if (queries != null && queries.containsKey("oauth_verifier")){
                    // The request token is approved
                    // Now get the Access Token with the verifier
                    System.out.println("------ APPROVED Request token --------");
                    oauth.getAccessToken(queries.get("oauth_verifier"));

                    // TODO: hide the webView now??
                }

            }

        });

    }


    @Override
    public void onBackPressed(){

        if (webView.canGoBack()){
            webView.goBack();
        } else {
            onLoginFail();
        }
    }


    @Override
    public void onRequestTokenReceived(OAuthService oauth) {
        String url = oauth.getAuthorizationURL();
        WebAction(url);
    }

    @Override
    public void onLoginFail() {
        Log.d(TAG, "----- LOGIN FAILED ------");
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    @Override
    public void onLoginSuccess() {
        Log.d(TAG, "----- LOGIN SUCCESS ------");
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}