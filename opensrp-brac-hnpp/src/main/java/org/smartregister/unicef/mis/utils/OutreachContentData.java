package org.smartregister.unicef.mis.utils;

import java.io.Serializable;

public class OutreachContentData implements Serializable {
    public String provider;
    public String division;
    public int divisionId;
    public String district;
    public int districtId;
    public String upazila;
    public int upazilaId;
    public String paurasava;
    public int paurasavaId;
    public String unionName;
    public int unionId;
    public String oldWardName;
    public int oldWardId;
    public String newWardName;
    public int newWardId;
    public String blockName;
    public int blockId;
    public String outreachName;
    public String outreachId;
    public String centerType;
    public String address;
    public long serverVersion;
    transient public String mobile;
    public double latitude;
    public double longitude;
    transient public String microplanStatus;
    /*
    "outreach_info": {
    "provider": "ak",
    "division": "MYMENSINGH",
    "divisionId": 24570,
    "district": "JAMALPUR",
    "districtId": 24571,
    "upazila": "BAKSHIGANJ",
    "upazilaId": 42856,
    "paurasava": "NO PAURASAVA",
    "paurasavaId": 45136,
    "unionName": "BAGARCHAR",
    "unionId": 45137,
    "oldWardName": "BAGARCHAR:WARD 1",
    "oldWardId": 45138,
    "newWardName": "BAGARCHAR:WARD 1",
    "newWardId": 45139,
    "blockName": "BAGARCHAR:WARD 1:GA2",
    "blockId": 45145,
    "outreachId": "qe23ewrqwr",
    "outreachName": "moheswar",
    "centerType": "outreach",
    "address": "sdfsfasde"

  }
     */

}
