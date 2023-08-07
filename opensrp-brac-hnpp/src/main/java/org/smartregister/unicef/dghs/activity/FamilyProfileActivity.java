package org.smartregister.unicef.dghs.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.utils.PermissionUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.event.PermissionEvent;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.activity.BaseFamilyProfileActivity;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.MigrationContract;
import org.smartregister.unicef.dghs.custom_view.FamilyFloatingMenu;
import org.smartregister.unicef.dghs.fragment.AddMemberFragment;
import org.smartregister.unicef.dghs.fragment.FamilyHistoryFragment;
import org.smartregister.unicef.dghs.fragment.FamilyProfileDueFragment;
import org.smartregister.unicef.dghs.interactor.MigrationInteractor;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.listener.FloatingMenuListener;
import org.smartregister.unicef.dghs.model.HnppFamilyProfileModel;
import org.smartregister.unicef.dghs.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.fragment.FamilyProfileMemberFragment;
import org.smartregister.unicef.dghs.presenter.FamilyProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

public class FamilyProfileActivity extends BaseFamilyProfileActivity  implements FamilyProfileExtendedContract.View{

    public String moduleId;
    public String houseHoldId;
//    public GlobalSearchContentData globalSearchContentData;
    private Handler handler;
    protected String familyBaseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;
    protected FamilyFloatingMenu familyFloatingMenu;

    @Override
    protected void onResumption() {
        super.onResumption();
        FloatingMenuListener.getInstance(this, presenter().familyBaseEntityId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.profile_menu, menu);
        setupMenuOptions(menu);
        MenuItem addMember = menu.findItem(org.smartregister.chw.core.R.id.add_member);
        if (addMember != null) {
            addMember.setVisible(false);
        }



        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int i = item.getItemId();
        if (i == org.smartregister.chw.core.R.id.action_family_details) {
            startFormForEdit();
        } else if (i == org.smartregister.chw.core.R.id.action_remove_member) {
            Intent frm_intent = new Intent(this, getFamilyRemoveMemberClass());
            frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getFamilyBaseEntityId());
            frm_intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
            frm_intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
            startActivityForResult(frm_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        } else if (i == org.smartregister.chw.core.R.id.action_change_head) {
            Intent fh_intent = new Intent(this, getFamilyProfileMenuClass());
            fh_intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, getFamilyBaseEntityId());
            fh_intent.putExtra(CoreFamilyProfileMenuActivity.MENU, CoreConstants.MenuType.ChangeHead);
            startActivityForResult(fh_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        } else if (i == org.smartregister.chw.core.R.id.action_change_care_giver) {
            Intent pc_intent = new Intent(this, getFamilyProfileMenuClass());
            pc_intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, getFamilyBaseEntityId());
            pc_intent.putExtra(CoreFamilyProfileMenuActivity.MENU, CoreConstants.MenuType.ChangePrimaryCare);
            startActivityForResult(pc_intent, CoreConstants.ProfileActivityResults.CHANGE_COMPLETED);
        }else if(i == org.smartregister.chw.core.R.id.action_member_import){
            GlobalSearchActivity.startMigrationFilterActivity(FamilyProfileActivity.this,HnppConstants.MIGRATION_TYPE.HH.name(),getFamilyBaseEntityId());

        }
        else {
            super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshMemberList(FetchStatus fetchStatus) {
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
    public FamilyProfileExtendedContract.Presenter presenter() {
        return (FamilyProfilePresenter) presenter;
    }

    protected void setPrimaryCaregiver(String caregiver) {
        if (StringUtils.isNotBlank(caregiver)) {
            this.primaryCaregiver = caregiver;
            getIntent().putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, caregiver);
        }
    }


    protected void refreshMemberFragment(String careGiverID, String familyHeadID) {
        BaseFamilyProfileMemberFragment memberFragment = this.getProfileMemberFragment();
        if (memberFragment != null) {
            if (StringUtils.isNotBlank(careGiverID)) {
                memberFragment.setPrimaryCaregiver(careGiverID);
            }
            if (StringUtils.isNotBlank(familyHeadID)) {
                memberFragment.setFamilyHead(familyHeadID);
            }
            refreshMemberList(FetchStatus.fetched);
        }
    }

    protected void setFamilyHead(String head) {
        if (StringUtils.isNotBlank(head)) {
            this.familyHead = head;
            getIntent().putExtra(Constants.INTENT_KEY.FAMILY_HEAD, head);
        }
    }


    public void startFormForEdit() {
        if (familyBaseEntityId != null) {
            presenter().fetchProfileData();
        }
    }


    public String getFamilyBaseEntityId() {
        return familyBaseEntityId;
    }


    @Override
    public void startChildForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {
        presenter().startChildForm(formName, entityId, metadata, currentLocationId);
    }
    public void startChildFromWithMotherInfo(String motherEntityId) throws Exception {
        ((FamilyProfilePresenter)presenter).startChildFromWithMotherInfo(motherEntityId);
    }

    public void startMemberProfile(String baseEntityId) throws Exception {
//        CommonPersonObjectClient client = HnppDBUtils.getCommonPersonByBaseEntityId(baseEntityId);


        CommonPersonObjectClient commonPersonObjectClient = clientObject(baseEntityId);
        if(TextUtils.isEmpty(familyBaseEntityId)){
            Toast.makeText(this,"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(commonPersonObjectClient.getCaseId())){
            Toast.makeText(this,"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
        String dobString = Utils.getDuration(Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false));
        Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
        if (yearOfBirth != null && yearOfBirth > 5) {
            ((FamilyProfilePresenter)presenter).startMemberProfileForMigration(commonPersonObjectClient);
        }else{
            ((FamilyProfilePresenter)presenter).startChildProfileForMigration(commonPersonObjectClient,familyBaseEntityId);
        }
    }
    @Override
    public void updateHasPhone(boolean hasPhone) {
        if (familyFloatingMenu != null) {
            familyFloatingMenu.reDraw(hasPhone);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (requestCode == org.smartregister.util.PermissionUtils.PHONE_STATE_PERMISSION_REQUEST_CODE) {
            boolean granted = (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (granted) {
                PermissionEvent event = new PermissionEvent(requestCode, granted);
                EventBus.getDefault().post(event);
            } else {
                Toast.makeText(this, getText(org.smartregister.chw.core.R.string.allow_calls_denied), Toast.LENGTH_LONG).show();
            }
        }
        if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            getGPSLocation();
        }
    }

    public void updateDueCount(final int dueCount) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> adapter.updateCount(Pair.create(1, dueCount)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        findViewById(R.id.update_profile_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFormForEdit();
            }
        });
        findViewById(R.id.call_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNo = HnppDBUtils.getFamilyMobileNo(familyBaseEntityId);
                if(!TextUtils.isEmpty(mobileNo)){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + mobileNo));
                    startActivity(intent);
                }


               // FamilyCallDialogFragment.launchDialog(FamilyProfileActivity.this, familyBaseEntityId);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null) handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    protected void setupViews() {
        super.setupViews();
        CircleImageView profileView = findViewById(org.smartregister.chw.core.R.id.imageview_profile);
        profileView.setBorderWidth(2);

        // add floating menu
        familyFloatingMenu = new FamilyFloatingMenu(this);
        LinearLayout.LayoutParams linearLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        familyFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        addContentView(familyFloatingMenu, linearLayoutParams);
        familyFloatingMenu.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMemberFragment addmemberFragment = AddMemberFragment.newInstance();
                addmemberFragment.setContext(FamilyProfileActivity.this);
                addmemberFragment.show(getFragmentManager(), AddMemberFragment.DIALOG_TAG);
            }
        });
        familyFloatingMenu.setClickListener(
                FloatingMenuListener.getInstance(this, presenter().familyBaseEntityId())
        );
        HnppConstants.updateAppBackground(findViewById(R.id.family_toolbar));
        if(HnppConstants.isPALogin()){
            familyFloatingMenu.setVisibility(View.GONE);
        }
//        globalSearchContentData = (GlobalSearchContentData) getIntent().getSerializableExtra(GlobalSearchDetailsActivity.EXTRA_SEARCH_CONTENT);
//
//        if(globalSearchContentData != null){
//            HnppConstants.showDialogWithAction(this,getString(R.string.dialog_title), "", new Runnable() {
//                @Override
//                public void run() {
//                    globalSearchContentData.setFamilyBaseEntityId(familyBaseEntityId);
//                    globalSearchContentData.setHhId(houseHoldId);
//                    new MigrationInteractor(new AppExecutors()).migrateMember(globalSearchContentData, new MigrationContract.MigrationPostInteractorCallBack() {
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(FamilyProfileActivity.this,"Successfully migrated,Syncing data",Toast.LENGTH_SHORT).show();
//                            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
//                            Intent intent = new Intent(FamilyProfileActivity.this, FamilyRegisterActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(intent);
//                            finish();
//
//                        }
//
//                        @Override
//                        public void onFail() {
//                            Toast.makeText(FamilyProfileActivity.this,"Fail to migrate",Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    });
//
//                }
//            });
//        }
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
    protected void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId),houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }

    protected void refreshList(Fragment fragment) {
        if (fragment instanceof FamilyProfileMemberFragment) {
            FamilyProfileMemberFragment familyProfileMemberFragment = ((FamilyProfileMemberFragment) fragment);
            if (familyProfileMemberFragment.presenter() != null) {
                familyProfileMemberFragment.refreshListView();
            }
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
//                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//                    @Override
//                    public void onPost(double latitude, double longitude) {
//                        try{
                            Intent intent = new Intent(FamilyProfileActivity.this, Utils.metadata().familyMemberFormActivity);
                            //HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
                            intent.putExtra("json", jsonForm.toString());
                            Form form = new Form();
                            if(!HnppConstants.isReleaseBuild()){
                                form.setActionBarBackground(R.color.test_app_color);

                            }else{
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }
                            form.setHideSaveLabel(true);
                            form.setSaveLabel("");
                            form.setWizard(true);
                            intent.putExtra("form", form);
                            startActivityForResult(intent, 2244);
//                        }catch (Exception e){
//
//                        }

//                    }
//                });

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
    boolean isProcessing = false;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                    houseHoldId =  HnppJsonFormUtils.getHouseholdIdFromForm(form);
                    ((FamilyProfilePresenter)presenter).updateHouseIdAndModuleId(houseHoldId);
                    model.updateHouseIdAndModuleId(houseHoldId,moduleId );
                    presenter().updateFamilyRegister(jsonString);
                    presenter().verifyHasPhone();
                }else {
                    if(TextUtils.isEmpty(familyBaseEntityId)){
                        Toast.makeText(this,"familyBaseEntityId no found",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    try{
                        String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                        JSONObject formWithConsent = new JSONObject(jsonString);
                        processForm(encounterType,formWithConsent.toString());
                    }catch (JSONException je){
                        je.printStackTrace();
                    }
//                    String[] generatedString;
//                    String title;
//                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
//
//                    String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
//                    String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
//                    if (encounterType.equals(HnppConstants.EventType.CHILD_REGISTRATION)) {
//                        generatedString = HnppJsonFormUtils.getValuesFromChildRegistrationForm(form);
//                        title = String.format(getString(R.string.dialog_confirm_save_child),fullName,generatedString[0],generatedString[2],generatedString[1]);
//
//                    }else {
//                        generatedString = HnppJsonFormUtils.getValuesFromRegistrationForm(form);
//                         title = String.format(getString(R.string.dialog_confirm_save),fullName,generatedString[0],generatedString[2],generatedString[1]);
//
//                    }
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
//                                processForm(encounterType,formWithConsent.toString());
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
//                                processForm(encounterType,formWithConsent.toString());
//                            }catch (JSONException je){
//                                je.printStackTrace();
//                            }
//                        }
//                    });
//

                }
            } catch (Exception e) {
                e.printStackTrace();
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
            /*Runnable runnable = () -> {
                Log.v("SAVE_VISIT","isProcessing>>"+isProcessing);
                if(!isProcessing){
                    isProcessing = true;
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    String formSubmissionId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    String visitId = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    isSave.set(processAndSaveVisitForm(jsonString,formSubmissionId,visitId));
                }
                appExecutors.mainThread().execute(() -> {
                    isProcessing = false;
                    if(isSave.get()){
                        hideProgressDialog();
                        showServiceDoneDialog(true);
                    }else {
                        hideProgressDialog();
                        showServiceDoneDialog(false);
                    }

                });
            };*/

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
           /* appExecutors.diskIO().execute(runnable);*/
//            try{
//                Thread.sleep(5000);
//            }catch (Exception e){
//
//            }
//            appExecutors.diskIO().execute(runnable);

        }

    }
    private Observable<Integer> processAndSaveVisitForm(String jsonString, String formSubmissionId, String visitId){
       return  Observable.create(e-> {
           if(TextUtils.isEmpty(familyBaseEntityId)){
               e.onNext(2);
           }
           Map<String, String> jsonStrings = new HashMap<>();
           jsonStrings.put("First",jsonString);
           try {
               JSONObject form = new JSONObject(jsonString);
               String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
               type = HnppJsonFormUtils.getEncounterType(type);

               Visit visit = HnppJsonFormUtils.saveVisit(familyBaseEntityId, type, jsonStrings,formSubmissionId,visitId,jsonString);
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
                isProcessing = false;
                //if(isSuccess){
                    if(familyHistoryFragment !=null){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                familyHistoryFragment.onActivityResult(0,0,null);
                                mViewPager.setCurrentItem(3,true);
                                HnppConstants.isViewRefresh = true;
                                presenter().refreshProfileView();
                            }
                        },2000);
                   // }
                }
            }
        });
        dialog.show();

    }
    private void processJson(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected Class<? extends FamilyRemoveMemberActivity> getFamilyRemoveMemberClass() {
        return FamilyRemoveMemberActivity.class;
    }

    protected Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass() {
        return null;
    }
    HnppFamilyProfileModel model;

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        familyHead = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_HEAD);
        primaryCaregiver = getIntent().getStringExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER);
        familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        moduleId = getIntent().getStringExtra(HnppConstants.KEY.MODULE_ID);
        houseHoldId = getIntent().getStringExtra(DBConstants.KEY.UNIQUE_ID);
        model = new HnppFamilyProfileModel(familyName,moduleId,houseHoldId,familyBaseEntityId);
        presenter = new FamilyProfilePresenter(this, model,houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        boolean isComesFromGlobalSearch = getIntent().getBooleanExtra(HnppConstants.KEY.IS_COMES_FROM_MIGRATION,false);
        if(isComesFromGlobalSearch){
            String baseEntityId = getIntent().getStringExtra(Constants.INTENT_KEY.BASE_ENTITY_ID);
            try {
                startMemberProfile(baseEntityId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            if(!HnppConstants.isPALogin()) viewPager.setCurrentItem(1);
        }

        return viewPager;
    }

    private void setupMenuOptions(Menu menu) {

        MenuItem removeMember = menu.findItem(org.smartregister.chw.core.R.id.action_remove_member);
        MenuItem changeFamHead = menu.findItem(org.smartregister.chw.core.R.id.action_change_head);
        MenuItem changeCareGiver = menu.findItem(org.smartregister.chw.core.R.id.action_change_care_giver);
        menu.findItem(R.id.action_remove_member).setTitle(R.string.remove_house_and_member);

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
//        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//            @Override
//            public void onPost(double latitude, double longitude) {
                try{
                    Map<String,String> hhByBaseEntityId = HnppDBUtils.getDetails(familyBaseEntityId,"ec_family");
                    JSONObject jsonForm = FormUtils.getInstance(getApplicationContext()).getFormJson(HnppConstants.JSON_FORMS.HOME_VISIT_FAMILY);
                    HnppJsonFormUtils.updateHhVisitForm(jsonForm, hhByBaseEntityId);
//                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,familyBaseEntityId);
                    startHHFormActivity(jsonForm,REQUEST_HOME_VISIT);

                }catch (Exception e){
                    e.printStackTrace();
                    hideProgressDialog();
                }
//            }
//        });


    }
    public void openProfile(String baseEntityId){
        CommonPersonObjectClient commonPersonObjectClient = clientObject(baseEntityId);
        if(TextUtils.isEmpty(familyBaseEntityId)){
            Toast.makeText(this,"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(commonPersonObjectClient.getCaseId())){
            Toast.makeText(this,"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
        String dobString = Utils.getDuration(Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, false));
        Integer yearOfBirth = CoreChildUtils.dobStringToYear(dobString);
        if (yearOfBirth != null && yearOfBirth > 5) {
            familyProfileMemberFragment.goToOtherMemberProfileActivity(commonPersonObjectClient);
        }else{
            familyProfileMemberFragment.goToChildProfileActivity(commonPersonObjectClient);
        }

    }
    public void startHHFormActivity(JSONObject jsonForm, int requestCode) {
        if(TextUtils.isEmpty(familyBaseEntityId)){
            Toast.makeText(this,"BaseEntityId showing empty",Toast.LENGTH_SHORT).show();
            return;
        }
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
    private CommonPersonObjectClient getFamilyClientObject(String baseEntityId) {
        CommonRepository commonRepository =Utils.context().commonrepository(Utils.metadata().familyRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }

    @Override
    public void errorOccured(String message) {

        Toast.makeText(FamilyProfileActivity.this, message, Toast.LENGTH_SHORT).show();
        finish();
    }
}
