package com.cs550.teama.spotflickr.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;

public class HotspotList implements Serializable {
    @Exclude private String listId;
    private String name;
    private String description;
    private String user_id;

    public HotspotList() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public HotspotList(String name, String description, String user_id) {
        this.name = name;
        this.description = description;
        this.user_id = user_id;
    }


    public void setListId(String listId) {
        this.listId = listId;
    }
    public String getListId() { return listId; }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getUserID() {
        return user_id;
    }
}
