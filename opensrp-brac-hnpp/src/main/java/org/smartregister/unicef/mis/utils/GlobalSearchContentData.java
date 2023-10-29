package org.smartregister.unicef.mis.utils;

import java.io.Serializable;

public class GlobalSearchContentData implements Serializable {
    private String divisionId;
    private String districtId;
    private String upozillaId;
    private String villageId;
    private String gender;
    private String phoneNo;
    private String id;
    private String migrationType;
    private String baseEntityId;
    private String familyBaseEntityId;
    private String wardName;
    private int blockId;
    private String blockName;
    private String hhId;
    private String selectedBlockId;
    private String dob;
    private String shrId;

    public OtherVaccineContentData getOtherVaccineContentData() {
        return otherVaccineContentData;
    }

    public void setOtherVaccineContentData(OtherVaccineContentData otherVaccineContentData) {
        this.otherVaccineContentData = otherVaccineContentData;
    }

    private OtherVaccineContentData otherVaccineContentData;

    public void setShrId(String shrId) {
        this.shrId = shrId;
    }

    public String getShrId() {
        return shrId;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getDob() {
        return dob;
    }

    public void setUpozillaId(String upozillaId) {
        this.upozillaId = upozillaId;
    }

    public String getUpozillaId() {
        return upozillaId;
    }

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

    public void setId(String id) {
        this.id = id;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getId() {
        return id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }
}
