package org.smartregister.unicef.dghs.utils;

public class GuestMemberData {
    private String name;
    private String baseEntityId;
    private String memberId;
    private String dob;
    private String gender;
    private String division;
    private String district;
    private String upozila;
    private long lastSubmissionDate;
    private String phoneNo;
    private String shrId;

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getUpozila() {
        return upozila;
    }

    public void setUpozila(String upozila) {
        this.upozila = upozila;
    }

    public String getShrId() {
        return shrId;
    }

    public void setShrId(String shrId) {
        this.shrId = shrId;
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



    public String getMemberId() {
        return memberId;
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
