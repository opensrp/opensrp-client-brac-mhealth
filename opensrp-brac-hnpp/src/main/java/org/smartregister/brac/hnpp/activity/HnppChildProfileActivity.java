package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.adapter.ReferralCardViewAdapter;
import org.smartregister.brac.hnpp.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.brac.hnpp.fragment.ChildHistoryFragment;
import org.smartregister.brac.hnpp.fragment.ChildImmunizationFragment;
import org.smartregister.brac.hnpp.fragment.HnppChildProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.HnppMemberProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.fragment.MemberOtherServiceFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.utils.ChildDBConstants;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.HouseHoldInfo;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.activity.CoreChildMedicalHistoryActivity;
import org.smartregister.chw.core.activity.CoreUpcomingServicesActivity;
import org.smartregister.chw.core.custom_views.CoreFamilyMemberFloatingMenu;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.presenter.HnppChildProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;

public class HnppChildProfileActivity extends HnppCoreChildProfileActivity {
    public CoreFamilyMemberFloatingMenu familyFloatingMenu;
    public RelativeLayout referralRow;
    public RecyclerView referralRecyclerView;
    public CommonPersonObjectClient commonPersonObject;

    @Override
    protected void onCreation() {
        super.onCreation();
        initializePresenter();
        onClickFloatingMenu = getOnClickFloatingMenu(this, (HnppChildProfilePresenter) presenter);
        setupViews();
        setUpToolbar();
        HnppConstants.isViewRefresh = false;
        Toolbar toolbar = (Toolbar)this.findViewById(R.id.collapsing_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
    MemberOtherServiceFragment memberOtherServiceFragment;
    ChildHistoryFragment memberHistoryFragment;
    HnppChildProfileDueFragment profileMemberFragment;
    ViewPager mViewPager;
    protected ViewPagerAdapter adapter;
    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        commonPersonObject = ((HnppChildProfilePresenter)presenter()).commonPersonObjectClient;
        this.mViewPager = viewPager;
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        profileMemberFragment =(HnppChildProfileDueFragment) HnppChildProfileDueFragment.newInstance(this.getIntent().getExtras());
        profileMemberFragment.setCommonPersonObjectClient(commonPersonObject);
        profileMemberFragment.setBaseEntityId(childBaseEntityId);
        adapter.addFragment(profileMemberFragment, this.getString(R.string.due).toUpperCase());
        memberOtherServiceFragment = new MemberOtherServiceFragment();
        memberHistoryFragment = ChildHistoryFragment.getInstance(this.getIntent().getExtras());
        memberHistoryFragment.setBaseEntityId(childBaseEntityId);
        memberOtherServiceFragment.setCommonPersonObjectClient(commonPersonObject);
        adapter.addFragment(memberOtherServiceFragment, this.getString(R.string.other_service).toUpperCase());
        adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
        viewPager.setOffscreenPageLimit(3);
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
        menu.findItem(R.id.action_remove_member).setVisible(false);
        menu.findItem(R.id.action_remove_member).setTitle("সদস্য বাদ দিন / মাইগ্রেট / মৃত্যু");
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(false);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void openMedicalHistoryScreen() {
        Map<String, Date> vaccine = ((HnppChildProfilePresenter) presenter()).getVaccineList();
        CoreChildMedicalHistoryActivity.startMedicalHistoryActivity(this, ((CoreChildProfilePresenter) presenter()).getChildClient(), patientName, lastVisitDay,
                ((HnppChildProfilePresenter) presenter()).getDateOfBirth(), new LinkedHashMap<>(vaccine), CoreChildMedicalHistoryActivity.class);
    }

    private void openUpcomingServicePage() {
        CoreUpcomingServicesActivity.startUpcomingServicesActivity(this, ((CoreChildProfilePresenter) presenter()).getChildClient());
    }
    public void openVisitHomeScreen(boolean isEditMode) {
        ChildVaccinationActivity.startChildVaccinationActivity(this,this.getIntent().getExtras(),commonPersonObject);
        //ChildHomeVisitActivity.startMe(this, memberObject, isEditMode, ChildHomeVisitActivity.class);
    }

    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final HnppChildProfilePresenter presenter) {
        return viewId -> {
            switch (viewId) {
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, presenter.getFamilyId());
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
    public void openRefereal() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_REFERRAL,REQUEST_HOME_VISIT);
    }

    public void openFollowUp() {
        startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_FOLLOWUP,REQUEST_HOME_VISIT);
    }
    public void openReferealFollowUp(ReferralFollowUpModel referralFollowUpModel) {

        try {
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
            jsonForm.put(JsonFormUtils.ENTITY_ID, childBaseEntityId);
            HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralReason(),referralFollowUpModel.getReferralPlace());
            Intent intent;
            intent = new Intent(this, HnppAncJsonFormActivity.class);
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
            if (this != null) {
                this.startActivityForResult(intent, REQUEST_HOME_VISIT);
            }
        }catch (Exception e){

        }
    }
    public void startAnyFormActivity(String formName, int requestCode) {
        try {
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
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
            jsonForm.put(JsonFormUtils.ENTITY_ID, memberObject.getFamilyHead());
            Intent intent = new Intent(this, HnppAncJsonFormActivity.class);
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
            if (this != null) {
                this.startActivityForResult(intent, requestCode);
            }
        }catch (Exception e){

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK){
            HnppConstants.isViewRefresh = true;

        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            Map<String, String> jsonStrings = new HashMap<>();
            jsonStrings.put("First",jsonString);
            Visit visit = null;
            try {
                JSONObject form = new JSONObject(jsonString);
                String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                type = HnppJsonFormUtils.getEncounterType(type);
                // persist to database


                visit = HnppJsonFormUtils.saveVisit(false, childBaseEntityId, type, jsonStrings, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(memberHistoryFragment !=null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        memberHistoryFragment.onActivityResult(0,0,null);
                        mViewPager.setCurrentItem(2,true);

                    }
                },1000);
            }

        }else if(resultCode == Activity.RESULT_OK && requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT){
           if(mViewPager!=null) mViewPager.setCurrentItem(0,true);
        } else if(resultCode == Activity.RESULT_OK && requestCode == ChildVaccinationActivity.VACCINE_REQUEST_CODE){
            profileMemberFragment.setUserVisibleHint(true);
        }

        super.onActivityResult(requestCode, resultCode, data);

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
        intent.putExtra(Constants.INTENT_KEY.VILLAGE_TOWN, Utils.getValue(commonPersonObject, DBConstants.KEY.VILLAGE_TOWN, false));

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }
}
