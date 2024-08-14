package org.smartregister.brac.hnpp.location;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SSModel {
    public int id;
    @SerializedName(("full_name"))
    public String skName;
    @SerializedName(("sk_username"))
    public String skUserName;
    public String ss_id;
    public String username;
    @SerializedName("withoutsk")
    public String withoutsk;
    @SerializedName("simprints_enable")
    public boolean simprints_enable = false;
    @SerializedName("payment_enable")
    public boolean payment_enable = false;
    public boolean is_selected = false;
    public ArrayList<SSLocations> locations = new ArrayList<>() ;


}
