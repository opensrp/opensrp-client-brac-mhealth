package org.smartregister.brac.hnpp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.PermissionUtils;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.fragment.FamilyHistoryFragment;
import org.smartregister.brac.hnpp.fragment.FamilyProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.interactor.MigrationInteractor;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnGpsDataGenerateListener;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.task.GenerateGPSTask;
import org.smartregister.brac.hnpp.task.GenerateLatitudeLongitudeTask;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.brac.hnpp.fragment.FamilyProfileMemberFragment;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

public class FamilyProfileActivity extends CoreFamilyProfileActivity {

    public String moduleId;
    public String houseHoldId;
    public MigrationSearchContentData migrationSearchContentData;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setupMenuOptions(menu);
        return true;
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        HnppConstants.updateAppBackground(findViewById(R.id.family_toolbar));
        if(HnppConstants.isPALogin()){
            familyFloatingMenu.setVisibility(View.GONE);
        }
        migrationSearchContentData = (MigrationSearchContentData) getIntent().getSerializableExtra(MigrationSearchDetailsActivity.EXTRA_SEARCH_CONTENT);

        if(migrationSearchContentData != null){
            HnppConstants.showDialogWithAction(this,getString(R.string.dialog_title), "", new Runnable() {
                @Override
                public void run() {
                    migrationSearchContentData.setFamilyBaseEntityId(familyBaseEntityId);
                    migrationSearchContentData.setHhId(houseHoldId);
                    new MigrationInteractor(new AppExecutors()).migrateMember(migrationSearchContentData, new MigrationContract.MigrationPostInteractorCallBack() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(FamilyProfileActivity.this,"Successfully migrated,Syncing data",Toast.LENGTH_SHORT).show();
                            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                            Intent intent = new Intent(FamilyProfileActivity.this, FamilyRegisterActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }

                        @Override
                        public void onFail() {
                            Toast.makeText(FamilyProfileActivity.this,"Fail to migrate",Toast.LENGTH_SHORT).show();


                        }
                    });

                }
            });
        }
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        Toolbar toolbar = (Toolbar)this.findViewById(R.id.family_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        HnppConstants.isViewRefresh = false;

    }
    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId),houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }



    @Override
    protected void refreshList(Fragment fragment) {
        if (fragment instanceof FamilyProfileMemberFragment) {
            FamilyProfileMemberFragment familyProfileMemberFragment = ((FamilyProfileMemberFragment) fragment);
            if (familyProfileMemberFragment.presenter() != null) {
                familyProfileMemberFragment.refreshListView();
            }
        }
    }
    @Override
    public void startFormForEdit() {
        super.startFormForEdit();
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
                            Intent intent = new Intent(FamilyProfileActivity.this, Utils.metadata().familyMemberFormActivity);
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                    String[] sss =  HnppJsonFormUtils.getHouseholdIdModuleIdFromForm(form);
                    houseHoldId = sss[0];
                    moduleId = sss[1];
                    ((FamilyProfilePresenter)presenter).updateHouseIdAndModuleId(houseHoldId);
                    model.updateHouseIdAndModuleId(houseHoldId,moduleId );
                    presenter().updateFamilyRegister(jsonString);
                    presenter().verifyHasPhone();
                }else {
                    String[] generatedString;
                    String title;
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);

                    if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(HnppConstants.EventType.CHILD_REGISTRATION)) {
                        generatedString = HnppJsonFormUtils.getValuesFromChildRegistrationForm(form);
                        title = String.format(getString(R.string.dialog_confirm_save_child),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    }else {
                        generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
                         title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);

                    }

                    Log.v("FORM_SAVE","generatedString:"+generatedString);
                    HnppConstants.showSaveFormConfirmationDialog(this, title, new Runnable() {
                        @Override
                        public void run() {
                           processJson(requestCode, resultCode, data);
                        }
                    });


                }
            } catch (Exception e) {
                Timber.e(e);
            }
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

                visit = HnppJsonFormUtils.saveVisit(false,false,false,"", familyBaseEntityId, type, jsonStrings, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(familyHistoryFragment !=null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        familyHistoryFragment.onActivityResult(0,0,null);
                        mViewPager.setCurrentItem(3,true);

                    }
                },1000);
            }
            HnppConstants.isViewRefresh = true;

        }


    }
    private void processJson(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected Class<? extends CoreFamilyRemoveMemberActivity> getFamilyRemoveMemberClass() {
        return FamilyRemoveMemberActivity.class;
    }

    @Override
    protected Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass() {
        return FamilyProfileMenuActivity.class;
    }
    HnppFamilyProfileModel model;

    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        moduleId = getIntent().getStringExtra(HnppConstants.KEY.MODULE_ID);
        houseHoldId = getIntent().getStringExtra(DBConstants.KEY.UNIQUE_ID);
        model = new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId);
        presenter = new FamilyProfilePresenter(this, model,houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }
    private FamilyHistoryFragment familyHistoryFragment;
    private FamilyProfileMemberFragment familyProfileMemberFragment;
    private ViewPager mViewPager;

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        familyProfileMemberFragment = (FamilyProfileMemberFragment)FamilyProfileMemberFragment.newInstance(getIntent().getExtras());
        adapter.addFragment(familyProfileMemberFragment,this.getString(R.string.member));
        FamilyProfileDueFragment familyProfileDueFragment =(FamilyProfileDueFragment) FamilyProfileDueFragment.newInstance(getIntent().getExtras());
        familyHistoryFragment = FamilyHistoryFragment.getInstance(getIntent().getExtras());
        if(!HnppConstants.isPALogin()){
            adapter.addFragment(familyProfileDueFragment,this.getString(R.string.due));
        }
        adapter.addFragment(familyHistoryFragment, this.getString(R.string.activity).toUpperCase());
        if(HnppConstants.isPALogin()){
            viewPager.setOffscreenPageLimit(2);
        }else{
            viewPager.setOffscreenPageLimit(3);
        }
        viewPager.setAdapter(adapter);
        if (getIntent().getBooleanExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, false) ||
                getIntent().getBooleanExtra(Constants.INTENT_KEY.GO_TO_DUE_PAGE, false)) {
            viewPager.setCurrentItem(1);
        }

        return viewPager;
    }

    private void setupMenuOptions(Menu menu) {

        MenuItem removeMember = menu.findItem(org.smartregister.chw.core.R.id.action_remove_member);
        MenuItem changeFamHead = menu.findItem(org.smartregister.chw.core.R.id.action_change_head);
        MenuItem changeCareGiver = menu.findItem(org.smartregister.chw.core.R.id.action_change_care_giver);
        menu.findItem(R.id.action_remove_member).setTitle("খানা/সদস্য বাতিল করুন");

        if (removeMember != null) {
            removeMember.setVisible(true);
        }

        if (changeFamHead != null) {
            changeFamHead.setVisible(false);
        }

        if (changeCareGiver != null) {
            changeCareGiver.setVisible(false);
        }
        if(HnppConstants.isPALogin()){
            if (removeMember != null) {
                removeMember.setVisible(false);
            }

            if (changeFamHead != null) {
                changeFamHead.setVisible(false);
            }

            if (changeCareGiver != null) {
                changeCareGiver.setVisible(false);
            }
        }
    }
    public void openHomeVisitFamily() {
        getGPSLocation();

    }
    private void getGPSLocation(){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try{
                    JSONObject jsonForm = FormUtils.getInstance(getApplicationContext()).getFormJson(HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY);
                    ArrayList<String[]> memberList = HnppDBUtils.getAllMembersInHouseHold(familyBaseEntityId);
                    HnppJsonFormUtils.updateFormWithAllMemberName(jsonForm,memberList);
                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    startHHFormActivity(jsonForm,REQUEST_HOME_VISIT);

                }catch (Exception e){
                    e.printStackTrace();
                    hideProgressDialog();
                }
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            getGPSLocation();
        }
    }
    public void openProfile(String baseEntityId){
        CommonPersonObjectClient commonPersonObjectClient = clientObject(baseEntityId);
        String dobString = Utils.getDuration(Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false));
        Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
        if (yearOfBirth != null && yearOfBirth > 5) {
            familyProfileMemberFragment.goToOtherMemberProfileActivity(commonPersonObjectClient);
        }else{
            familyProfileMemberFragment.goToChildProfileActivity(commonPersonObjectClient);
        }

    }
    public void startHHFormActivity(JSONObject jsonForm, int requestCode) {
        try {
            jsonForm.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, familyBaseEntityId);
            Intent intent;
            intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
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
            if (this != null) {
                this.startActivityForResult(intent, requestCode);
            }
        }catch (Exception e){

        }
    }
    public void startAnyFormActivity(String formName, int requestCode) {
        try {
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);

            jsonForm.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, familyBaseEntityId);
            Intent intent;
            intent = new Intent(this, org.smartregister.family.util.Utils.metadata().familyMemberFormActivity);
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
            if (this != null) {
                this.startActivityForResult(intent, requestCode);
            }
        }catch (Exception e){

        }
    }
    private CommonPersonObjectClient clientObject(String baseEntityId) {
        CommonRepository commonRepository =Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }
}
