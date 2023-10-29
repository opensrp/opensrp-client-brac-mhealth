package org.smartregister.unicef.mis.utils;

public class VisitHistory {
    private String visitId;
    private String visitType;
    private String baseEntityId;
    private String lmpDate;
    private String eddDate;
    private long startVisitDate;//LMPDATE
    private long endVisitDate;
    private long eddDateLong;
    private String title;

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

    public void setEddDateLong(long eddDateLong) {
        this.eddDateLong = eddDateLong;
    }

    public long getEddDateLong() {
        return eddDateLong;
    }
}
