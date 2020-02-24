package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.brac.hnpp.fragment.HnppMemberProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.fragment.MemberOtherServiceFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreFamilyOtherMemberProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.custom_views.CoreFamilyMemberFloatingMenu;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.FamilyOtherMemberProfileFragment;
import org.smartregister.brac.hnpp.presenter.HnppFamilyOtherMemberActivityPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyOtherMemberProfileFragment;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.contract.BaseProfileContract;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppConstants.MEMBER_ID_SUFFIX;

public class HnppFamilyOtherMemberProfileActivity extends CoreFamilyOtherMemberProfileActivity {
    public static final int REQUEST_HOME_VISIT = 5555;
    private static final int REQUEST_CODE_PREGNANCY_OUTCOME = 5556;

    private CustomFontTextView textViewDetails3;
    private String familyBaseEntityId;

    private TextView textViewAge,textViewName;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_other_member_profile);

        Toolbar toolbar = findViewById(org.smartregister.family.R.id.family_toolbar);
        HnppConstants.updateAppBackground(toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        appBarLayout = findViewById(org.smartregister.family.R.id.toolbar_appbarlayout);

        imageRenderHelper = new ImageRenderHelper(this);

        initializePresenter();

        setupViews();
    }
    public void updateDueCount(final int dueCount) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> adapter.updateCount(Pair.create(1, dueCount)));
    }
    @Override
    public void setProfileName(String fullName) {
       try{
           String[] str = fullName.split(",");
           this.textViewName.setText(str[0]);
           this.textViewAge.setText(getString(R.string.age,str[1]));

       }catch (Exception e){

       }

    }
    @Override
    public void setFamilyServiceStatus(String status) {

    }

    @Override
    public void setProfileDetailOne(String detailOne) {
        ((TextView)findViewById(R.id.textview_detail_one)).setText(HnppConstants.getGender(detailOne));

    }

    @Override
    public void setProfileImage(String baseEntityId, String entityType) {
        super.setProfileImage(baseEntityId, entityType);
        String dobString = org.smartregister.family.util.Utils.getDuration(org.smartregister.family.util.Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false));

        String yearSub =  dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        if(!TextUtils.isEmpty(yearSub) && Integer.parseInt(yearSub) >=5){
            ((ImageView)findViewById(R.id.imageview_profile)).setImageResource(R.mipmap.ic_member);
        }
    }

    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        setupMenuOptions(menu);
        return true;
    }
    @Override
    protected void startPncRegister() {
        HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                HnppConstants.JSON_FORMS.PNC_FORM, null, familyBaseEntityId, familyName);
    }

    @Override
    protected void startAncRegister() {
        HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                HnppConstants.JSON_FORMS.ANC_FORM, null, familyBaseEntityId, familyName);
    }

    @Override
    public void startMalariaRegister() {
//        startAnyFormActivity(HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME,REQUEST_CODE_PREGNANCY_OUTCOME);
        //TODO implement start anc malaria for HF
//        HnppHomeVisitActivity.startMe(this, new MemberObject(commonPersonObject), false);
//        startPregnancyFormActivity(HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
//        HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
//                HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, HnppJsonFormUtils.getUniqueMemberId(familyBaseEntityId), familyBaseEntityId, familyName);
        HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, HnppJsonFormUtils.getUniqueMemberId(familyBaseEntityId), familyBaseEntityId, familyName);
    }



    @Override
    protected void removeIndividualProfile() {
        Timber.d("Remove member action is not required in HF");
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HnppFamilyOtherMemberProfileActivity.this,
                commonPersonObject, familyBaseEntityId, familyHead, primaryCaregiver, FamilyRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        findViewById(org.smartregister.chw.core.R.id.viewpager).setVisibility(View.VISIBLE);

        textViewDetails3 = findViewById(R.id.textview_detail_three);
        textViewAge = findViewById(R.id.textview_age);
        textViewName = findViewById(R.id.textview_name);
        familyFloatingMenu.hideFab();

    }

    @Override
    public void setProfileDetailThree(String detailThree) {
        super.setProfileDetailThree(detailThree);
        if(!TextUtils.isEmpty(detailThree)) {
            detailThree = detailThree.replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                    .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
            detailThree = detailThree.substring(detailThree.length() - MEMBER_ID_SUFFIX);
            textViewDetails3.setText("ID: " + detailThree);
        }

    }
    MemberOtherServiceFragment memberOtherServiceFragment;
    MemberHistoryFragment memberHistoryFragment;
    HnppMemberProfileDueFragment profileMemberFragment;
    ViewPager mViewPager;
    String gender = "";
    String maritalStatus ="";
    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        List<Map<String,String>> genderMaritalStatus = HnppDBUtils.getGenderMaritalStatus(baseEntityId);
        if(genderMaritalStatus != null && genderMaritalStatus.size()>0) {
            gender = genderMaritalStatus.get(0).get("gender");
            maritalStatus = genderMaritalStatus.get(0).get("marital_status");
            commonPersonObject.getColumnmaps().put("gender", gender);
            commonPersonObject.getColumnmaps().put("marital_status", maritalStatus);
        }
        profileMemberFragment =(HnppMemberProfileDueFragment) HnppMemberProfileDueFragment.newInstance(this.getIntent().getExtras());
        profileMemberFragment.setCommonPersonObjectClient(commonPersonObject);
        adapter.addFragment(profileMemberFragment, this.getString(R.string.due).toUpperCase());
        memberOtherServiceFragment = new MemberOtherServiceFragment();
        memberHistoryFragment = MemberHistoryFragment.getInstance(this.getIntent().getExtras());
        memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
        adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());
        adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        return viewPager;
    }

    public void startAnyFormActivity(String formName, int requestCode) {
       try {
           JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
           HnppJsonFormUtils.addEDDField(formName,jsonForm,baseEntityId);
           jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
           Intent intent;
           if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)){
               HnppJsonFormUtils.addMaritalStatus(jsonForm,maritalStatus);
           }

//           if(formName.contains("anc"))
           HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
           JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
           JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
           updateFormField(jsonArray,"height",visitLogRepository.getHeight(baseEntityId));
           intent = new Intent(this, HnppAncJsonFormActivity.class);
//           else
//               intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
           intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

           Form form = new Form();
           form.setWizard(false);
           form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

           intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
           intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
           if (this != null) {
               this.startActivityForResult(intent, requestCode);
           }
       }catch (Exception e){
           
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
    protected void startEditMemberJsonForm(Integer title_resource, CommonPersonObjectClient client) {


        try {
            JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getFamilyMemberRegister(), this, client, Utils.metadata().familyMemberRegister.updateEventType);
            String moduleId = HnppDBUtils.getModuleId(familyHead);
            HnppJsonFormUtils.updateFormWithModuleId(form,moduleId,familyBaseEntityId);
            HnppJsonFormUtils.updateFormWithSimPrintsEnable(form);
            startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            //TODO: Need to check request code
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

//            String type = StringUtils.isBlank(parentEventType) ? getEncounterType() : getEncounterType();
           // String type = HnppJsonFormUtils.getEncounterType();
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            Map<String, String> jsonStrings = new HashMap<>();
            jsonStrings.put("First",jsonString);
            Visit visit = null;
            try {
            JSONObject form = new JSONObject(jsonString);
            String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);
            // persist to database


                visit = HnppJsonFormUtils.saveVisit(false, baseEntityId, type, jsonStrings, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(memberHistoryFragment !=null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        memberHistoryFragment.onActivityResult(0,0,null);
                        mViewPager.setCurrentItem(2,true);
                        if(profileMemberFragment !=null){
                            profileMemberFragment.updateStaticView();
                        }

                    }
                },1000);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected BaseProfileContract.Presenter getFamilyOtherMemberActivityPresenter(
            String familyBaseEntityId, String baseEntityId, String familyHead, String primaryCaregiver, String villageTown, String familyName) {
        this.familyBaseEntityId = familyBaseEntityId;
        return new HnppFamilyOtherMemberActivityPresenter(this, new BaseFamilyOtherMemberProfileActivityModel(),
                null, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }

    @Override
    protected CoreFamilyMemberFloatingMenu getFamilyMemberFloatingMenu() {
        if (familyFloatingMenu == null) {
            prepareFab();
        }
        return familyFloatingMenu;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_malaria_followup_visit) {
            startPncRegister();
            return true;
        }if (i == R.id.action_pregnancy_out_come) {
            startMalariaRegister();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected Context getFamilyOtherMemberProfileActivity() {
        return HnppFamilyOtherMemberProfileActivity.this;
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivity() {
        return FamilyProfileActivity.class;
    }

    @Override
    public void openFamilyDueTab() {
        Intent intent = new Intent(this, getFamilyProfileActivity());
        intent.putExtras(getIntent().getExtras());

//        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyBaseEntityId);
//        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
//        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
//        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, familyName);
//        String moduleId = HnppChildUtils.getModuleId(familyHead);
//        intent.putExtra(HnppConstants.KEY.MODULE_ID, moduleId);
//        String villageTown = getIntent().getStringExtra(Constants.INTENT_KEY.VILLAGE_TOWN);
//        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN,villageTown);
//        intent.putExtra(DBConstants.KEY.UNIQUE_ID, getIntent().getStringExtra(DBConstants.KEY.UNIQUE_ID));
        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        super.updateHasPhone(hasPhone);
        if (!hasPhone) {
            familyFloatingMenu.hideFab();
        }

    }
    public void openServiceForms(String formName){
        startAnyFormActivity(formName,REQUEST_HOME_VISIT);
    }
    public void openRefereal() {
        if(gender.equalsIgnoreCase("F")){
            startAnyFormActivity(HnppConstants.JSON_FORMS.WOMEN_REFERRAL,REQUEST_HOME_VISIT);
        }else{
            startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_REFERRAL,REQUEST_HOME_VISIT);

        }
    }
    public void openReferealFollowUp(ReferralFollowUpModel referralFollowUpModel) {

        try {
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralReason(),referralFollowUpModel.getReferralPlace());
            Intent intent;
            intent = new Intent(this, HnppAncJsonFormActivity.class);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            if (this != null) {
                this.startActivityForResult(intent, REQUEST_HOME_VISIT);
            }
        }catch (Exception e){

        }
    }

    public void openHomeVisitSingleForm(String formName){
        startAnyFormActivity(formName,REQUEST_HOME_VISIT);
    }

    public void openHomeVisitForm(){
        HnppHomeVisitActivity.startMe(this, new MemberObject(commonPersonObject), false);
    }

    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        onClickFloatingMenu = viewId -> {
            if (viewId == R.id.call_layout) {
                FamilyCallDialogFragment.launchDialog(this, familyBaseEntityId);
            }
        };
    }

    @Override
    protected BaseFamilyOtherMemberProfileFragment getFamilyOtherMemberProfileFragment() {
        return FamilyOtherMemberProfileFragment.newInstance(getIntent().getExtras());
    }

    private void prepareFab() {
        familyFloatingMenu = new FamilyMemberFloatingMenu(this);
        familyFloatingMenu.fab.setOnClickListener(v -> FamilyCallDialogFragment.launchDialog(this, familyBaseEntityId));
    }
    public static void startMe(Activity activity, MemberObject memberObject, String familyHeadName, String familyHeadPhoneNumber, CommonPersonObjectClient patient) {

    }

    private void setupMenuOptions(Menu menu) {
        menu.findItem(R.id.action_remove_member).setTitle("সদস্য বাদ দিন / মাইগ্রেট / মৃত্যু");
        menu.findItem(R.id.action_anc_registration).setTitle("গর্ভবতী রেজিস্ট্রেশন");
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setTitle("PNC রেজিস্ট্রেশন");
        menu.findItem(R.id.action_pregnancy_out_come).setTitle("প্রসবের ফলাফল");
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(true);

        if (FormApplicability.isWomanOfReproductiveAge(commonPersonObject)) {
            menu.findItem(R.id.action_anc_registration).setVisible(true);
//            menu.findItem(R.id.action_pregnancy_out_come).setVisible(true);
        } else {
            menu.findItem(R.id.action_anc_registration).setVisible(false);
//            menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);
        }
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);


    }
    public void updatePregnancyOutcomeVisible(String eventType){

        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)
        || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
            menu.findItem(R.id.action_pregnancy_out_come).setVisible(true);
        }else{
            menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);
        }
    }
    public void updateAncRegisterVisible(String eventType){

        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ELCO) )
        {
            menu.findItem(R.id.action_anc_registration).setVisible(true);
        }else{
            menu.findItem(R.id.action_anc_registration).setVisible(false);
        }
    }
}
