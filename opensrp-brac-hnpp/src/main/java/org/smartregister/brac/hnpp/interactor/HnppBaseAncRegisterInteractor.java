package org.smartregister.brac.hnpp.interactor;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncRegisterContract;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;

public class HnppBaseAncRegisterInteractor extends BaseAncRegisterInteractor {

    protected BaseAncRegisterContract.Model model;
    @Override
    public void saveRegistration(final String jsonString, final boolean isEditMode, final BaseAncRegisterContract.InteractorCallBack callBack, final String table) {

        Runnable runnable = () -> {
            // save it
            String encounterType = "";
            boolean hasChildren = false;

            try {
                JSONObject form = new JSONObject(jsonString);
                encounterType = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

                if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {

                    saveRegistration(form.toString(), table);

                    String motherBaseId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject deliveryDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.DELIVERY_DATE);
                    hasChildren = StringUtils.isNotBlank(deliveryDate.optString(JsonFormUtils.VALUE));

                    JSONObject uniqueID = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.UNIQUE_ID);
                    if (StringUtils.isNotBlank(uniqueID.optString(JsonFormUtils.VALUE))) {
                        String childBaseEntityId = JsonFormUtils.generateRandomUUIDString();
                        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();
                        JSONObject pncForm = getModel().getFormAsJson(Constants.FORMS.PNC_CHILD_REGISTRATION, childBaseEntityId, getLocationID());

                        JSONObject familyIdObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.RELATIONAL_ID);
                        String familyBaseEntityId = familyIdObject.getString(JsonFormUtils.VALUE);
                        pncForm = JsonFormUtils.populatePNCForm(pncForm, fields, familyBaseEntityId);
                        HnppJsonFormUtils.processAttributesWithChoiceIDsForSave(fields);

                       // processChild(fields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId);
                        if (pncForm != null) {
                            saveRegistration(pncForm.toString(), EC_CHILD);
                            NCUtils.saveVaccineEvents(fields, childBaseEntityId);
                        }
                    }

                } else if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.ANC_REGISTRATION)) {

                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject lmp = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.LAST_MENSTRUAL_PERIOD);
                    boolean hasLmp = StringUtils.isNotBlank(lmp.optString(JsonFormUtils.VALUE));

                    if (!hasLmp) {
                        JSONObject eddJson = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "edd");
                        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy");

                        LocalDate lmpDate = dateTimeFormat.parseLocalDate(eddJson.optString(JsonFormUtils.VALUE)).plusDays(-280);
                        lmp.put(JsonFormUtils.VALUE, dateTimeFormat.print(lmpDate));
                    }

                    saveRegistration(form.toString(), table);
                } else {
                    saveRegistration(jsonString, table);
                }
            } catch (Exception e) {
                Timber.e(e);
            }

            String finalEncounterType = encounterType;
            boolean finalHasChildren = hasChildren;
            appExecutors.mainThread().execute(() -> {
                try {
                    callBack.onRegistrationSaved( isEditMode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };
        appExecutors.diskIO().execute(runnable);
    }
    protected String getLocationID() {
        return Context.getInstance().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
    }
    public BaseAncRegisterContract.Model getModel() {
        if (model == null)
            model = new BaseAncRegisterModel();
        return model;
    }
    private void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);

        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    @Override
    public void processPncChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        super.processPncChild(fields, allSharedPreferences, entityId, familyBaseEntityId, motherBaseId);
    }

    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, JsonFormUtils.formTag(allSharedPreferences), entityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.MOTHER, motherBaseId);
            JSONObject clientjson = new JSONObject(JsonFormUtils.gson.toJson(pncChild));
            pncChild.setAddresses(updateWithSSLocation(clientjson));

            AncLibrary.getInstance().getUniqueIdRepository().close(pncChild.getIdentifier(Constants.JSON_FORM_EXTRA.OPENSPR_ID));

            NCUtils.getSyncHelper().addClient(pncChild.getBaseEntityId(), clientjson);

        } catch (Exception e) {
            Timber.e(e);
        }
    }
    private static List<Address> updateWithSSLocation(JSONObject clientjson){
        try{
            String addessJson = clientjson.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            return listAddress;
        }catch (Exception e){

        }
        return new ArrayList<>();

    }
}



