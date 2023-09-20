package org.smartregister.brac.hnpp.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static com.vijay.jsonwizard.constants.JsonFormConstants.STEP1;
import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.Constants.TABLES.EC_CHILD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HouseHoldFormTypeFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberFragment;
import org.smartregister.brac.hnpp.fragment.MemberListDialogFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.listener.OnUpdateMemberList;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class HouseHoldVisitActivity extends CoreFamilyProfileActivity{
    Button nextButton;
    int currentFragmentIndex = 0;
    List<Fragment> fragmentList = Arrays.asList(new HouseHoldFormTypeFragment(), new HouseHoldMemberFragment(), new HouseHoldMemberDueFragment());
    List<String> fragmentTagList = Arrays.asList(HouseHoldFormTypeFragment.TAG, HouseHoldMemberFragment.TAG, HouseHoldMemberDueFragment.TAG);
    public String moduleId;
    public String houseHoldId;
    HnppFamilyProfileModel model;
    public MigrationSearchContentData migrationSearchContentData;
    private Handler handler;
    boolean isProcessing = false;
    Dialog dialog;
    public ArrayList<String> memberListJson = new ArrayList<>();
    public ArrayList<String> removedMemberListJson = new ArrayList<>();
    public ArrayList<String> migratedMemberListJson = new ArrayList<>();

    public ArrayList<String> pregancyMemberListJson = new ArrayList<>();
    public static ArrayList<String> deletedMembersBaseEntityId = new ArrayList<>();

    OnUpdateMemberList onUpdateMemberList;
    OnUpdateMemberList onUpdateMemberCount;

    public void listenMemberUpdateStatus(OnUpdateMemberList onUpdateMemberList){
        this.onUpdateMemberList = onUpdateMemberList;
    }

    public void listenMemberUpdateStatusFromFrag(OnUpdateMemberList onUpdateMemberList){
        this.onUpdateMemberCount = onUpdateMemberList;
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_hh_visit);
        resetData();
        initializePresenter();

        nextButton = findViewById(R.id.next_button);

        setupFragment(fragmentList.get(currentFragmentIndex),fragmentTagList.get(currentFragmentIndex));
        currentFragmentIndex = 1;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentFragmentIndex < 3) {
                    setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));

                    //change text is user on last fragment
                    if(currentFragmentIndex == fragmentList.size()-1){
                        nextButton.setText("Submit");
                    }

                    currentFragmentIndex++;
                }
            }
        });

    }

    private void resetData() {
        memberListJson.clear();
        removedMemberListJson.clear();
        migratedMemberListJson.clear();
        pregancyMemberListJson.clear();
    }

    /**
     * fragment transaction
     * @param fragment for rendering
     * @param tag to add backstack
     */
    private void setupFragment(Fragment fragment,String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getFamilyBaseEntityId());
        bundle.putString(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
        bundle.putString(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
        fragment.setArguments(bundle);

        this.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(tag)
                .add(R.id.hh_visit_container,fragment)
                .commit();
    }

    @Override
    protected void onResumption() {

    }

    @Override
    protected void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId),houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);

    }

    @Override
    protected void refreshList(Fragment item) {

    }

    @Override
    protected Class<? extends CoreFamilyRemoveMemberActivity> getFamilyRemoveMemberClass() {
        return FamilyRemoveMemberActivity.class;
    }

    @Override
    protected Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass() {
        return null;
    }



    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        moduleId = getIntent().getStringExtra(HnppConstants.KEY.MODULE_ID);
        houseHoldId = getIntent().getStringExtra(DBConstants.KEY.UNIQUE_ID);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);

        model = new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId);
        presenter = new FamilyProfilePresenter(this, model,houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }



    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    public void onBackPressed() {
       try{
           super.onBackPressed();
           currentFragmentIndex--;
           if(currentFragmentIndex == fragmentList.size()-1){
               nextButton.setText("Submit");
           }
       }catch (Exception e){
           finish();
       }
    }

    @Override
    public void errorOccured(String message) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == MemberListDialogFragment.REQUEST_CODE){
            MemberTypeEnum memberTypeEnum = (MemberTypeEnum) data.getSerializableExtra(MemberListDialogFragment.MEMBER_TYPE);
            if(memberTypeEnum == MemberTypeEnum.DEATH){
                Member member = (Member) data.getParcelableExtra(MemberListDialogFragment.MEMBER);
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                try {
                    assert member != null;
                    confirmRemove(form,member,memberTypeEnum);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else if(memberTypeEnum == MemberTypeEnum.MIGRATION){
                Member member = (Member) data.getParcelableExtra(MemberListDialogFragment.MEMBER);
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                try {
                    assert member != null;
                    confirmRemove(form,member,memberTypeEnum);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else if(memberTypeEnum == MemberTypeEnum.ELCO){
                pregancyMemberListJson.add(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
                onUpdateMemberCount.update(true);
                onUpdateMemberList.update(true);
            }

        }
        /*else if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                memberListJson.add(jsonString);

                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                    String[] sss =  HnppJsonFormUtils.getHouseholdIdModuleIdFromForm(form);
                    houseHoldId = sss[0];
                    moduleId = sss[1];
                    ((FamilyProfilePresenter)presenter).updateHouseIdAndModuleId(houseHoldId);
                    model.updateHouseIdAndModuleId(houseHoldId,moduleId );
                    presenter().updateFamilyRegister(jsonString);
                    presenter().verifyHasPhone();
                }
                else {
                    if(TextUtils.isEmpty(familyBaseEntityId)){
                        Toast.makeText(this,"familyBaseEntityId no found",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    String[] generatedString;
                    String title;
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
                    String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                    if (encounterType.equals(HnppConstants.EventType.CHILD_REGISTRATION)) {
                        generatedString = HnppJsonFormUtils.getValuesFromChildRegistrationForm(form);
                        title = String.format(getString(R.string.dialog_confirm_save_child),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    }else {
                        generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
                        title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    }

                    HnppConstants.showSaveFormConfirmationDialog(this, title, new OnDialogOptionSelect() {
                        @Override
                        public void onClickYesButton() {

                            try{
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field,true);
                                processForm(encounterType,formWithConsent.toString());
                            }catch (JSONException je){
                                je.printStackTrace();
                            }
                        }

                        @Override
                        public void onClickNoButton() {
                            try{
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field,false);
                                processForm(encounterType,formWithConsent.toString());
                            }catch (JSONException je){
                                je.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Timber.e(e);
            }
            HnppConstants.isViewRefresh = true;
        }*/

        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){
            if(isProcessing) return;
            AtomicInteger isSave = new AtomicInteger(2);
            showProgressDialog(R.string.please_wait_message);

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
            String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();

            processAndSaveVisitForm(jsonString,formSubmissionId,visitId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer aInteger) {
                            isSave.set(aInteger);
                            Log.v("SAVE_VISIT","onError>>"+aInteger);
                        }

                        @Override
                        public void onError(Throwable e) {
                            hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            Log.d("visitCalledCompleted","true");
                            if(isSave.get() == 1){
                                hideProgressDialog();
                                showServiceDoneDialog(1);
                            }else if(isSave.get() == 3){
                                hideProgressDialog();
                                showServiceDoneDialog(3);
                            }else {
                                hideProgressDialog();
                                isProcessing = false;
                                //showServiceDoneDialog(false);
                            }

                            onUpdateMemberCount.update(true);
                        }
                    });

        }
    }


    Observable<String> executeQuery( Intent data){
        return  Observable.create(e->{
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);

            String eventType = processVisitFormAndSave(jsonString);

            SQLiteDatabase database = CoreChwApplication.getInstance().getRepository().getWritableDatabase();
            //database.execSQL(sql1);
            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE ec_anc_register.base_entity_id IN " +
                    "(select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = 0) ";

            database.execSQL(sql);
            e.onNext(eventType);
        });
    }

    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject jsonObject = org.smartregister.chw.anc.util.JsonFormUtils.getFormAsJson(formName);
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
        Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
        visit.setPreProcessedJson(new Gson().toJson(baseEvent));
        try{
            // visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), HnppConstants.EVENT_TYPE.SS_INFO, visit.getDate()));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        }catch (Exception e){
            e.printStackTrace();
        }
        FormParser.processVisitLog(visit);
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    private void processChild(JSONArray fields, AllSharedPreferences allSharedPreferences, String entityId, String familyBaseEntityId, String motherBaseId) {
        try {
            Client pncChild = org.smartregister.util.JsonFormUtils.createBaseClient(fields, org.smartregister.chw.anc.util.JsonFormUtils.formTag(allSharedPreferences), entityId);
            if(familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                pncChild.setLastName(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
            }
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.FAMILY, familyBaseEntityId);
            pncChild.addRelationship(org.smartregister.chw.anc.util.Constants.RELATIONSHIP.MOTHER, motherBaseId);
            JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(pncChild));
            EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
            SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
            JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
            pncChild.setAddresses(updateWithSSLocation(dsasd));
            clientjson.put("addresses",dsasd.getJSONArray("addresses"));
            AncLibrary.getInstance().getUniqueIdRepository().close(pncChild.getIdentifier(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.OPENSPR_ID));

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


    private String processVisitFormAndSave(String jsonString){
        String encounter_type = "";
        long startTime = System.currentTimeMillis();
        try {
            JSONObject form = new JSONObject(jsonString);
            HnppJsonFormUtils.setEncounterDateTime(form);

            JSONObject step1 = form.getJSONObject(STEP1);
            encounter_type = form.optString(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);
            if(encounter_type.equalsIgnoreCase(CoreConstants.EventType.PREGNANCY_OUTCOME)){
                if(!familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                    JSONArray fields = step1.getJSONArray(FIELDS);
                    updateUniqueId(fields);
                }
                try{

                    String motherBaseId = form.optString(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    String gender = org.smartregister.util.JsonFormUtils.getFieldValue(fields,"gender");

                    JSONObject uniqueID = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, org.smartregister.chw.anc.util.DBConstants.KEY.UNIQUE_ID);
                    if (StringUtils.isNotBlank(uniqueID.optString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE))) {
                        String childBaseEntityId = org.smartregister.chw.anc.util.JsonFormUtils.generateRandomUUIDString();
                        AllSharedPreferences allSharedPreferences = ImmunizationLibrary.getInstance().context().allSharedPreferences();
                        JSONObject pncForm = getFormAsJson(org.smartregister.chw.anc.util.Constants.FORMS.PNC_CHILD_REGISTRATION, childBaseEntityId, getLocationID());

                        JSONObject familyIdObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, org.smartregister.chw.anc.util.DBConstants.KEY.RELATIONAL_ID);
                        String familyBaseEntityId = familyIdObject.getString(org.smartregister.chw.anc.util.JsonFormUtils.VALUE);
                        pncForm = org.smartregister.chw.anc.util.JsonFormUtils.populatePNCForm(pncForm, fields, familyBaseEntityId);
                        HnppJsonFormUtils.processAttributesWithChoiceIDsForSave(fields);
                        HnppJsonFormUtils.updateProviderIdAtClient(fields,familyBaseEntityId);
                        if(!StringUtils.isEmpty(gender)){
                            if (pncForm != null) {
                                if(familyName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)){
                                    processChild(fields, allSharedPreferences, childBaseEntityId, motherBaseId, motherBaseId);
                                    saveRegistration(pncForm.toString(), "ec_guest_member");
                                }else{
                                    processChild(fields, allSharedPreferences, childBaseEntityId, familyBaseEntityId, motherBaseId);
                                    saveRegistration(pncForm.toString(), EC_CHILD);
                                    NCUtils.saveVaccineEvents(fields, childBaseEntityId);
                                }

                            }
                        }

                    }
                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);
                }catch (Exception e){
                    e.printStackTrace();

                }



            }
            else if (encounter_type.equalsIgnoreCase(org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_REGISTRATION)) {
                try{
                    saveRegistration(form.toString(), HnppConstants.TABLE_NAME.ANC_MEMBER);
                }catch (Exception e){

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return encounter_type;
    }

    private String getLocationID() {
       return Context.getInstance().allSharedPreferences().getPreference("CURRENT_LOCATION_ID");
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

    public void confirmRemove(final String formStr, Member currentMember, MemberTypeEnum memberTypeEnum) throws JSONException {
        JSONObject form = new JSONObject(formStr);
        String memberName = currentMember.getName();
        if (StringUtils.isNotBlank(memberName) && getFragmentManager() != null) {
            String title ="";
            JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
            JSONObject removeReasonObj = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, "remove_reason");
            try{
                String value = removeReasonObj.getString(CoreJsonFormUtils.VALUE);
                if(value.equalsIgnoreCase("মৃত্যু নিবন্ধন")){
                    title = String.format(getString(R.string.confirm_remove_text), memberName);
                }else if(value.equalsIgnoreCase("স্থানান্তর")){
                    title = String.format(getString(R.string.confirm_migrate_text), memberName);
                }else {
                    title = String.format(getString(R.string.confirm_other_text), memberName);
                }
            }catch (Exception e){

            }
           /* FamilyRemoveMemberConfirmDialog dialog = FamilyRemoveMemberConfirmDialog.newInstance(title);
            dialog.show(this.getSupportFragmentManager(), FamilyRemoveMemberFragment.DIALOG_TAG);*/

            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this).create();
            View dialogView = LayoutInflater.from(this).inflate(R.layout.family_remove_member_confrim_dialog_fragment,null);
            alertDialog.setView(dialogView);
            CustomFontTextView remove = dialogView.findViewById(R.id.remove);
            CustomFontTextView cancel = dialogView.findViewById(R.id.cancel);

            ((TextView) dialogView.findViewById(R.id.message)).setText(title);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //onUpdateMemberList.update(false);
                    alertDialog.dismiss();
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());
                        String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                        String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                        Visit visit =  HnppJsonFormUtils.saveVisit(false,false,false,"", currentMember.getBaseEntityId(), type, jsonStrings, "",formSubmissionId,visitId);
                        if(visit !=null){
                            HnppHomeVisitIntentService.processVisits();
                            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                        }

                        if(memberTypeEnum == MemberTypeEnum.DEATH){
                            removedMemberListJson.add(formStr);
                        }else if(memberTypeEnum.name().equals( MemberTypeEnum.MIGRATION.name())){
                            migratedMemberListJson.add(formStr);
                        }

                        onUpdateMemberList.update(true);
                        onUpdateMemberCount.update(true);

                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    alertDialog.dismiss();

                    /*Intent intent = new Intent(HouseHoldVisitActivity.this, FamilyRegisterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/
                }
            });

            alertDialog.show();

            /*dialog.setOnRemove(() -> {
                //getPresenter().processRemoveForm(form);
                try{
                    String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                    type = HnppJsonFormUtils.getEncounterType(type);
                    Map<String, String> jsonStrings = new HashMap<>();
                    jsonStrings.put("First",form.toString());
                    String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    Visit visit =  HnppJsonFormUtils.saveVisit(false,false,false,"", currentMember.getBaseEntityId(), type, jsonStrings, "",formSubmissionId,visitId);
                    if(visit !=null){
                        HnppHomeVisitIntentService.processVisits();
                        VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                    }
                }catch (Exception e){
                    e.printStackTrace();

                }


                Intent intent = new Intent(this, FamilyRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });*/
           /* dialog.setOnRemoveActivity(() -> {
                if (this != null) {
                    this.finish();
                }
            });*/
        }
    }

    private Observable<Integer> processAndSaveVisitForm(String jsonString, String formSubmissionId, String visitId){
        return  Observable.create(e-> {
            if(TextUtils.isEmpty(familyBaseEntityId)){
                e.onNext(2);
            }
            Map<String, String> jsonStrings = new HashMap<>();
            //jsonStrings.put("First",jsonString);
            try {
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                jsonStrings.put("First",form.toString());

                String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);

                Visit visit = HnppJsonFormUtils.saveVisit(false,false,false,"", familyBaseEntityId, type, jsonStrings, "",formSubmissionId,visitId);
                if(visit!=null && !visit.getVisitId().equals("0")){
                    HnppHomeVisitIntentService.processVisits();
                    FormParser.processVisitLog(visit);
                    //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                    e.onNext(1);
                    e.onComplete();
                }else if(visit!=null && visit.getVisitId().equals("0")){
                    e.onNext(3);
                    e.onComplete();
                }else{
                    e.onNext(2);
                    e.onComplete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                e.onNext(2);
                e.onComplete();
            }

        });
    }

    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog!=null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":isSuccess==3?"সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        android.widget.Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessing = false;
                //if(isSuccess){

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onActivityResult(0,0,null);
                            HnppConstants.isViewRefresh = true;
                            presenter().refreshProfileView();
                        }
                    },2000);
                    // }

            }
        });
        dialog.show();

    }

    private void processForm(String encounter_type, String jsonString){
        if (encounter_type.equals(CoreConstants.EventType.CHILD_REGISTRATION)) {

            presenter().saveChildForm(jsonString, false);

        } else if (encounter_type.equals(Utils.metadata().familyMemberRegister.registerEventType)) {

            String careGiver = presenter().saveChwFamilyMember(jsonString);
            if(TextUtils.isEmpty(careGiver) || TextUtils.isEmpty(familyBaseEntityId)){
                Toast.makeText(this,getString(R.string.address_not_found),Toast.LENGTH_LONG).show();
                return;
            }
            if (presenter().updatePrimaryCareGiver(getApplicationContext(), jsonString, familyBaseEntityId, careGiver)) {
                setPrimaryCaregiver(careGiver);
                refreshPresenter();
                refreshMemberFragment(careGiver, null);
            }

            presenter().verifyHasPhone();
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(houseHoldId == null){
            new AlertDialog.Builder(this).setMessage(R.string.household_id_null_message)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }).show();
            return;
        }
        try{
            if(jsonForm.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)){
                if(HnppConstants.isPALogin()){
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                Intent intent = new Intent(this, Utils.metadata().familyFormActivity);
                intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                Form form = new Form();
                if(!HnppConstants.isReleaseBuild()){
                    form.setActionBarBackground(R.color.test_app_color);

                }else{
                    form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                }
                form.setWizard(false);

                intent.putExtra("form", form);
                this.startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
            }else{
                if(HnppConstants.isPALogin()){
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                    @Override
                    public void onPost(double latitude, double longitude) {
                        try{
                            Intent intent = new Intent(HouseHoldVisitActivity.this, Utils.metadata().familyMemberFormActivity);
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                            intent.putExtra("json", jsonForm.toString());
                            intent.putExtra("json", jsonForm.toString());
                            Form form = new Form();
                            if(!HnppConstants.isReleaseBuild()){
                                form.setActionBarBackground(R.color.test_app_color);

                            }else{
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }
                            form.setWizard(false);
                            intent.putExtra("form", form);
                            startActivityForResult(intent, 2244);
                        }catch (Exception e){

                        }

                    }
                });

            }
        }catch (Exception e){

        }
    }

    private void openAsReadOnlyMode(JSONObject jsonForm){
        Intent intent = new Intent(this, HnppFormViewActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setWizard(false);
        if(!HnppConstants.isReleaseBuild()){
            form.setActionBarBackground(R.color.test_app_color);

        }else{
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

        }
        form.setHideSaveLabel(true);
        form.setSaveLabel("");
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
        if (this != null) {
            this.startActivity(intent);
        }
    }

    @Override
    public void refreshMemberList(FetchStatus fetchStatus) {
        ///
    }

}