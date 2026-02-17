package com.example.myapplication.data.model;

public class Task {
    private String firestoreId;
    private String name;
    private String description;
    private String category;
    private int categoryColor;
    private FrequencyType frequencyType;
    private Integer repeatInterval;
    private RepeatUnit repeatUnit;
    private Long startDate;
    private Long endDate;
    private int difficultyXP;
    private int importanceXP;
    private int totalXP;
    private String userId;
    private Long completedDate;
    private Long createdTimestamp;
    private String status; // "ACTIVE", "DONE", "UNDONE", "PAUSED", "CANCELLED"

    public Task() {
        this.status = "ACTIVE";
    }

    public Task(String name, String description, String category, int categoryColor,
                FrequencyType frequencyType, Integer repeatInterval, RepeatUnit repeatUnit,
                Long startDate, Long endDate, int difficultyXP, int importanceXP,
                String userId, Long createdTimestamp) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.categoryColor = categoryColor;
        this.frequencyType = frequencyType;
        this.repeatInterval = repeatInterval;
        this.repeatUnit = repeatUnit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.difficultyXP = difficultyXP;
        this.importanceXP = importanceXP;
        this.userId = userId;
        this.createdTimestamp = createdTimestamp;
        this.status = "ACTIVE";
        updateTotalXP();
    }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCategoryColor() { return categoryColor; }
    public void setCategoryColor(int categoryColor) { this.categoryColor = categoryColor; }

    public FrequencyType getFrequencyType() { return frequencyType; }
    public void setFrequencyType(FrequencyType frequencyType) { this.frequencyType = frequencyType; }

    public Integer getRepeatInterval() { return repeatInterval; }
    public void setRepeatInterval(Integer repeatInterval) { this.repeatInterval = repeatInterval; }

    public RepeatUnit getRepeatUnit() { return repeatUnit; }
    public void setRepeatUnit(RepeatUnit repeatUnit) { this.repeatUnit = repeatUnit; }

    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }

    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }

    public int getDifficultyXP() { return difficultyXP; }
    public void setDifficultyXP(int difficultyXP) { this.difficultyXP = difficultyXP; updateTotalXP(); }

    public int getImportanceXP() { return importanceXP; }
    public void setImportanceXP(int importanceXP) { this.importanceXP = importanceXP; updateTotalXP(); }

    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getCompletedDate() { return completedDate; }
    public void setCompletedDate(Long completedDate) { this.completedDate = completedDate; }

    public Long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(Long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    private void updateTotalXP() {
        this.totalXP = this.difficultyXP + this.importanceXP;
    }

    public void setDifficultyByLabel(String level) {
        switch (level) {
            case "Veoma lak":       this.difficultyXP = 1;  break;
            case "Lak":             this.difficultyXP = 3;  break;
            case "Težak":           this.difficultyXP = 7;  break;
            case "Ekstremno težak": this.difficultyXP = 20; break;
        }
        updateTotalXP();
    }

    public void setImportanceByLabel(String level) {
        switch (level) {
            case "Normalan":        this.importanceXP = 1;   break;
            case "Važan":           this.importanceXP = 3;   break;
            case "Ekstremno važan": this.importanceXP = 10;  break;
            case "Specijalan":      this.importanceXP = 100; break;
        }
        updateTotalXP();
    }

    public String getDifficultyLabel() {
        switch (difficultyXP) {
            case 1:  return "Veoma lak";
            case 3:  return "Lak";
            case 7:  return "Težak";
            case 20: return "Ekstremno težak";
            default: return "Nepoznato";
        }
    }

    public String getImportanceLabel() {
        switch (importanceXP) {
            case 1:   return "Normalan";
            case 3:   return "Važan";
            case 10:  return "Ekstremno važan";
            case 100: return "Specijalan";
            default:  return "Nepoznato";
        }
    }

    public boolean canBeMarked() {
        if (!"ACTIVE".equals(status)) return false;
        if (startDate == null) return false;
        long threeDaysMs = 3L * 24 * 60 * 60 * 1000;
        return System.currentTimeMillis() - startDate <= threeDaysMs;
    }

    public boolean canBeEdited() {
        return "ACTIVE".equals(status) || "PAUSED".equals(status);
    }

    public enum FrequencyType { ONE_TIME, REPEATING }
    public enum RepeatUnit { DAY, WEEK }
}



