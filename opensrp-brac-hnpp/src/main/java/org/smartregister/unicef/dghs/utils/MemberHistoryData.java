package org.smartregister.unicef.dghs.utils;

public class MemberHistoryData {

    private int imageSource;
    private String title;
    private String eventType;
    private long visitDate;
    private String visitDay;
    private String visitDetails;
    private String memberName;
    private String baseEntityId;
    private boolean isDelay;
    public String visitId;
    private String serviceTakenDate;
    private String scheduleDate;

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public void setServiceTakenDate(String serviceTakenDate) {
        this.serviceTakenDate = serviceTakenDate;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public String getServiceTakenDate() {
        return serviceTakenDate;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setDelay(boolean delay) {
        isDelay = delay;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getImageSource() {
        return imageSource;
    }

    public void setImageSource(int imageSource) {
        this.imageSource = imageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String visitType) {
        this.eventType = visitType;
    }

    public long getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(long visitDate) {
        this.visitDate = visitDate;
    }

    public String getVisitDetails() {
        return visitDetails;
    }

    public void setVisitDetails(String visitDetails) {
        this.visitDetails = visitDetails;
    }

    public void setVisitDay(String visitDay) {
        this.visitDay = visitDay;
    }

    public String getVisitDay() {
        return visitDay;
    }
}
