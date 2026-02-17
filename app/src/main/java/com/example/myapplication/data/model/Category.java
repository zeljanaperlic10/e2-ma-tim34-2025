package com.example.myapplication.data.model;

public class Category {
    private String firestoreId;
    private String name;
    private int color;
    private String userId;

    public Category() {}

    public Category(String name, int color, String userId) {
        this.name = name;
        this.color = color;
        this.userId = userId;
    }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
