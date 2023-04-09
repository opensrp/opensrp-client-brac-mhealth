package org.smartregister.unicef.dghs.location;


import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.unicef.dghs.HnppApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HALocationHelper {

    private static HALocationHelper instance;
    private ArrayList<WardLocation> wardList = new ArrayList<>();

    private HALocationHelper(){
        wardList = HnppApplication.getGeoLocationRepository().getAllWard();
    }

    public ArrayList<WardLocation> getWardList() {
        return wardList;
    }
    public void updateWardList(){
        wardList = HnppApplication.getGeoLocationRepository().getAllWard();
    }

    public static HALocationHelper getInstance(){
        if(instance == null){
            instance = new HALocationHelper();
        }
        return instance;
    }

    public String generateHouseHoldId(HALocation HALocation, String lastFourDigit){
        return  HALocation.division.code+""+ HALocation.district.code+""+ HALocation.upazila.code+""
                + HALocation.union.code+""+ HALocation.ward.code+""
                + HALocation.block.code+""+lastFourDigit;
    }
    public Address getSSAddress(HALocation HALocation){
        Address address = new Address();
        address.setAddressType("usual_residence");
        HashMap<String,String> addressMap = new HashMap<>();
        addressMap.put("address1", HALocation.union.name);
        addressMap.put("address2", HALocation.upazila.name);
        addressMap.put("address3", HALocation.ward.name);
        addressMap.put("address8", HALocation.block.id+"");
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
        client.addAttribute("union_id", HALocation.union.id+"");
        client.addAttribute("ward_id", HALocation.ward.id+"");
        client.addAttribute("block_id", HALocation.block.id+"");
        return client;
    }
    public Map<String,String> getGeoIdentifier(HALocation HALocation){
        Map<String,String> identifiers = new HashMap<>();
        identifiers.put("division_id", HALocation.division.id+"");
        identifiers.put("district_id", HALocation.district.id+"");
        identifiers.put("upazila_id", HALocation.upazila.id+"");
        identifiers.put("union_id", HALocation.union.id+"");
        identifiers.put("ward_id", HALocation.ward.id+"");
        identifiers.put("block_id", HALocation.block.id+"");
        return identifiers;
    }
    public static void clearLocation(){
        instance =null;
    }

}
