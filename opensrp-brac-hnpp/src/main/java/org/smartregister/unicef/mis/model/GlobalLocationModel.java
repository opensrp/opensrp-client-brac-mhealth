package org.smartregister.unicef.mis.model;

public class GlobalLocationModel {
    public int id;
    public int parentLocationId;
    public int locationTagId;
    public String code;
    public String name;

    @Override
    public String toString() {
        return  name;
    }
}
