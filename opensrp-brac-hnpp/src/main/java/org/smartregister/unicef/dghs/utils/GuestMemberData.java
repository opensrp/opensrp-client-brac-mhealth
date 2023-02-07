package org.smartregister.unicef.dghs.utils;

public class GuestMemberData {
    private String name;
    private String baseEntityId;
    private String memberId;
    private String dob;
    private String gender;
    private String village;
    private String villageId;
    private long lastSubmissionDate;
    private String phoneNo;
    private String ssName;

    public void setVillageId(String villageId) {
        this.villageId = villageId;
    }

    public String getVillageId() {
        return villageId;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getVillage() {
        return village;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setSsName(String ssName) {
        this.ssName = ssName;
    }

    public String getSsName() {
        return ssName;
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
