package org.smartregister.unicef.dghs.location;


import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.unicef.dghs.HnppApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public Client addGeolocationIds(GeoLocation geoLocation, Client client){
        client.addAttribute("division_id",geoLocation.division.id+"");
        client.addAttribute("district_id",geoLocation.district.id+"");
        client.addAttribute("upazila_id",geoLocation.upazila.id+"");
        client.addAttribute("union_id",geoLocation.union.id+"");
        client.addAttribute("ward_id",geoLocation.ward.id+"");
        client.addAttribute("block_id",geoLocation.block.id+"");
        return client;
    }
    public Map<String,String> getGeoIdentifier(GeoLocation geoLocation){
        Map<String,String> identifiers = new HashMap<>();
        identifiers.put("division_id",geoLocation.division.id+"");
        identifiers.put("district_id",geoLocation.district.id+"");
        identifiers.put("upazila_id",geoLocation.upazila.id+"");
        identifiers.put("union_id",geoLocation.union.id+"");
        identifiers.put("ward_id",geoLocation.ward.id+"");
        identifiers.put("block_id",geoLocation.block.id+"");
        return identifiers;
    }
    public static void clearLocation(){
        instance =null;
    }

}
