package org.smartregister.brac.hnpp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Member implements Parcelable {
    String name;
    String gender;
    String age;
    String baseEntityId;
    String familyBaseEntityId;

    public String getMobileNo() {
        return mobileNo;
    }

    String careGiver;
    String familyHead;
    String mobileNo;
    String familyName;
    String motherName;
    int status = 3;//1-> success//2-> failed//3-> default

    public Member(String name, String gender, String age, String baseEntityId,String familyBaseEntityId,
                  String careGiver,String familyHead,String mobileNo,String familyName,String motherName) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.baseEntityId = baseEntityId;
        this.familyBaseEntityId = familyBaseEntityId;
        this.careGiver = careGiver;
        this.familyHead = familyHead;
        this.mobileNo = mobileNo;
        this.familyName = familyName;
        this.motherName = motherName;
    }

    Member(){}

    protected Member(Parcel in) {
        name = in.readString();
        gender = in.readString();
        age = in.readString();
        baseEntityId = in.readString();
        familyBaseEntityId = in.readString();
        careGiver = in.readString();
        familyHead = in.readString();
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    public String getCareGiver() {
        return careGiver;
    }

    public String getFamilyHead() {
        return familyHead;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getMotherName() {
        return motherName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(gender);
        parcel.writeString(age);
        parcel.writeString(baseEntityId);
        parcel.writeString(familyBaseEntityId);
        parcel.writeString(careGiver);
        parcel.writeString(familyHead);
    }
}
