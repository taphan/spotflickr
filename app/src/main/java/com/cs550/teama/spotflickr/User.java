package com.cs550.teama.spotflickr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    private String username;
    private String email;
    private String password;
    private List<String> hotspot_id_list;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String password, List<String> hotspot_id_list) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.hotspot_id_list = hotspot_id_list;
    }

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public List<String> getHotspot_id_list() {
        return hotspot_id_list;
    }
}
