package org.smartregister.brac.hnpp.utils;

public class IdentityModel {
    String guid;
    String baseEntityId="";
    String name;
    String tier;
    String familyHead;
    String age;
    String husband;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHusband() {
        return husband;
    }

    public void setHusband(String husband) {
        this.husband = husband;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getTier() {
        return tier;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFamilyHead(String familyHead) {
        this.familyHead = familyHead;
    }

    public String getFamilyHead() {
        return familyHead;
    }
}