package org.smartregister.unicef.dghs.model;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.GeoLocation;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyRegisterModel;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.unicef.dghs.utils.HnppJsonFormUtils.makeReadOnlyFields;
import static org.smartregister.util.JsonFormUtils.FIELDS;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

public class HnppFamilyRegisterModel extends BaseFamilyRegisterModel {


    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject form = getFormUtils().getFormJson(formName);
        if (form == null) {
            return null;
        }
        if(HnppConstants.isPALogin()){
            makeReadOnlyFields(form);
        }
        HnppJsonFormUtils.addValueAtJsonForm(form,"registration_date", HnppConstants.getTodayDate());
        HnppJsonFormUtils.updateFormWithWardName(form, HnppApplication.getGeoLocationRepository().getAllWard());
        return HnppJsonFormUtils.getFormAsJson(form, formName, entityId, currentLocationId);
    }

    @Override
    public List<FamilyEventClient> processRegistration(String jsonString) {
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject jobkect = jsonObject.getJSONObject("step1");
            JSONArray field = jobkect.getJSONArray(FIELDS);
            JSONObject blockIdIdObj = getFieldJSONObject(field, "block_id");
            String blockId = blockIdIdObj.getString("value");
            Log.v("HH_REGISTER","processRegistration:blockId:"+blockId);
            try{

                String hhid = jobkect.getString( "hhid");
                HouseholdIdRepository householdIdRepo = HnppApplication.getHNPPInstance().getHouseholdIdRepository();
                householdIdRepo.close(blockId,hhid);
            }catch (Exception e){

            }
            try{
                String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                JSONObject providerIdObj = getFieldJSONObject(field, "provider_id");
                providerIdObj.put("value",userName);

            }catch (Exception e){
                e.printStackTrace();

            }
            List<FamilyEventClient> familyEventClientList = new ArrayList<>();

            if(jsonObject.has(org.smartregister.family.util.JsonFormUtils.STEP2)){
                JSONObject stepTwo = jsonObject.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP2);
                JSONArray jsonArray2 = stepTwo.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray2.length(); i++) {
                    field.put(jsonArray2.getJSONObject(i));
                }

//                processAttributesWithChoiceIDsForSave(jsonArray2);
                jsonObject.remove(org.smartregister.family.util.JsonFormUtils.STEP2);
            }

            processAttributesWithChoiceIDsForSave(field);
            FamilyEventClient familyEventClient = HnppJsonFormUtils.processFamilyUpdateForm(Utils.context().allSharedPreferences(), jsonObject.toString());
            if (familyEventClient == null) {
                return familyEventClientList;
            }
            String headUniqueId = familyEventClient.getClient().getIdentifier(Utils.metadata().uniqueIdentifierKey);
            if (StringUtils.isNotBlank(headUniqueId)) {
                //String familyUniqueId = headUniqueId ;//+ Constants.IDENTIFIER.FAMILY_SUFFIX;
                familyEventClient.getClient().addIdentifier(Utils.metadata().uniqueIdentifierKey, headUniqueId);
            }
            //}

            // Update the family head and primary caregiver
            Client familyClient = familyEventClient.getClient();
            familyClient.addRelationship(Utils.metadata().familyRegister.familyHeadRelationKey, familyEventClient.getClient().getBaseEntityId());
            familyClient.addRelationship(Utils.metadata().familyRegister.familyCareGiverRelationKey, familyEventClient.getClient().getBaseEntityId());
            List<Address> listAddress = new ArrayList<>();
            GeoLocation selectedLocation = HnppApplication.getGeoLocationRepository().getLocationByBlock(blockId);
            if(selectedLocation == null){
                return null;
            }else{
                listAddress.add(GeoLocationHelper.getInstance().getSSAddress(selectedLocation));
            }
            familyClient.setAddresses(listAddress);

            GeoLocationHelper.getInstance().addGeolocationIds(selectedLocation,familyClient);
            Event event = familyEventClient.getEvent();
            event.setIdentifiers(GeoLocationHelper.getInstance().getGeoIdentifier(selectedLocation));
            familyEventClient.setClient(familyClient);
            familyEventClientList.add(familyEventClient);

            if(listAddress.size() == 0){
                return null;
            }
            return familyEventClientList;

        }catch (Exception e){
            Timber.e(e);
        }
        return null;

    }
    private static JSONArray processAttributesWithChoiceIDsForSave(JSONArray fields) {
        for (int i = 0; i < fields.length(); i++) {
            try {
                JSONObject fieldObject = fields.getJSONObject(i);
//                if(fieldObject.has("openmrs_entity")){
//                    if(fieldObject.getString("openmrs_entity").equalsIgnoreCase("person_attribute")){
                if (fieldObject.has("openmrs_choice_ids")&&fieldObject.getJSONObject("openmrs_choice_ids").length()>0) {
                    if (fieldObject.has("value")) {
                        String valueEntered = fieldObject.getString("value");
                        fieldObject.put("value", fieldObject.getJSONObject("openmrs_choice_ids").get(valueEntered));
                    }
                }
//                    }
//                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }
        return fields;
    }

}
