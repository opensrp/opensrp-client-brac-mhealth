package org.smartregister.unicef.mis.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class MicroPlanEpiData implements Serializable {
    public boolean isViewMode = false;
    public int blockId;
    public String blockName;
    public int year;
    public String microPlanStatus;
    public String outreachName;
    public String outreachId;
    public String unionName;
    public int unionId;
    public String oldWardName;
    public int oldWardId;
    public String newWardName;
    public int newWardId;
    public String houseHoldNo;
    public String centerType;
    public MicroPlanTypeData microPlanTypeData;
    public DistributionData distributionData;
    public SessionPlanData sessionPlanData;
    public WorkerData workerData;
    public SuperVisorData superVisorData;

}
