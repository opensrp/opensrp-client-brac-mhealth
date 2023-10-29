package org.smartregister.unicef.mis.model;

public class ReferralFollowUpModel {
    private String baseEntityId;
    private String referralReason;
    private String referralPlace;

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public void setReferralReason(String referralReason) {
        this.referralReason = referralReason;
    }

    public void setReferralPlace(String referralPlace) {
        this.referralPlace = referralPlace;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public String getReferralReason() {
        return referralReason;
    }

    public String getReferralPlace() {
        return referralPlace;
    }
}
