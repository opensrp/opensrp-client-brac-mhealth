package org.smartregister.brac.hnpp.utils;

import java.io.Serializable;

public class MigrationSearchContentData implements Serializable {
    private String divisionId;
    private String districtId;
    private String villageId;
    private String gender;
    private String age;
    private String migrationType;
    private String baseEntityId;

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setMigrationType(String migrationType) {
        this.migrationType = migrationType;
    }

    public String getMigrationType() {
        return migrationType;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getVillageId() {
        return villageId;
    }

    public void setVillageId(String villageId) {
        this.villageId = villageId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
