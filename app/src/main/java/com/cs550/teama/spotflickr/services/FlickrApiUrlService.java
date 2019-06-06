package com.cs550.teama.spotflickr.services;

import android.util.Base64;
import android.util.Log;

import com.cs550.teama.spotflickr.App;
import com.cs550.teama.spotflickr.R;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class FlickrApiUrlService {
    private final static String TAG = "OAuthService";
    private final static String API_KEY = App.getContext().getString(R.string.flickr_api_key);
    private final static String SIGNATURE_KEY = App.getContext().getString(R.string.flickr_api_secret);
    private final static String BASE_URL = "https://www.flickr.com/services/rest/";
    private Map<String, String> params;
    private OAuthService oAuthService;

    public FlickrApiUrlService(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
        this.params = new HashMap<>();
    }
    private String getSignature(String key, String data){
        final String HMAC_ALGORITHM = "HmacSHA1";
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), HMAC_ALGORITHM);
        Mac macInstance = null;
        try {
            macInstance = Mac.getInstance(HMAC_ALGORITHM);
            macInstance.init(keySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] signedBytes = macInstance.doFinal(data.getBytes());
        return android.util.Base64.encodeToString(signedBytes, Base64.NO_WRAP);
    }

    private String getSignature(Map<String, String> params, String secretKey, String tokenUrl){
        String httpMethod = "GET";
        String queryText = getQueryTextByParams(params);
        String encodedQueryText = Utils.oauthEncode(queryText);
        String encodedUrl = Utils.oauthEncode(tokenUrl);
        String baseString = httpMethod + "&" + encodedUrl + "&" + encodedQueryText;
        return getSignature(secretKey, baseString);
    }

    private String getQueryTextByParams(Map<String, String> params){
        String result = "";
        // sort the parameters ascending
        ArrayList<String> sorted = new ArrayList<>();
        sorted.addAll(params.keySet());
        Collections.sort(sorted);
        for (String key : sorted){
            result += key + "=" + params.get(key) + "&";
        }
        return result.substring(0,result.length()-1); // Remove last "&" character
    }

    private void addMustHaveParams(Map<String, String> params){
        params.put("oauth_nonce",  "flickr_permission" + System.currentTimeMillis());
        params.put("oauth_timestamp",  String.valueOf(System.currentTimeMillis()/1000));
        params.put("oauth_consumer_key", API_KEY);
        params.put("oauth_signature_method","HMAC-SHA1");
        params.put("oauth_version", "1.0");
    }

    private String getLoginOAuthUrl() {
        Map<String, String> params = new HashMap<>();
        addMustHaveParams(params);
        params.put("oauth_token", oAuthService.getAccessTokenResponse().get("oauth_token")); // Get from other class (OAuthService)
        params.put("nojsoncallback", "1");
        params.put("format", "json");
        params.put("api_key", API_KEY);
        params.put("method", "flickr.test.login");

        String signature = getSignature(params, SIGNATURE_KEY + "&" +
                oAuthService.getAccessTokenResponse().get("oauth_token_secret"), BASE_URL);
        params.put("oauth_signature", Utils.oauthEncode(signature));
        return BASE_URL + "?" + getQueryTextByParams(params);
    }

    private void getFinalLoginUrl() {
        String oAuthUrl = this.getLoginOAuthUrl();
        Log.d(TAG, oAuthUrl);
    }
}
