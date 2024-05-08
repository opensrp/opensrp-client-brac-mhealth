package org.smartregister.unicef.mis.imci.model;

import java.io.Serializable;

public class IMCIReport implements Serializable {
    String baseEntityId;
    String imciType;
    String assessmentResultType;
    String treatment;
    String assessmentResult;
    String assessmentDate;
    long assessmentTimeStamp;

    public void setAssessmentDate(String assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getAssessmentDate() {
        return assessmentDate;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getImciType() {
        return imciType;
    }

    public void setImciType(String imciType) {
        this.imciType = imciType;
    }

    public String getAssessmentResultType() {
        return assessmentResultType;
    }

    public void setAssessmentResultType(String assessmentResultType) {
        this.assessmentResultType = assessmentResultType;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getAssessmentResult() {
        return assessmentResult;
    }

    public void setAssessmentResult(String assessmentResult) {
        this.assessmentResult = assessmentResult;
    }

    public long getAssessmentTimeStamp() {
        return assessmentTimeStamp;
    }

    public void setAssessmentTimeStamp(long assessmentTimeStamp) {
        this.assessmentTimeStamp = assessmentTimeStamp;
    }
}
