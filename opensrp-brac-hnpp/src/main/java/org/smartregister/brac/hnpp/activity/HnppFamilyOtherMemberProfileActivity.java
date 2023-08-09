package org.smartregister.brac.hnpp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Tier;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.brac.hnpp.fragment.HnppMemberProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.fragment.MemberOtherServiceFragment;
import org.smartregister.brac.hnpp.job.SurveyHistoryJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.ForceSyncModel;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.model.Survey;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.HouseHoldInfo;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
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
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsVerification;
import org.smartregister.simprint.SimPrintsVerifyActivity;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.contract.BaseProfileContract;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static org.smartregister.brac.hnpp.utils.HnppConstants.MEMBER_ID_SUFFIX;
import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.makeReadOnlyFields;

public class HnppFamilyOtherMemberProfileActivity extends CoreFamilyOtherMemberProfileActivity {
    public static final int REQUEST_HOME_VISIT = 5555;
    public static final int REQUEST_SIMPRINTS_VERIFY = 1222;
    public static final String IS_COMES_IDENTITY = "is_comes";

    private CustomFontTextView textViewDetails3;
    private String familyBaseEntityId;

    private TextView textViewAge,textViewName;
    private boolean isVerified,verificationNeeded;
    private String guId;
    private String moduleId;
    private Handler handler;


    public boolean isNeedToVerify() {
        return verificationNeeded && !isVerified;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    AppExecutors appExecutors = new AppExecutors();
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_other_member_profile);
        handler = new Handler();
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
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        HnppConstants.isViewRefresh = false;
    }
    boolean isComesFromIdentity;
    private void updateFingerPrintIcon(){
        isComesFromIdentity = getIntent().getBooleanExtra(IS_COMES_IDENTITY,false);
        if(isComesFromIdentity){
            isVerified = true;
            verificationNeeded = true;
        }
       moduleId = HnppDBUtils.getModuleId(familyHead);
        guId = HnppDBUtils.getGuid(baseEntityId);

        Log.v("VERIFY_SIMPRINT","moduleId:"+moduleId+":guid:"+guId+":baseEntityId:"+baseEntityId);
        if(!TextUtils.isEmpty(guId) && !guId.equalsIgnoreCase(HnppConstants.TEST_GU_ID)){
            ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
            boolean simPrintsEnable = false;
            if(ssLocationForms.size() > 0){
                simPrintsEnable = ssLocationForms.get(0).simprints_enable;
            }
            if(simPrintsEnable){
                findViewById(R.id.finger_print).setVisibility(View.VISIBLE);
                verificationNeeded = true;
            }


        }

    }

    @Override
    public void startFormForEdit(Integer title_resource) {
        super.startFormForEdit(title_resource);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(HnppConstants.isPALogin()){
            openAsReadOnlyMode(jsonForm);
            return;
        }
        HnppConstants.appendLog("SAVE_VISIT","start familyMemberFormActivity>>>baseEntityId:"+baseEntityId);

        Intent intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());


        Form form = new Form();
        if(!HnppConstants.isReleaseBuild()){
            form.setActionBarBackground(R.color.test_app_color);

        }else{
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

        }
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
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
       // HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId);
    }

    @Override
    public void startAncRegister() {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.ANC_FORM, null, familyBaseEntityId, familyName,textViewName.getText().toString(),latitude,longitude);
            }
        });
    }

    @Override
    public void startMalariaRegister() {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, HnppJsonFormUtils.getUniqueMemberId(familyBaseEntityId), familyBaseEntityId, familyName,textViewName.getText().toString(),latitude,longitude);
            }
        });
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

        updateFingerPrintIcon();
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        List<Map<String,String>> genderMaritalStatus = HnppDBUtils.getGenderMaritalStatus(baseEntityId);
        if(genderMaritalStatus != null && genderMaritalStatus.size()>0) {
            gender = genderMaritalStatus.get(0).get("gender");
            maritalStatus = genderMaritalStatus.get(0).get("marital_status");
            commonPersonObject.getColumnmaps().put("gender", gender);
            commonPersonObject.getColumnmaps().put("marital_status", maritalStatus);
        }
        if(!HnppConstants.isPALogin()){
            profileMemberFragment =(HnppMemberProfileDueFragment) HnppMemberProfileDueFragment.newInstance(this.getIntent().getExtras());
            profileMemberFragment.setCommonPersonObjectClient(commonPersonObject);
            adapter.addFragment(profileMemberFragment, this.getString(R.string.due).toUpperCase());
        }

        memberOtherServiceFragment = new MemberOtherServiceFragment();
        memberHistoryFragment = MemberHistoryFragment.getInstance(this.getIntent().getExtras());
        memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
        adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());
        adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
        if(!HnppConstants.isPALogin()){
            viewPager.setOffscreenPageLimit(3);
        }else{
            viewPager.setOffscreenPageLimit(2);
        }
        viewPager.setAdapter(adapter);
        return viewPager;
    }
    String requestedFormName;
    int requestedRequestCode;

    public void startAnyFormActivity(String formName, int requestCode) {
        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        requestedFormName = formName;
        requestedRequestCode = requestCode;
        if(!ignoreSimprintCheck && isNeedToVerify()){
            showVerifyDialog();
            return;

        }
        getGPSLocation(formName,requestCode);


    }

    public void startAnyFormActivity(String formName, int requestCode,String from) {
        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        requestedFormName = formName;
        requestedRequestCode = requestCode;
        if(!ignoreSimprintCheck && isNeedToVerify()){
            showVerifyDialog();
            return;

        }
        getGPSLocation(formName,requestCode,from);


    }
    private void getGPSLocation(String formName,int requestCode){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                processJsonForm(formName,requestCode,latitude,longitude,"");
            }
        });

    }

    private void getGPSLocation(String formName,int requestCode,String from){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                processJsonForm(formName,requestCode,latitude,longitude,from);
            }
        });

    }
    private void processJsonForm(String formName,int requestCode,double latitude, double longitude,String from){
        try {
            HnppConstants.appendLog("SAVE_VISIT","processJsonForm>>>formName:"+formName);

            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
            HnppJsonFormUtils.addEDDField(formName,jsonForm,baseEntityId);
            try{
                HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                HnppJsonFormUtils.addAddToStockValue(jsonForm);
            }catch (Exception e){

            }
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)){
                HnppJsonFormUtils.addMaritalStatus(jsonForm,maritalStatus);
            }
            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM) ||
                    formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM) ||
                    formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)){
                HnppJsonFormUtils.addLastAnc(jsonForm,baseEntityId,false);
            } else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM)||
               formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_AFTER_48_HOUR)
                    ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_BEFORE_48_HOUR)  ){
                HnppJsonFormUtils.addLastPnc(jsonForm,baseEntityId,false);
                int pncDay = FormApplicability.getDayPassPregnancyOutcome(baseEntityId);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_day_passed", String.valueOf(pncDay));
            }
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PREGNANT_WOMAN_DIETARY_DIVERSITY)){
                if(from!=null && from.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_valid_lmp","true");
                }else{
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_valid_lmp","false");
                }

            }
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)){
                if(gender.equalsIgnoreCase("F")){
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_women","true");
                }
            }
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.NCD_PACKAGE)){
                HnppJsonFormUtils.addNcdSugerPressure(baseEntityId,jsonForm);
            }

//           if(formName.contains("anc"))
            HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            String height = visitLogRepository.getHeight(baseEntityId);
            if(!TextUtils.isEmpty(height)){
                JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                updateFormField(jsonArray,"height",height);
            }

            intent = new Intent(this, HnppAncJsonFormActivity.class);
//           else
//               intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            startActivityForResult(intent, requestCode);

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
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(CoreConstants.JSON_FORM.getFamilyMemberRegister(), HnppFamilyOtherMemberProfileActivity.this, client, Utils.metadata().familyMemberRegister.updateEventType);
                    String moduleId = HnppDBUtils.getModuleId(familyHead);
                    HnppJsonFormUtils.updateFormWithModuleId(form,moduleId,familyBaseEntityId);
                    HnppJsonFormUtils.updateFormWithSimPrintsEnable(form,familyHead);
                    if(HnppConstants.isPALogin()){
                        makeReadOnlyFields(form);
                    }
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(form,latitude,longitude);
                    }catch (Exception e){

                    }
                    startFormActivity(form);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }
        });


    }
    private void showFailAlertDialog(String message, String threshold){
        new AlertDialog.Builder(this).setMessage(message)
                .setTitle("ফিঙ্গার প্রিন্ট ভেরিফিকেশন রেজাল্ট").setCancelable(false)
                .setPositiveButton("আরেকবার চেষ্টা করি", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startSimprintVerify();
                    }
                })
                .setNegativeButton("আঙ্গুলের ছাপ মেলেনি", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                showNotFoundDialog();
            }
        }).show();
    }
    private void showSuccessAlertDialog(String message, String threshold){
        new AlertDialog.Builder(this).setMessage(message)
                .setTitle("ফিঙ্গার প্রিন্ট ভেরিফিকেশন রেজাল্ট").setCancelable(false)
                .setNegativeButton("ঠিক আছে", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.dismiss();
                        openServiceForm();

                    }
                }).show();
    }
    private void openServiceForm(){
        if(referralFollowUpModel!=null){
            openReferealFollowUp(referralFollowUpModel);
        }else if(needToStartHomeVisit){
            HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                @Override
                public void onPost(double latitude, double longitude) {
                    HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,isComesFromIdentity,verificationNeeded,isVerified,checkedItem,latitude,longitude);

                }
            });
            needToStartHomeVisit = false;
        }
        else{
            startAnyFormActivity(requestedFormName,requestedRequestCode);
        }
    }
    String checkedItem = "";
    private void addCheckedText(String text){
        if(TextUtils.isEmpty(checkedItem)){
            checkedItem = text;
        }else{
            checkedItem = checkedItem+","+text;
        }
    }
    private void disagreeDialog(){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.not_verified_disagree);
        TextView textViewNotFound = dialog.findViewById(R.id.not_found_txt);
        textViewNotFound.setText(textViewName.getText().toString()+" কে যাচাই করা যায় নি");
        Button service_btn = dialog.findViewById(R.id.service_btn);
        Button retry_btn = dialog.findViewById(R.id.retry_btn);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ignoreSimprintCheck = true;
                checkedItem = "disagree";
                Log.v("VERIFY","checkedItem>>"+checkedItem);
                openServiceForm();
            }
        });
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startSimprintVerify();
            }
        });
        dialog.show();

    }
    int selectedCount = 0;

    private void showNotFoundDialog(){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.view_not_verified);
        Button service_btn = dialog.findViewById(R.id.service_btn);
        Button retry_btn = dialog.findViewById(R.id.retry_btn);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        CheckBox checkBox1 = dialog.findViewById(R.id.check_box_1);
        CheckBox checkBox2 = dialog.findViewById(R.id.check_box_2);
        CheckBox checkBox5 = dialog.findViewById(R.id.check_box_5);
        checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                    checkBox5.setChecked(false);
                    checkedItem = checkedItem.replace("জানা নেই","");
                    addCheckedText(checkBox1.getText().toString());
                selectedCount++;
            }else{
                selectedCount --;
            }
        });
        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                checkBox5.setChecked(false);
                    checkedItem = checkedItem.replace("জানা নেই","");
                    addCheckedText(checkBox2.getText().toString());
                selectedCount++;
            }else{
                selectedCount --;
            }
        });

        checkBox5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                checkBox1.setChecked(false);
                checkBox2.setChecked(false);
                checkedItem = checkBox5.getText().toString();
                selectedCount++;
            }else{
                selectedCount --;
            }
        });
        service_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedCount == 0){
                    Toast.makeText(HnppFamilyOtherMemberProfileActivity.this,"যে কোন একটি কারণ সিলেক্ট করুন",Toast.LENGTH_LONG).show();
                    return;
                }
                dialog.dismiss();
                ignoreSimprintCheck = true;
                Log.v("VERIFY","checkedItem>>"+checkedItem);
                openServiceForm();
            }
        });
        retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startSimprintVerify();
            }
        });
        dialog.show();

    }
    private void showVerifyDialog(){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_before_verify);
        Button verify_btn = dialog.findViewById(R.id.verify_btn);
        verify_btn.setText(textViewName.getText().toString()+" কে যাচাই করি");
        ImageView imageView = dialog.findViewById(R.id.finger_print);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startSimprintVerify();
            }
        });
        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startSimprintVerify();
            }
        });
        dialog.show();

    }
    Dialog dialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog!=null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":isSuccess==3?"সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                isProcessingHV = false;
                isProcessingANCVisit = false;
               // if(isSuccess){
                    if(memberHistoryFragment !=null){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
//                        memberHistoryFragment.onActivityResult(0,0,null);
                                if(!HnppConstants.isPALogin()){

                                    if(profileMemberFragment !=null){
                                        profileMemberFragment.updateStaticView();
                                    }
                                    if(memberOtherServiceFragment !=null){
                                        memberOtherServiceFragment.updateStaticView();
                                    }
                                    mViewPager.setCurrentItem(2,true);
                                }else{
                                    if(memberOtherServiceFragment !=null){
                                        memberOtherServiceFragment.updateStaticView();
                                    }
                                    mViewPager.setCurrentItem(1,true);

                                }


                            }
                        },500);
                    }
              //  }
            }
        });
        dialog.show();

    }
    public void startSimprintVerify(){
        if(!TextUtils.isEmpty(moduleId)){
            SimPrintsVerifyActivity.startSimprintsVerifyActivity(HnppFamilyOtherMemberProfileActivity.this,moduleId,guId,REQUEST_SIMPRINTS_VERIFY);

        }else{
            Toast.makeText(HnppFamilyOtherMemberProfileActivity.this,"Please select module id",Toast.LENGTH_LONG).show();
        }
    }
    boolean isProcessingHV = false;
    boolean isProcessingANCVisit = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("BASE_ENTITY_ID","familyBaseEntityId:"+familyBaseEntityId+":baseEntityId:"+baseEntityId);

        if(TextUtils.isEmpty(baseEntityId)){
            Toast.makeText(this,"BaseEntityId should not be empty",Toast.LENGTH_SHORT).show();
            finish();
        }
        if(requestCode == REQUEST_SIMPRINTS_VERIFY ){
            if(resultCode == Activity.RESULT_OK){
                SimPrintsVerification verifyResults = (SimPrintsVerification) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                String guId = verifyResults.getGuid();
                if(TextUtils.isEmpty(guId)){
                    isVerified = false;
                    disagreeDialog();
                    return;
                }
                Tier tier = verifyResults.getTier();
                float confidence = verifyResults.getConfidence();
                Log.v("VERIFY_SIMPRINT","verify:"+guId+":tier:"+tier+":confidence:"+confidence);
                if(!TextUtils.isEmpty(guId) && guId.equalsIgnoreCase(this.guId) && confidence >= HnppConstants.VERIFY_THRESHOLD){
                    isVerified = true;
                    showSuccessAlertDialog("আঙ্গুলের ছাপ মিলেছে \n নাম : "+textViewName.getText().toString(),confidence+"");
                }else{
                    isVerified = false;
                    showFailAlertDialog("সামনের ব্যক্তি "+textViewName.getText().toString()+" না",confidence+"");
                }
            }else{
                Toast.makeText(this,"SIMPRINTS_BIOMETRICS_COMPLETE_CHECK false",Toast.LENGTH_LONG).show();
                isVerified = false;
                showFailAlertDialog("সামনের ব্যক্তি "+textViewName.getText().toString()+" না","not found");
            }

            return;
        }
        if(resultCode == Activity.RESULT_OK){
            HnppConstants.isViewRefresh = true;

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

                if(isProcessingHV) return;
                Log.d("calledMultiVisit","true");

                isProcessingHV = true;
                AtomicInteger isSave = new AtomicInteger(2); /// 1-> Success / 2-> Regular error  3-> Already submitted visit error
                showProgressDialog(R.string.please_wait_message);
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                String visitId = JsonFormUtils.generateRandomUUIDString();
                HnppConstants.appendLog("SAVE_VISIT","isProcessingHV>>>baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

                    processVisitFormAndSave(jsonString,formSubmissionId,visitId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Integer aInteger) {
                                    isSave.set(aInteger);
                                    Log.d("visitCalledOnnext","true");
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
                                        //showServiceDoneDialog(false);
                                    }
                                }
                            });
        }
       else if (resultCode == Activity.RESULT_OK && requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT){
            if(isProcessingANCVisit) return;
            AtomicBoolean isSave = new AtomicBoolean(false);
            showProgressDialog(R.string.please_wait_message);

            isProcessingANCVisit = true;
            processVisits()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSave.set(aBoolean);
                        }

                        @Override
                        public void onError(Throwable e) {
                            hideProgressDialog();
                        }

                        @Override
                        public void onComplete() {
                            if(isSave.get()){
                                hideProgressDialog();
                                showServiceDoneDialog(1);
                            }else {
                                hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });

        }
        else if(resultCode == RESULT_OK && requestCode == org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON){
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            try{
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                if (form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.family.util.Utils.metadata().familyMemberRegister.updateEventType)) {
                    String[] generatedString;
                    String title;
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
                    generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
                    title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    HnppConstants.showSaveFormConfirmationDialog(this, title, new OnDialogOptionSelect() {
                        @Override
                        public void onClickYesButton() {

                            try{
                                JSONObject formWithConsent = new JSONObject(jsonString);
                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
                                JSONArray field = jobkect.getJSONArray(FIELDS);
                                HnppJsonFormUtils.addConsent(field,true);
                                presenter().updateFamilyMember(formWithConsent.toString());
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
                                presenter().updateFamilyMember(formWithConsent.toString());
                            }catch (JSONException je){
                                je.printStackTrace();
                            }
                        }
                    });
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        else if(resultCode == Activity.RESULT_OK && requestCode == HnppConstants.SURVEY_KEY.MM_SURVEY_REQUEST_CODE){
            if(processSurveyResponse(data)){
                Toast.makeText(this,"Survey done",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Fail to Survey",Toast.LENGTH_SHORT).show();
            }


        }

    }
    private boolean processSurveyResponse(Intent data){
        String response = data.getStringExtra(HnppConstants.SURVEY_KEY.DATA);
        Log.v("SURVEY_APP","response processSurveyResponse:"+response);
        try{
            JSONObject jsonObject = new JSONObject(response);
            String form_name = jsonObject.getString("form_name");
            String date_time = jsonObject.getString("date");
            String uuid = jsonObject.getString("uuid");
            Long time_stamp;
            try{
                time_stamp = jsonObject.getLong("time_stamp");
            }catch (Exception e){
                time_stamp = Long.parseLong(jsonObject.getString("time_stamp"));
            }
            String form_id = jsonObject.optString("form_id");

            Survey survey = new Survey();
            survey.formName = form_name;
            survey.formId = form_id;
            survey.uuid = uuid;
            survey.timestamp = time_stamp;
            survey.baseEntityId = baseEntityId;
            survey.dateTime = date_time;
            survey.type = HnppConstants.SURVEY_KEY.MM_TYPE;
            HnppApplication.getSurveyHistoryRepository().addOrUpdate(survey,HnppConstants.SURVEY_KEY.MM_TYPE);
            return true;

        }catch (Exception e){
            e.printStackTrace();

        }
        return false;
    }
    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionId, String visitId){

        return  Observable.create(e->{
                    if(TextUtils.isEmpty(baseEntityId)) e.onNext(2);
                    try {
                        JSONObject form = new JSONObject(jsonString);
                        HnppJsonFormUtils.setEncounterDateTime(form);

                        Log.v("DATEEEE",""+form.getJSONObject("metadata").getJSONObject("today").getString("value"));

                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());
                        HnppConstants.appendLog("SAVE_VISIT","baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

                        Visit visit = HnppJsonFormUtils.saveVisit(isComesFromIdentity,verificationNeeded, isVerified,checkedItem, baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);

                        if(visit!=null && !visit.getVisitId().equals("0")){
                            HnppHomeVisitIntentService.processVisits();
                            FormParser.processVisitLog(visit);
                            HnppConstants.appendLog("SAVE_VISIT","processVisitLog done formSubmissionId:"+formSubmissionId+":type:"+type);

                           // return true;
                            e.onNext(1);//success
                            e.onComplete();

                        }else if(visit!=null && visit.getVisitId().equals("0")){
                            e.onNext(3);//already exist
                            e.onComplete();
                        }else{
                            //return false;
                            e.onNext(2);//error
                            e.onComplete();
                        }
                    } catch (Exception ex) {
                        HnppConstants.appendLog("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        Log.d("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        e.onNext(2);//error
                        e.onComplete();
                    }
                   // return false;
                   // e.onNext(false);

                }
                );


    }
    private Observable<Boolean> processVisits(){
        return Observable.create(e->{
            try{
                HnppHomeVisitIntentService.processVisits();
                VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                //return true;
                e.onNext(true);
                e.onComplete();
            }catch (Exception ex){
                //return false;
                e.onNext(false);
                e.onComplete();
            }
        });

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
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (i){
            case R.id.action_malaria_followup_visit:
                startPncRegister();
                return true;
            case R.id.action_pregnancy_out_come:
                startMalariaRegister();
                return true;
            case R.id.action_mm_survey:
                try{
                    JSONObject mmObj = HnppConstants.populateMemberData(baseEntityId);
                    Intent intent = HnppConstants.passToSurveyApp(HnppConstants.SURVEY_KEY.MM_TYPE, mmObj.toString(), this);
                    Log.v("SURVEY_APP","request:"+intent.getExtras().toString());
                    startActivityForResult(intent, HnppConstants.SURVEY_KEY.MM_SURVEY_REQUEST_CODE);

                }catch (ActivityNotFoundException activityNotFoundException){
                    Toast.makeText(this,"App not installed",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            case R.id.action_survey_history:
                if(HnppConstants.isNeedToCallSurveyHistoryApi()){
                    SurveyHistoryJob.scheduleJobImmediately(SurveyHistoryJob.TAG);
                }
                SurveyHistoryActivity.startSurveyHistoryActivity(this,HnppConstants.SURVEY_KEY.MM_TYPE,baseEntityId);
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
        HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyBaseEntityId);
        if(houseHoldInfo !=null){
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, houseHoldInfo.getHouseHoldHeadId());
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, houseHoldInfo.getPrimaryCaregiverId());
            intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldInfo.getHouseHoldName());
            intent.putExtra(DBConstants.KEY.UNIQUE_ID, houseHoldInfo.getHouseHoldUniqueId());
            intent.putExtra(HnppConstants.KEY.MODULE_ID, houseHoldInfo.getModuleId());

        }
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
    public void openCoronaIndividualForm(){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                Intent intent = new Intent(HnppFamilyOtherMemberProfileActivity.this, HnppAncJsonFormActivity.class);
                try{
                    HnppConstants.appendLog("SAVE_VISIT","openCoronaIndividualForm>>>baseEntityId:"+baseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(HnppFamilyOtherMemberProfileActivity.this).getFormJson(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);


                    Form form = new Form();
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);

                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    startActivityForResult(intent, REQUEST_HOME_VISIT);

                }catch (Exception e){

                }
            }
        });



    }
    public void openServiceForms(String formName){

        if(TextUtils.isEmpty(baseEntityId)){
            Toast.makeText(this,"BaseEntityId should not be empty",Toast.LENGTH_SHORT).show();
            finish();
        }
        startAnyFormActivity(formName,REQUEST_HOME_VISIT);
    }
    public void openRefereal() {
        if(gender.equalsIgnoreCase("F")){
            startAnyFormActivity(HnppConstants.JSON_FORMS.WOMEN_REFERRAL,REQUEST_HOME_VISIT);
        }else{
            startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_REFERRAL,REQUEST_HOME_VISIT);

        }
    }
    public void openWomanDietaryDiversity(String from) {
       startAnyFormActivity(HnppConstants.JSON_FORMS.PREGNANT_WOMAN_DIETARY_DIVERSITY,REQUEST_HOME_VISIT,from);
    }


    private ReferralFollowUpModel referralFollowUpModel;
    private boolean ignoreSimprintCheck = false;
    public void openReferealFollowUp(ReferralFollowUpModel refFollowModel) {
        this.referralFollowUpModel = refFollowModel;

        if(!ignoreSimprintCheck && isNeedToVerify()){
            showVerifyDialog();
            return;
        }
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPost(double latitude, double longitude) {
                if(TextUtils.isEmpty(baseEntityId)){
                    Toast.makeText(HnppFamilyOtherMemberProfileActivity.this, "baseentity id null", Toast.LENGTH_SHORT).show();
                    finish();
                }
                try {
                    HnppConstants.appendLog("SAVE_VISIT","openREFERREL_FOLLOWUPForm>>>baseEntityId:"+baseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(HnppFamilyOtherMemberProfileActivity.this).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    }catch (Exception e){

                    }
                    HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralReason(),referralFollowUpModel.getReferralPlace());
                    Intent intent;
                    intent = new Intent(HnppFamilyOtherMemberProfileActivity.this, HnppAncJsonFormActivity.class);
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

                    Form form = new Form();
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);
                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    referralFollowUpModel = null;
                    startActivityForResult(intent, REQUEST_HOME_VISIT);

                }catch (Exception e){

                }
            }
        });

    }

    public void openHomeVisitSingleForm(String formName){
        startAnyFormActivity(formName,REQUEST_HOME_VISIT);
    }
    private boolean needToStartHomeVisit = false;

    public void openHomeVisitForm(){
        if(!HnppApplication.getStockRepository().isAvailableStock(CoreConstants.EventType.ANC_HOME_VISIT)){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        needToStartHomeVisit = true;
        if(!ignoreSimprintCheck && isNeedToVerify()){
            showVerifyDialog();
            return;
        }
        needToStartHomeVisit = false;
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,isComesFromIdentity,verificationNeeded,isVerified,checkedItem,latitude,longitude);

            }
        });

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

    private void setupMenuOptions(Menu menu) {

        menu.findItem(R.id.action_remove_member).setTitle("সদস্য বাদ দিন / মাইগ্রেট / মৃত্যু");
        menu.findItem(R.id.action_anc_registration).setTitle("গর্ভবতী রেজিস্ট্রেশন");
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setTitle("PNC রেজিস্ট্রেশন");
        menu.findItem(R.id.action_pregnancy_out_come).setTitle("প্রসবের ফলাফল");
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);

        if(HnppConstants.isPALogin()){
            menu.findItem(R.id.action_remove_member).setVisible(false);
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
            menu.findItem(R.id.action_anc_registration).setVisible(false);
        }else{
            menu.findItem(R.id.action_remove_member).setVisible(true);
            if (FormApplicability.isWomanOfReproductiveAge(commonPersonObject)) {
                menu.findItem(R.id.action_anc_registration).setVisible(true);
            } else {
                menu.findItem(R.id.action_anc_registration).setVisible(false);
            }
            menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        }


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null) handler.removeCallbacksAndMessages(null);
    }
}
