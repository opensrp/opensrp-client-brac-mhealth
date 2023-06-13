package org.smartregister.brac.hnpp.model;

import com.google.gson.annotations.SerializedName;

public class HHVisitDurationModel {
    public int id;
    @SerializedName(("service_id"))
    public int serviceId;
    @SerializedName(("service_name"))
    public String serviceName;
    @SerializedName("value")
    public int value;
}
