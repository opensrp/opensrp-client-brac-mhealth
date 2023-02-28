package org.smartregister.unicef.dghs.model;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.core.model.CoreFamilyProfileModel;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.EventClientRepository;

import java.util.ArrayList;
import java.util.List;

import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.makeReadOnlyFields;

import android.text.TextUtils;
import android.util.Log;

public class HnppFamilyProfileModel extends CoreFamilyProfileModel {
    private String moduleId;
    private String houseHoldId;
    private String familyBaseEntityId;
    public HnppFamilyProfileModel(String familyName,String moduleId,String houseHoldId, String familyBaseEntityId) {
        super(familyName);
        this.moduleId = moduleId;
        this.houseHoldId = houseHoldId;
        this.familyBaseEntityId = familyBaseEntityId;
    }
    public void updateHouseIdAndModuleId(String houseHoldId, String moduleId){
        this.houseHoldId = houseHoldId;
        this.moduleId = moduleId;
    }

    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        Log.v("INVALID_REQ","getFormAsJson>>familyBaseEntityId:"+familyBaseEntityId+":houseHoldId:"+houseHoldId+":moduleId:"+moduleId);
        HnppConstants.appendLog("INVALID_REQ","getFormAsJson>>familyBaseEntityId:"+familyBaseEntityId+":houseHoldId:"+houseHoldId+":moduleId:"+moduleId);
        JSONObject form = getFormUtils().getFormJson(formName);
        if (form == null || TextUtils.isEmpty(familyBaseEntityId)) {
            return null;
        }
        HnppJsonFormUtils.updateFormWithMemberId(form,houseHoldId,familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithModuleId(form,moduleId,familyBaseEntityId);
        HnppJsonFormUtils.updateFormWithBlockInformation(form,familyBaseEntityId);
        if(HnppConstants.isPALogin()){
            makeReadOnlyFields(form);
        }

        return form;
    }

    @Override
    public FamilyEventClient processMemberRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = processRegistration(jsonString, familyBaseEntityId);

        if(familyEventClient == null) return null;
        EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
        try{
            JSONObject familyJSON = eventClientRepository.getClientByBaseEntityId(familyBaseEntityId);
            String addessJson = familyJSON.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            Client familyClient = familyEventClient.getClient();
            familyClient.setAddresses(listAddress);
            HnppConstants.appendLog("INVALID_REQ","processMemberRegistration setaddress"+listAddress.size());

        }catch (Exception e){
            HnppConstants.appendLog("INVALID_REQ","processMemberRegistration exception occured"+e.getMessage());

        }

        if(familyEventClient.getClient() == null || TextUtils.isEmpty(familyEventClient.getClient().getBaseEntityId()) ||
                familyEventClient.getClient().getAddresses().size() == 0){
            return null;
        }
        return familyEventClient;
    }
    public FamilyEventClient processUpdateMemberRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = HnppJsonFormUtils.processFamilyMemberForm(FamilyLibrary.getInstance().context().allSharedPreferences(), jsonString, familyBaseEntityId,Utils.metadata().familyMemberRegister.updateEventType);
        if (familyEventClient == null) {
            return null;
        }
        EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
        try{
            JSONObject familyJSON = eventClientRepository.getClientByBaseEntityId(familyBaseEntityId);
            String addessJson = familyJSON.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            Client familyClient = familyEventClient.getClient();

            familyClient.setAddresses(listAddress);
            HnppConstants.appendLog("INVALID_REQ","processUpdateMemberRegistration set address:"+listAddress.size());

        }catch (Exception e){
            HnppConstants.appendLog("INVALID_REQ","processUpdateMemberRegistration exception occured"+e.getMessage());

        }

        if(familyEventClient.getClient() == null || TextUtils.isEmpty(familyEventClient.getClient().getBaseEntityId()) ||
                familyEventClient.getClient().getAddresses().size() == 0){
            return null;
        }
       return familyEventClient;

    }
    private FamilyEventClient processRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = HnppJsonFormUtils.processFamilyMemberForm(FamilyLibrary.getInstance().context().allSharedPreferences(), jsonString, familyBaseEntityId,Utils.metadata().familyMemberRegister.registerEventType);
        if (familyEventClient == null) {
            return null;
        } else {
            this.updateWra(familyEventClient);
            return familyEventClient;
        }
    }


}
