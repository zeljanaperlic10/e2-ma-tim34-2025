package com.example.myapplication.data.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private boolean activated;
    private long registrationTime;

    public User() {} // Firebase mora imati prazan konstruktor

    public User(String id, String username, String email, String avatar, boolean activated, long registrationTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.activated = activated;
        this.registrationTime = registrationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public long getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(long registrationTime) {
        this.registrationTime = registrationTime;
    }
}
