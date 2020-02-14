package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HnppAncRegisterFragment;
import org.smartregister.brac.hnpp.interactor.HnppBaseAncRegisterInteractor;
import org.smartregister.brac.hnpp.listener.HnppFamilyBottomNavListener;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.presenter.HnppBaseAncRegisterPresenter;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;


public class HnppAncRegisterActivity extends CoreAncRegisterActivity {

    private HnppVisitLogRepository visitLogRepository;

    public static void startHnppAncRegisterActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, org.smartregister.brac.hnpp.activity.HnppAncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        unique_id = uniqueId;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.exit_app_message))
                .setTitle(getString(R.string.exit_app_title)).setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
    @Override
    protected void initializePresenter() {
        presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new HnppBaseAncRegisterInteractor());
    }
    @Override
    public void startFormActivity(JSONObject jsonForm) {

        try {
            visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            ANCRegister ancRegister = null;
            if (form_name != null && form_name.equals(HnppConstants.JSON_FORMS.ANC_FORM)) {
                ancRegister = visitLogRepository.getLastANCRegister(getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID));
            }
            Form form = new Form();
            form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
            form.setWizard(false);
            Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);

            JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormField(jsonArray, DBConstants.KEY.UNIQUE_ID, unique_id);
            updateFormField(jsonArray, DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAM_NAME, familyName);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            updateFormField(jsonArray, org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);
//            if (ancRegister != null) {
//                updateEncounterType(jsonForm);
//                updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.LAST_MENSTRUAL_PERIOD, ancRegister.getLastMenstrualPeriod());
//                updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.EDD, ancRegister.getEDD());
//                updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.NO_PREV_PREG, ancRegister.getNoPrevPreg());
//                updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.NO_SURV_CHILDREN, ancRegister.getNoSurvChildren());
//                updateFormField(jsonArray, HnppConstants.ANC_REGISTER_COLUMNS.HEIGHT, ancRegister.getHEIGHT());
//                form.setHideSaveLabel(true);
//                intent = new Intent(this, HnppFormViewActivity.class);
//            }
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
//            updateWithSSLocation();

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateEncounterType(JSONObject jsonForm) {
        try {
            jsonForm.put("encounter_type", HnppConstants.EVENT_TYPE.UPDATE_ANC_REGISTRATION);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFormField(JSONArray formFieldArrays, String formFeildKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFeildKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_register);
        bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);
        bottomNavigationView.setOnNavigationItemSelectedListener(new HnppFamilyBottomNavListener(this, bottomNavigationView));
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new HnppAncRegisterFragment();
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        this.finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            finish();
        }
        JSONObject form = null;
        String encounter_type ="";
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {
//            process the form

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                form = new JSONObject(jsonString);
                JSONObject step1 = form.getJSONObject("step1");
                String baseEnityId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
                if(encounter_type.equalsIgnoreCase("Pregnancy Outcome")){
                    JSONArray fields = step1.getJSONArray("fields");
                    updateUniqueId(fields);
                }

                if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)) {
                    //ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.PNC_HOME_VISIT, new Date());
                } else if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.ANC_HOME_VISIT)) {
                    //ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.ANC_HOME_VISIT, new Date());
                } else if(encounter_type.equalsIgnoreCase(CoreConstants.EventType.PREGNANCY_OUTCOME)){
                    HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppAncRegisterActivity.this, baseEnityId, "",
                            HnppConstants.JSON_FORMS.PNC_FORM, null, familyBaseEntityId, familyName);
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
        if(form!=null){
            data.putExtra(Constants.JSON_FORM_EXTRA.JSON,form.toString());
            if (encounter_type.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {
                try{
                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);

                    String motherBaseId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject deliveryDate = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.DELIVERY_DATE);

                    JSONObject uniqueID = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.UNIQUE_ID);
                    if (StringUtils.isNotBlank(uniqueID.optString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE))) {
                        String childBaseEntityId = org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString();
                        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();
                        JSONObject pncForm =getFormAsJson(Constants.FORMS.PNC_CHILD_REGISTRATION, childBaseEntityId, getLocationID());

                        JSONObject familyIdObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.RELATIONAL_ID);
                        String familyBaseEntityId = familyIdObject.getString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE);
                        pncForm = org.smartregister.chw.anc.util.JsonFormUtils.populatePNCForm(pncForm, fields, familyBaseEntityId);
                        HnppJsonFormUtils.processAttributesWithChoiceIDsForSave(fields);

                        processChild(fields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId);
                        if (pncForm != null) {
                            saveRegistration(pncForm.toString(), EC_CHILD);
                            NCUtils.saveVaccineEvents(fields, childBaseEntityId);
                        }
                    }
                }catch (Exception e){

                }




            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }

        }



    }
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = org.smartregister.chw.anc.util.JsonFormUtils.getFormAsJson(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }
    private void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);

        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }
    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.MOTHER, motherBaseId);
            JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            pncChild.setAddresses(updateWithSSLocation(clientjson));
            EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
            SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
            JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
            pncChild.setAddresses(updateWithSSLocation(dsasd));

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
    @Override
    public void onRegistrationSaved(boolean isEdit) {

    }

    public static void updateUniqueId(JSONArray fields){
        boolean has_delivery_date = false;
        for(int i=0;i<fields.length();i++){
            try {
                JSONObject object = fields.getJSONObject(i);
                if("delivery_date".equalsIgnoreCase(object.getString("key"))){

                    if(object.has("value")&&!StringUtils.isEmpty(object.getString("value"))){
                        has_delivery_date = true;
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!has_delivery_date){
            for(int i=0;i<fields.length();i++){
                try {
                    JSONObject object = fields.getJSONObject(i);
                    if("unique_id".equalsIgnoreCase(object.getString("key"))){

                        object.put("value","");
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
