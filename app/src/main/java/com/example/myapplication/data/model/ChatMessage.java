package com.example.myapplication.data.model;

public class ChatMessage {
    private String id;
    private String allianceId;
    private String senderId;
    private String senderUsername;
    private String message;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String id, String allianceId, String senderId,
                       String senderUsername, String message) {
        this.id = id;
        this.allianceId = allianceId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
