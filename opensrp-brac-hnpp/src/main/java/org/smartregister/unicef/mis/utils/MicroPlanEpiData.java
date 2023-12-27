package org.smartregister.unicef.mis.utils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class MicroPlanEpiData implements Serializable {
    public boolean isViewMode = false;
    public int blockId;
    public String blockName;
    public int year;
    @SerializedName("status")
    public String microPlanStatus;
    public String comments;
    public long serverVersion;
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
    @SerializedName("outreach_info")
    public OutreachContentData outreachContentData;
    @SerializedName("center_details")
    public MicroPlanTypeData microPlanTypeData;
    @SerializedName("distribution_data")
    public DistributionData distributionData;
    @SerializedName("session_plan")
    public SessionPlanData sessionPlanData;
    @SerializedName("worker_info")
    public WorkerData workerData;
    @SerializedName("supervisor_info")
    public SuperVisorData superVisorData;

}
