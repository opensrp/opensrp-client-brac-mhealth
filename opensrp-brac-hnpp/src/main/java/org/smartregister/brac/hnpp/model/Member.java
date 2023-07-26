package org.smartregister.brac.hnpp.model;

public class Member {
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
}
