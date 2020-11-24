package org.smartregister.brac.hnpp.utils;

public class GuestMemberData {
    private String name;
    private String baseEntityId;
    private String dob;
    private String gender;
    private long lastSubmissionDate;

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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getLastSubmissionDate() {
        return lastSubmissionDate;
    }

    public void setLastSubmissionDate(long lastSubmissionDate) {
        this.lastSubmissionDate = lastSubmissionDate;
    }
}
