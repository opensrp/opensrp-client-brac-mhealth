package org.smartregister.brac.hnpp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Member implements Parcelable {
    String name;
    String gender;
    String age;
    String baseEntityId;
    String familyBaseEntityId;
    String careGiver;
    String familyHead;

    public Member(String name, String gender, String age, String baseEntityId,String familyBaseEntityId,String careGiver,String familyHead) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.baseEntityId = baseEntityId;
        this.familyBaseEntityId = familyBaseEntityId;
        this.careGiver = careGiver;
        this.familyHead = familyHead;
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

    @Override
    public int describeContents() {
        return 0;
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
