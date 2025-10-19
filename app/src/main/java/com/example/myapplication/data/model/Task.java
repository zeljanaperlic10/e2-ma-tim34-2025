package com.example.myapplication.data.model;

public class Task {
    private int id;
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
    private String userId;          // ID korisnika koji je kreirao zadatak
    private Long completedDate;     // vreme kada je zadatak završen / XP dodeljen
    private Long createdTimestamp;  // vreme kada je zadatak kreiran

    public Task() { }

    public Task(String name, String description, String category, int categoryColor,
                FrequencyType frequencyType, Integer repeatInterval, RepeatUnit repeatUnit,
                Long startDate, Long endDate, int difficultyXP, int importanceXP,
                String userId, Long completedDate, Long createdTimestamp) {
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
        this.completedDate = completedDate;
        this.createdTimestamp = createdTimestamp;
        updateTotalXP();
    }

    // Getteri i setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
    public void setDifficultyXP(int difficultyXP) {
        this.difficultyXP = difficultyXP;
        updateTotalXP();
    }

    public int getImportanceXP() { return importanceXP; }
    public void setImportanceXP(int importanceXP) {
        this.importanceXP = importanceXP;
        updateTotalXP();
    }

    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getCompletedDate() { return completedDate; }
    public void setCompletedDate(Long completedDate) { this.completedDate = completedDate; }

    public Long getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(Long createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    // Helper metode za XP
    public void setDifficultyByLevel(String level) {
        switch(level) {
            case "Veoma lak": this.difficultyXP = 1; break;
            case "Lak": this.difficultyXP = 3; break;
            case "Težak": this.difficultyXP = 7; break;
            case "Ekstremno težak": this.difficultyXP = 20; break;
        }
        updateTotalXP();
    }

    public void setImportanceByLevel(String level) {
        switch(level) {
            case "Normalan": this.importanceXP = 1; break;
            case "Važan": this.importanceXP = 3; break;
            case "Ekstremno važan": this.importanceXP = 10; break;
            case "Specijalan": this.importanceXP = 100; break;
        }
        updateTotalXP();
    }

    private void updateTotalXP() {
        this.totalXP = this.difficultyXP + this.importanceXP;
    }

    // Enumi
    public enum FrequencyType { ONE_TIME, REPEATING }
    public enum RepeatUnit { DAY, WEEK }
}



