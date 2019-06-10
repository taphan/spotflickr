package com.cs550.teama.spotflickr.model;

import java.util.List;

public class User {

    private String fullname;
    private String username;
    private String user_nsid;
    private String email;
    private String password;
    private String oauth_token;
    private String oauth_token_secret;
    private List<String> hotspot_id_list;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullname, String username, String oauth_token, String oauth_token_secret,
                String user_nsid, String email, String password, List<String> hotspot_list) {
        this.fullname = fullname;
        this.username = username;
        this.user_nsid = user_nsid;
        this.oauth_token = oauth_token;
        this.oauth_token_secret = oauth_token_secret;
        this.email = email;
        this.password = password;
        this.hotspot_id_list = hotspot_list;
    }

    public String getFullname() {
        return fullname;
    }
    public String getUser_nsid() {
        return user_nsid;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getOauth_token() {
        return oauth_token;
    }
    public String getOauth_token_secret() {
        return oauth_token_secret;
    }
    public List<String> getHotspot_id_list() {
        return hotspot_id_list;
    }
    public void updatePassword(String password) {
        this.password = password;
    }
    public void deleteHotspotList(String listID) {hotspot_id_list.remove(listID);}
    public void addHotspotList(String listID) {
        hotspot_id_list.add(listID);
    }
    public int getHotspotIdListSize() {
        return hotspot_id_list.size();
    }

}
