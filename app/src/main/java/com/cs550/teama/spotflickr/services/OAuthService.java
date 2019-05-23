package com.cs550.teama.spotflickr.services;

import android.util.Base64;
import android.util.Log;

import com.cs550.teama.spotflickr.interfaces.ApiService;
import com.cs550.teama.spotflickr.interfaces.LoginObserver;
import com.cs550.teama.spotflickr.network.RetrofitInstance;

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

public class OAuthService {
    public static OAuthService INSTANCE;
    final static String API_KEY = "c78af6829b82ef76418e7563ee33fe85";
    final static String SIGNATURE_KEY = "76a0afc91403a9a7&";
    final static String TAG = "OAuthService";

    private Map<String, String> requestTokenResponse = new HashMap<>();
    private Map<String, String> accessTokenResponse = new HashMap<>();
    private final LoginObserver loginObserver;

    public OAuthService(LoginObserver loginObserver) {
        INSTANCE = this;
        this.loginObserver = loginObserver;
        getRequestToken();
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

    private String getRequestTokenUrl() {
        String requestTokenUrl = "https://www.flickr.com/services/oauth/request_token/";
        String baseString1 = "GET";
        String baseString2 = Utils.oauthEncode(requestTokenUrl);
        String nonce = "flickr_oauth" + String.valueOf(System.currentTimeMillis());
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        String callbackParam = "oauth_callback=" + Utils.oauthEncode("/"); // need / (or whatever) for the oauth to parse oauth_verifier
        String apiKeyParam = "oauth_consumer_key=" + API_KEY; //your apiKey from flickr
        String nonceParam = "oauth_nonce=" + nonce;
        String signatureMethodParam = "oauth_signature_method=" + "HMAC-SHA1";
        String timestampParam = "oauth_timestamp=" + timestamp;
        String versionParam = "oauth_version=" + "1.0";
        String unencBaseString3 = callbackParam + "&" + apiKeyParam + "&" + nonceParam + "&" + signatureMethodParam + "&" + timestampParam + "&" + versionParam;
        String baseString3 = Utils.oauthEncode(unencBaseString3);
        String baseString = baseString1 + "&" + baseString2 + "&" + baseString3;
        String signature = getSignature(SIGNATURE_KEY, baseString);
        String signatureParam = "oauth_signature=" + Utils.oauthEncode(signature);
        return requestTokenUrl + "?" + callbackParam + "&" + apiKeyParam + "&" +
                nonceParam + "&" + timestampParam + "&" + signatureMethodParam + "&" +
                versionParam + "&" + signatureParam;
    }

    private void getRequestToken() {
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = service.getOAuth(getRequestTokenUrl());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.d(TAG, res);
                    requestTokenResponse = Utils.separateParameters(res);
                    loginObserver.onRequestTokenReceived(OAuthService.this);
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


    public String getAuthorizationURL(){
        String url = "https://www.flickr.com/services/oauth/authorize?" +
                "oauth_token=" +
                this.requestTokenResponse.get("oauth_token");
        return url;
    }

    private String getAccessTokenUrl(String verifier) {
        Map<String, String> params = new HashMap<>();
        String requestTokenUrl = "https://www.flickr.com/services/oauth/access_token/";
        String httpMethod = "GET";
        String encodedRequestTokenUrl = Utils.oauthEncode(requestTokenUrl);

        params.put("nonce",  "flickr_oauth" + String.valueOf(System.currentTimeMillis()));
        params.put("timestamp",  String.valueOf(System.currentTimeMillis()/1000));
        params.put("consumer_key", API_KEY);// + "&" + requestTokenResponse.get("oauth_token_secret"));
        params.put("signature_method","HMAC-SHA1");
        params.put("verifier", verifier);
        params.put("token", requestTokenResponse.get("oauth_token"));
        //params.put("version", "1.0");
        String queryText = getQueryTextByParams(params);
        String encodedQueryText = Utils.oauthEncode(queryText);
        String baseString = httpMethod + "&" + encodedRequestTokenUrl + "&" + encodedQueryText;
        String signature = getSignature(SIGNATURE_KEY + requestTokenResponse.get("oauth_token_secret"), baseString);

        params.put("signature", Utils.oauthEncode(signature));

        return requestTokenUrl + "?" + getQueryTextByParams(params);
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


    public void getAccessToken(String verifier) {
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = service.getOAuth(getAccessTokenUrl(verifier));

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



    public Map<String, String> getRequestTokenResponse() {
        return requestTokenResponse;
    }

    public Map<String, String> getAccessTokenResponse() {
        return accessTokenResponse;
    }
}
