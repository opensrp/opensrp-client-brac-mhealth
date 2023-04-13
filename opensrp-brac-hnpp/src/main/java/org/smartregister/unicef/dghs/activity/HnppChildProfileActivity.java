package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.MUACWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.adapter.ReferralCardViewAdapter;
import org.smartregister.unicef.dghs.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.unicef.dghs.fragment.ChildHistoryFragment;
import org.smartregister.unicef.dghs.fragment.ChildImmunizationFragment;
import org.smartregister.unicef.dghs.fragment.GMPFragment;
import org.smartregister.unicef.dghs.fragment.HnppChildProfileDueFragment;
import org.smartregister.unicef.dghs.fragment.MemberOtherServiceFragment;
import org.smartregister.unicef.dghs.listener.OnClickFloatingMenu;
import org.smartregister.unicef.dghs.listener.OnPostDataWithGps;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.model.Survey;
import org.smartregister.unicef.dghs.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.ChildDBConstants;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.HouseHoldInfo;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreUpcomingServicesActivity;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.presenter.HnppChildProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

public class HnppChildProfileActivity extends HnppCoreChildProfileActivity implements WeightActionListener, HeightActionListener, MUACActionListener, VaccinationActionListener, ServiceActionListener {
    public FamilyMemberFloatingMenu familyFloatingMenu;
    public RelativeLayout referralRow;
    public RecyclerView referralRecyclerView;
    public CommonPersonObjectClient commonPersonObject;
    Handler handler;
    AppExecutors appExecutors = new AppExecutors();
    private ImageView imageView;
    @Override
    protected void onCreation() {
        super.onCreation();
        handler = new Handler();
        initializePresenter();
        onClickFloatingMenu = getOnClickFloatingMenu(this, (HnppChildProfilePresenter) presenter);
        setupViews();
        setUpToolbar();
        HnppConstants.isViewRefresh = false;
        Toolbar toolbar = (Toolbar)this.findViewById(R.id.collapsing_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    public void updateProfileIconColor(int color,String text){

        if(!TextUtils.isEmpty(text)){
            imageView .setColorFilter(ContextCompat.getColor(this, color), android.graphics.PorterDuff.Mode.MULTIPLY);

        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void updateTopBar() {
        if (gender.equalsIgnoreCase("পুরুষ")) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_blue));
        } else if (gender.equalsIgnoreCase("মহিলা")) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_pink));
        }
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));
    }
    @Override
    public void setProfileName(String fullName) {
        patientName = fullName;
        textViewParentName.setText(fullName);
    }

    @Override
    public void setParentName(String parentName) {

        textViewGender.append(","+parentName);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(HnppConstants.isPALogin()){
            openAsReadOnlyMode(jsonForm);
            return;
        }
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
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
    public void setAge(String age) {
        textViewChildName.setText(age);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int i = view.getId();
        if (i == R.id.last_visit_row) {
            openMedicalHistoryScreen();
        } else if (i == R.id.most_due_overdue_row) {
            openUpcomingServicePage();
        } else if (i == R.id.textview_record_visit || i == R.id.record_visit_done_bar) {
            openVisitHomeScreen(false);
        } else if (i == R.id.textview_edit) {
            openVisitHomeScreen(true);
        }
    }

    @Override
    protected void initializePresenter() {
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        if (familyName == null) {
            familyName = "";
        }

        presenter = new HnppChildProfilePresenter(this, new CoreChildProfileModel(familyName), houseHoldId, childBaseEntityId);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        imageView = findViewById(R.id.imageview_profile);
        HnppConstants.updateAppBackground(findViewById(org.smartregister.chw.core.R.id.collapsing_toolbar));
        initializeTasksRecyclerView();
        View recordVisitPanel = findViewById(R.id.record_visit_panel);
        recordVisitPanel.setVisibility(View.GONE);
        familyFloatingMenu = new FamilyMemberFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.END);
        addContentView(familyFloatingMenu, linearLayoutParams);
        prepareFab();
        fetchProfileData();
        familyFloatingMenu.hideFab();
        //presenter().fetchTasks();


    }

    @Override
    public void setServiceNameDue(String serviceName, String dueDate) {
        if (!TextUtils.isEmpty(serviceName)) {
            if(profileMemberFragment != null){
                profileMemberFragment.updateChildDueEntry(1,serviceName,dueDate);
            }
        }
    }

    @Override
    public void setServiceNameOverDue(String serviceName, String dueDate) {
        if(profileMemberFragment != null){
            profileMemberFragment.updateChildDueEntry(2,serviceName,dueDate);
        }
    }

    @Override
    public void setServiceNameUpcoming(String serviceName, String dueDate) {
        if(profileMemberFragment != null){
            profileMemberFragment.updateChildDueEntry(3,serviceName,dueDate);
        }

    }
//    MemberOtherServiceFragment memberOtherServiceFragment;
    ChildHistoryFragment memberHistoryFragment;
    HnppChildProfileDueFragment profileMemberFragment;
    ChildImmunizationFragment childImmunizationFragment;
    GMPFragment growthFragment;
    ViewPager mViewPager;
    protected ViewPagerAdapter adapter;
    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        commonPersonObject = ((HnppChildProfilePresenter)presenter()).commonPersonObjectClient;
        this.mViewPager = viewPager;
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if(!HnppConstants.isPALogin()){
            profileMemberFragment =(HnppChildProfileDueFragment) HnppChildProfileDueFragment.newInstance(this.getIntent().getExtras());
            profileMemberFragment.setCommonPersonObjectClient(commonPersonObject);
            profileMemberFragment.setBaseEntityId(childBaseEntityId);
            adapter.addFragment(profileMemberFragment, this.getString(R.string.due).toUpperCase());
        }

//            memberOtherServiceFragment = new MemberOtherServiceFragment();
            memberHistoryFragment = ChildHistoryFragment.getInstance(this.getIntent().getExtras());
            memberHistoryFragment.setBaseEntityId(childBaseEntityId);
//            memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
            childImmunizationFragment = ChildImmunizationFragment.newInstance(this.getIntent().getExtras());
            childImmunizationFragment.setChildDetails(commonPersonObject);
            growthFragment = GMPFragment.newInstance(this.getIntent().getExtras());
            growthFragment.setChildDetails(commonPersonObject);
            adapter.addFragment(childImmunizationFragment, this.getString(R.string.immunization).toUpperCase());
            adapter.addFragment(growthFragment, this.getString(R.string.gmp).toUpperCase());
//            adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());
            adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
            if(HnppConstants.isPALogin()){
                viewPager.setOffscreenPageLimit(2);
            }else{
                viewPager.setOffscreenPageLimit(3);
            }


        viewPager.setAdapter(adapter);
        return viewPager;
    }
    public void startChildHomeVisit(){

    }
    @Override
    public void setFamilyHasNothingDue() {
        layoutFamilyHasRow.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyHasServiceDue() {
        layoutFamilyHasRow.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyHasServiceOverdue() {
        layoutFamilyHasRow.setVisibility(View.GONE);
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        hideProgressBar();
        if (!hasPhone) {
            familyFloatingMenu.hideFab();
        }
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        handler.postDelayed(() -> {
            if (referralRecyclerView != null && taskList.size() > 0) {
                RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, ((HnppChildProfilePresenter) presenter()).getChildClient(), CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY);
                referralRecyclerView.setAdapter(mAdapter);
                referralRow.setVisibility(View.VISIBLE);

            }
        }, 100);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sick_child_follow_up:
                displayShortToast(R.string.clicked_sick_child);
                return true;
            case R.id.action_malaria_diagnosis:
                displayShortToast(R.string.clicked_malaria_diagnosis);
                return true;
            case R.id.action_remove_member:
                removeIndividualProfile();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void removeIndividualProfile() {
        Timber.d("Remove member action is not required in HF");
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HnppChildProfileActivity.this,
                commonPersonObject, "", "", "", FamilyRegisterActivity.class.getCanonicalName());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_anc_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(true);
        menu.findItem(R.id.action_remove_member).setTitle("সদস্য বাদ দিন / মাইগ্রেট / মৃত্যু");
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null) handler.removeCallbacksAndMessages(null);
    }

    private void openMedicalHistoryScreen() {
//        Map<String, Date> vaccine = ((HnppChildProfilePresenter) presenter()).getVaccineList();
//        CoreChildMedicalHistoryActivity.startMedicalHistoryActivity(this, ((CoreChildProfilePresenter) presenter()).getChildClient(), patientName, lastVisitDay,
//                ((HnppChildProfilePresenter) presenter()).getDateOfBirth(), new LinkedHashMap<>(vaccine), CoreChildMedicalHistoryActivity.class);
//
    }

    private void openUpcomingServicePage() {
        CoreUpcomingServicesActivity.startUpcomingServicesActivity(this, ((HnppChildProfilePresenter) presenter()).getChildClient());
    }
    public void openVisitHomeScreen(boolean isEditMode) {
        ChildVaccinationActivity.startChildVaccinationActivity(this,this.getIntent().getExtras(),commonPersonObject);
        //ChildHomeVisitActivity.startMe(this, memberObject, isEditMode, ChildHomeVisitActivity.class);
    }
    public void openGMPScreen() {
        ChildGMPActivity.startGMPActivity(this,this.getIntent().getExtras(),commonPersonObject);
        //ChildHomeVisitActivity.startMe(this, memberObject, isEditMode, ChildHomeVisitActivity.class);
    }

    @SuppressLint("NonConstantResourceId")
    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final HnppChildProfilePresenter presenter) {
        return viewId -> {
            switch (viewId) {
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, presenter.getFamilyId());
                    /*Intent intent = new Intent(HnppChildProfileActivity.this, PaymentActivity.class);
                    startActivity(intent);*/
                    break;
                case R.id.refer_to_facility_fab:
                    Toast.makeText(activity, "Refer to facility", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    }

    private void initializeTasksRecyclerView() {
        referralRecyclerView = findViewById(R.id.referral_card_recycler_view);
        referralRow = findViewById(R.id.referal_row);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        referralRecyclerView.setLayoutManager(layoutManager);
    }

    private void prepareFab() {
        familyFloatingMenu.fab.setOnClickListener(v -> FamilyCallDialogFragment.launchDialog(
                this, ((HnppChildProfilePresenter) presenter).getFamilyId()));

    }
    public void openServiceForms(String formName){
        startAnyFormActivity(formName,REQUEST_HOME_VISIT);
    }

    public void openEnc() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.ENC_REGISTRATION,REQUEST_HOME_VISIT);
    }
    public void openChildProfileVisit() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_PROFILE_VISIT,REQUEST_HOME_VISIT);
    }
    public void openRefereal() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_REFERRAL,REQUEST_HOME_VISIT);
    }

    public void openFollowUp() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openChildInfo(String eventType) {
        startAnyFormActivity(HnppConstants.eventTypeFormNameMapping.get(eventType),REQUEST_HOME_VISIT);
    }
    public void openCoronaIndividualForm(){
        Intent intent = new Intent(this, HnppAncJsonFormActivity.class);
        try{
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);
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
            if(HnppConstants.isPALogin()){
                form.setHideSaveLabel(true);
                form.setSaveLabel("");
            }
            this.startActivityForResult(intent, REQUEST_HOME_VISIT);

        }catch (Exception e){

        }


    }
    public void openReferealFollowUp(ReferralFollowUpModel referralFollowUpModel) {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    if(TextUtils.isEmpty(childBaseEntityId)){
                        Toast.makeText(HnppChildProfileActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "openReferealFollowUp>>childBaseEntityId:"+childBaseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(HnppChildProfileActivity.this).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, childBaseEntityId);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,getFamilyBaseId());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralReason(),referralFollowUpModel.getReferralPlace());
                    Intent intent;
                    intent = new Intent(HnppChildProfileActivity.this, HnppAncJsonFormActivity.class);
                    intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

                    Form form = new Form();
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);

                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }
                    if(HnppConstants.isPALogin()){
                        form.setHideSaveLabel(true);
                        form.setSaveLabel("");
                    }
                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
                    intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
                    startActivityForResult(intent, REQUEST_HOME_VISIT);

                }catch (Exception e){

                }
            }
        });


    }
    private String getFamilyBaseId(){
        return ((HnppChildProfilePresenter)presenter).getFamilyID();
    }
    public void startAnyFormActivity(String formName, int requestCode) {
//        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
//            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
//            return;
//        }
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                try {
                    if(TextUtils.isEmpty(childBaseEntityId)){
                        Toast.makeText(HnppChildProfileActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:"+childBaseEntityId+":formName:"+formName);

                    JSONObject jsonForm = FormUtils.getInstance(HnppChildProfileActivity.this).getFormJson(formName);
//                    try{
//                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,getFamilyBaseId());
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
                    try{
                        HnppJsonFormUtils.addAddToStockValue(jsonForm);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(HnppConstants.JSON_FORMS.IYCF_PACKAGE.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        updateFormField(jsonArray,"dob",dobFormate);
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        updateFormField(jsonArray,"weight",birthWeight);
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        String prevalue = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(childBaseEntityId+"_SOLID_FOOD");
                        if(!TextUtils.isEmpty(prevalue)){
                            updateFormField(jsonArray,"solid_food_month",prevalue);
                            JSONObject solidObj = getFieldJSONObject(jsonArray, "solid_food_month");
                            solidObj.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                        }
                        updateFormField(jsonArray,"dob",dobFormate);
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_INFO_7_24_MONTHS.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);

                        updateFormField(jsonArray,"dob",dobFormate);
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_PROFILE_VISIT.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);

                        updateFormField(jsonArray,"dob",dobFormate);
                    }
                    if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)){
                        if(gender.equalsIgnoreCase("F")){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_women","true");
                        }
                    }
                    jsonForm.put(JsonFormUtils.ENTITY_ID, memberObject.getFamilyHead());
                    Intent intent = new Intent(HnppChildProfileActivity.this, HnppAncJsonFormActivity.class);
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
//            }
//        });


    }
    boolean isProcessing = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(TextUtils.isEmpty(childBaseEntityId)){
            Toast.makeText(HnppChildProfileActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (resultCode == Activity.RESULT_OK){
            HnppConstants.isViewRefresh = true;
            if(data!=null && data.getBooleanExtra("VACCINE_TAKEN",false)){

                    appExecutors.diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                            NCUtils.startClientProcessing();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });


                VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);

            }

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){
            if(isProcessing) return;
            AtomicInteger isSave = new AtomicInteger(2);
            showProgressDialog(R.string.please_wait_message);

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
            String visitId = JsonFormUtils.generateRandomUUIDString();
            HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

           processVisitFormAndSave(jsonString,formSubmissionId,visitId)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Observer<Integer>() {
                       @Override
                       public void onSubscribe(Disposable d) {

                       }

                       @Override
                       public void onNext(Integer integer) {
                           isSave.set(integer);
                       }

                       @Override
                       public void onError(Throwable e) {
                           hideProgressDialog();
                       }

                       @Override
                       public void onComplete() {
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
          /*  Runnable runnable = () -> {
                if(!isProcessing){
                    isProcessing = true;
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                    String visitId = JsonFormUtils.generateRandomUUIDString();
                    HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

                    isSave.set(processVisitFormAndSave(jsonString,formSubmissionId,visitId));
                }
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(isSave.get()){
                            hideProgressDialog();
                            showServiceDoneDialog(true);
                        }else {
                            hideProgressDialog();
                            showServiceDoneDialog(false);
                        }
                    }
                });
            };
            appExecutors.diskIO().execute(runnable);*/

        }else if(resultCode == Activity.RESULT_OK && requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT){
           if(mViewPager!=null) mViewPager.setCurrentItem(0,true);
        } else if(resultCode == Activity.RESULT_OK && requestCode == ChildVaccinationActivity.VACCINE_REQUEST_CODE){
            profileMemberFragment.setUserVisibleHint(true);
        }

        super.onActivityResult(requestCode, resultCode, data);

    }
    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionid, String visitId){
       return Observable.create(e-> {
            if(TextUtils.isEmpty(childBaseEntityId)) e.onNext(2);
            try {
                JSONObject form = new JSONObject(jsonString);
                String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);
                Map<String, String> jsonStrings = new HashMap<>();
                jsonStrings.put("First",form.toString());
                HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":type:"+type);

                Visit visit = HnppJsonFormUtils.saveVisit(false,false,false,"", childBaseEntityId, type, jsonStrings, "",formSubmissionid,visitId);
                if(visit!=null && !visit.getVisitId().equals("0")){
                    HnppHomeVisitIntentService.processVisits();
                    FormParser.processVisitLog(visit);
                    HnppConstants.appendLog("SAVE_VISIT", "processVisitLog done:"+formSubmissionid+":type:"+type);

                    //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                    e.onNext(1);
                    e.onComplete();

                }else if(visit != null && visit.getVisitId().equals("0")){
                    e.onNext(3);
                    e.onComplete();
                }else{
                    e.onNext(2);
                    e.onComplete();
                }
            } catch (Exception ex) {
                HnppConstants.appendLog("SAVE_VISIT","exception processVisitFormAndSave >>"+ex.getMessage());
                e.onNext(1);
                e.onComplete();
            }
        });
    }
    Dialog dialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog != null) return;
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
                isProcessing = false;
                //if(isSuccess){
                    if(memberHistoryFragment !=null){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                mViewPager.setCurrentItem(3,true);
//                                if(memberOtherServiceFragment !=null){
//                                    memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
//                                    memberOtherServiceFragment.updateStaticView();
//                                }

                            }
                        },1000);
                    }
                //}
            }
        });
        dialog.show();

    }

    public void openFamilyDueTab() {
        Intent intent = new Intent(this,FamilyProfileActivity.class);
        String familyId = Utils.getValue(commonPersonObject, ChildDBConstants.KEY.RELATIONAL_ID, false);
        HouseHoldInfo houseHoldInfo = HnppDBUtils.getHouseHoldInfo(familyId);
        if(houseHoldInfo !=null){
            intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, houseHoldInfo.getHouseHoldHeadId());
            intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, houseHoldInfo.getPrimaryCaregiverId());
            intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, houseHoldInfo.getHouseHoldName());
            intent.putExtra(DBConstants.KEY.UNIQUE_ID, houseHoldInfo.getHouseHoldUniqueId());
            intent.putExtra(HnppConstants.KEY.MODULE_ID, houseHoldInfo.getModuleId());

        }
        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, familyId);
        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, Utils.getValue(commonPersonObject, HnppConstants.KEY.VILLAGE_NAME, false));

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }
    @Override
    public void onHeightTaken(HeightWrapper heightWrapper) {
        if(growthFragment!=null){
            growthFragment.onHeightTaken(heightWrapper);
        }
    }

    @Override
    public void onMUACTaken(MUACWrapper muacWrapper) {
        if(growthFragment!=null){
            growthFragment.onMUACTaken(muacWrapper);
        }
    }

    @Override
    public void onWeightTaken(WeightWrapper weightWrapper) {
        if(growthFragment!=null){
            growthFragment.onWeightTaken(weightWrapper);
        }
    }

    @Override
    public void onGiveToday(ServiceWrapper serviceWrapper, View view) {
        childImmunizationFragment.onGiveToday(serviceWrapper,view);
    }

    @Override
    public void onGiveEarlier(ServiceWrapper serviceWrapper, View view) {
        childImmunizationFragment.onGiveEarlier(serviceWrapper,view);
    }

    @Override
    public void onUndoService(ServiceWrapper serviceWrapper, View view) {
        childImmunizationFragment.onUndoService(serviceWrapper,view);
    }

    @Override
    public void onVaccinateToday(ArrayList<VaccineWrapper> arrayList, View view) {
        childImmunizationFragment.onVaccinateToday(arrayList,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);

    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        childImmunizationFragment.onVaccinateEarlier(arrayList,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);

    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        childImmunizationFragment.onUndoVaccination(vaccineWrapper,view);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);

    }
}
