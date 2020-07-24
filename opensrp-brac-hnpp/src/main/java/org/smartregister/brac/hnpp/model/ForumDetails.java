package org.smartregister.brac.hnpp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ForumDetails {
    String forumType;
    String forumName;
    @SerializedName("place")
    HHMemberProperty place;
    String noOfParticipant;
    ArrayList<HHMemberProperty> participants;

    
}
