package com.example.myapplication.data.model;

public class AllianceInvitation {
    private String id;
    private String allianceId;
    private String allianceName;
    private String fromUserId;
    private String fromUsername;
    private String toUserId;
    private long timestamp;
    private String status; // "pending", "accepted", "declined"

    public AllianceInvitation() {}

    public AllianceInvitation(String id, String allianceId, String allianceName,
                              String fromUserId, String fromUsername, String toUserId) {
        this.id = id;
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.fromUserId = fromUserId;
        this.fromUsername = fromUsername;
        this.toUserId = toUserId;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }

    public String getAllianceName() {
        return allianceName;
    }

    public void setAllianceName(String allianceName) {
        this.allianceName = allianceName;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
