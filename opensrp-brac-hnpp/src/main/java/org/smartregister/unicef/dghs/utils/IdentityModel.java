package org.smartregister.unicef.dghs.utils;

public class IdentityModel {
    String id;
    String baseEntityId="";
    String name;
    String tier;
    String familyHead;
    String age;
    String husband;
    String originalGuId;

    public void setOriginalGuId(String originalGuId) {
        this.originalGuId = originalGuId;
    }

    public String getOriginalGuId() {
        return originalGuId;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
