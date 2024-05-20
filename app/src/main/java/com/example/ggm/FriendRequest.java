package com.example.ggm;

public class FriendRequest {
    public String senderId;
    public String receiverId;
    public String status;
    public String key; // Thêm trường key để lưu key của nút Firebase

    public FriendRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
    }

    public FriendRequest(String senderId, String receiverId, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
