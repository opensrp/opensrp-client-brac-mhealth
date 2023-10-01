package org.smartregister.brac.hnpp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ReferralFollowupJsonModel implements Parcelable {
    String json;
    ReferralFollowUpModel referralFollowUpModel;

    public ReferralFollowupJsonModel(String json, ReferralFollowUpModel referralFollowUpModel) {
        this.json = json;
        this.referralFollowUpModel = referralFollowUpModel;
    }

    protected ReferralFollowupJsonModel(Parcel in) {
        json = in.readString();
        referralFollowUpModel = in.readParcelable(ReferralFollowUpModel.class.getClassLoader());
    }

    public static final Creator<ReferralFollowupJsonModel> CREATOR = new Creator<ReferralFollowupJsonModel>() {
        @Override
        public ReferralFollowupJsonModel createFromParcel(Parcel in) {
            return new ReferralFollowupJsonModel(in);
        }

        @Override
        public ReferralFollowupJsonModel[] newArray(int size) {
            return new ReferralFollowupJsonModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(json);
        parcel.writeParcelable(referralFollowUpModel, i);
    }

    public String getJson() {
        return json;
    }

    public ReferralFollowUpModel getReferralFollowUpModel() {
        return referralFollowUpModel;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void setReferralFollowUpModel(ReferralFollowUpModel referralFollowUpModel) {
        this.referralFollowUpModel = referralFollowUpModel;
    }
}
