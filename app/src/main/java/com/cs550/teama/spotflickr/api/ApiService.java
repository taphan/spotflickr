package com.cs550.teama.spotflickr.api;

import com.cs550.teama.spotflickr.model.Photos;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ApiService {
    @GET(".")
    Call<Photos> getRecentPhotos(@QueryMap Map<String, String> options);
}
