package com.cs550.teama.spotflickr.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HotspotList implements Serializable {
    @Exclude private String listId;
    private String name;
    private String description;
    private String user_id;
    private List<String> hotspot_id;

    public HotspotList() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public HotspotList(String name, String description, String user_id, List<String> hotspot_id) {
        this.name = name;
        this.description = description;
        this.user_id = user_id;
        this.hotspot_id = hotspot_id;
    }


    public void setUser_id(String userId) { this.user_id = userId; }
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
    public String getUser_id() {
        return user_id;
    }
    public List<String> getHotspot_id() { return hotspot_id; }
    public void deleteHotspot(String listID) {hotspot_id.remove(listID);}
    public void addHotspot(String ID) {
        if (hotspot_id == null)
            hotspot_id = new ArrayList<String>();
        hotspot_id.add(ID);
    }
    public int getHotspotIdSize() {
        return hotspot_id.size();
    }
}
