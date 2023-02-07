package org.smartregister.unicef.dghs.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ForumDetails implements Serializable {
    public String forumType;
    public String forumName;
    @SerializedName("place")
    public HHMemberProperty place;
    public String noOfParticipant;
    public ArrayList<HHMemberProperty> participants;
    public String forumDate;
    public String ssName;
    public String villageName;
    public String clusterName;
    public String noOfAdoTakeFiveFood;
    public String noOfServiceTaken;
    public int sIndex;
    public int vIndex;
    public int cIndex;

}

