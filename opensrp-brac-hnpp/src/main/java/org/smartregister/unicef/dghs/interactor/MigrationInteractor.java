package org.smartregister.unicef.dghs.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.contract.MigrationContract;
import org.smartregister.unicef.dghs.domain.HouseholdId;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.GeoLocation;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.MigrationSearchContentData;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.List;

import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.memberCountWithZero;

public class MigrationInteractor  {
    private static final String MIGRATION_POST = "/rest/event/migrate?";
    private AppExecutors appExecutors;
    public MigrationInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public void migrateHH(MigrationSearchContentData migrationSearchContentData, MigrationContract.MigrationPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            Client baseClient = generateHHClient(migrationSearchContentData);

            boolean isSuccess = postData(migrationSearchContentData,baseClient);
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }
    public void migrateMember(MigrationSearchContentData migrationSearchContentData, MigrationContract.MigrationPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            Client baseClient = generateMemberClient(migrationSearchContentData);

            boolean isSuccess = postData(migrationSearchContentData,baseClient);
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }

    private boolean postData(MigrationSearchContentData migrationSearchContentData,Client baseClient ) {
        if(baseClient == null)
            return false;
        try {
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if (TextUtils.isEmpty(userName)) {
                return false;
            }
            String url = baseUrl + MIGRATION_POST + "districtId=" + migrationSearchContentData.getDistrictId() + "&divisionId=" + migrationSearchContentData.getDivisionId()
                    + "&villageId=" + migrationSearchContentData.getVillageId() + "&type="+migrationSearchContentData.getMigrationType();
//            String url = baseUrl + MIGRATION_POST + "districtId=10371&divisionId=10349&villageId=9315&type="+migrationSearchContentData.getMigrationType();

            String json = new Gson().toJson(baseClient);
            ArrayList<String> list = new ArrayList<>();
            list.add(json);
            JSONObject request = new JSONObject();
            request.put(AllConstants.KEY.CLIENTS,list);

            Log.v("MIGRATION_POST", "url:" + url+"payload:"+request.toString());
            org.smartregister.domain.Response resp = httpAgent.post(url,request.toString());
            if (resp.isFailure()) {
                throw new NoHttpResponseException(MIGRATION_POST + " not returned data");
            }
            String responseStr = (String)resp.payload();
            Log.v("MIGRATION_POST", "responseStr:" + responseStr);
            if(responseStr.equalsIgnoreCase("ok")){
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private Client generateMemberClient(MigrationSearchContentData migrationSearchContentData) {
        Client baseClient = new Client(migrationSearchContentData.getBaseEntityId());
        baseClient.addRelationship("family",migrationSearchContentData.getFamilyBaseEntityId());
        if(TextUtils.isEmpty(migrationSearchContentData.getBlockName())){
            String ssName = HnppDBUtils.getBlockNameFromFamilyTable(migrationSearchContentData.getFamilyBaseEntityId());
            migrationSearchContentData.setBlockName(ssName);
        }
        GeoLocation ss = HnppApplication.getGeoLocationRepository().getLocationByBlock(migrationSearchContentData.getBlockId()+"");
        if(ss==null){
            return null;
        }
        baseClient.addAttribute("house_hold_id",migrationSearchContentData.getHhId());
        String unique_id = generateMemberId(migrationSearchContentData.getHhId(),migrationSearchContentData.getFamilyBaseEntityId());
        if(unique_id.isEmpty()){
            return null;
        }
        baseClient.addIdentifier("opensrp_id",unique_id);
        GeoLocationHelper.getInstance().addGeolocationIds(ss,baseClient);
        List<Address> listAddress = new ArrayList<>();
        listAddress.add(GeoLocationHelper.getInstance().getSSAddress(ss));
        baseClient.setAddresses(listAddress);

        return baseClient;
    }

    private String generateMemberId(String hhId, String familyBaseEntityId) {
        int memberCount = HnppApplication.ancRegisterRepository().getMemberCountWithoutRemove(familyBaseEntityId);
        String uId = hhId+memberCountWithZero(memberCount+1);
        return uId;
    }

    private Client generateHHClient(MigrationSearchContentData migrationSearchContentData) {
        Client baseClient = new Client(migrationSearchContentData.getBaseEntityId());
        baseClient.addRelationship("family_head",migrationSearchContentData.getBaseEntityId());
        baseClient.addRelationship("primary_caregiver",migrationSearchContentData.getBaseEntityId());
        if(TextUtils.isEmpty(migrationSearchContentData.getBlockName())){
            String ssName = HnppDBUtils.getBlockNameFromFamilyTable(migrationSearchContentData.getBaseEntityId());
            migrationSearchContentData.setBlockName(ssName);
        }
        baseClient.addAttribute("ward_name",migrationSearchContentData.getWardName());
        baseClient.addAttribute("block_id",migrationSearchContentData.getBlockId());

        GeoLocation ss = HnppApplication.getGeoLocationRepository().getLocationByBlock(migrationSearchContentData.getBlockId()+"");

        String unique_id = generateHHId(ss);
        if(unique_id.isEmpty()){
            return null;
        }
        baseClient.addIdentifier("opensrp_id",unique_id);

        List<Address> listAddress = new ArrayList<>();
        listAddress.add(GeoLocationHelper.getInstance().getSSAddress(ss));
        GeoLocationHelper.getInstance().addGeolocationIds(ss,baseClient);
        baseClient.setAddresses(listAddress);

        return baseClient;
    }

    private String generateHHId(GeoLocation geoLocation) {
        String unique_id = "";


        HouseholdIdRepository householdIdRepo = HnppApplication.getHNPPInstance().getHouseholdIdRepository();
        String block_id = String.valueOf(geoLocation.block.id);
        HouseholdId hhid = householdIdRepo.getNextHouseholdId(block_id);
        if(hhid == null){
            return "";
        }
        unique_id = GeoLocationHelper.getInstance().generateHouseHoldId(geoLocation, hhid.getOpenmrsId() + "");

        return unique_id;
    }
}
