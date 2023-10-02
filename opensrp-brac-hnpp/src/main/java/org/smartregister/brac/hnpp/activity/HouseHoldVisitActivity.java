package org.smartregister.brac.hnpp.activity;

import static org.smartregister.chw.anc.util.JsonFormUtils.formTag;
import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;
import static org.smartregister.util.JsonFormUtils.generateRandomUUIDString;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rey.material.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.HouseHoldChildProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldFormTypeFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberFragment;
import org.smartregister.brac.hnpp.listener.OnEachMemberDueValidate;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.listener.OnUpdateMemberList;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.model.ChildService;
import org.smartregister.brac.hnpp.model.HhForumDetails;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.model.Member;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.EventClientRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HouseHoldVisitActivity extends CoreFamilyProfileActivity {
    Button nextButton;
    TextView titleTv;
    public int currentFragmentIndex = 0;
    List<Fragment> fragmentList = Arrays.asList(new HouseHoldFormTypeFragment(), new HouseHoldMemberFragment(), new HouseHoldMemberDueFragment());
    List<String> fragmentTagList = Arrays.asList(HouseHoldFormTypeFragment.TAG, HouseHoldMemberFragment.TAG, HouseHoldMemberDueFragment.TAG);
    public String moduleId;
    public String houseHoldId;
    public HnppFamilyProfileModel model;
    public MigrationSearchContentData migrationSearchContentData;

    public OnUpdateMemberList onUpdateMemberList;
    public OnEachMemberDueValidate onEachMemberDueValidate;

    boolean isSuccess = false;


    public FragmentManager fragmentManager;
    private boolean isFinalSubmission = false;
    public static int HOUSE_HOLD_FINISH_CODE = 301;

    public ArrayList<Member> memberArrayList = new ArrayList<>();
    public HashMap<String,ArrayList<MemberProfileDueData>> memberServiceMap = new HashMap<>();
    public HashMap<String,ArrayList<ChildService>> childServiceMap = new HashMap<>();


    public void listenMemberUpdateStatus(OnUpdateMemberList onUpdateMemberList) {
        this.onUpdateMemberList = onUpdateMemberList;
    }

    public void isValidateDueData(OnEachMemberDueValidate onEachMemberDueValidate) {
        this.onEachMemberDueValidate = onEachMemberDueValidate;
    }

    @Override
    protected void onCreation() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_hh_visit);
        initializePresenter();

        nextButton = findViewById(R.id.next_button);
        titleTv = findViewById(R.id.title_tv);

        nextButton.setVisibility(View.VISIBLE);

        setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);

                // if tag is boolean and true
                //means all data added
                //then submit data
                if (nextButton.getTag() instanceof Boolean) {
                    if (((boolean) nextButton.getTag())) {
                        if (fragment instanceof HouseHoldFormTypeFragment) {
                            if (((HouseHoldFormTypeFragment) fragment).finalValidation()) {
                                submitTotalData(HnppConstants.EVENT_TYPE.HOUSE_HOLD_VISIT);
                            } else {
                                Toast.makeText(HouseHoldVisitActivity.this, getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                    }
                }

                //need to add data from other fragment
                if (fragment instanceof HouseHoldFormTypeFragment) {
                    if (((HouseHoldFormTypeFragment) fragment).initalValidation()) {
                        gotoNextFrag(fragment);
                    } else {
                        Toast.makeText(HouseHoldVisitActivity.this, getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
                    }
                }else if (fragment instanceof HouseHoldMemberDueFragment) {
                    int isValid = ((HouseHoldMemberDueFragment) fragment).validate();
                    if(isValid == 3){
                        Toast.makeText(HouseHoldVisitActivity.this, getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
                    }else {
                        onBackPressed();
                    }
                } else if (fragment instanceof HouseHoldChildProfileDueFragment) {
                    int isValid = ((HouseHoldChildProfileDueFragment) fragment).validate();
                    if(isValid == 3){
                        Toast.makeText(HouseHoldVisitActivity.this, getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
                    }else {
                        onBackPressed();
                    }
                }
                else if (fragment instanceof HouseHoldMemberFragment) {
                    if (((HouseHoldMemberFragment) fragment).isValidateHHMembers()) {
                        isFinalSubmission = true;
                        memberArrayList = ((HouseHoldMemberFragment) fragment).memberArrayList;
                        updateHHVisitLayoutVisibility();
                        nextButton.setText(getString(R.string.submit));
                        nextButton.setTag(true);
                    } else {
                        Toast.makeText(HouseHoldVisitActivity.this, getString(R.string.continue_to_submit_data_msg), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void submitTotalData(String eventType) {
        showProgressDialog(R.string.please_wait_message);
        Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if (fragment instanceof HouseHoldFormTypeFragment) {
            proccessAndSaveHHData(fragment, eventType)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSuccess = aBoolean;
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            hideProgressDialog();
                            if (isSuccess) {
                                showServiceDoneDialog(1);
                            } else {
                                showServiceDoneDialog(2);
                            }

                        }
                    });
            // Log.d("eeeeeeeeee",""+event);
        }
    }

    Dialog dialog;

    private void showServiceDoneDialog(Integer isSuccess) {
        if (dialog != null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess == 1 ? "খানার তথ্য দেওয়া সম্পূর্ণ হয়েছে" : isSuccess == 3 ? "সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে" : "খানার তথ্য দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        android.widget.Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                hideProgressDialog();
                setResult(HOUSE_HOLD_FINISH_CODE);
                finish();
            }
        });
        dialog.show();
    }

    private Observable<Boolean> proccessAndSaveHHData(Fragment fragment, String eventType) {
        return Observable.create(e -> {
            try {
                String baseEntityId = generateRandomUUIDString();

                HhForumDetails hhForumDetails = new HhForumDetails();
                hhForumDetails.isMemberAdded = ((HouseHoldFormTypeFragment) fragment).memberListJson.size() > 0;
                hhForumDetails.isDeadInfoAdded = ((HouseHoldFormTypeFragment) fragment).removedMemberListJson.size() > 0;
                hhForumDetails.isMigrationAdded = ((HouseHoldFormTypeFragment) fragment).migratedMemberListJson.size() > 0;
                hhForumDetails.isPregnancyAdded = ((HouseHoldFormTypeFragment) fragment).pregancyMemberListJson.size() > 0;
                hhForumDetails.isHhInfoAdded = ((HouseHoldFormTypeFragment) fragment).isValidateHhVisit;
                hhForumDetails.isMemberImported = ((HouseHoldFormTypeFragment) fragment).memberImportListJson.size() > 0;
                FormTag formTag = formTag(Utils.getAllSharedPreferences());
                formTag.appVersionName = BuildConfig.VERSION_NAME;
                Log.v("FORUM_TEST", "processAndSaveForum>>eventType:" + eventType + ":baseEntityId:" + baseEntityId);
                Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(new JSONArray(), formTag, baseEntityId);
                baseClient.setClientType(eventType);
                baseClient.addAttribute("houseHoldDate", new Date());
                baseClient.addAttribute("houseHoldType", eventType);
                baseClient.addIdentifier("opensrp_id", generateRandomUUIDString());

                JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseClient));
                EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
                SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
                JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
                baseClient.setAddresses(updateWithSSLocation(dsasd));
                clientjson.put("addresses", dsasd.getJSONArray("addresses"));
                getSyncHelper().addClient(baseClient.getBaseEntityId(), clientjson);

                Event baseEvent = HnppJsonFormUtils.processHHVisitEvent(baseEntityId, HnppConstants.EVENT_TYPE.HOUSE_HOLD_VISIT, hhForumDetails,memberArrayList,memberServiceMap,childServiceMap);
                if (baseEvent != null) {
                    baseEvent.setFormSubmissionId(org.smartregister.util.JsonFormUtils.generateRandomUUIDString());
                    org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(Utils.getAllSharedPreferences(), baseEvent);
                    String visitID = "";
                    if (!TextUtils.isEmpty(baseEvent.getEventId())) {
                        visitID = baseEvent.getEventId();
                    } else {
                        visitID = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                    }

                    Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
                    visit.setPreProcessedJson(new Gson().toJson(baseEvent));

                    visitRepository().addVisit(visit);
                    visitRepository().completeProcessing(visit.getVisitId());
                    JSONObject eventJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseEvent));
                    Log.v("FORUM_TEST", "addEvent>>eventType:" + baseClient.getBaseEntityId() + ":eventJson:" + eventJson);

                    getSyncHelper().addEvent(baseClient.getBaseEntityId(), eventJson);
                    long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                    Date lastSyncDate = new Date(lastSyncTimeStamp);
                    Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
                    e.onNext(true);
                    e.onComplete();
                }
            } catch (Exception ex) {
                e.onNext(false);
                e.onComplete();
            }
        });
    }

    private static List<Address> updateWithSSLocation(JSONObject clientjson) {
        try {
            String addessJson = clientjson.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            return listAddress;
        } catch (Exception e) {

        }
        return new ArrayList<>();

    }

    private static VisitRepository visitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }

    private void gotoNextFrag(Fragment fragment) {
        if (currentFragmentIndex < 3) {
            setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));
        }
    }

    /**
     * fragment transaction
     *
     * @param fragment for rendering
     * @param tag      to add backstack
     */
    public void setupFragment(Fragment fragment, String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getFamilyBaseEntityId());
        bundle.putString(Constants.INTENT_KEY.FAMILY_HEAD, familyHead);
        bundle.putString(Constants.INTENT_KEY.PRIMARY_CAREGIVER, primaryCaregiver);
        fragment.setArguments(bundle);
        if (fragmentManager == null) {
            fragmentManager = this.getSupportFragmentManager();
        }

        fragmentManager.beginTransaction()
                .add(R.id.hh_visit_container, fragment)
                .addToBackStack(tag)
                .commit();

        currentFragmentIndex++;

    }

    public void setupFragment(Fragment fragment, String tag, Bundle bdl) {
        fragment.setArguments(bdl);
        if (fragmentManager == null) {
            fragmentManager = this.getSupportFragmentManager();
        }
        fragmentManager
                .beginTransaction()
                .add(R.id.hh_visit_container, fragment)
                .addToBackStack(tag)
                .commit();

        if (fragment instanceof HouseHoldMemberDueFragment ||
                fragment instanceof HouseHoldChildProfileDueFragment) {
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setText(getString(R.string.submit));
        }

        currentFragmentIndex++;
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new HnppFamilyProfileModel(familyName, moduleId, houseHoldId, familyBaseEntityId), houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);

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

        model = new HnppFamilyProfileModel(familyName, moduleId, houseHoldId, familyBaseEntityId);
        presenter = new FamilyProfilePresenter(this, model, houseHoldId, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }


    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    public void onBackPressed() {

       /* if (currentFragmentIndex == 0) {
            finish();
            return;
        }*/
        Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if(fragment instanceof HouseHoldFormTypeFragment){
            finish();
            return;
        }
        else if (fragment instanceof HouseHoldMemberDueFragment) {
            HouseHoldMemberDueFragment fragment1 = ((HouseHoldMemberDueFragment) fragment);
            int isValid = fragment1.validate();
            if (isValid == 1 || isValid == 3) {
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setText(getString(R.string.next));
                String baseEntityId = fragment1.baseEntityId;
                memberServiceMap.put(baseEntityId,fragment1.serviceList);
                super.onBackPressed();
            }else {
                return;
            }
        } else if (fragment instanceof HouseHoldChildProfileDueFragment) {
            HouseHoldChildProfileDueFragment fragment1 = ((HouseHoldChildProfileDueFragment) fragment);
            int isValid = fragment1.validate();
            if (isValid == 1 || isValid == 3) {
                nextButton.setVisibility(View.VISIBLE);
                nextButton.setText(getString(R.string.next));
                String baseEntityId = fragment1.childBaseEntityId;
                childServiceMap.put(baseEntityId,fragment1.serviceList);
                super.onBackPressed();
            }else {
                return;
            }
        } else if (fragment instanceof HouseHoldMemberFragment) {
            if(((HouseHoldMemberFragment) fragment).isAnyDataAdded()) {
                return;
            }else {
                super.onBackPressed();
            }
        }

        currentFragmentIndex--;
    }

    void updateHHVisitLayoutVisibility(){
        super.onBackPressed();
        Fragment curFragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if (curFragment instanceof HouseHoldFormTypeFragment) {
            if (isFinalSubmission) {
                ((HouseHoldFormTypeFragment) curFragment).hhUpdateLay.setVisibility(View.VISIBLE);
                ((HouseHoldFormTypeFragment) curFragment).newBornLay.setVisibility(View.GONE);
                ((HouseHoldFormTypeFragment) curFragment).deathInfoLay.setVisibility(View.GONE);
                ((HouseHoldFormTypeFragment) curFragment).migrationInfoLay.setVisibility(View.GONE);
                ((HouseHoldFormTypeFragment) curFragment).pregnancyRegLay.setVisibility(View.GONE);
                ((HouseHoldFormTypeFragment) curFragment).memberImportLay.setVisibility(View.GONE);
            }
        }

        currentFragmentIndex--;
    }

    @Override
    public void errorOccured(String message) {

    }

    public FamilyProfilePresenter getfamilyProfilePresenter() {
        return ((FamilyProfilePresenter) presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if (currentFragment instanceof HouseHoldFormTypeFragment) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else if (currentFragment instanceof HouseHoldMemberDueFragment) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else if (currentFragment instanceof HouseHoldChildProfileDueFragment) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if (houseHoldId == null) {
            new AlertDialog.Builder(this).setMessage(R.string.household_id_null_message)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    }).show();
            return;
        }
        try {
            if (jsonForm.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                if (HnppConstants.isPALogin()) {
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                Intent intent = new Intent(this, Utils.metadata().familyFormActivity);
                intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                Form form = new Form();
                if (!HnppConstants.isReleaseBuild()) {
                    form.setActionBarBackground(R.color.test_app_color);

                } else {
                    form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                }
                form.setWizard(false);

                intent.putExtra("form", form);
                this.startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
            } else {
                if (HnppConstants.isPALogin()) {
                    openAsReadOnlyMode(jsonForm);
                    return;
                }
                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                    @Override
                    public void onPost(double latitude, double longitude) {
                        try {
                            Intent intent = new Intent(HouseHoldVisitActivity.this, Utils.metadata().familyMemberFormActivity);
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm, latitude, longitude);
                            intent.putExtra("json", jsonForm.toString());
                            intent.putExtra("json", jsonForm.toString());
                            Form form = new Form();
                            if (!HnppConstants.isReleaseBuild()) {
                                form.setActionBarBackground(R.color.test_app_color);

                            } else {
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }
                            form.setWizard(false);
                            intent.putExtra("form", form);
                            startActivityForResult(intent, 2244);
                        } catch (Exception e) {

                        }

                    }
                });

            }
        } catch (Exception e) {

        }
    }

    private void openAsReadOnlyMode(JSONObject jsonForm) {
        Intent intent = new Intent(this, HnppFormViewActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setWizard(false);
        if (!HnppConstants.isReleaseBuild()) {
            form.setActionBarBackground(R.color.test_app_color);

        } else {
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