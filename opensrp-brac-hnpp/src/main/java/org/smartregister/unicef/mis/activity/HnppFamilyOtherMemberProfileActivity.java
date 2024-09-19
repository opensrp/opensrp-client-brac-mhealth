package org.smartregister.unicef.mis.activity;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.joda.time.LocalDate;
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
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.unicef.mis.fragment.HnppMemberProfileDueFragment;
import org.smartregister.unicef.mis.fragment.MemberHistoryFragment;
import org.smartregister.unicef.mis.fragment.WomanImmunizationFragment;
import org.smartregister.unicef.mis.job.VisitLogServiceJob;
import org.smartregister.unicef.mis.listener.FloatingMenuListener;
import org.smartregister.unicef.mis.listener.OnClickFloatingMenu;
import org.smartregister.unicef.mis.listener.OnPostDataWithGps;
import org.smartregister.unicef.mis.model.ReferralFollowUpModel;
import org.smartregister.unicef.mis.repository.HnppVisitLogRepository;
import org.smartregister.unicef.mis.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.FormApplicability;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.HouseHoldInfo;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.fragment.FamilyOtherMemberProfileFragment;
import org.smartregister.unicef.mis.presenter.HnppFamilyOtherMemberActivityPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyOtherMemberProfileFragment;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.unicef.mis.utils.RiskyModel;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.contract.BaseProfileContract;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.time.Period;
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
import mpower.iot.IOTActivity;
import timber.log.Timber;

import static org.smartregister.unicef.mis.utils.HnppConstants.MEMBER_ID_SUFFIX;

public class HnppFamilyOtherMemberProfileActivity extends BaseFamilyOtherMemberProfileActivity  implements FamilyOtherMemberProfileExtendedContract.View, VaccinationActionListener, ServiceActionListener {
    public static final int REQUEST_HOME_VISIT = 5555;
    public static final String IS_COMES_IDENTITY = "is_comes";
    public static final String IS_COMES_GLOBAL_SEARCH= "is_comes_global";
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
    private Button memberSurveyBtn;
    private Button addChildBtn;
    private boolean isComesFromGlobalSearch;

    public boolean isNeedToVerify() {
        return verificationNeeded && !isVerified;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_other_member_profile);
        handler = new Handler();
        Toolbar toolbar = findViewById(R.id.family_toolbar);
       // HnppConstants.updateAppBackground(toolbar);
        setSupportActionBar(toolbar);
        addChildBtn = findViewById(R.id.add_child_btn);
        memberSurveyBtn = findViewById(R.id.member_visit_btn);
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
        findViewById(R.id.update_profile_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFormForEdit(org.smartregister.chw.core.R.string.edit_member_form_title);
            }
        });
        memberSurveyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMemberProfileUpdate();
            }
        });
        addChildBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startChildForm();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.risk_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RiskyDataDisplayActivity.startInvalidActivity(baseEntityId,HnppFamilyOtherMemberProfileActivity.this);
                //openRiskFactorDialog();
            }
        });
    }
    private void openRiskFactorDialog(){
        ArrayList<RiskyModel> riskyModels = HnppApplication.getRiskDetailsRepository().getRiskyKeyByEntityId(baseEntityId);
        StringBuilder builder = new StringBuilder();
        for (RiskyModel riskyModel:riskyModels) {
            String[] fs= riskyModel.riskyKey.split(",");
            if(fs.length>0){
                for (String key:fs) {
                    Log.v("RISK_FACTOR","key>>"+key+":value:"+riskyModel.riskyValue);
                    builder.append(HnppConstants.getRiskeyFactorMapping().get(key)==null?key:HnppConstants.getRiskeyFactorMapping().get(key));
                    builder.append(":");
                    builder.append(riskyModel.riskyValue);
                    builder.append("\n");
                }
            }else{
                Log.v("RISK_FACTOR","key>>"+riskyModel.riskyKey+":value:"+riskyModel.riskyValue);
                builder.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey)==null?riskyModel.riskyKey:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey));
                builder.append(":");
                builder.append(riskyModel.riskyValue);
                builder.append("\n");
            }

        }

        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        TextView message = dialog.findViewById(R.id.text_tv);
        titleTv.setText(R.string.risk_cause);
        message.setText(builder.toString());
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
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
        form.setWizard(true);
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

    @Override
    public void openProfile(String baseEntityId) {
        if(TextUtils.isEmpty(baseEntityId))return;
        CommonRepository commonRepository = org.smartregister.family.util.Utils.context().commonrepository(org.smartregister.family.util.Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());

            Intent intent = new Intent(this, HnppChildProfileActivity.class);
            intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, false);
            intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, client.getCaseId());
            intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, new MemberObject(client));
            startActivity(intent);

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
        return !isComesFromGlobalSearch;
    }
    protected void startPncRegister() {
       // HnppPncRegisterActivity.startHnppPncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId);
    }

    public void startAncRegister() {
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.ANC_FORM, null, familyBaseEntityId, familyName,textViewName.getText().toString(),0,0);
//            }
//        });
    }
    public void startMalariaRegister() {
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(HnppFamilyOtherMemberProfileActivity.this, baseEntityId, PhoneNumber,
                        HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME, HnppJsonFormUtils.getUniqueMemberId(familyBaseEntityId), familyBaseEntityId, familyName,textViewName.getText().toString(),0,0);
//            }
//        });
     }

     protected void removeIndividualProfile() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HnppFamilyOtherMemberProfileActivity.this,
                commonPersonObject, familyBaseEntityId, false, FamilyRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        TextView toolbarTitle = findViewById(org.smartregister.chw.core.R.id.toolbar_title);
        if(!TextUtils.isEmpty(presenter().getFamilyName())){
            toolbarTitle.setText(String.format(getString(org.smartregister.chw.core.R.string.return_to_family_name), presenter().getFamilyName()));
        }else{
            toolbarTitle.setText("");
        }


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
            //detailThree = detailThree.substring(detailThree.length() - MEMBER_ID_SUFFIX);
            textViewDetails3.setText(detailThree);
        }
        if(!TextUtils.isEmpty(shrId)){
            textViewDetails3.setText(shrId);
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
        memberHistoryFragment.setIsNeedAncTitle(FormApplicability.isWomenImmunizationApplicable(commonPersonObject));
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
                viewPager.setOffscreenPageLimit(2);
            }else{
                viewPager.setOffscreenPageLimit(1);
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
            Form form = new Form();
            form.setWizard(true);
            JSONObject jsonForm =  HnppJsonFormUtils.getJsonObject(formName);

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
            Intent intent = new Intent(this, HnppAncJsonFormActivity.class);
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)){
                HnppJsonFormUtils.addMaritalStatus(jsonForm,maritalStatus);
            }
            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_VISIT_FORM)){
                String lmpDate = HnppDBUtils.getLmpDate(baseEntityId);
                int noOfAnc = (FormApplicability.getANCCount(baseEntityId)+1);
                String date = HnppConstants.getScheduleAncDate(lmpDate,noOfAnc);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"lmp", lmpDate);
                HnppJsonFormUtils.changeFormTitle(jsonForm,FormApplicability.getANCTitle(baseEntityId));
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"anc_count", noOfAnc+"");
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", date);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"blood_group",HnppDBUtils.getBloodGroup(baseEntityId));
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"Systolic", systolic+"");
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"diastolic", diastolic+"");
                if(fasting!=0 || random !=0){
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"blood_sugar_exam", getString(R.string.yes));
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"fbs_result", fasting+"");
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"rbs_result", random+"");
                }
                String[] weights = HnppDBUtils.getWeightFromBaseEntityId(baseEntityId);
                if(weights.length>0){
                    HnppJsonFormUtils.addJsonKeyValue(jsonForm,"previous_weight",weights[0]);
                    long periodSeconds = (System.currentTimeMillis() - Long.parseLong(weights[1])) / 1000;
                    long elapsedDays = periodSeconds / 60 / 60 / 24;
                    int monthDiff = (int)elapsedDays/30;

                    //int monthDiff = FormApplicability.getMonthsDifference(new LocalDate(weights[1]),new LocalDate(System.currentTimeMillis()));
                    HnppJsonFormUtils.addJsonKeyValue(jsonForm,"month_diff",monthDiff+"");
                }
                form.setWizard(true);
                form.setHideSaveLabel(true);
                form.setSaveLabel("");
                form.setHomeAsUpIndicator(org.smartregister.family.R.mipmap.ic_cross_white);
                form.setNavigationBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);
                intent.putExtra("IS_NEED_SAVE",false);
            } else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM)){
                String deliveryDate = FormApplicability.getDeliveryDate(baseEntityId);
                int pncCount = (FormApplicability.getPNCCount(baseEntityId)+1);
                String date = HnppConstants.getSchedulePncDate(deliveryDate,pncCount);
                HnppJsonFormUtils.changeFormTitle(jsonForm,FormApplicability.getPncTitle(baseEntityId));
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"delivery_date", deliveryDate+"");
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", pncCount+"");
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", date);
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"blood_pressure_systolic", systolic+"");
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"blood_pressure_diastolic", diastolic+"");
//                form.setWizard(true);
//                form.setHideSaveLabel(true);
//                form.setSaveLabel("");
//                form.setHomeAsUpIndicator(org.smartregister.family.R.mipmap.ic_cross_white);
//                form.setNavigationBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);
//                intent.putExtra("IS_NEED_SAVE",false);
            }
//            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.NEW_BORN_PNC_1_4)){
//                HnppJsonFormUtils.changeFormTitle(jsonForm,FormApplicability.getPncTitle(baseEntityId));
//                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", (FormApplicability.getPNCCount(baseEntityId)+1)+"");
////                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", date);
//                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
//                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", HnppConstants.getTodayDate());
//            }

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


//           else
//               intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());



            form.setActionBarBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);

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
    private void startChildForm() throws Exception{
        JSONObject form = HnppJsonFormUtils.getJsonObject(CoreConstants.JSON_FORM.CHILD_REGISTER);
        Map<String, String> womenInfo = HnppDBUtils.getMotherName(baseEntityId);
        HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyBaseEntityId);
        if(houseHoldInfo!=null){
            HnppJsonFormUtils.updateFormWithMemberId(form,houseHoldInfo.getHouseHoldUniqueId(),familyBaseEntityId);
            HnppJsonFormUtils.updateChildFormWithMetaData(form, houseHoldInfo.getHouseHoldUniqueId(),familyBaseEntityId);
        }
        HnppJsonFormUtils.updateFormWithMotherName(form,womenInfo.get("first_name")+" "+womenInfo.get("last_name"),womenInfo.get("member_name_bengla"),baseEntityId,familyBaseEntityId,womenInfo.get("phone_number"));
        HnppJsonFormUtils.updateFormWithBlockInfo(form,familyBaseEntityId);
        startFormActivity(form);

    }

    private void openServiceForm(){
        if(referralFollowUpModel!=null){
            openReferealFollowUp(referralFollowUpModel);
        }else if(needToStartHomeVisit){
            HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                @Override
                public void onPost(double latitude, double longitude) {
                    HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,false,verificationNeeded,isVerified,"",latitude,longitude);

                }
            });
            needToStartHomeVisit = false;
        }
        else{
            startAnyFormActivity(requestedFormName,requestedRequestCode);
        }
    }


    Dialog dialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog!=null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?getString(R.string.service_done_succ):isSuccess==3?getString(R.string.service_already_given):getString(R.string.survice_done_failed));
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(v -> {
            dialog.dismiss();
            dialog = null;
            isProcessingHV = false;
            isProcessingANCVisit = false;
                if(memberHistoryFragment !=null){
                    handler.postDelayed(() -> {
                        hideProgressDialog();
                        if(profileMemberFragment !=null){
                            profileMemberFragment.updateStaticView();
                        }
                        mViewPager.setCurrentItem(0,true);
                    },500);
                }
        });
        dialog.show();

    }

    boolean isProcessingHV = false;
    boolean isProcessingANCVisit = false;
    private float systolic,diastolic,heartrate,fasting,random;
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
        if(resultCode == Activity.RESULT_OK && requestCode == IOTActivity.IOT_REQUEST_CODE){
            String yesNoOption = data.getStringExtra(IOTActivity.EXTRA_INTENT_DATA);
            if(yesNoOption.equalsIgnoreCase("no")){
                startAnyFormActivity(formName,REQUEST_HOME_VISIT);
            }else{
                systolic = data.getFloatExtra(IOTActivity.EXTRA_SYSTOLIC,0);
                diastolic = data.getFloatExtra(IOTActivity.EXTRA_DIASTOLIC,0);
                heartrate = data.getFloatExtra(IOTActivity.EXTRA_HR,0);
                fasting = data.getFloatExtra(IOTActivity.EXTRA_FASTING,0);
                random = data.getFloatExtra(IOTActivity.EXTRA_RANDOM,0);
                startAnyFormActivity(formName,REQUEST_HOME_VISIT);
            }
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
                                    if(isSave.get() == -1){
                                        hideProgressDialog();
                                        finish();
                                        openFamilyDueTab();
                                    }
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
                    try{
                        JSONObject formWithConsent = new JSONObject(jsonString);
                        presenter().updateFamilyMember(formWithConsent.toString());
                    }catch (JSONException je){
                        je.printStackTrace();
                    }
                }
                else if(form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE).equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_REGISTRATION)){
                    presenter().saveChildForm(jsonString);
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
                        HnppJsonFormUtils.setEncounterDateTime(form);

                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());
                        HnppConstants.appendLog("SAVE_VISIT","baseEntityId:"+baseEntityId+":formSubmissionId:"+formSubmissionId);

                        Visit visit = HnppJsonFormUtils.saveVisit(baseEntityId, type, jsonStrings,formSubmissionId,visitId,jsonString);

                        if(visit!=null && !visit.getVisitId().equals("0")){
                            HnppHomeVisitIntentService.processVisits();
                            FormParser.processVisitLog(visit);
                            HnppConstants.appendLog("SAVE_VISIT","processVisitLog done formSubmissionId:"+formSubmissionId+":type:"+type);

                           // return true;
                            if(visit.getVisitId().equals("-1")){
                                e.onNext(-1);
                            }else{
                                e.onNext(1);
                            }
                            //success
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
            case R.id.action_show_risk:
                RiskyDataDisplayActivity.startInvalidActivity(baseEntityId,HnppFamilyOtherMemberProfileActivity.this);

                return true;
            case R.id.action_general_disease:
                openMemberDisease();
                return true;
            case R.id.action_member_survey:
                openMemberProfileUpdate();
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
    public void openPragnencyHistory(){
       // MemberHistoryDialogFragment.getInstance(this,this.getIntent().getExtras());
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

                    JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);


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
    public void openMemberDisease() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.MEMBER_DISEASE,REQUEST_HOME_VISIT);

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

                    JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
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
                    form.setWizard(true);
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
        this.formName = formName;
//        if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC_VISIT_FORM)){
//            IOTActivity.startIOTActivity(this,true);
//        }else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM)){
//            IOTActivity.startIOTActivity(this,false);
//        }
//        else{
            startAnyFormActivity(formName,REQUEST_HOME_VISIT);
       // }
        //

    }
    private boolean needToStartHomeVisit = false;
    private String formName;

    public void openHomeVisitForm(){
        if(!HnppApplication.getStockRepository().isAvailableStock(CoreConstants.EventType.ANC_HOME_VISIT)){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        needToStartHomeVisit = false;
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppHomeVisitActivity.startMe(HnppFamilyOtherMemberProfileActivity.this, new MemberObject(commonPersonObject), false,false,verificationNeeded,isVerified,"",latitude,longitude);

            }
        });

    }

    @Override
    protected void initializePresenter() {
        commonPersonObject = (CommonPersonObjectClient) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON);
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
        familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        String villageTown = getIntent().getStringExtra(Constants.INTENT_KEY.VILLAGE_TOWN);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        isComesFromGlobalSearch = getIntent().getBooleanExtra(IS_COMES_GLOBAL_SEARCH,false);
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

        menu.findItem(R.id.action_remove_member).setTitle(R.string.remove_member_or_migrade);
        menu.findItem(R.id.action_anc_registration).setTitle(R.string.pregnancy_reg);
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setTitle(R.string.pnc_reg);
        menu.findItem(R.id.action_pregnancy_out_come).setTitle(R.string.preg_outcome);
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
        menu.findItem(R.id.action_kmc_home).setVisible(false);
        menu.findItem(R.id.action_kmc_hospital).setVisible(false);
        menu.findItem(R.id.action_scanu_followup).setVisible(false);
    }
    private void updateRiskView(boolean isRisk){
        findViewById(R.id.risk_view).setVisibility(isRisk?View.VISIBLE:View.GONE);
    }
    public void updatePregnancyOutcomeVisible(String eventType){
//        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY) || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)

        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){

            menu.findItem(R.id.action_pregnancy_out_come).setVisible(true);
            addChildBtn.setVisibility(View.GONE);
            updateRiskView(HnppDBUtils.isAncRisk(baseEntityId));
        }else{
            updateRiskView(HnppDBUtils.isPncRisk(baseEntityId));
            menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);
            if(gender.equalsIgnoreCase("F") && maritalStatus!=null && maritalStatus.equalsIgnoreCase("Married") && FormApplicability.getNoOfBornChild(baseEntityId)){
                addChildBtn.setVisibility(View.VISIBLE);
            }else{
                addChildBtn.setVisibility(View.GONE);
            }
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
