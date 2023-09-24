package org.smartregister.brac.hnpp.activity;

import static org.smartregister.chw.anc.util.JsonFormUtils.formTag;
import static org.smartregister.chw.anc.util.NCUtils.getSyncHelper;
import static org.smartregister.util.JsonFormUtils.generateRandomUUIDString;

import android.app.AlertDialog;
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
import org.smartregister.brac.hnpp.model.HhForumDetails;
import org.smartregister.brac.hnpp.model.HnppFamilyProfileModel;
import org.smartregister.brac.hnpp.presenter.FamilyProfilePresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
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


    public FragmentManager fragmentManager;

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


        setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));
        currentFragmentIndex = 1;

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nextButton.getTag() instanceof Boolean){
                    if(((boolean) nextButton.getTag())){
                        try {
                            submitTotalData(HnppConstants.EVENT_TYPE.HOUSE_HOLD_VISIT);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                }
                Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
                if(fragment instanceof HouseHoldFormTypeFragment){
                    if( ((HouseHoldFormTypeFragment) fragment).isValidateHHType()){
                        gotoNextFrag(fragment);
                    }else {
                        Toast.makeText(HouseHoldVisitActivity.this,"Fill up all forms to continue",Toast.LENGTH_SHORT).show();
                    }
                }else if(fragment instanceof HouseHoldMemberFragment){
                    if( ((HouseHoldMemberFragment) fragment).isValidateHHMembers()){
                        fragmentManager.popBackStack();
                        nextButton.setText(getString(R.string.submit));
                        nextButton.setTag(true);
                    }else {
                        Toast.makeText(HouseHoldVisitActivity.this,"Fill up all forms to continue",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void submitTotalData(String eventType) throws JSONException {
        Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if(fragment instanceof HouseHoldFormTypeFragment){
           proccessAndSaveHHData(fragment,eventType)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Observer<Boolean>() {
                       @Override
                       public void onSubscribe(Disposable d) {

                       }

                       @Override
                       public void onNext(Boolean aBoolean) {

                       }

                       @Override
                       public void onError(Throwable e) {

                       }

                       @Override
                       public void onComplete() {

                       }
                   });
           // Log.d("eeeeeeeeee",""+event);
        }
    }

    private Observable<Boolean> proccessAndSaveHHData(Fragment fragment,String eventType) {
        return Observable.create(e -> {
            String baseEntityId = generateRandomUUIDString();

            HhForumDetails hhForumDetails = new HhForumDetails();
            hhForumDetails.isMemberAdded = ((HouseHoldFormTypeFragment) fragment).memberListJson.size()>0;
            hhForumDetails.isDeadInfoAdded = ((HouseHoldFormTypeFragment) fragment).removedMemberListJson.size()>0;
            hhForumDetails.isMigrationAdded = ((HouseHoldFormTypeFragment) fragment).migratedMemberListJson.size()>0;
            hhForumDetails.isPregnancyAdded = ((HouseHoldFormTypeFragment) fragment).pregancyMemberListJson.size()>0;
            hhForumDetails.isHhInfoAdded = ((HouseHoldFormTypeFragment) fragment).isValidateHhVisit;

            FormTag formTag = formTag(Utils.getAllSharedPreferences());
            formTag.appVersionName = BuildConfig.VERSION_NAME;
            Log.v("FORUM_TEST","processAndSaveForum>>eventType:"+eventType+":baseEntityId:"+baseEntityId);
            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(new JSONArray(), formTag, baseEntityId);
            baseClient.setClientType(eventType);
            baseClient.addAttribute("houseHoldDate",new Date());
            baseClient.addAttribute("houseHoldType",eventType);
            baseClient.addIdentifier("opensrp_id",generateRandomUUIDString());

            JSONObject clientjson = new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseClient));
            EventClientRepository eventClientRepository = FamilyLibrary.getInstance().context().getEventClientRepository();
            SQLiteDatabase db = HnppApplication.getInstance().getRepository().getReadableDatabase();
            JSONObject dsasd = eventClientRepository.getClient(db, familyBaseEntityId);
            baseClient.setAddresses(updateWithSSLocation(dsasd));
            clientjson.put("addresses",dsasd.getJSONArray("addresses"));
            getSyncHelper().addClient(baseClient.getBaseEntityId(), clientjson);

            Event baseEvent = HnppJsonFormUtils.processHHVisitEvent(baseEntityId, HnppConstants.EVENT_TYPE.HOUSE_HOLD_VISIT,hhForumDetails);
            if (baseEvent != null) {
                baseEvent.setFormSubmissionId(org.smartregister.util.JsonFormUtils.generateRandomUUIDString());
                org.smartregister.chw.anc.util.JsonFormUtils.tagEvent(Utils.getAllSharedPreferences(), baseEvent);
                String visitID ="";
                if(!TextUtils.isEmpty(baseEvent.getEventId())){
                    visitID = baseEvent.getEventId();
                }else{
                    visitID = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
                }

                Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
                visit.setPreProcessedJson(new Gson().toJson(baseEvent));
//           try{
//               visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), eventType, visit.getDate()));
//           }catch (Exception e){
//
//           }

                visitRepository().addVisit(visit);
                visitRepository().completeProcessing(visit.getVisitId());
                JSONObject eventJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseEvent));
                Log.v("FORUM_TEST","addEvent>>eventType:"+baseClient.getBaseEntityId()+":eventJson:"+eventJson);

                getSyncHelper().addEvent(baseClient.getBaseEntityId(), eventJson);
//            List<EventClient> eventClientList = new ArrayList();
//            org.smartregister.domain.db.Event domainEvent = org.smartregister.family.util.JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.db.Event.class);
//            org.smartregister.domain.db.Client domainClient = org.smartregister.family.util.JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.db.Client.class);
//            eventClientList.add(new EventClient(domainEvent, domainClient));

                long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
//            getClientProcessorForJava().processClient(eventClientList);
                Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
                //return visit;
                e.onNext(true);
                e.onComplete();
            }
        });
    }

    private static List<Address> updateWithSSLocation(JSONObject clientjson){
        try{
            String addessJson = clientjson.getString("addresses");
            JSONArray jsonArray = new JSONArray(addessJson);
            List<Address> listAddress = new ArrayList<>();
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Address address = new Gson().fromJson(jsonObject.toString(), Address.class);
                listAddress.add(address);
            }
            return listAddress;
        }catch (Exception e){

        }
        return new ArrayList<>();

    }

    private static VisitRepository visitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }

    private void gotoNextFrag(Fragment fragment) {
            if (currentFragmentIndex < 3) {
                setupFragment(fragmentList.get(currentFragmentIndex), fragmentTagList.get(currentFragmentIndex));
                currentFragmentIndex++;
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
            currentFragmentIndex--;
            if (currentFragmentIndex == 0) {
                finish();
                return;
            }
            /*for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof HouseHoldMemberDueFragment) {
                    ((HouseHoldMemberDueFragment) fragment).validate();
                }
            }*/
        Fragment fragment = fragmentManager.findFragmentById(R.id.hh_visit_container);
        if (fragment instanceof HouseHoldMemberDueFragment) {
            ((HouseHoldMemberDueFragment) fragment).validate();
        }else if(fragment instanceof HouseHoldChildProfileDueFragment){
            ((HouseHoldChildProfileDueFragment) fragment).validate();
        }
        super.onBackPressed();

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
        if(currentFragment instanceof HouseHoldFormTypeFragment){
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }else if(currentFragment instanceof  HouseHoldMemberDueFragment){
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }else if(currentFragment instanceof  HouseHoldChildProfileDueFragment){
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