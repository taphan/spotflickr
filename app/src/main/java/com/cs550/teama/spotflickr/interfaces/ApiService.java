package com.cs550.teama.spotflickr.interfaces;

import com.cs550.teama.spotflickr.model.photo.PhotoDetail;
import com.cs550.teama.spotflickr.model.photo.Photos;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface ApiService {
    @GET(".")
    Call<PhotoDetail> getPhotoDetails(@QueryMap Map<String, String> options);

    @GET()
    Call<Photos> getPhotosForLocation(@Url String url);

    @GET
    Call<ResponseBody> getOAuth(@Url String url);


}
