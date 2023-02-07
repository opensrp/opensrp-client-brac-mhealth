package org.smartregister.unicef.dghs.utils;

public class ANCRegister {
    public String LAST_MENSTRUAL_PERIOD;
    public String EDD;
    public String NO_PREV_PREG;
    public String NO_SURV_CHILDREN;
    public String HEIGHT;

    public ANCRegister() {
    }

    public String getLastMenstrualPeriod() {
        return LAST_MENSTRUAL_PERIOD;
    }

    public String getEDD() {
        return EDD;
    }

    public String getNoPrevPreg() {
        return NO_PREV_PREG;
    }

    public void setLastMenstrualPeriod(String lastMenstrualPeriod) {
        LAST_MENSTRUAL_PERIOD = lastMenstrualPeriod;
    }

    public void setEDD(String edd) {
        EDD = edd;
    }

    public void setNoPrevPreg(String noPrevPreg) {
        NO_PREV_PREG = noPrevPreg;
    }

    public void setNoSurvChildren(String noSurvChildren) {
        NO_SURV_CHILDREN = noSurvChildren;
    }

    public void setHEIGHT(String height) {
        HEIGHT = height;
    }

    public String getNoSurvChildren() {
        return NO_SURV_CHILDREN;
    }

    public String getHEIGHT() {
        return HEIGHT;
    }
}
