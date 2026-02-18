package com.example.myapplication.data.model;

public class Boss {
    private String firestoreId;
    private int level;
    private int hp;
    private int maxHp;
    private boolean defeated;
    private String userId;

    public Boss() {}

    public Boss(int level, int maxHp, String userId) {
        this.level = level;
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.defeated = false;
        this.userId = userId;
    }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean defeated) { this.defeated = defeated; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public static int calculateHpForLevel(int level) {
        if (level == 1) return 200;
        int prevHp = calculateHpForLevel(level - 1);
        return prevHp * 2 + prevHp / 2;
    }
}
