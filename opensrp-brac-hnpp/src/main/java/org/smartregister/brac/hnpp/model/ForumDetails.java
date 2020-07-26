package org.smartregister.brac.hnpp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ForumDetails {
    public String forumType;
    public String forumName;
    @SerializedName("place")
    public HHMemberProperty place;
    public String noOfParticipant;
    public ArrayList<HHMemberProperty> participants;

    
}
