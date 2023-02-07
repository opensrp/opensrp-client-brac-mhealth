package org.smartregister.unicef.dghs.utils;

public class VisitLog {

    public String visitId;
    public String visitType;
    public String baseEntityId;
    public String familyId;
    public long visitDate;
    public String eventType;
    public String visitJson;
    public String referPlace;
    public String referReason;
    public String pregnantStatus;
    public String blockName;

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public void setPregnantStatus(String pregnantStatus) {
        this.pregnantStatus = pregnantStatus;
    }

    public String getPregnantStatus() {
        return pregnantStatus;
    }

    public String getReferPlace() {
        return referPlace;
    }

    public void setReferPlace(String refer_place) {
        this.referPlace = refer_place;
    }

    public String getReferReason() {
        return referReason;
    }

    public void setReferReason(String refer_reason) {
        this.referReason = refer_reason;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public long getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(long visitDate) {
        this.visitDate = visitDate;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getVisitJson() {
        return visitJson;
    }

    public void setVisitJson(String visitJson) {
        this.visitJson = visitJson;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getFamilyId() {
        return familyId;
    }
}
