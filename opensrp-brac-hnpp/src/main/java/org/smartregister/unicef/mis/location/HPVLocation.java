package org.smartregister.unicef.mis.location;


import java.io.Serializable;

public class HPVLocation implements Serializable {
    public String country;
    public String code;
    public String division;
    public int division_id;
    public String division_code;
    public String district;
    public int district_id;
    public String district_code;
    public String cc_upazila;
    public int cc_upazila_id;
    public String cc_upazila_code;

    public String paurasava;
    public int paurasava_id;
    public String paurasava_code;

    public String union_name;
    public int union_id;
    public String union_code;

    public String old_ward;
    public int old_ward_id;
    public String old_ward_code;

    public String ward;
    public int ward_id;
    public String ward_code;



    public String block;
    public int block_id;
    public String block_code;

    @Override
    public String toString() {
        return  block;
    }

}
