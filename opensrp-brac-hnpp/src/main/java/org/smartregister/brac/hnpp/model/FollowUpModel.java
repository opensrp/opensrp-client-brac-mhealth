package org.smartregister.brac.hnpp.model;

public class FollowUpModel {
    public String baseEntityId;
    public String followUpDate;
    public String nextFollowUpDate;
    public String visitDate;
    public String telephonyFollowUpDate;
    public String specialFollowUpDate;
    public int noOfAnc;
    public String memberName;
    public String memberMobileNo;
    public String dueDate;
    public int ancType;

    public FollowUpModel(String baseEntityId, String followUpDate, String nextFollowUpDate, String visitDate, String telephonyFollowUpDate, String specialFollowUpDate, int noOfAnc, String memberName, String memberMobileNo, String dueDate, int ancType) {
        this.baseEntityId = baseEntityId;
        this.followUpDate = followUpDate;
        this.nextFollowUpDate = nextFollowUpDate;
        this.visitDate = visitDate;
        this.telephonyFollowUpDate = telephonyFollowUpDate;
        this.specialFollowUpDate = specialFollowUpDate;
        this.noOfAnc = noOfAnc;
        this.memberName = memberName;
        this.memberMobileNo = memberMobileNo;
        this.dueDate = dueDate;
        this.ancType = ancType;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public String getFollowUpDate() {
        return followUpDate;
    }

    public String getNextFollowUpDate() {
        return nextFollowUpDate;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public String getTelephonyFollowUpDate() {
        return telephonyFollowUpDate;
    }

    public String getSpecialFollowUpDate() {
        return specialFollowUpDate;
    }

    public int getNoOfAnc() {
        return noOfAnc;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberMobileNo() {
        return memberMobileNo;
    }

    public String getDueDate() {
        return dueDate;
    }

    public int getAncType() {
        return ancType;
    }
}
