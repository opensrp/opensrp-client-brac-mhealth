package org.smartregister.brac.hnpp.model;

public class RiskyPatientFilterType {
    int visitScheduleToday = 0;
    int visitScheduleNextThree = 0;
    int visitScheduleNextSeven = 0;
    int visitScheduleLastDay = 0;
    int visitScheduleLastThree = 0;
    int visitScheduleLastSeven = 0;
    int visitScheduleAllDue = 0;

    public int getVisitScheduleToday() {
        return visitScheduleToday;
    }

    public void setVisitScheduleToday(int visitScheduleToday) {
        this.visitScheduleToday = visitScheduleToday;
    }

    public int getVisitScheduleNextThree() {
        return visitScheduleNextThree;
    }

    public void setVisitScheduleNextThree(int visitScheduleNextThree) {
        this.visitScheduleNextThree = visitScheduleNextThree;
    }

    public int getVisitScheduleNextSeven() {
        return visitScheduleNextSeven;
    }

    public void setVisitScheduleNextSeven(int visitScheduleNextSeven) {
        this.visitScheduleNextSeven = visitScheduleNextSeven;
    }

    public int getVisitScheduleLastDay() {
        return visitScheduleLastDay;
    }

    public void setVisitScheduleLastDay(int visitScheduleLastDay) {
        this.visitScheduleLastDay = visitScheduleLastDay;
    }

    public int getVisitScheduleLastThree() {
        return visitScheduleLastThree;
    }

    public void setVisitScheduleLastThree(int visitScheduleLastThree) {
        this.visitScheduleLastThree = visitScheduleLastThree;
    }

    public int getVisitScheduleLastSeven() {
        return visitScheduleLastSeven;
    }

    public void setVisitScheduleLastSeven(int visitScheduleLastSeven) {
        this.visitScheduleLastSeven = visitScheduleLastSeven;
    }

    public int getVisitScheduleAllDue() {
        return visitScheduleAllDue;
    }

    public void setVisitScheduleAllDue(int visitScheduleAllDue) {
        this.visitScheduleAllDue = visitScheduleAllDue;
    }
}
