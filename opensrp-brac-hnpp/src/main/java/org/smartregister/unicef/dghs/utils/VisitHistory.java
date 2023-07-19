package org.smartregister.unicef.dghs.utils;

public class VisitHistory {
    public String visitId;
    public String visitType;
    public String baseEntityId;
    public String lmpDate;
    public String eddDate;
    public long startVisitDate;//LMPDATE
    public long endVisitDate;
    public String title;

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

    public String getLmpDate() {
        return lmpDate;
    }

    public void setLmpDate(String lmpDate) {
        this.lmpDate = lmpDate;
    }

    public String getEddDate() {
        return eddDate;
    }

    public void setEddDate(String eddDate) {
        this.eddDate = eddDate;
    }

    public long getStartVisitDate() {
        return startVisitDate;
    }

    public void setStartVisitDate(long startVisitDate) {
        this.startVisitDate = startVisitDate;
    }

    public long getEndVisitDate() {
        return endVisitDate;
    }

    public void setEndVisitDate(long endVisitDate) {
        this.endVisitDate = endVisitDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
