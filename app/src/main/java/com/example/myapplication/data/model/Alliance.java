package com.example.myapplication.data.model;

import java.util.ArrayList;
import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private String leaderId;
    private String leaderUsername;
    private List<String> memberIds;
    private List<String> memberUsernames;
    private long createdTime;
    private boolean missionActive;
    private long missionEndTime;

    public Alliance() {
        this.memberIds = new ArrayList<>();
        this.memberUsernames = new ArrayList<>();
    }

    public Alliance(String id, String name, String leaderId, String leaderUsername) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
        this.leaderUsername = leaderUsername;
        this.memberIds = new ArrayList<>();
        this.memberUsernames = new ArrayList<>();
        this.memberIds.add(leaderId);
        this.memberUsernames.add(leaderUsername);
        this.createdTime = System.currentTimeMillis();
        this.missionActive = false;
        this.missionEndTime = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderUsername() {
        return leaderUsername;
    }

    public void setLeaderUsername(String leaderUsername) {
        this.leaderUsername = leaderUsername;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getMemberUsernames() {
        return memberUsernames;
    }

    public void setMemberUsernames(List<String> memberUsernames) {
        this.memberUsernames = memberUsernames;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isMissionActive() {
        return missionActive;
    }

    public void setMissionActive(boolean missionActive) {
        this.missionActive = missionActive;
    }

    public long getMissionEndTime() {
        return missionEndTime;
    }

    public void setMissionEndTime(long missionEndTime) {
        this.missionEndTime = missionEndTime;
    }
}
