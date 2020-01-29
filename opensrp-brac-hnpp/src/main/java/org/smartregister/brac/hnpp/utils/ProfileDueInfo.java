package org.smartregister.brac.hnpp.utils;

public class ProfileDueInfo {
    private String name;
    private String baseEntityId;
    private String age;
    private String eventType;
    private String gender;
    private String maritalStatus;
    private String dob;
    private String originalEventType;

    public String getOriginalEventType() {
        return originalEventType;
    }

    public void setOriginalEventType(String originalEventType) {
        this.originalEventType = originalEventType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}
