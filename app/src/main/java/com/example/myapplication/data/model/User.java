package com.example.myapplication.data.model;

public class User {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private boolean activated;
    private long registrationTime;

    // Napredovanje
    private int level;
    private int xp;
    private int requiredXp;
    private int coins;
    private int pp;
    private String title;
    private long lastLevelUpTimestamp; // Početak trenutne etape

    public User() {
        this.level = 0;
        this.xp = 0;
        this.requiredXp = 200;
        this.coins = 0;
        this.pp = 40;
        this.title = "Početnik";
        this.lastLevelUpTimestamp = System.currentTimeMillis();
    }

    public User(String id, String username, String email, String avatar, boolean activated, long registrationTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.activated = activated;
        this.registrationTime = registrationTime;
        this.level = 0;
        this.xp = 0;
        this.requiredXp = 200;
        this.coins = 0;
        this.pp = 40;
        this.title = "Početnik";
        this.lastLevelUpTimestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public long getRegistrationTime() { return registrationTime; }
    public void setRegistrationTime(long registrationTime) { this.registrationTime = registrationTime; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getRequiredXp() { return requiredXp; }
    public void setRequiredXp(int requiredXp) { this.requiredXp = requiredXp; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getPp() { return pp; }
    public void setPp(int pp) { this.pp = pp; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getLastLevelUpTimestamp() { return lastLevelUpTimestamp; }
    public void setLastLevelUpTimestamp(long lastLevelUpTimestamp) { this.lastLevelUpTimestamp = lastLevelUpTimestamp; }
}