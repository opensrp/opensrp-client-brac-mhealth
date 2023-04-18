package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyOtherMemberProfileExtendedContract;
import org.smartregister.chw.core.fragment.CoreFamilyOtherMemberProfileFragment;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.activity.BaseFamilyOtherMemberProfileActivity;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.unicef.dghs.fragment.HnppMemberProfileDueFragment;
import org.smartregister.unicef.dghs.fragment.MemberHistoryFragment;
import org.smartregister.unicef.dghs.fragment.WomanImmunizationFragment;
import org.smartregister.unicef.dghs.job.VisitLogServiceJob;
import org.smartregister.unicef.dghs.listener.FloatingMenuListener;
import org.smartregister.unicef.dghs.listener.OnClickFloatingMenu;
import org.smartregister.unicef.dghs.listener.OnPostDataWithGps;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.HouseHoldInfo;
import org.smartregister.unicef.dghs.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.fragment.FamilyOtherMemberProfileFragment;
import org.smartregister.unicef.dghs.presenter.HnppFamilyOtherMemberActivityPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyOtherMemberProfileFragment;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.contract.BaseProfileContract;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
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
import static org.smartregister.unicef.dghs.utils.HnppConstants.MEMBER_ID_SUFFIX;
import static org.smartregister.unicef.dghs.utils.HnppConstants.getLastDateOfAMonth;

public class HnppFamilyOtherMemberProfileActivity extends BaseFamilyOtherMemberProfileActivity  implements FamilyOtherMemberProfileExtendedContract.View, VaccinationActionListener, ServiceActionListener {
    public static final int REQUEST_HOME_VISIT = 5555;
    public static final String IS_COMES_IDENTITY = "is_comes";

    private CustomFontTextView textViewDetails3;
    private String familyBaseEntityId;

    private TextView textViewAge,textViewName;
    private boolean isVerified,verificationNeeded;
    private String guId;
    private String moduleId;
    private String shrId;
    private Handler handler;
    protected FamilyMemberFloatingMenu familyFloatingMenu;
    protected String baseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;
    protected String PhoneNumber;
    protected CommonPersonObjectClient commonPersonObject;
    protected OnClickFloatingMenu onClickFloatingMenu;
    private TextView textViewFamilyHas;
    private RelativeLayout layoutFamilyHasRow;

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
        Toolbar toolbar = findViewById(R.id.family_toolbar);
       // HnppConstants.updateAppBackground(toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        appBarLayout = findViewById(R.id.toolbar_appbarlayout);
        imageRenderHelper = new ImageRenderHelper(this);

        initializePresenter();

        setupViews();
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        HnppConstants.isViewRefresh = false;
    }

    public void startFormForEdit(Integer title_resource) {
        CommonRepository commonRepository = org.smartregister.family.util.Utils.context().commonrepository(org.smartregister.family.util.Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject personObject = commonRepository.findByBaseEntityId(commonPersonObject.getCaseId());
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(personObject.getCaseId(), personObject.getDetails(), "");
        client.setColumnmaps(personObject.getColumnmaps());

        startEditMemberJsonForm(title_resource, client);
    }

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
    @Override
    public void refreshList() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                refreshList(adapter.getItem(i));
            }
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                for (int i = 0; i < adapter.getCount(); i++) {
                    refreshList(adapter.getItem(i));
                }
            });
        }
    }
    protected void refreshList(Fragment fragment) {
        if (fragment instanceof CoreFamilyOtherMemberProfileFragment) {
            CoreFamilyOtherMemberProfileFragment familyOtherMemberProfileFragment = ((CoreFamilyOtherMemberProfileFragment) fragment);
            if (familyOtherMemberProfileFragment.presenter() != null) {
                familyOtherMemberProfileFragment.refreshListView();
            }
        }
    }
    @Override
    public Context getContext() {
        return this;
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
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.other_member_menu, menu);
        this.menu = menu;
        MenuItem addMember = menu.findItem(org.smartregister.chw.core.R.id.add_member);
        if (addMember != null) {
            addMember.setVisible(false);
        }

        setupMenuOptions(menu);
        return true;
    }
    protected void startPncRegister() {
       // HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId);
    }

    public void startAncRegister() {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.ANC_FORM, null, familyBaseEntityId, familyName,textViewName.getText().toString(),latitude,longitude);
            }
        });
    }
    public void startMalariaRegister() {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, HnppJsonFormUtils.getUniqueMemberId(familyBaseEntityId), familyBaseEntityId, familyName,textViewName.getText().toString(),latitude,longitude);
            }
        });
     }

     protected void removeIndividualProfile() {
        Timber.d("Remove member action is not required in HF");
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HnppFamilyOtherMemberProfileActivity.this,
                commonPersonObject, familyBaseEntityId, familyHead, primaryCaregiver, FamilyRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        TextView toolbarTitle = findViewById(org.smartregister.chw.core.R.id.toolbar_title);
        toolbarTitle.setText(String.format(getString(org.smartregister.chw.core.R.string.return_to_family_name), presenter().getFamilyName()));

        TabLayout tabLayout = findViewById(org.smartregister.chw.core.R.id.tabs);
        tabLayout.setSelectedTabIndicatorHeight(0);

        findViewById(org.smartregister.chw.core.R.id.viewpager).setVisibility(View.GONE);

        // add floating menu
        familyFloatingMenu = getFamilyMemberFloatingMenu();
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        addContentView(familyFloatingMenu, linearLayoutParams);

        familyFloatingMenu.setClickListener(onClickFloatingMenu);
        textViewFamilyHas = findViewById(org.smartregister.chw.core.R.id.textview_family_has);
        layoutFamilyHasRow = findViewById(org.smartregister.chw.core.R.id.family_has_row);

        layoutFamilyHasRow.setOnClickListener(this);
        findViewById(org.smartregister.chw.core.R.id.viewpager).setVisibility(View.VISIBLE);

        textViewDetails3 = findViewById(R.id.textview_detail_three);
        textViewAge = findViewById(R.id.textview_age);
        textViewName = findViewById(R.id.textview_name);
        familyFloatingMenu.hideFab();

    }
    @Override
    protected void onResumption() {
        super.onResumption();
        FloatingMenuListener.getInstance(this, presenter().getFamilyBaseEntityId());
    }

    @Override
    public HnppFamilyOtherMemberActivityPresenter presenter() {
        return (HnppFamilyOtherMemberActivityPresenter) presenter;
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void setProfileDetailThree(String detailThree) {
        super.setProfileDetailThree(detailThree);
        if(!TextUtils.isEmpty(detailThree)) {
            detailThree = detailThree.replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                    .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
            detailThree = detailThree.substring(detailThree.length() - MEMBER_ID_SUFFIX);
            textViewDetails3.setText("ID: " + detailThree);
        }
        if(!TextUtils.isEmpty(shrId)){
            textViewDetails3.setText("ID: " + shrId);
        }

    }
//    MemberOtherServiceFragment memberOtherServiceFragment;
    MemberHistoryFragment memberHistoryFragment;
    HnppMemberProfileDueFragment profileMemberFragment;
    WomanImmunizationFragment womanImmunizationFragment;
    ViewPager mViewPager;
    String gender = "";
    String maritalStatus ="";
    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        List<Map<String,String>> genderMaritalStatus = HnppDBUtils.getGenderMaritalStatus(baseEntityId);
        if(genderMaritalStatus.size()>0) {
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

//        memberOtherServiceFragment = new MemberOtherServiceFragment();
        memberHistoryFragment = MemberHistoryFragment.getInstance(this.getIntent().getExtras());
//        memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
        womanImmunizationFragment = WomanImmunizationFragment.newInstance(this.getIntent().getExtras());
        womanImmunizationFragment.setChildDetails(commonPersonObject);
//        adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());

        if(FormApplicability.isWomenImmunizationApplicable(commonPersonObject)){
            adapter.addFragment(womanImmunizationFragment, this.getString(R.string.immunization).toUpperCase());
            if(!HnppConstants.isPALogin()){
                viewPager.setOffscreenPageLimit(3);
            }else{
                viewPager.setOffscreenPageLimit(2);
            }
        }else{
            if(!HnppConstants.isPALogin()){
                viewPager.setOffscreenPageLimit(3);
            }else{
                viewPager.setOffscreenPageLimit(2);
            }
        }

        adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
        viewPager.setAdapter(adapter);
        return viewPager;
    }
    String requestedFormName;
    int requestedRequestCode;

    public void startAnyFormActivity(String formName, int requestCode) {
//        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
//            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
//            return;
//        }
        requestedFormName = formName;
        requestedRequestCode = requestCode;
        getGPSLocation(formName,requestCode);


    }
    private void getGPSLocation(String formName,int requestCode){
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                processJsonForm(formName,requestCode,0.0,0.0);
//            }
//        });

    }
    private void processJsonForm(String formName,int requestCode,double latitude, double longitude){
        try {
            HnppConstants.appendLog("SAVE_VISIT","processJsonForm>>>formName:"+formName);

            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
            HnppJsonFormUtils.addEDDField(formName,jsonForm,baseEntityId);
//            try{
//                HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            try{
                HnppJsonFormUtils.addAddToStockValue(jsonForm);
            }catch (Exception e){

            }
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)){
                HnppJsonFormUtils.addMaritalStatus(jsonForm,maritalStatus);
            }
            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_VISIT_FORM)){
                String lmpDate = HnppDBUtils.getLmpDate(baseEntityId);
                String date = HnppConstants.getScheduleLmpDate(lmpDate,1);
                HnppJsonFormUtils.changeFormTitle(jsonForm,FormApplicability.getANCTitle(baseEntityId));
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"anc_type", FormApplicability.getANCType(baseEntityId));
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", date);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
            } else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM)){
                int pncDay = FormApplicability.getDayPassPregnancyOutcome(baseEntityId);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_day_passed", String.valueOf(pncDay));
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
    protected void startEditMemberJsonForm(Integer title_resource, CommonPersonObjectClient client) {
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                try {
                    JSONObject form = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(HnppConstants.JSON_FORM.getFamilyMemberDetailsRegister(), HnppFamilyOtherMemberProfileActivity.this, client, Utils.metadata().familyMemberRegister.updateEventType);
                    String moduleId = HnppDBUtils.getModuleId(familyHead);
                    HnppJsonFormUtils.updateFormWithChampType(form,moduleId,familyBaseEntityId);
//
//                    try{
//                        HnppJsonFormUtils.updateLatitudeLongitude(form,latitude,longitude,familyBaseEntityId);
//                    }catch (Exception e){
//
//                    }
                    startFormActivity(form);
                } catch (Exception e) {
                    Timber.e(e);
                }
//            }
//        });


    }

    private void openServiceForm(){
        if(referralFollowUpModel!=null){
            openReferealFollowUp(referralFollowUpModel);
        }else if(needToStartHomeVisit){
            HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                @Override
                public void onPost(double latitude, double longitude) {
                    HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,false,verificationNeeded,isVerified,checkedItem,latitude,longitude);

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
                openServiceForm();
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
//                                    if(memberOtherServiceFragment !=null){
//                                        memberOtherServiceFragment.updateStaticView();
//                                    }
                                    mViewPager.setCurrentItem(2,true);
                                }else{
//                                    if(memberOtherServiceFragment !=null){
//                                        memberOtherServiceFragment.updateStaticView();
//                                    }
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

    boolean isProcessingHV = false;
    boolean isProcessingANCVisit = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("BASE_ENTITY_ID","familyBaseEntityId:"+familyBaseEntityId+":baseEntityId:"+baseEntityId);

        if(TextUtils.isEmpty(baseEntityId)){
            Toast.makeText(this,"BaseEntityId should not be empty",Toast.LENGTH_SHORT).show();
            finish();
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
                if (form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.family.util.Utils.metadata().familyMemberRegister.updateEventType)) {
                    try{
                        JSONObject formWithConsent = new JSONObject(jsonString);
                        presenter().updateFamilyMember(formWithConsent.toString());
                    }catch (JSONException je){
                        je.printStackTrace();
                    }

//
//                    String[] generatedString;
//                    String title;
//                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
//
//                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
//                    generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
//                    title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);
//
//                    HnppConstants.showSaveFormConfirmationDialog(this, title, new OnDialogOptionSelect() {
//                        @Override
//                        public void onClickYesButton() {
//
//                            try{
//                                JSONObject formWithConsent = new JSONObject(jsonString);
//                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
//                                JSONArray field = jobkect.getJSONArray(FIELDS);
//                                HnppJsonFormUtils.addConsent(field,true);
//                                presenter().updateFamilyMember(formWithConsent.toString());
//                            }catch (JSONException je){
//                                je.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onClickNoButton() {
//                            try{
//                                JSONObject formWithConsent = new JSONObject(jsonString);
//                                JSONObject jobkect = formWithConsent.getJSONObject("step1");
//                                JSONArray field = jobkect.getJSONArray(FIELDS);
//                                HnppJsonFormUtils.addConsent(field,false);
//                                presenter().updateFamilyMember(formWithConsent.toString());
//                            }catch (JSONException je){
//                                je.printStackTrace();
//                            }
//                        }
//                    });
//
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }
    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionId, String visitId){

        return  Observable.create(e->{
                    if(TextUtils.isEmpty(baseEntityId)) e.onNext(2);
                    try {
                        JSONObject form = new JSONObject(jsonString);
                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());
                        HnppConstants.appendLog("SAVE_VISIT","baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

                        Visit visit = HnppJsonFormUtils.saveVisit(false,verificationNeeded, isVerified,checkedItem, baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);

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
    protected BaseProfileContract.Presenter getFamilyOtherMemberActivityPresenter(
            String familyBaseEntityId, String baseEntityId, String familyHead, String primaryCaregiver, String villageTown, String familyName) {
        this.familyBaseEntityId = familyBaseEntityId;
        return new HnppFamilyOtherMemberActivityPresenter(this, new BaseFamilyOtherMemberProfileActivityModel(),
                null, familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
    }
    protected FamilyMemberFloatingMenu getFamilyMemberFloatingMenu() {
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
            case R.id.action_malaria_registration:
                startMalariaRegister();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_anc_registration:
                startAncRegister();
                return true;
            case R.id.action_registration:
                startFormForEdit(org.smartregister.chw.core.R.string.edit_member_form_title);
                return true;
            case R.id.action_remove_member:
                removeIndividualProfile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected Context getFamilyOtherMemberProfileActivity() {
        return HnppFamilyOtherMemberProfileActivity.this;
    }

    protected Class<? extends FamilyProfileActivity> getFamilyProfileActivity() {
        return FamilyProfileActivity.class;
    }

    public void openFamilyDueTab() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);
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
        if (familyFloatingMenu != null) {
            familyFloatingMenu.redrawWithOption(familyFloatingMenu,hasPhone);
        }
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
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
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
    public void openMemberProfileUpdate() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_PROFILE_VISIT,REQUEST_HOME_VISIT);

    }
    public void openRefereal() {
        if(gender.equalsIgnoreCase("F")){
            startAnyFormActivity(HnppConstants.JSON_FORMS.WOMEN_REFERRAL,REQUEST_HOME_VISIT);
        }else{
            startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_REFERRAL,REQUEST_HOME_VISIT);

        }
    }
    private ReferralFollowUpModel referralFollowUpModel;
    public void openReferealFollowUp(ReferralFollowUpModel refFollowModel) {
        this.referralFollowUpModel = refFollowModel;

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
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
                    }catch (Exception e){

                    }
                    //HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralReason(),referralFollowUpModel.getReferralPlace());
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
        needToStartHomeVisit = false;
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,false,verificationNeeded,isVerified,checkedItem,latitude,longitude);

            }
        });

    }

    @Override
    protected void initializePresenter() {
        commonPersonObject = (CommonPersonObjectClient) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON);
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        shrId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        String villageTown = getIntent().getStringExtra(Constants.INTENT_KEY.VILLAGE_TOWN);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        PhoneNumber = commonPersonObject.getColumnmaps().get(CoreConstants.JsonAssets.FAMILY_MEMBER.PHONE_NUMBER);
        shrId =commonPersonObject.getColumnmaps().get(HnppConstants.KEY.SHR_ID);
        presenter = getFamilyOtherMemberActivityPresenter(familyBaseEntityId, baseEntityId, familyHead, primaryCaregiver, villageTown, familyName);
        Log.v("BASE_ENTITY_ID","familyBaseEntityId:"+familyBaseEntityId+":baseEntityId:"+baseEntityId+":shrId:"+shrId);
        onClickFloatingMenu = viewId -> {
            if (viewId == R.id.call_layout) {
                FamilyCallDialogFragment.launchDialog(this, familyBaseEntityId);
            }
        };
    }
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
//        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)

        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
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
    @Override
    public void onGiveToday(ServiceWrapper serviceWrapper, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onGiveToday(serviceWrapper,view);
    }

    @Override
    public void onGiveEarlier(ServiceWrapper serviceWrapper, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onGiveEarlier(serviceWrapper,view);
    }

    @Override
    public void onUndoService(ServiceWrapper serviceWrapper, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onUndoService(serviceWrapper,view);
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> arrayList, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onVaccinateToday(arrayList,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onVaccinateEarlier(arrayList,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        if(womanImmunizationFragment!=null) womanImmunizationFragment.onUndoVaccination(vaccineWrapper,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
    }
}
