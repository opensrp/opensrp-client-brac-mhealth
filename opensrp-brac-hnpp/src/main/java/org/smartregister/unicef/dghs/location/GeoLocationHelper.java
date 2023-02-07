package org.smartregister.unicef.dghs.location;


import org.smartregister.clientandeventmodel.Address;
import org.smartregister.unicef.dghs.HnppApplication;

import java.util.ArrayList;
import java.util.HashMap;

public class GeoLocationHelper {

    private static GeoLocationHelper instance;
    private ArrayList<WardLocation> wardList = new ArrayList<>();

    private GeoLocationHelper(){
        wardList = HnppApplication.getGeoLocationRepository().getAllWard();
    }

    public ArrayList<WardLocation> getWardList() {
        return wardList;
    }
    public void updateWardList(){
        wardList = HnppApplication.getGeoLocationRepository().getAllWard();
    }

    public static GeoLocationHelper getInstance(){
        if(instance == null){
            instance = new GeoLocationHelper();
        }
        return instance;
    }

    public String generateHouseHoldId(GeoLocation geoLocation, String lastFourDigit){
        return  geoLocation.division.code+""+ geoLocation.district.code+""+ geoLocation.upazila.code+""
                + geoLocation.union.code+""+ geoLocation.ward.code+""
                + geoLocation.block.code+""+lastFourDigit;
    }
    public Address getSSAddress(GeoLocation geoLocation){
        Address address = new Address();
        address.setAddressType("usual_residence");
        HashMap<String,String> addressMap = new HashMap<>();
        addressMap.put("address1", geoLocation.union.name);
        addressMap.put("address2", geoLocation.upazila.name);
        addressMap.put("address3", geoLocation.ward.name);
        addressMap.put("address8", geoLocation.block.id+"");
        address.setAddressFields(addressMap);
        address.setStateProvince(geoLocation.division.name);
        address.setCityVillage(geoLocation.block.name);
        address.setCountyDistrict(geoLocation.district.name);
        address.setCountry(geoLocation.country.name);
        return address;
    }
    public static void clearLocation(){
        instance =null;
    }

}
