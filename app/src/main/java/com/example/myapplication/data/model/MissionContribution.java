package com.example.myapplication.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Prati doprinos svakog korisnika u specijalnoj misiji saveza.
 *
 * Specijalni zadaci:
 * 1. Kupovina u prodavnici (max 5) → 2 HP
 * 2. Uspešan udarac u regularnoj borbi (max 10) → 2 HP
 * 3. Rešavanje lakih/normalnih/važnih zadataka (HP max 10) → 1 HP; lak+normalan = 2HP
 * 4. Rešavanje ostalih zadataka (max 6) → 4 HP
 * 5. Bez neurađenih zadataka tokom trajanja → 10 HP (na kraju)
 * 6. Poruka u savezu po danu → 4 HP po danu
 */
public class MissionContribution {

    private String userId;
    private String username;
    private String allianceId;

    // Brojači
    private int storePurchases;        // max 5, svaki daje 2 HP
    private int successfulHits;        // max 10 (HP-wise), svaki daje 2 HP
    private int easyTasksHpContrib;    // HP doprinos od lakih/norm/važnih zadataka (max 10 HP)
    private int otherTasksCount;       // max 6, svaki daje 4 HP
    private List<String> messageDays;  // "yyyy-MM-dd" dani u kojima su slane poruke (svaki = 4 HP)
    private boolean hasUndoneTask;     // da li ima neurađen zadatak tokom misije

    private int totalHpContributed;    // ukupan HP koji je smanjio bosovom HP-u

    public MissionContribution() {
        messageDays = new ArrayList<>();
        hasUndoneTask = false;
    }

    public MissionContribution(String userId, String username, String allianceId) {
        this.userId = userId;
        this.username = username;
        this.allianceId = allianceId;
        this.messageDays = new ArrayList<>();
        this.hasUndoneTask = false;
    }

    // ===== Getters & Setters =====

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public int getStorePurchases() { return storePurchases; }
    public void setStorePurchases(int storePurchases) { this.storePurchases = storePurchases; }

    public int getSuccessfulHits() { return successfulHits; }
    public void setSuccessfulHits(int successfulHits) { this.successfulHits = successfulHits; }

    public int getEasyTasksHpContrib() { return easyTasksHpContrib; }
    public void setEasyTasksHpContrib(int easyTasksHpContrib) { this.easyTasksHpContrib = easyTasksHpContrib; }

    public int getOtherTasksCount() { return otherTasksCount; }
    public void setOtherTasksCount(int otherTasksCount) { this.otherTasksCount = otherTasksCount; }

    public List<String> getMessageDays() { return messageDays; }
    public void setMessageDays(List<String> messageDays) { this.messageDays = messageDays; }

    public boolean isHasUndoneTask() { return hasUndoneTask; }
    public void setHasUndoneTask(boolean hasUndoneTask) { this.hasUndoneTask = hasUndoneTask; }

    public int getTotalHpContributed() { return totalHpContributed; }
    public void setTotalHpContributed(int totalHpContributed) { this.totalHpContributed = totalHpContributed; }

    // ===== Računanje ukupnog HP doprinosa (bez "bez neurađenih" — to se dodaje na kraju) =====
    public int calculateCurrentHp() {
        int hp = 0;
        hp += Math.min(storePurchases, 5) * 2;
        // successfulHits: max 10 HP (5 udarca po 2 HP), ali spec kaže max 10 HP iz ove kategorije
        hp += Math.min(successfulHits * 2, 20); // max 10 udarca × 2 = 20 HP, ali spec ima cap na 20
        hp += Math.min(easyTasksHpContrib, 10);
        hp += Math.min(otherTasksCount, 6) * 4;
        hp += messageDays.size() * 4;
        return hp;
    }
}