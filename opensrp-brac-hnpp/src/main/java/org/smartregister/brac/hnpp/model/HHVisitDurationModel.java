package org.smartregister.brac.hnpp.model;

import com.google.gson.annotations.SerializedName;

public class HHVisitDurationModel {
    public int id;
    @SerializedName(("serviceId"))
    public int serviceId;
    @SerializedName(("serviceName"))
    public String serviceName;
    @SerializedName("value")
    public int value;
}
