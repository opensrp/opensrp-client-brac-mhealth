package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.text.HtmlCompat;
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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
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
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.adapter.ReferralCardViewAdapter;
import org.smartregister.unicef.mis.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.unicef.mis.fragment.ChildHistoryFragment;
import org.smartregister.unicef.mis.fragment.ChildImmunizationFragment;
import org.smartregister.unicef.mis.fragment.GMPFragment;
import org.smartregister.unicef.mis.fragment.HnppChildProfileDueFragment;
import org.smartregister.unicef.mis.imci.activity.ImciMainActivity;
import org.smartregister.unicef.mis.imci.fragment.IMCIAssessmentDialogFragment;
import org.smartregister.unicef.mis.imci.fragment.IMCIAssessmentReportDialogFragment;
import org.smartregister.unicef.mis.job.VaccineDueUpdateServiceJob;
import org.smartregister.unicef.mis.listener.OnClickFloatingMenu;
import org.smartregister.unicef.mis.model.ReferralFollowUpModel;
import org.smartregister.unicef.mis.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.ChildDBConstants;
import org.smartregister.unicef.mis.utils.FormApplicability;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.HouseHoldInfo;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreUpcomingServicesActivity;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.presenter.HnppChildProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.mis.utils.ReferralData;
import org.smartregister.unicef.mis.utils.RiskyModel;
import org.smartregister.util.DateUtil;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.unicef.mis.utils.HnppConstants.showDialogWithAction;

public class HnppChildProfileActivity extends HnppCoreChildProfileActivity implements WeightActionListener, HeightActionListener, MUACActionListener, VaccinationActionListener, ServiceActionListener {
    private static final int REQUEST_AEFI_CHILD = 55551;
    public FamilyMemberFloatingMenu familyFloatingMenu;
    public RelativeLayout referralRow;
    public RecyclerView referralRecyclerView;
    public CommonPersonObjectClient commonPersonObject;
    Handler handler;
    AppExecutors appExecutors = new AppExecutors();
    private ImageView imageView,aefiImageBtn,missedImageBtn;
    private boolean hasAefi = false;
    private String aefiVaccine = "";
    private Button memberSurveyBtn;
    @Override
    protected void onCreation() {
        super.onCreation();
        handler = new Handler();
        initializePresenter();
        onClickFloatingMenu = getOnClickFloatingMenu(this, (HnppChildProfilePresenter) presenter);
        setupViews();
        setUpToolbar();
        memberSurveyBtn = findViewById(R.id.member_visit_btn);
        Toolbar toolbar = (Toolbar)this.findViewById(R.id.collapsing_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        findViewById(R.id.update_profile_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HnppChildProfilePresenter) presenter()).startFormForEdit(getResources().getString(org.smartregister.chw.core.R.string.edit_child_form_title),
                        ((HnppChildProfilePresenter) presenter()).getChildClient());
            }
        });
        findViewById(R.id.imci_report_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMCIAssessmentReportDialogFragment.getInstance(HnppChildProfileActivity.this).setBaseEntityId(childBaseEntityId);
            }
        });
        memberSurveyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChildProfileVisit();
            }
        });
        findViewById(R.id.is_risk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRiskFactorDialog();
            }
        });

    }
    public void showScanuFollowUpDialog(Context context){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_two_button);
        TextView textViewTitle = dialog.findViewById(R.id.text_tv);
        TextView titleTxt = dialog.findViewById(R.id.title_tv);
        titleTxt.setText(R.string.followup_scanu);
        textViewTitle.setText(R.string.agree_scanu_followup);
        dialog.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.ok_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openScanuFollowup();
            }
        });
        dialog.show();
    }
    @SuppressLint("SetTextI18n")
    private void openRiskFactorDialog(){

        String weight = HnppDBUtils.getBirthWeight(childBaseEntityId);
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        TextView message = dialog.findViewById(R.id.text_tv);
        titleTv.setText(R.string.risk_type);
        if(!TextUtils.isEmpty(weight) && Integer.parseInt(weight)<2000){
            message.setText(getString(R.string.child_weight_less)+weight+getString(R.string.gm));
        }
        ArrayList<RiskyModel> riskyModels = HnppApplication.getRiskDetailsRepository().getRiskyKeyByEntityId(childBaseEntityId);
        StringBuilder builder = new StringBuilder();
        for (RiskyModel riskyModel:riskyModels) {
            String[] fs= riskyModel.riskyValue.split(",");
            if(fs.length>0){
                for (String key:fs) {
                    Log.v("RISK_FACTOR","key>>"+key+":value:"+riskyModel.riskyValue);
                    builder.append(HnppConstants.getRiskeyFactorMapping().get(key)==null?key:HnppConstants.getRiskeyFactorMapping().get(key));
                   // builder.append(":");
                    //builder.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue)==null?riskyModel.riskyValue:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue));

                    builder.append("\n");
                }
            }else{
                Log.v("RISK_FACTOR","key>>"+riskyModel.riskyKey+":value:"+riskyModel.riskyValue);
                builder.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey)==null?riskyModel.riskyKey:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyKey));
                //builder.append(":");
                //builder.append(HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue)==null?riskyModel.riskyValue:HnppConstants.getRiskeyFactorMapping().get(riskyModel.riskyValue));
                builder.append("\n");
            }

        }
        if(!TextUtils.isEmpty(builder.toString())){
            message.append("\n");
            message.append(builder.toString());
        }

        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    public void updateProfileIconColor(int color,String text){

//        if(!TextUtils.isEmpty(text)){
//            imageView .setColorFilter(ContextCompat.getColor(this, color), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void updateTopBar() {
        commonPersonObject = ((HnppChildProfilePresenter)presenter()).commonPersonObjectClient;
        if (gender.equalsIgnoreCase( getString(R.string.man))) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_blue));
        } else if (gender.equalsIgnoreCase( getString(R.string.woman))) {
            imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_pink));
        }
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));
        String aefi = Utils.getValue(commonPersonObject.getColumnmaps(), HnppConstants.KEY.HAS_AEFI, false);
        String isRisk = Utils.getValue(commonPersonObject.getColumnmaps(), HnppConstants.KEY.IS_RISK, false);
        String vaccineDueDate = Utils.getValue(commonPersonObject.getColumnmaps(), HnppConstants.KEY.DUE_VACCINE_DATE, false);
        if(HnppConstants.isMissedSchedule(vaccineDueDate)){
            findViewById(R.id.missed_schedule_img).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.missed_schedule_img).setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(aefi)){
            hasAefi = aefi.equalsIgnoreCase("yes")|| aefi.equalsIgnoreCase( getString(R.string.yes));
            if(hasAefi){
                aefiImageBtn.setVisibility(View.VISIBLE);
            }
        }
        int count = HnppApplication.getImciReportRepository().getIMCIReportCount(childBaseEntityId);
        if(count>0){
            findViewById(R.id.imci_report_btn).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.imci_report_btn).setVisibility(View.GONE);
        }
        ReferralData referralData = HnppApplication.getReferralRepository().getIsReferralDataById(childBaseEntityId);
        if(referralData!=null){
            findViewById(R.id.child_followup).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.child_followup).setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(isRisk) && (isRisk.equalsIgnoreCase("1") || isRisk.equalsIgnoreCase("true"))){
            findViewById(R.id.is_risk).setVisibility(View.VISIBLE);
        }
        aefiVaccine = Utils.getValue(commonPersonObject.getColumnmaps(), HnppConstants.KEY.AEFI_VACCINE, false);


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
        form.setWizard(true);

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
        aefiImageBtn = findViewById(R.id.aefi_child_img);
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
        aefiImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAefiVaccineDialog();
            }
        });
        findViewById(R.id.child_followup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowupBasedOnAge();
            }
        });
        findViewById(R.id.child_followup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFollowupBasedOnAge();
            }
        });
        findViewById(R.id.home_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HnppChildProfileActivity.this, FamilyRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                finish();
            }
        });


    }
    private void openFollowupBasedOnAge(){
        String dobString = Utils.getValue(commonPersonObject.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false);
        Date dob = Utils.dobStringToDate(dobString);
        String eventType = FormApplicability.isDueChildEccd(dob);
        if(TextUtils.isEmpty(eventType))return;
        if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_2_3_MONTH)){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_2_3_MONTH);
        }
        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_4_6_MONTH) ){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_4_6_MONTH);
        }
        else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_7_9_MONTH) ){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_7_9_MONTH);
        }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_10_12_MONTH)){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_10_12_MONTH);
        }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_18_MONTH) ){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_18_MONTH);
        }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_24_MONTH)){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_24_MONTH);
        }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_ECCD_36_MONTH) ){
            openChildInfo(HnppConstants.EVENT_TYPE.CHILD_ECCD_36_MONTH);
        }
    }
    private void openAefiVaccineDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        TextView message = dialog.findViewById(R.id.text_tv);
        titleTv.setText(R.string.problems_occure_after_types_of_vaccine);
        message.setText(getVaccineName());
        Button ok_btn = dialog.findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * getting real vaccine name here
     */
    private String getVaccineName(){
        String[] vaccineKeyList = aefiVaccine.split(",");
        StringBuilder realVaccineName = new StringBuilder();

        for(String key : vaccineKeyList){
            realVaccineName.append(HnppConstants.getVaccineNameMapping().get(key)).append("\n");
        }
        return  realVaccineName.toString();
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
        commonPersonObject.getColumnmaps().put(org.smartregister.family.util.DBConstants.KEY.GENDER,gender);
        if(!HnppConstants.isPALogin()){
            profileMemberFragment =(HnppChildProfileDueFragment) HnppChildProfileDueFragment.newInstance(this.getIntent().getExtras());
            profileMemberFragment.setCommonPersonObjectClient(commonPersonObject);
            profileMemberFragment.setBaseEntityId(childBaseEntityId);

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
            adapter.addFragment(profileMemberFragment, this.getString(R.string.due).toUpperCase());
//            adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());
            adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
            if(HnppConstants.isPALogin()){
                viewPager.setOffscreenPageLimit(2);
            }else{
                viewPager.setOffscreenPageLimit(4);
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
            case R.id.action_general_disease:
                openChildDiseaseForm();
                return true;
            case R.id.action_member_survey:
                openChildProfileVisit();
                return true;
            case R.id.action_scanu_followup:
                showScanuFollowUpDialog(HnppChildProfileActivity.this);
                return true;
            case R.id.action_kmc_home:
                openKMCHome();
                return true;
            case R.id.action_kmc_hospital:
                openKMCHomeFollowUp();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    boolean isComesFromDeath = false;
    protected void removeIndividualProfile() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HnppChildProfileActivity.this,
                commonPersonObject, "", isComesFromDeath, FamilyRegisterActivity.class.getCanonicalName());
    }
    Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.findItem(R.id.action_anc_registration).setVisible(false);
        menu.findItem(R.id.action_malaria_registration).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(true);
        menu.findItem(R.id.action_remove_member).setTitle(R.string.remove_member_or_migrade);
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);

        return true;
    }
    public void updateScanuFollowupMenu(boolean isVisible){
        this.menu.findItem(R.id.action_scanu_followup).setVisible(isVisible);
    }
    public void updateKMCFollowupMenu(boolean isVisible){
        this.menu.findItem(R.id.action_kmc_home).setVisible(isVisible);
        this.menu.findItem(R.id.action_kmc_hospital).setVisible(isVisible);
    }
    Dialog removeChildDialog;
    public void showDeathRegistrationPopUp(){
        if(removeChildDialog!=null) return;
        isComesFromDeath = true;
        removeChildDialog = new Dialog(this);
        removeChildDialog.setCancelable(false);
        removeChildDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeChildDialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = removeChildDialog.findViewById(R.id.title_tv);
        titleTv.setText(R.string.meg_death_reg);
        titleTv.setTextColor(getResources().getColor(R.color.red));
        Button ok_btn = removeChildDialog.findViewById(R.id.ok_btn);
        ok_btn.setText(R.string.okay);
        Button close_btn = removeChildDialog.findViewById(R.id.close_btn);
        close_btn.setVisibility(View.VISIBLE);
        close_btn.setText(R.string.later_fillup);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeChildDialog.dismiss();
                finish();
            }
        });
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeChild();
            }
        });
        if(!removeChildDialog.isShowing())removeChildDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null) handler.removeCallbacksAndMessages(null);
        if(vaccineUpdateDataBroadcastReceiver!=null) unregisterReceiver(vaccineUpdateDataBroadcastReceiver);
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
    public void openGMPRefereal() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.GMP_REFERREL_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openGMPSessionPlan() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.GMP_SESSION_INFO,REQUEST_HOME_VISIT);
    }
    public void openFollowUp() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openNewBorn() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.NEW_BORN_PNC_1_4,REQUEST_HOME_VISIT);
    }
    public void openKMCHome() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.KMC_SERVICE_HOME,REQUEST_HOME_VISIT);
    }
    public void openKMCHomeFollowUp() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.KMC_HOME_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openKMCHospital() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.KMC_SERVICE_HOSPITAL,REQUEST_HOME_VISIT);
    }
    public void openKMCHospitalFollowup() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.KMC_HOSPITAL_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openScanuFollowup() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.SCANU_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openAefiForm() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.AEFI_CHILD_,REQUEST_AEFI_CHILD);
    }
    public void openChildDiseaseForm() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_DISEASE,REQUEST_HOME_VISIT);
    }
    public void openChildInfo(String eventType) {
        startAnyFormActivity(HnppConstants.eventTypeFormNameMapping.get(eventType),REQUEST_HOME_VISIT);
    }
    public void openIMCIActivity(){
        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
        Date date = Utils.dobStringToDate(DOB);
        String dobFormate = HnppConstants.DDMMYY.format(date);
        int dayPass = DateUtil.dayDifference(new LocalDate(date),new LocalDate(System.currentTimeMillis()));
        if(dayPass>60){
            ImciMainActivity.startIMCIActivity(this,childBaseEntityId,dobFormate,ImciMainActivity.REQUEST_IMCI_ACTIVITY,ImciMainActivity.IMCI_TYPE_2_59);
        }else{
            ImciMainActivity.startIMCIActivity(this,childBaseEntityId,dobFormate,ImciMainActivity.REQUEST_IMCI_ACTIVITY,ImciMainActivity.IMCI_TYPE_0_2);

        }
    }
    public void removeChild() {
        removeIndividualProfile();
    }
    public void openCoronaIndividualForm(){
        Intent intent = new Intent(this, HnppAncJsonFormActivity.class);
        try{
            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void openReferealFollowUp(ReferralFollowUpModel referralFollowUpModel) {
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onPost(double latitude, double longitude) {
                try {
                    if(TextUtils.isEmpty(childBaseEntityId)){
                        Toast.makeText(HnppChildProfileActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "openReferealFollowUp>>childBaseEntityId:"+childBaseEntityId);

                    JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, childBaseEntityId);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,0,0,getFamilyBaseId());
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
//            }
//        });


    }
    private String getFamilyBaseId(){
        return ((HnppChildProfilePresenter)presenter).getFamilyID();
    }



    public void startAnyFormActivity(String formName, int requestCode) {

                try {
                    if(TextUtils.isEmpty(childBaseEntityId)){
                        Toast.makeText(HnppChildProfileActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:"+childBaseEntityId+":formName:"+formName);

                    JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(formName);

                    try{
                        HnppJsonFormUtils.addAddToStockValue(jsonForm);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        updateFormField(jsonArray,"dob",dobFormate);
                    }catch (Exception e){
                        e.printStackTrace();

                    }
                    if(HnppConstants.JSON_FORMS.IYCF_PACKAGE.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);

                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        updateFormField(jsonArray,"weight",birthWeight);
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP.equalsIgnoreCase(formName)){
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());

                    }

                    else if(HnppConstants.JSON_FORMS.NEW_BORN_PNC_1_4.equalsIgnoreCase(formName)){
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        int newPncCount = FormApplicability.getNewPNCCount(childBaseEntityId);
                        if(newPncCount<=0){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","Yes");
                        }else{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","No");
                        }
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", HnppConstants.getScheduleNewPncDate(dobFormate,newPncCount+1));
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"dob", dobFormate);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", (newPncCount+1)+"");
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"birth_weight", birthWeight,!TextUtils.isEmpty(birthWeight));
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"mother_id", HnppDBUtils.getMotherId(childBaseEntityId));
                    }

                    else if(HnppConstants.JSON_FORMS.KMC_SERVICE_HOME.equalsIgnoreCase(formName) ||
                            HnppConstants.JSON_FORMS.KMC_HOME_FOLLOWUP.equalsIgnoreCase(formName)){
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        int newPncCount = FormApplicability.getKMCHomeFollowupCount(childBaseEntityId);
                        if(newPncCount<=0){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","Yes");
                        }else{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","No");
                        }
                        try{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", HnppConstants.getScheduleKMCHomeDate(childBaseEntityId,newPncCount+1));
                        }catch (Exception e){

                        }
                        boolean isImmature = FormApplicability.isImmature(childBaseEntityId);
                        if(isImmature){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"mother_ga", "৩৬ সপ্তাহ বা তার কম (অপরিণত শিশু)");
                        }

                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"dob", dobFormate);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", (newPncCount+1)+"");
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"birth_weight", birthWeight);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"mother_id", HnppDBUtils.getMotherId(childBaseEntityId));
                        ReferralData referralData = HnppApplication.getReferralRepository().getIsReferralDataById(childBaseEntityId);
                        if(referralData!=null) {
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm, "is_referred", getString(R.string.yes));
                        }
                    } else if(HnppConstants.JSON_FORMS.KMC_SERVICE_HOSPITAL.equalsIgnoreCase(formName)||
                            HnppConstants.JSON_FORMS.KMC_HOSPITAL_FOLLOWUP.equalsIgnoreCase(formName)){
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        int newPncCount = FormApplicability.getKMCServiceHospitalCount(childBaseEntityId);
                        if(newPncCount<=0){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","Yes");
                        }else{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","No");
                        }
                        try{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"schedule_date", HnppConstants.getScheduleKMCHomeDate(childBaseEntityId,newPncCount+1));
                        }catch (Exception e){

                        }
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"dob", dobFormate);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", (newPncCount+1)+"");
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"birth_weight", birthWeight);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"mother_id", HnppDBUtils.getMotherId(childBaseEntityId));
                    }
                    else if(HnppConstants.JSON_FORMS.SCANU_FOLLOWUP.equalsIgnoreCase(formName)){
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);
                        int newPncCount = FormApplicability.getScanuCount(childBaseEntityId);
                        if(newPncCount<=0){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","Yes");
                        }else{
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_first_time","No");
                        }
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"dob", dobFormate);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_count", (newPncCount+1)+"");
                        String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"birth_weight", birthWeight);
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"service_taken_date", HnppConstants.getTodayDate());
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"mother_id", HnppDBUtils.getMotherId(childBaseEntityId));
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_ECCD_2_3_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_4_6_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_7_9_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_10_12_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_18_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_24_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_ECCD_36_MONTH.equalsIgnoreCase(formName)
                            || HnppConstants.JSON_FORMS.CHILD_DISEASE.equalsIgnoreCase(formName) ){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        try{
                            HnppJsonFormUtils.updateFormWithSBKDivision(jsonForm);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        ReferralData referralData = HnppApplication.getReferralRepository().getIsReferralDataById(childBaseEntityId);
                        if(referralData!=null){
                            updateFormField(jsonArray,"is_referred",getString(R.string.yes));
                            updateFormField(jsonArray,"referral_id",referralData.referralId);
                            updateFormField(jsonArray,"previous_referred",getString(R.string.yes));
                            updateFormField(jsonArray,"refered_place",referralData.referralPlace);
                            updateFormField(jsonArray,"refered_date", HnppConstants.DDMMYY.format(new Date(referralData.referralDate)));
                        }
                    }
                    else if(HnppConstants.JSON_FORMS.CHILD_PROFILE_VISIT.equalsIgnoreCase(formName)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        String DOB = ((HnppChildProfilePresenter) presenter).getDateOfBirth();
                        Date date = Utils.dobStringToDate(DOB);
                        String dobFormate = HnppConstants.DDMMYY.format(date);

                        updateFormField(jsonArray,"dob",dobFormate);
                    }
                    if(HnppConstants.JSON_FORMS.GMP_REFERREL_FOLLOWUP.equalsIgnoreCase(formName)){
                        String sessionInfo = HnppDBUtils.getSessionInfo(childBaseEntityId);
                        if(sessionInfo.equalsIgnoreCase("কমিউনিটি") || sessionInfo.equalsIgnoreCase("Community")) {
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"session_info","Community");
                        }
                        if(sessionInfo.equalsIgnoreCase("স্বাস্থ্য কেন্দ্রে") || sessionInfo.equalsIgnoreCase("Health Care Facility")){                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"session_info","Community");
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"session_info","Facility");
                        }
                    }
                    if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)){
                        if(gender.equalsIgnoreCase("F")){
                            HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_women","true");
                        }
                    }
                    if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.AEFI_CHILD_)){
                        JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                        JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String key = jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase();

                            /*
                              setting aefi child status
                             */
                            if(key.equalsIgnoreCase("aefi_child_status")){
                                String child_status = getString(R.string.no);
                                if(hasAefi){
                                    child_status =  getString(R.string.yes);
                                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, true);
                                }
                                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,child_status);
                            }

                            /*
                             setting vaccine list data
                             */
                            if(key.equalsIgnoreCase("vaccine_list")){
                                if(!TextUtils.isEmpty(aefiVaccine)){

                                    HnppJsonFormUtils.processAEFIValueWithChoiceIdsForEdit(jsonObject,aefiVaccine);

                                }

                            }

                            if(key.equalsIgnoreCase("previous_vaccine_problem")){
                                if(!TextUtils.isEmpty(aefiVaccine)){
                                    jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE,aefiVaccine);

                                }

                            }


                        }
                    }
                    jsonForm.put(JsonFormUtils.ENTITY_ID, memberObject.getFamilyHead());
                    Intent intent = new Intent(HnppChildProfileActivity.this, HnppAncJsonFormActivity.class);
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
                    intent.putExtra("BASE_ENTITY_ID",childBaseEntityId);
                    startActivityForResult(intent, requestCode);

                }catch (Exception e){
                    Log.v("ERRRRR",e.getMessage());
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
        if(resultCode == Activity.RESULT_OK && requestCode == ImciMainActivity.REQUEST_IMCI_ACTIVITY){
            fetchProfileData();
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_AEFI_CHILD){
            AtomicInteger isSave = new AtomicInteger(2);
            showProgressDialog(R.string.please_wait_message);
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
                            hideProgressDialog();
                           fetchProfileData();
                        }
                    });
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
                HnppJsonFormUtils.setEncounterDateTime(form);

                String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);
                Map<String, String> jsonStrings = new HashMap<>();
                jsonStrings.put("First",form.toString());
                HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":type:"+type);

                Visit visit = HnppJsonFormUtils.saveVisit(childBaseEntityId, type, jsonStrings, formSubmissionid,visitId,jsonString);
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
        titleTv.setText(isSuccess==1? getString(R.string.service_done_succ):isSuccess==3? getString(R.string.service_already_given): getString(R.string.survice_done_failed));
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
                                if(growthFragment !=null){
                                    growthFragment.updateProfileColor();
                                }
                                try {
                                    Thread.sleep(2000);
                                    ReferralData referralData = HnppApplication.getReferralRepository().getIsReferralDataById(childBaseEntityId);
                                    if(referralData!=null){
                                        findViewById(R.id.child_followup).setVisibility(View.VISIBLE);
                                    }else{
                                        findViewById(R.id.child_followup).setVisibility(View.GONE);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }


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
        if(arrayList!=null && arrayList.size()>0){
            StringBuilder builder = new StringBuilder();
            for (VaccineWrapper vaccineWrapper: arrayList){
                builder.append(vaccineWrapper.getName());
                builder.append("\n --------------\n");
                builder.append(HnppConstants.DDMMYY.format(vaccineWrapper.getUpdatedVaccineDate().toDate()));
                builder.append("\n --------------\n");
            }
            showDialogWithAction(HnppChildProfileActivity.this, getString(R.string.tika_info_comfirm), builder.toString()
                    ,new Runnable() {
                        @Override
                        public void run() {
                            childImmunizationFragment.onVaccinateToday(arrayList,view);
                            handler.postDelayed(() -> {
                                childImmunizationFragment.updateImmunizationView();
                                VaccineDueUpdateServiceJob.scheduleJobImmediately(VaccineDueUpdateServiceJob.TAG);
                            },1000);
                            HnppConstants.isViewRefresh = true;
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            updateMissedScheduleIcon();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
        }



    }

    @Override
    public void onVaccinateEarlier(ArrayList<VaccineWrapper> arrayList, View view) {
        if(arrayList!=null && arrayList.size()>0){
            StringBuilder builder = new StringBuilder();
            for (VaccineWrapper vaccineWrapper: arrayList){
                builder.append(vaccineWrapper.getName());
                builder.append("\n --------------\n");
                builder.append(HnppConstants.DDMMYY.format(vaccineWrapper.getUpdatedVaccineDate().toDate()));
                builder.append("\n --------------\n");
            }
            showDialogWithAction(HnppChildProfileActivity.this, getString(R.string.tika_info_comfirm), builder.toString()
                    ,new Runnable() {
                        @Override
                        public void run() {
                            childImmunizationFragment.onVaccinateEarlier(arrayList,view);
                            handler.postDelayed(() -> {
                                childImmunizationFragment.updateImmunizationView();
                                VaccineDueUpdateServiceJob.scheduleJobImmediately(VaccineDueUpdateServiceJob.TAG);

                            },1000);
                            HnppConstants.isViewRefresh = true;
                            registerVaccineBroadcast();
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
        }

    }

    @Override
    public void onUndoVaccination(VaccineWrapper vaccineWrapper, View view) {
        childImmunizationFragment.onUndoVaccination(vaccineWrapper,view);
        handler.postDelayed(() -> {
            childImmunizationFragment.updateImmunizationView();
            VaccineDueUpdateServiceJob.scheduleJobImmediately(VaccineDueUpdateServiceJob.TAG);
        },1000);
        HnppConstants.isViewRefresh = true;
        registerVaccineBroadcast();
    }
    private void updateMissedScheduleIcon(){
        String vaccineDueDate = HnppDBUtils.getDueVaccineDate(childBaseEntityId);
        Log.v("CHILD_FILTER","updateMissedScheduleIcon>>vaccineDueDate:"+vaccineDueDate+":isMissed:"+HnppConstants.isMissedSchedule(vaccineDueDate));

        if(HnppConstants.isMissedSchedule(vaccineDueDate)){
            findViewById(R.id.missed_schedule_img).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.missed_schedule_img).setVisibility(View.GONE);
        }
    }
    BroadcastReceiver vaccineUpdateDataBroadcastReceiver;
    private void registerVaccineBroadcast(){
        vaccineUpdateDataBroadcastReceiver = new VaccineDueSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("VACCINE_UPDATE");
        registerReceiver(vaccineUpdateDataBroadcastReceiver, intentFilter);
    }
    private class VaccineDueSyncBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(isFinishing()) return;
                if(intent != null && intent.getAction().equalsIgnoreCase("VACCINE_UPDATE")){
                    updateMissedScheduleIcon();
                }
            }catch (Exception e){

            }

        }
    }
}
