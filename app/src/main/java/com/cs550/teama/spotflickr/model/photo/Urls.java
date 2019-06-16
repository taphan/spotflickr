package com.cs550.teama.spotflickr.model.photo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Urls {

    @SerializedName("url")
    @Expose
    private List<Url> url = null;

    public List<Url> getUrl() {
        return url;
    }

    public void setUrl(List<Url> url) {
        this.url = url;
    }

}
