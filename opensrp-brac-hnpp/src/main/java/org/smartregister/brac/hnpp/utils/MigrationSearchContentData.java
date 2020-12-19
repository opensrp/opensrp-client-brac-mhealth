package org.smartregister.brac.hnpp.utils;

import org.smartregister.clientandeventmodel.Address;

import java.io.Serializable;

public class MigrationSearchContentData implements Serializable {
    private String divisionId;
    private String districtId;
    private String villageId;
    private String gender;
    private String startAge;
    private String age;
    private String migrationType;
    private String baseEntityId;
    private String familyBaseEntityId;
    private String ssName;
    private int villageIndex;
    private String villageName;
    private String hhId;
    private String selectedVillageId;

    public void setSelectedVillageId(String selectedVillageId) {
        this.selectedVillageId = selectedVillageId;
    }

    public String getSelectedVillageId() {
        return selectedVillageId;
    }

    public void setHhId(String hhId) {
        this.hhId = hhId;
    }

    public String getHhId() {
        return hhId;
    }

    public void setFamilyBaseEntityId(String familyBaseEntityId) {
        this.familyBaseEntityId = familyBaseEntityId;
    }

    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }

    public String getSsName() {
        return ssName;
    }

    public void setSsName(String ssName) {
        this.ssName = ssName;
    }

    public int getVillageIndex() {
        return villageIndex;
    }

    public void setVillageIndex(int villageIndex) {
        this.villageIndex = villageIndex;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String villageName) {
        this.villageName = villageName;
    }

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

    public void setStartAge(String startAge) {
        this.startAge = startAge;
    }

    public String getStartAge() {
        return startAge;
    }
}
