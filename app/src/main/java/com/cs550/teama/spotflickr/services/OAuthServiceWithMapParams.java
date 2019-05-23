package com.cs550.teama.spotflickr.services;

import android.util.Base64;
import android.util.Log;

import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.interfaces.LoginObserver;
import com.cs550.teama.spotflickr.network.RetrofitOAuthInstance;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OAuthServiceWithMapParams {

    public static OAuthServiceWithMapParams INSTANCE;
    final static String TAG = "OAuthService";
    final static String API_KEY = "c78af6829b82ef76418e7563ee33fe85";
    final static String SIGNATURE_KEY = "76a0afc91403a9a7&";

    private Map<String, String> requestTokenResponse = new HashMap<>();
    private Map<String, String> accessTokenResponse = new HashMap<>();
    private final LoginObserver loginObserver;

    public OAuthServiceWithMapParams(LoginObserver loginObserver) {
        INSTANCE = this;
        this.loginObserver = loginObserver;
        getRequestToken();
    }

    private void getRequestToken() {
        ApiService service = RetrofitOAuthInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = service.getOAuthRequestToken(getRequestTokenQuery());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.d(TAG, res);
                    requestTokenResponse = Utils.separateParameters(res);
                    loginObserver.onRequestTokenReceived(OAuthServiceWithMapParams.this);
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private Map<String, String> getRequestTokenQuery() {
        Map<String, String> params = getBaseQueryMap();
        params.put("oauth_callback", "www.flickr.com");

        String signature = getUnencodedSignature(params);
        params.put("oauth_signature", Utils.oauthEncode(signature));
        return params;
    }

    public void getAccessToken(String verifier) {
        ApiService service = RetrofitOAuthInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = service.getOAuthRequestToken(getAccessTokenQuery(verifier));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.body() != null){
                        String res = response.body().string();
                        Log.d(TAG, res);
                        accessTokenResponse = Utils.separateParameters(res);
                        loginObserver.onLoginSuccess();
                    }
                } catch (IOException  e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
                loginObserver.onLoginFail();
            }
        });
    }

    private Map<String, String> getAccessTokenQuery(String verifier) {
        Map<String, String> params = getBaseQueryMap();
        params.put("oauth_verifier", verifier);
        params.put("oauth_token", requestTokenResponse.get("oauth_token"));

        String signature = getUnencodedSignature(params);
        params.put("oauth_signature", Utils.oauthEncode(signature));
        return params;
    }

    private Map<String, String> getBaseQueryMap() {
        Map<String, String> params = new HashMap<>();
        params.put("oauth_nonce",  "flickr_oauth" + String.valueOf(System.currentTimeMillis()));
        params.put("oauth_timestamp",  String.valueOf(System.currentTimeMillis()/1000));
        params.put("oauth_consumer_key", API_KEY);// + "&" + requestTokenResponse.get("oauth_token_secret"));
        params.put("oauth_signature_method","HMAC-SHA1");
        params.put("oauth_version", "1.0");
        return params;
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
        return Base64.encodeToString(signedBytes, Base64.NO_WRAP);
    }

    private String getUnencodedSignature(Map<String, String> params) {
        String requestTokenUrl = "https://www.flickr.com/services/oauth/access_token/";
        String httpMethod = "GET";
        String encodedRequestTokenUrl = Utils.oauthEncode(requestTokenUrl);
        String queryText = getQueryTextByParams(params);
        String encodedQueryText = Utils.oauthEncode(queryText);
        String baseString = httpMethod + "&" + encodedRequestTokenUrl + "&" + encodedQueryText;
        return getSignature(SIGNATURE_KEY + requestTokenResponse.get("oauth_token_secret"), baseString);
    }

    private String getQueryTextByParams(Map<String, String> params){
        String result = "";
        // sort the parameters ascending
        ArrayList<String> sorted = new ArrayList<>();
        sorted.addAll(params.keySet());
        Collections.sort(sorted);
        for (String key : sorted){
            result += "oauth_"+ key + "=" + params.get(key) + "&";
        }
        return result.substring(0,result.length()-1); // Remove last "&" character
    }

    public String getAuthorizationURL(){
        String url = "https://www.flickr.com/services/oauth/authorize?" +
                "oauth_token=" +
                this.requestTokenResponse.get("oauth_token");
        return url;
    }

    public Map<String, String> getRequestTokenResponse() {
        return requestTokenResponse;
    }

    public Map<String, String> getAccessTokenResponse() {
        return accessTokenResponse;
    }
}
