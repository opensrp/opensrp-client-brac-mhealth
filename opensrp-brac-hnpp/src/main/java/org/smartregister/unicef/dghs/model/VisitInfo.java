package org.smartregister.unicef.dghs.model;

public class VisitInfo {
    private long visitDate;
    private int visitCount;

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public void setVisitDate(long visitDate) {
        this.visitDate = visitDate;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public long getVisitDate() {
        return visitDate;
    }
}
