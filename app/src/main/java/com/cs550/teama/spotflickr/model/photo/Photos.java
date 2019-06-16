package com.cs550.teama.spotflickr.model.photo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Photos {

    @SerializedName("photos")
    @Expose
    private PhotosDetails photos;
    @SerializedName("stat")
    @Expose
    private String stat;

    public PhotosDetails getPhotos() {
        return photos;
    }

    public void setPhotos(PhotosDetails photos) {
        this.photos = photos;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

}