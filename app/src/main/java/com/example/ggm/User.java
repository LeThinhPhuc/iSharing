package com.example.ggm;

import java.util.Map;

public class User {
    public String id;
    public double latitude;
    public double longitude;
    public String password;
    public String email;
    public Map<String, Friend> friends;

    // Constructor mặc định cần thiết cho Firebase
    public User() {
    }

    // Constructor đầy đủ
    // Constructor đầy đủ
    public User(String id, double latitude, double longitude, String pass, Map<String, Friend> friends) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.password = pass; // Cập nhật trường pass
        this.friends = friends;
    }


    // Getter và Setter (nếu cần)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Map<String, Friend> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Friend> friends) {
        this.friends = friends;
    }

    public void setPass(String pass) {
        this.password = pass;
    }

    public String getPass() {
        return password;
    }
}
