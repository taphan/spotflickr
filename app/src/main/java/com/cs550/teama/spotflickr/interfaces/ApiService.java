package com.cs550.teama.spotflickr.interfaces;

import com.cs550.teama.spotflickr.model.Photos;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface ApiService {
    @GET(".")
    Call<Photos> getRecentPhotos(@QueryMap Map<String, String> options);
    @GET
    Call<ResponseBody> getPhotoFromUrl(@Url String url);
    @GET
    Call<ResponseBody> getOAuth(@Url String url);
    @GET("/request_token")
    Call<ResponseBody> getOAuthRequestToken(@QueryMap Map<String, String> options);
    @GET("/access_token")
    Call<ResponseBody> getOAuthAccessToken(@QueryMap Map<String, String> options);
}
