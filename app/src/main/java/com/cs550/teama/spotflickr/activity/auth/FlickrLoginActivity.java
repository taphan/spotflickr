package com.cs550.teama.spotflickr.activity.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.interfaces.LoginObserver;
import com.cs550.teama.spotflickr.services.OAuthService;
import com.cs550.teama.spotflickr.services.Utils;

import java.util.Map;

public class FlickrLoginActivity extends AppCompatActivity implements LoginObserver {
    private final static String TAG = "FlickrLoginActivity";
    WebView webView;
    ProgressBar progressBar_cyclic;
    SwipeRefreshLayout swipe;
    OAuthService oauth;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickr_oauth);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onLoginFail());

        webView = findViewById(R.id.webView);
        progressBar_cyclic = findViewById(R.id.progressBar_cyclic);
        progressBar_cyclic.setVisibility(View.GONE);
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(() -> {
            if (!oauth.getAuthorizationURL().isEmpty())
                WebAction(oauth.getAuthorizationURL());
        });
        oauth = new OAuthService(this);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void WebAction(final String loginUrl){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.loadUrl(loginUrl);
        swipe.setRefreshing(true);
        webView.setWebViewClient(new WebViewClient(){

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                webView.loadUrl("file:///android_assets/error.html");
            }

            public void onPageFinished(WebView view, String url) {
                swipe.setRefreshing(false);
            }
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                /* Flickr do not have a callback for authentication rejections.
                   Which means there are no easy way to listen to the "No Thanks" button click.
                   Our approach is to check if the url goes directly from the loginUrl to flickr.com
                 */
                if (webView.canGoBack()){ // If there is a previous URL
                    String prevUrl = webView.copyBackForwardList().getItemAtIndex(webView.copyBackForwardList().getCurrentIndex()-1).getUrl();
                    if (prevUrl.equals(loginUrl) && url.equals(OAuthService.FLICKR_URL)){
                        onLoginFail();
                    }
                }

                // Check if there is oauth_verifier in the url.
                Log.d(TAG, " ----- OAuth step 2 URL: " + url);
                Map<String, String> queries = Utils.getUrlParameters(url);
                if (queries != null && queries.containsKey("oauth_verifier")){
                    // The request token is approved
                    // Now get the Access Token with the verifier
                    oauth.getAccessToken(queries.get("oauth_verifier"));
                    // hide the webView and show a circle
                    webView.setVisibility(View.GONE);
                    progressBar_cyclic.setVisibility(View.VISIBLE);
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
