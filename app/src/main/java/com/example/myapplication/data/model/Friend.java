package com.example.myapplication.data.model;

public class Friend {
    private String userId;
    private String username;
    private String avatar;
    private long addedTime;

    public Friend() {}

    public Friend(String userId, String username, String avatar, long addedTime) {
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.addedTime = addedTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }
}
