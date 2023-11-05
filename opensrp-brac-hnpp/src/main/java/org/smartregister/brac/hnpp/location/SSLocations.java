package org.smartregister.brac.hnpp.location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Address;

import java.io.Serializable;
import java.util.HashMap;

public class SSLocations implements Serializable {
    public BaseLocation division;
    public BaseLocation country;
    public BaseLocation district;
    public BaseLocation city_corporation_upazila;
    public BaseLocation pourasabha;
    public BaseLocation union_ward;
    public BaseLocation village;

}
