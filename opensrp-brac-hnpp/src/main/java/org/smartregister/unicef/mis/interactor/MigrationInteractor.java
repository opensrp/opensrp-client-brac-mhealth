package org.smartregister.unicef.mis.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.MigrationContract;
import org.smartregister.unicef.mis.domain.HouseholdId;
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.location.HALocation;
import org.smartregister.unicef.mis.repository.HouseholdIdRepository;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.List;

import static org.smartregister.unicef.mis.utils.HnppJsonFormUtils.memberCountWithZero;

public class MigrationInteractor  {
    private static final String MIGRATION_POST = "/rest/event/migrate?";
    private AppExecutors appExecutors;
    public MigrationInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public void migrateHH(GlobalSearchContentData globalSearchContentData, MigrationContract.MigrationPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            Client baseClient = generateHHClient(globalSearchContentData);

            boolean isSuccess = postData(globalSearchContentData,baseClient);
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }
    public void migrateMember(GlobalSearchContentData globalSearchContentData, MigrationContract.MigrationPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            Client baseClient = generateMemberClient(globalSearchContentData);

            boolean isSuccess = postData(globalSearchContentData,baseClient);
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }

    private boolean postData(GlobalSearchContentData globalSearchContentData, Client baseClient ) {
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
            String url = baseUrl + MIGRATION_POST + "districtId=" + globalSearchContentData.getDistrictId() + "&divisionId=" + globalSearchContentData.getDivisionId()
                    + "&villageId=" + globalSearchContentData.getVillageId() + "&type="+ globalSearchContentData.getMigrationType();
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
    private Client generateMemberClient(GlobalSearchContentData globalSearchContentData) {
        Client baseClient = new Client(globalSearchContentData.getBaseEntityId());
        baseClient.addRelationship("family", globalSearchContentData.getFamilyBaseEntityId());
        if(TextUtils.isEmpty(globalSearchContentData.getBlockName())){
            String ssName = HnppDBUtils.getBlockNameFromFamilyTable(globalSearchContentData.getFamilyBaseEntityId());
            globalSearchContentData.setBlockName(ssName);
        }
        HALocation ss = HnppApplication.getHALocationRepository().getLocationByBlock(globalSearchContentData.getBlockId()+"");
        if(ss==null){
            return null;
        }
        baseClient.addAttribute("house_hold_id", globalSearchContentData.getHhId());
        String unique_id = generateMemberId(globalSearchContentData.getHhId(), globalSearchContentData.getFamilyBaseEntityId());
        if(unique_id.isEmpty()){
            return null;
        }
        baseClient.addIdentifier("opensrp_id",unique_id);
        HALocationHelper.getInstance().addGeolocationIds(ss,baseClient);
        List<Address> listAddress = new ArrayList<>();
        listAddress.add(HALocationHelper.getInstance().getSSAddress(ss));
        baseClient.setAddresses(listAddress);

        return baseClient;
    }

    private String generateMemberId(String hhId, String familyBaseEntityId) {
        int memberCount = HnppApplication.ancRegisterRepository().getMemberCountWithoutRemove(familyBaseEntityId);
        String uId = hhId+memberCountWithZero(memberCount+1);
        return uId;
    }

    private Client generateHHClient(GlobalSearchContentData globalSearchContentData) {
        Client baseClient = new Client(globalSearchContentData.getBaseEntityId());
        baseClient.addRelationship("family_head", globalSearchContentData.getBaseEntityId());
        baseClient.addRelationship("primary_caregiver", globalSearchContentData.getBaseEntityId());
        if(TextUtils.isEmpty(globalSearchContentData.getBlockName())){
            String ssName = HnppDBUtils.getBlockNameFromFamilyTable(globalSearchContentData.getBaseEntityId());
            globalSearchContentData.setBlockName(ssName);
        }
        baseClient.addAttribute("ward_name", globalSearchContentData.getWardName());
        baseClient.addAttribute("block_id", globalSearchContentData.getBlockId());

        HALocation ss = HnppApplication.getHALocationRepository().getLocationByBlock(globalSearchContentData.getBlockId()+"");

        String unique_id = generateHHId(ss);
        if(unique_id.isEmpty()){
            return null;
        }
        baseClient.addIdentifier("opensrp_id",unique_id);

        List<Address> listAddress = new ArrayList<>();
        listAddress.add(HALocationHelper.getInstance().getSSAddress(ss));
        HALocationHelper.getInstance().addGeolocationIds(ss,baseClient);
        baseClient.setAddresses(listAddress);

        return baseClient;
    }

    private String generateHHId(HALocation HALocation) {
        String unique_id = "";


        HouseholdIdRepository householdIdRepo = HnppApplication.getHNPPInstance().getHouseholdIdRepository();
        String block_id = String.valueOf(HALocation.block.id);
        HouseholdId hhid = householdIdRepo.getNextHouseholdId(block_id);
        if(hhid == null){
            return "";
        }
        unique_id = HALocationHelper.getInstance().generateHouseHoldId(HALocation, hhid.getOpenmrsId() + "");

        return unique_id;
    }
}
