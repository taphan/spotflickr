package com.cs550.teama.spotflickr.model.photo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoInfo {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("secret")
    @Expose
    private String secret;
    @SerializedName("server")
    @Expose
    private String server;
    @SerializedName("farm")
    @Expose
    private Integer farm;
    @SerializedName("dateuploaded")
    @Expose
    private String dateuploaded;
    @SerializedName("isfavorite")
    @Expose
    private Integer isfavorite;
    @SerializedName("license")
    @Expose
    private Integer license;
    @SerializedName("safety_level")
    @Expose
    private Integer safetyLevel;
    @SerializedName("rotation")
    @Expose
    private Integer rotation;
    @SerializedName("originalsecret")
    @Expose
    private String originalsecret;
    @SerializedName("originalformat")
    @Expose
    private String originalformat;
    @SerializedName("owner")
    @Expose
    private Owner owner;
    @SerializedName("title")
    @Expose
    private Title title;
    @SerializedName("description")
    @Expose
    private Description description;
    @SerializedName("dates")
    @Expose
    private Dates dates;
    @SerializedName("views")
    @Expose
    private Integer views;
    @SerializedName("people")
    @Expose
    private People people;
    @SerializedName("tags")
    @Expose
    private Tags tags;
    @SerializedName("urls")
    @Expose
    private Urls urls;
    @SerializedName("media")
    @Expose
    private String media;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getFarm() {
        return farm;
    }

    public void setFarm(Integer farm) {
        this.farm = farm;
    }

    public String getDateuploaded() {
        return dateuploaded;
    }

    public void setDateuploaded(String dateuploaded) {
        this.dateuploaded = dateuploaded;
    }

    public Integer getIsfavorite() {
        return isfavorite;
    }

    public void setIsfavorite(Integer isfavorite) {
        this.isfavorite = isfavorite;
    }

    public Integer getLicense() {
        return license;
    }

    public void setLicense(Integer license) {
        this.license = license;
    }

    public Integer getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(Integer safetyLevel) {
        this.safetyLevel = safetyLevel;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public String getOriginalsecret() {
        return originalsecret;
    }

    public void setOriginalsecret(String originalsecret) {
        this.originalsecret = originalsecret;
    }

    public String getOriginalformat() {
        return originalformat;
    }

    public void setOriginalformat(String originalformat) {
        this.originalformat = originalformat;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Dates getDates() {
        return dates;
    }

    public void setDates(Dates dates) {
        this.dates = dates;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public People getPeople() {
        return people;
    }

    public void setPeople(People people) {
        this.people = people;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public Urls getUrls() {
        return urls;
    }

    public void setUrls(Urls urls) {
        this.urls = urls;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

}
