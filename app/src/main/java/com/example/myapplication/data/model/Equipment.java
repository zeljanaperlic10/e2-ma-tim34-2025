package com.example.myapplication.data.model;

public class Equipment {
    private String firestoreId;
    private String type; // "CLOTHING", "WEAPON"
    private String name;
    private int bonusPp;
    private int bonusSuccessChance;
    private int bonusAttacks;
    private int remainingBattles;
    private String userId;

    public Equipment() {}

    public Equipment(String type, String name, int bonusPp, int bonusSuccessChance,
                     int bonusAttacks, int remainingBattles, String userId) {
        this.type = type;
        this.name = name;
        this.bonusPp = bonusPp;
        this.bonusSuccessChance = bonusSuccessChance;
        this.bonusAttacks = bonusAttacks;
        this.remainingBattles = remainingBattles;
        this.userId = userId;
    }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBonusPp() { return bonusPp; }
    public void setBonusPp(int bonusPp) { this.bonusPp = bonusPp; }

    public int getBonusSuccessChance() { return bonusSuccessChance; }
    public void setBonusSuccessChance(int bonusSuccessChance) { this.bonusSuccessChance = bonusSuccessChance; }

    public int getBonusAttacks() { return bonusAttacks; }
    public void setBonusAttacks(int bonusAttacks) { this.bonusAttacks = bonusAttacks; }

    public int getRemainingBattles() { return remainingBattles; }
    public void setRemainingBattles(int remainingBattles) { this.remainingBattles = remainingBattles; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public static Equipment createRandomClothing(String userId) {
        String[] names = {"Rukavice", "Štit", "Čizme"};
        int randomIndex = (int) (Math.random() * names.length);
        String name = names[randomIndex];

        switch (name) {
            case "Rukavice":
                return new Equipment("CLOTHING", "Rukavice", 0, 0, 0, 2, userId);
            case "Štit":
                return new Equipment("CLOTHING", "Štit", 0, 10, 0, 2, userId);
            case "Čizme":
                return new Equipment("CLOTHING", "Čizme", 0, 0, 0, 2, userId);
            default:
                return null;
        }
    }

    public static Equipment createRandomWeapon(String userId) {
        String[] names = {"Mač", "Luk i strela"};
        int randomIndex = (int) (Math.random() * names.length);
        return new Equipment("WEAPON", names[randomIndex], 0, 0, 0, -1, userId);
    }
}