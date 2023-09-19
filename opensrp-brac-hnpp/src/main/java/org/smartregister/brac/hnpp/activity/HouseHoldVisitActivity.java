package org.smartregister.brac.hnpp.activity;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MemberListContract;
import org.smartregister.brac.hnpp.fragment.FamilyRemoveMemberFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldFormTypeFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberFragment;
import org.smartregister.brac.hnpp.fragment.MemberListDialogFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.brac.hnpp.presenter.MemberHistoryPresenter;
import org.smartregister.brac.hnpp.presenter.MemberListPresenter;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberTypeEnum;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.chw.core.fragment.FamilyRemoveMemberConfirmDialog;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

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
    public static ArrayList<String> memberListJson = new ArrayList<>();
    public static ArrayList<String> removedMemberListJson = new ArrayList<>();
    public static ArrayList<String> migratedMemberListJson = new ArrayList<>();
    public static ArrayList<String> pregancyMemberListJson = new ArrayList<>();
    public static ArrayList<String> deletedMembersBaseEntityId = new ArrayList<>();



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
        Log.d("dataaaaaaaa",resultCode+" "+requestCode+" "+data.getParcelableExtra(MemberListDialogFragment.MEMBER)+"  "+data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
        if(resultCode == Activity.RESULT_OK && requestCode == MemberListDialogFragment.REQUEST_CODE){
            MemberTypeEnum memberTypeEnum = (MemberTypeEnum) data.getSerializableExtra(MemberListDialogFragment.MEMBER_TYPE);
            Member member = (Member) data.getParcelableExtra(MemberListDialogFragment.MEMBER);
            if(memberTypeEnum == MemberTypeEnum.DEATH){
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                HouseHoldVisitActivity.removedMemberListJson.add(form);
                try {
                    confirmRemove(form,member);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else if(memberTypeEnum == MemberTypeEnum.MIGRATION){
                String form = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                HouseHoldVisitActivity.migratedMemberListJson.add(form);
                try {
                    confirmRemove(form,member);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else if(memberTypeEnum == MemberTypeEnum.ELCO){
                HouseHoldVisitActivity.pregancyMemberListJson.add(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
            }

        }
        else if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {

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
        }

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
                        }
                    });
        }
    }

    public void confirmRemove(final String formStr, Member currentMember) throws JSONException {
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
            FamilyRemoveMemberConfirmDialog dialog = FamilyRemoveMemberConfirmDialog.newInstance(title);
            dialog.show(this.getSupportFragmentManager(), FamilyRemoveMemberFragment.DIALOG_TAG);
            dialog.setOnRemove(() -> {
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
            });
            dialog.setOnRemoveActivity(() -> {
                if (this != null) {
                    this.finish();
                }
            });
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