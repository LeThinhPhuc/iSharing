package com.example.ggm;

public class Friend {
    public String id;
    public double latitude;
    public double longitude;

    // Constructor mặc định cần thiết cho Firebase
    public Friend() {
    }

    // Constructor đầy đủ
    public Friend(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
