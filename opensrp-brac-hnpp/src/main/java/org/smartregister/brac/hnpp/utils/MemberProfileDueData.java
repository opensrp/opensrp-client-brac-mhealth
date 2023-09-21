package org.smartregister.brac.hnpp.utils;

import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;

public class MemberProfileDueData {
    private int imageSource;
    private String title;
    private String subTitle;
    private int type;
    public String from;
    boolean status;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    private String eventType;
    private ReferralFollowUpModel referralFollowUpModel;

    public void setReferralFollowUpModel(ReferralFollowUpModel referralFollowUpModel) {
        this.referralFollowUpModel = referralFollowUpModel;
    }

    public ReferralFollowUpModel getReferralFollowUpModel() {
        return referralFollowUpModel;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public int getImageSource() {
        return imageSource;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
