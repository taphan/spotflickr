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

        String nonceParam = getNonceParam();
        String callbackParam = getCallbackParam();
        String apiKeyParam = getApiKeyParam();
        String signatureMethodParam = getSignatureMethodParam();
        String timestampParam = getTimeStampParam();
        String versionParam = getVersionParam();

        String baseString1 = "GET";
        String baseString2 = oauthEncode(requestTokenUrl);
        String unencBaseString3 = callbackParam + "&" + apiKeyParam + "&" + nonceParam + "&" + signatureMethodParam + "&" + timestampParam + "&" + versionParam;

        String baseString3 = oauthEncode(unencBaseString3);
        String baseString = baseString1 + "&" + baseString2 + "&" + baseString3;

        String signature = getSignature(SIGNATURE_KEY, baseString);
        String signatureParam = "oauth_signature=" + oauthEncode(signature);
        return requestTokenUrl + "?" + callbackParam + "&" + apiKeyParam + "&" +
                nonceParam + "&" + timestampParam + "&" + signatureMethodParam + "&" +
                versionParam + "&" + signatureParam;
    }

    private String getAccessTokenUrl() {
        String requestTokenUrl = "https://www.flickr.com/services/oauth/access_token/";

        String nonceParam = getNonceParam();
        String timestampParam = getTimeStampParam();
        String verifierParam = getVerifierParam();
        String tokenParam = getTokenParam();
        String apiKeyParam = getApiKeyParam();
        String signatureMethodParam = getSignatureMethodParam();
        String versionParam = getVersionParam();

        String baseString1 = "GET";
        String baseString2 = oauthEncode(requestTokenUrl);
        String unencBaseString3 = tokenParam + "&" + verifierParam + "&" + apiKeyParam + "&" + nonceParam + "&" + signatureMethodParam + "&" + timestampParam + "&" + versionParam;

        String baseString3 = oauthEncode(unencBaseString3);
        String baseString = baseString1 + "&" + baseString2 + "&" + baseString3;

        String signature = getSignature(SIGNATURE_KEY, baseString);
        String signatureParam = "oauth_signature=" + oauthEncode(signature);
        return requestTokenUrl + "?" + tokenParam + "&" + verifierParam  + "&" + apiKeyParam + "&" +
                nonceParam + "&" + timestampParam + "&" + signatureMethodParam + "&" +
                versionParam + "&" + signatureParam;
    }

    private String getVerifierParam() {
        return "oauth_verifier=";
    }

    // TODO: Update when stage 2 is completed
    private String getTokenParam() {
        return "oauth_token=" + requestTokenResponse.get("oauth_token");
    }

    private String getNonceParam() {
        String nonce = "flickr_oauth" + String.valueOf(System.currentTimeMillis());
        return "oauth_nonce=" + nonce;
    }

    private String getCallbackParam() {
        return "oauth_callback=";
    }

    private String getApiKeyParam() {
        return "oauth_consumer_key=" + API_KEY;
    }

    private String getSignatureMethodParam() {
        return "oauth_signature_method=" + "HMAC-SHA1";
    }

    private String getVersionParam() {
        return "oauth_version=" + "1.0";
    }

    private String getTimeStampParam() {
        String timestamp = String.valueOf(System.currentTimeMillis()/1000);
        return "oauth_timestamp=" + timestamp;
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
