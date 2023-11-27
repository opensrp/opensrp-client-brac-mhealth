package org.smartregister.brac.hnpp.location;

import android.util.Log;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.IdentityModel;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.brac.hnpp.model.SkLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SSLocationHelper {

    private static SSLocationHelper instance;

    //private ArrayList<SSLocationForm> ssLocationForms = new ArrayList<>();
    private ArrayList<SSModel> ssModels  = new ArrayList<>();
    private SSLocationHelper(){
        setSsLocationForms();
    }
    public static SSLocationHelper getInstance(){
        if(instance == null){
            instance = new SSLocationHelper();
        }
        return instance;
    }

    public ArrayList<SSModel> getSsModels() {
        if(ssModels !=null && ssModels.isEmpty()){
            setSsLocationForms();
        }
        return ssModels;
    }
    public ArrayList<SSModel> getAllSks() {
       return HnppApplication.getSSLocationRepository().getAllSks();
    }
    public ArrayList<SSModel> getAllSelectedSks() {
        return HnppApplication.getSSLocationRepository().getAllSelectedSks();
    }
    public ArrayList<SSModel> getAllSS(String skUserName) {
        return HnppApplication.getSSLocationRepository().getAllSS(skUserName);
    }
    public ArrayList<SSModel> getAllSelectedSS(String skUserName) {
        return HnppApplication.getSSLocationRepository().getAllSelectedSS(skUserName);
    }
    public boolean isUpdated(ArrayList<SSModel> selectedSSId){
        boolean isUpdated = false;
        for (SSModel ssInfo: selectedSSId){
            isUpdated = HnppApplication.getSSLocationRepository().updateSelection(true, ssInfo.ss_id) > 0;
        }
        return isUpdated;
    }

    private void setSsLocationForms(){
            ssModels.clear();
            if(HnppConstants.isPALogin()){
                ssModels =  HnppApplication.getSSLocationRepository().getAllSelectedLocations();
            }else{
                ssModels =  HnppApplication.getSSLocationRepository().getAllLocations();
            }
    }
    public void updateModel(){
        if(ssModels !=null){
            setSsLocationForms();
        }
    }
    public ArrayList<String> getSelectedVillageId(){
        ArrayList<String> villageids = new ArrayList<>();
        getSsModels();
        if(ssModels !=null && ssModels.size()>0){
            for(SSModel ssModel : ssModels){
                for (SSLocations ssLocations : ssModel.locations){
                    villageids.add(ssLocations.village.id+"");
                }
            }
        }
        Log.v("SSLocationHelper","getSelectedVillageId: villageIds:"+villageids);
        return villageids;
    }
    public SkLocation getSkLocation(){
        SkLocation sklocation = new SkLocation();
        ArrayList<String> villageids = new ArrayList<>();
        String districtId = "";
        if(ssModels !=null && ssModels.size()>0){
            for(SSModel ssModel : ssModels){

                for (SSLocations ssLocations : ssModel.locations){
                    villageids.add(ssLocations.village.id+"");
                    districtId = ssLocations.district.id+"";
                }
            }
            sklocation.villageId = villageids;
            sklocation.districtId = districtId;

        }
        Log.v("SSLocationHelper","sklocation: sklocation:"+sklocation);
        return sklocation;
    }
    public SSLocations getSSLocationBySSName(String ssName){
        for(SSModel ssModel : ssModels){
            if(ssModel.username.equalsIgnoreCase(ssName)){
                return ssModel.locations.get(0);
            }
        }
        return null;
    }

    public String generateHouseHoldId(SSLocations ssLocations,String lastFourDigit){
        return  ssLocations.division.code+""+ssLocations.district.code+""+ssLocations.city_corporation_upazila.code+""
                +ssLocations.pourasabha.code+""+ssLocations.union_ward.code+""
                +ssLocations.village.code+""+lastFourDigit;
    }
    public Address getSSAddress(SSLocations ssLocations){
        Address address = new Address();
        address.setAddressType("usual_residence");
        HashMap<String,String> addressMap = new HashMap<>();
        addressMap.put("address1", ssLocations.union_ward.name);
        addressMap.put("address2", ssLocations.city_corporation_upazila.name);
        addressMap.put("address3", ssLocations.pourasabha.name);
        addressMap.put("address8", ssLocations.village.id+"");
        address.setAddressFields(addressMap);
        address.setStateProvince(ssLocations.division.name);
        address.setCityVillage(ssLocations.village.name);
        address.setCountyDistrict(ssLocations.district.name);
        address.setCountry(ssLocations.country.name);
        return address;
    }
    public Map<String,String> getGeoIdentifier(SSLocations ssLocations){
        Map<String,String> identifiers = new HashMap<>();
        identifiers.put("division_id", ssLocations.division.id+"");
        identifiers.put("district_id", ssLocations.district.id+"");
        identifiers.put("city_corporation_upazila_id", ssLocations.city_corporation_upazila.id+"");
        identifiers.put("pourasabha_id", ssLocations.pourasabha.id+"");
        identifiers.put("union_ward_id", ssLocations.union_ward.id+"");
        identifiers.put("village_id", ssLocations.village.id+"");
        identifiers.put("division", ssLocations.division.name+"");
        identifiers.put("district", ssLocations.district.name+"");
        identifiers.put("city_corporation_upazila", ssLocations.city_corporation_upazila.name+"");
        identifiers.put("pourasabha", ssLocations.pourasabha.name+"");
        identifiers.put("union_ward", ssLocations.union_ward.name+"");
        identifiers.put("village", ssLocations.village.name+"");
        return identifiers;
    }
    public Map<String,String> getMemberIdentifier(IdentityModel identityModel,Map<String,String> identifiers){
        if(identifiers == null){
             identifiers = new HashMap<>();
        }
        identifiers.put("name", identityModel.getName()+"");
        identifiers.put("mobile", identityModel.getPhoneNo()+"");
        identifiers.put("dob", identityModel.getDob()+"");
        return identifiers;
    }
    public static void clearLocation(){
        instance =null;
    }

}
