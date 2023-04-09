package org.smartregister.unicef.dghs.location;


import java.io.Serializable;

public class HALocation implements Serializable {
    public BaseLocation country;
    public BaseLocation division;
    public BaseLocation district;
    public BaseLocation upazila;
    public BaseLocation union;
    public BaseLocation block;
    public BaseLocation ward;

}
