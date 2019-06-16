package com.cs550.teama.spotflickr.model.hotspot;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;

public class Hotspot implements Serializable {
    @Exclude private String Id;
    private String name;
    private float latitude;
    private float longitude;
    private String description;
    private String list_id;

    public Hotspot() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Hotspot(String name, float latitude, float longitude, String description, String list_id) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.list_id = list_id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }
    public String getId() { return Id; }
    public String getName() {
        return name;
    }
    public float getLatitude() { return latitude; }
    public float getLongitude() { return longitude; }
    public String getDescription() {
        return description;
    }
    public String getList_id() { return list_id; }
}
