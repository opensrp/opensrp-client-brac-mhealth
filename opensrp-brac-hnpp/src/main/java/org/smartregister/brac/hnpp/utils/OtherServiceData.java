package org.smartregister.brac.hnpp.utils;

import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;

public class OtherServiceData {
    private int imageSource;
    private String title;
    private String subTitle;
    private int type;
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
}
