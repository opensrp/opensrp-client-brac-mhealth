package org.smartregister.brac.hnpp.model;

public class AncFollowUpModel {
    public String baseEntityId;
    public long followUpDate;
    public long nextFollowUpDate;
    public long visitDate;
    public long telephonyFollowUpDate;
    public long specialFollowUpDate;
    public int noOfAnc;
    public int isCalledTelephonic;

    public String highRiskKey;
    public String highRiskValue;
    public String lowRiskKey;
    public String lowRiskValue;
    public int riskType;

    public String memberName;
    public String memberPhoneNum;

}
