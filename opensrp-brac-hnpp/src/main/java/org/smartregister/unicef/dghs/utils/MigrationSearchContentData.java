package org.smartregister.unicef.dghs.utils;

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
    private String wardName;
    private int blockId;
    private String blockName;
    private String hhId;
    private String selectedBlockId;

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getSelectedBlockId() {
        return selectedBlockId;
    }

    public void setSelectedBlockId(String selectedBlockId) {
        this.selectedBlockId = selectedBlockId;
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
