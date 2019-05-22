package com.cs550.teama.spotflickr.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.cs550.teama.spotflickr.network.RetrofitInstance;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OAuthService {
    final static String API_KEY = "c78af6829b82ef76418e7563ee33fe85";
    final static String SIGNATURE_KEY = "76a0afc91403a9a7&"; // APP_SECRET
    final static String TAG = "OAuthService";
    private Map<String, String> requestTokenResponse = new HashMap<>();

    public OAuthService() {
        getRequestToken();
    }

    private String oauthEncode(String input) {
        Map<String, String> oathEncodeMap = new HashMap<>();
        oathEncodeMap.put("\\*", "%2A");
        oathEncodeMap.put("\\+", "%20");
        oathEncodeMap.put("%7E", "~");
        String encoded = "";
        try {
            encoded = URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, String> entry : oathEncodeMap.entrySet()) {
            encoded = encoded.replaceAll(entry.getKey(), entry.getValue());
        }
        return encoded;
    }

    @SuppressLint("NewApi")
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
        return Base64.getEncoder().encodeToString(signedBytes);
    }

    private String getRequestTokenUrl() {
        String requestTokenUrl = "https://www.flickr.com/services/oauth/request_token/";
        String baseString1 = "GET";
        String baseString2 = oauthEncode(requestTokenUrl);
        String nonce = "flickr_oauth" + String.valueOf(System.currentTimeMillis());
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        String callbackParam = "oauth_callback=";
        String apiKeyParam = "oauth_consumer_key=" + API_KEY; //your apiKey from flickr
        String nonceParam = "oauth_nonce=" + nonce;
        String signatureMethodParam = "oauth_signature_method=" + "HMAC-SHA1";
        String timestampParam = "oauth_timestamp=" + timestamp;
        String versionParam = "oauth_version=" + "1.0";
        String unencBaseString3 = callbackParam + "&" + apiKeyParam + "&" + nonceParam + "&" + signatureMethodParam + "&" + timestampParam + "&" + versionParam;
        String baseString3 = oauthEncode(unencBaseString3);
        String baseString = baseString1 + "&" + baseString2 + "&" + baseString3;
        String signature = getSignature(SIGNATURE_KEY, baseString);
        String signatureParam = "oauth_signature=" + oauthEncode(signature);
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
                    String[] resArray = res.split("&");
                    for (int i=0; i < resArray.length; i++) {
                        String currentResponse = resArray[i];
                        String[] keyValue = currentResponse.split("=");
                        requestTokenResponse.put(keyValue[0], keyValue[1]);
                    }
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
}
