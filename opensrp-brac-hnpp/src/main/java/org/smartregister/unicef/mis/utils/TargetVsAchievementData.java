package org.smartregister.unicef.mis.utils;

public class TargetVsAchievementData extends DashBoardData {

    private int targetId;
    private int targetCount;
    private String targetName;
    private String year;
    private String month;
    private String day;
    private String startDate;
    private String endDate;
    private int achievementCount;
    private int achievementPercentage;
    private long timestamp;
    private int avgTargetCount;
    private int avgAchievmentCount;
    private int avgAchievementPercentage;
    private int isMonthData;

    public TargetVsAchievementData(){

    }
    public TargetVsAchievementData(String eventType, String title){
        super(eventType, title);
    }
    public int getAchievementPercentage() {
        return achievementPercentage;
    }

    public void setAchievementPercentage(int achievementPercentage) {
        this.achievementPercentage = achievementPercentage;
    }

    public int getAvgAchievementPercentage() {
        return avgAchievementPercentage;
    }

    public void setAvgAchievementPercentage(int avgAchievementPercentage) {
        this.avgAchievementPercentage = avgAchievementPercentage;
    }

    public int getAvgTargetCount() {
        return avgTargetCount;
    }

    public void setAvgTargetCount(int avgTargetCount) {
        this.avgTargetCount = avgTargetCount;
    }

    public int getAvgAchievmentCount() {
        return avgAchievmentCount;
    }

    public void setAvgAchievmentCount(int avgAchievmentCount) {
        this.avgAchievmentCount = avgAchievmentCount;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAchievementCount() {
        return achievementCount;
    }

    public void setAchievementCount(int achievementCount) {
        this.achievementCount = achievementCount;
    }



    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setIsMonthData(int isMonthData) {
        this.isMonthData = isMonthData;
    }

    public int getIsMonthData() {
        return isMonthData;
    }
}
