package org.smartregister.brac.hnpp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ReferralFollowUpModel implements Parcelable {
    private String baseEntityId;
    private String referralReason;
    private String referralPlace;

    public ReferralFollowUpModel() {}
    public ReferralFollowUpModel(Parcel in) {
        baseEntityId = in.readString();
        referralReason = in.readString();
        referralPlace = in.readString();
    }

    public static final Creator<ReferralFollowUpModel> CREATOR = new Creator<ReferralFollowUpModel>() {
        @Override
        public ReferralFollowUpModel createFromParcel(Parcel in) {
            return new ReferralFollowUpModel(in);
        }

        @Override
        public ReferralFollowUpModel[] newArray(int size) {
            return new ReferralFollowUpModel[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(baseEntityId);
        parcel.writeString(referralReason);
        parcel.writeString(referralPlace);
    }
}
