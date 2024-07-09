package org.smartregister.unicef.mis.location;


import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.USER_ID;

import android.annotation.SuppressLint;
import android.util.Log;

import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HALocationHelper {

    private static HALocationHelper instance;
    private ArrayList<WardLocation> unionList = new ArrayList<>();

    private HALocationHelper(){
        unionList = HnppApplication.getHALocationRepository().getAllUnion();
    }

    public ArrayList<WardLocation> getUnionList() {
        return unionList;
    }
    public void updateWardList(){
        unionList = HnppApplication.getHALocationRepository().getAllUnion();
    }

    public static HALocationHelper getInstance(){
        if(instance == null){
            instance = new HALocationHelper();
        }
        return instance;
    }
    @SuppressLint("DefaultLocale")
    public String generateIdForOCA(){
        long time = System.currentTimeMillis();
        Log.v("DEFAULT_ID","OCA HHID:"+time);
        return time+"";
//        int householdCount = HnppDBUtils.getOCACount();
//        String householdCountFourDigit = String.format("%04d", householdCount+1);
//        String userId = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(USER_ID);
//        userId = String.format("%05d", Integer.parseInt(userId));
//        long time = System.currentTimeMillis();
//        String random3digit = String.format("%03d", time % 1000);
//        return  "2"+userId+""+random3digit+""+householdCountFourDigit;
    }
    @SuppressLint("DefaultLocale")
    public String generateHouseHoldId(){
        //int householdCount = HnppDBUtils.getHouseHoldCount();
       // String householdCountFourDigit = String.format("%04d", householdCount+1);
       // String userId = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(USER_ID);
       // userId = String.format("%05d", Integer.parseInt(userId));
        long time = System.currentTimeMillis();//1712187690535

//        String newId = "1"+getFormattedTimestamp();
//        Log.v("DEFAULT_ID","time:"+time+":display:"+newId+":newIdLength:"+newId.length()+":random:"+getFormattedTimestamp());
//        String random3digit = String.format("%03d", time % 1000);
//        String finalId = "1"+userId+""+random3digit+""+householdCountFourDigit;
        Log.v("DEFAULT_ID","HHID:"+time);
        return  time+"";
//        return  "1"+HALocation.division.code+""+ HALocation.district.code+""+ HALocation.upazila.code+""
//                + HALocation.union.code+""+ HALocation.ward.code+""
//                + HALocation.block.code+""+generatedRandomId();
    }

    public static Long getFormattedTimestamp() {
        // Get the current time
        Date now = new Date();

        // Format the timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String formattedTimestamp = formatter.format(now);
        return Long.parseLong(formattedTimestamp)/1000;
    }
    public Address getSSAddress(HALocation HALocation){
        Address address = new Address();
        address.setAddressType("usual_residence");
        HashMap<String,String> addressMap = new HashMap<>();
        addressMap.put("address1", HALocation.union.name);
        addressMap.put("address2", HALocation.upazila.name);
        addressMap.put("paurasava",HALocation.paurasava.name);
        addressMap.put("address3", HALocation.ward.name);
        addressMap.put("address8", HALocation.block.id+"");
        addressMap.put("old_ward", HALocation.old_ward.name+"");
        address.setAddressFields(addressMap);
        address.setStateProvince(HALocation.division.name);
        address.setCityVillage(HALocation.block.name);
        address.setCountyDistrict(HALocation.district.name);
        address.setCountry(HALocation.country.name);
        return address;
    }
    public Client addGeolocationIds(HALocation HALocation, Client client){
        client.addAttribute("division_id", HALocation.division.id+"");
        client.addAttribute("district_id", HALocation.district.id+"");
        client.addAttribute("upazila_id", HALocation.upazila.id+"");
        client.addAttribute("paurasava_id",HALocation.paurasava.id+"");
        client.addAttribute("union_id", HALocation.union.id+"");
        client.addAttribute("ward_id", HALocation.ward.id+"");
        client.addAttribute("block_id", HALocation.block.id+"");
        client.addAttribute("old_ward_id", HALocation.old_ward.id+"");
        return client;
    }
    public Client addOOCGeolocationIds(String divisionId,String districtId, String upazilaId, Client client){
        client.addAttribute("division_id", divisionId);
        client.addAttribute("district_id", districtId);
        client.addAttribute("upazila_id", upazilaId);
        return client;
    }
    public Event addOOCIdentifier(String divisionId, String districtId, String upazilaId, Event event){
        Map<String,String> identifiers = new HashMap<>();
        identifiers.put("division_id", divisionId+"");
        identifiers.put("district_id", districtId+"");
        identifiers.put("upazila_id", upazilaId+"");
        event.setIdentifiers(identifiers);
        return event;
    }
    public Map<String,String> getGeoIdentifier(HALocation HALocation){
        Map<String,String> identifiers = new HashMap<>();
        identifiers.put("division_id", HALocation.division.id+"");
        identifiers.put("district_id", HALocation.district.id+"");
        identifiers.put("upazila_id", HALocation.upazila.id+"");
        identifiers.put("paurasava_id", HALocation.paurasava.id+"");
        identifiers.put("union_id", HALocation.union.id+"");
        identifiers.put("ward_id", HALocation.ward.id+"");
        identifiers.put("block_id", HALocation.block.id+"");
        identifiers.put("old_ward_id", HALocation.old_ward.id+"");
        return identifiers;
    }
    public static void clearLocation(){
        instance =null;
    }

}
