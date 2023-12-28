package org.smartregister.unicef.mis.utils;

import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.unicef.mis.repository.MicroPlanRepository;

import java.io.Serializable;

public class SessionPlanData implements Serializable {
    public String year;
    public String januaryDate;
    public String februaryDate;
    public String marchDate;
    public String aprilDate;
    public String mayDate;
    public String juneDate;
    public String julyDate;
    public String augustDate;
    public String septemberDate;
    public String octoberDate;
    public String novemberDate;
    public String decemberDate;
    public String additionalMonth1;
    public String additionalMonth2;
    public String additionalMonth3;
    public String additionalMonth4;
    public String additionalMonth1Date;
    public String additionalMonth2Date;
    public String additionalMonth3Date;
    public String additionalMonth4Date;
    public String yearlyCount;
    public boolean saturday,sunday,monday,tuesday,wednesday,thursday;
    public boolean other;
    public String status = MicroPlanRepository.MICROPLAN_STATUS_TAG.PENDING.getValue();

}
