package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

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
import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.listener.HnppFamilyBottomNavListener;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncRegisterInteractor;
import org.smartregister.chw.anc.model.BaseAncRegisterModel;
import org.smartregister.chw.anc.presenter.BaseAncRegisterPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.util.FormUtils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;


public class HnppAncRegisterActivity extends CoreAncRegisterActivity {

    private HnppVisitLogRepository visitLogRepository;
    private static String motherName;
    private static String baseEntityId;
    private static double latitude;
    private static double longitude;


    public static void startHnppAncRegisterActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name, String moName,double lat,double longi) {
        Intent intent = new Intent(activity, org.smartregister.brac.hnpp.activity.HnppAncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        motherName = moName;
        latitude = lat;
        longitude = longi;
        baseEntityId = memberBaseEntityID;
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
                        Intent a = new Intent(Intent.ACTION_MAIN);
                        a.addCategory(Intent.CATEGORY_HOME);
                        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(a);
                        finish();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // baseEntityId = getIntent().getStringExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        if(ssLocationForms.size() > 0){
            boolean simPrintsEnable = ssLocationForms.get(0).simprints_enable;
            if(simPrintsEnable){
                findViewById(R.id.simprints_identity).setVisibility(View.VISIBLE);
                findViewById(R.id.simprints_identity).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HnppAncRegisterActivity.this, SimprintsIdentityActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(org.smartregister.chw.core.R.anim.slide_in_up, org.smartregister.chw.core.R.anim.slide_out_up);
                    }
                });
            }else{
                findViewById(R.id.simprints_identity).setVisibility(View.GONE);
            }
        }
    }
    @Override
    protected void initializePresenter() {
        presenter = new BaseAncRegisterPresenter(this, new BaseAncRegisterModel(), new BaseAncRegisterInteractor());
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        try {
            visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            ANCRegister ancRegister = null;
            if (form_name != null && form_name.equals(HnppConstants.JSON_FORMS.ANC_FORM)) {
                ancRegister = visitLogRepository.getLastANCRegister(getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID));
            }
            try{
                HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
            }catch (Exception e){
                e.printStackTrace();
            }
            Form form = new Form();
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            form.setWizard(false);
            Intent intent = new Intent(this, HnppAncJsonFormActivity.class);

            JSONObject stepOne = jsonForm.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            updateFormField(jsonArray, DBConstants.KEY.UNIQUE_ID, unique_id);
            updateFormField(jsonArray, DBConstants.KEY.TEMP_UNIQUE_ID, unique_id);
            updateFormField(jsonArray, "temp_name", motherName+" এর বাবু");
            updateMinDate(jsonArray);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAM_NAME, familyName);
            updateFormField(jsonArray, CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER, phone_number);
            updateFormField(jsonArray, org.smartregister.family.util.DBConstants.KEY.RELATIONAL_ID, familyBaseEntityId);

            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
//            updateWithSSLocation();

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

            startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void updateMinDate(JSONArray jsonArray) throws JSONException{
       try{
           JSONObject min_date = CoreJsonFormUtils.getFieldJSONObject(jsonArray, "delivery_date");
           String lmp = FormApplicability.getLmp(baseEntityId);
           //int days = CoreJsonFormUtils.getDayFromDate(lmp);
           int days = getDaysFromDate(lmp);
           min_date.put("min_date", "today-" + days + "d");
       }catch (Exception e){

       }

    }

    private int getDaysFromDate(String date){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date past,now;
        int days=0;
        try {
            past = format.parse(date);
            now = new Date();
            days = (int) TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
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
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            finish();
        }
        JSONObject form = null;
        String encounter_type = "";
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {
//            process the form

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                form = new JSONObject(jsonString);
                JSONObject step1 = form.getJSONObject("step1");
                String baseEnityId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
                if (encounter_type.equalsIgnoreCase("Pregnancy Outcome")) {
                    if (!familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                        JSONArray fields = step1.getJSONArray("fields");
                        updateUniqueId(fields);
                    }

                }

                if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)) {
                    //ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.PNC_HOME_VISIT, new Date());
                } else if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.ANC_HOME_VISIT)) {
                    //ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.ANC_HOME_VISIT, new Date());
                } else if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.PREGNANCY_OUTCOME)) {
                    if (!familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                        HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppAncRegisterActivity.this, baseEnityId, "",
                                HnppConstants.JSON_FORMS.PNC_FORM, null, familyBaseEntityId, familyName);
                    }

                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }
        if (form != null) {
            data.putExtra(Constants.JSON_FORM_EXTRA.JSON, form.toString());
            if (encounter_type.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {
                try {
                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);

                    String motherBaseId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    String gender = org.smartregister.util.JsonFormUtils.getFieldValue(fields, "gender");

                    JSONObject uniqueID = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.UNIQUE_ID);
                    if (StringUtils.isNotBlank(uniqueID.optString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE))) {
                        String childBaseEntityId = org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString();
                        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();
                        JSONObject pncForm = getFormAsJson(Constants.FORMS.PNC_CHILD_REGISTRATION, childBaseEntityId, getLocationID());

                        JSONObject familyIdObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.RELATIONAL_ID);
                        String familyBaseEntityId = familyIdObject.getString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE);

                        if (!StringUtils.isEmpty(gender)) {
                            String dob = org.smartregister.util.JsonFormUtils.getFieldValue(fields, "dob");
                            pncForm = org.smartregister.chw.anc.util.JsonFormUtils.populatePNCForm(pncForm, fields, familyBaseEntityId,motherBaseId,childBaseEntityId,dob,"");
                            HnppJsonFormUtils.processAttributesWithChoiceIDsForSave(fields);
                            if (pncForm != null) {
                                if (familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                                    processChild(fields, allSharedPreferences, childBaseEntityId, motherBaseId, motherBaseId);

                                    saveRegistration(pncForm.toString(), "ec_guest_member");
                                } else {
                                    processChild(fields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId);

                                    saveRegistration(pncForm.toString(), EC_CHILD);
                                    //TODO need_change
                                   // NCUtils.saveVaccineEvents(fields, childBaseEntityId);
                                }

                            }
                        }

                    }
                } catch (Exception e) {

                }

//                if(JobManager.instance().getAllJobRequestsForTag(HnppPncCloseJob.TAG).isEmpty()){
                HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
//                }


            } else {
                if (encounter_type.equalsIgnoreCase(Constants.EVENT_TYPE.ANC_REGISTRATION)) {
                    try {
                        saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_MEMBER);
                    } catch (Exception e) {

                    }
                }
                //super.onActivityResult(requestCode, resultCode, data);
            }
            if (familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                Intent intent = new Intent();
                intent.putExtra("event_type", HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
                setResult(RESULT_OK, intent);
                finish();
            }

        }


    }

    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = FormUtils.getInstance(this).getFormJson(formName);
        org.smartregister.chw.anc.util.JsonFormUtils.getRegistrationForm(jsonObject, entityId, currentLocationId);

        return jsonObject;
    }
    private void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);


        String visitID ="";
        if(!TextUtils.isEmpty(baseEvent.getEventId())){
            visitID = baseEvent.getEventId();
        }else{
            visitID = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
        }
        Log.v("ANC_HISTORY","visitId:"+visitID);

        Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
        visit.setPreProcessedJson(new Gson().toJson(baseEvent));
        try{
           // visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), HnppConstants.EVENT_TYPE.SS_INFO, visit.getDate()));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        }catch (Exception e){
            e.printStackTrace();
        }
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }
    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            if(familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                pncChild.setLastName(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
            }
            pncChild.addRelationship(Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(Constants.RELATIONSHIP.MOTHER, motherBaseId);
            JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
            SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
            JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
            pncChild.setAddresses(updateWithSSLocation(dsasd));
            clientjson.put("addresses",dsasd.getJSONArray("addresses"));
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
