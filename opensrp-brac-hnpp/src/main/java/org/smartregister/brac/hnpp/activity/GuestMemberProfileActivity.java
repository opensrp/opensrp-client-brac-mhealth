package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.GuestMemberContract;
import org.smartregister.brac.hnpp.fragment.GuestMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.presenter.GuestMemberProfilePresenter;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.BaseProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;
import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.brac.hnpp.utils.HnppConstants.MEMBER_ID_SUFFIX;
import static org.smartregister.chw.core.utils.CoreJsonFormUtils.REQUEST_CODE_GET_JSON;
import static org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID;

import java.util.concurrent.atomic.AtomicBoolean;

public class GuestMemberProfileActivity extends BaseProfileActivity implements GuestMemberContract.View,View.OnClickListener{

    String baseEntityId;
    private GuestMemberData guestMemberData;
    private TextView textViewMemberId,textViewName,textViewAge;
    private Button editBtn;
    private CircleImageView imageViewProfile;
    private ViewPager mViewPager;
    private ViewPagerAdapter adapter;
    private GuestMemberProfilePresenter presenter;
    private Handler handler;
    AppExecutors appExecutors = new AppExecutors();
    private boolean isProcessing = false;

    public static void startGuestMemberProfileActivity(Activity activity , String baseEntityId){
        Intent intent = new Intent(activity,GuestMemberProfileActivity.class);
        intent.putExtra(BASE_ENTITY_ID,baseEntityId);
        activity.startActivity(intent);
    }
    @Override
    protected void initializePresenter() {

    }

    @Override
    protected void fetchProfileData() {

    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_other_member_profile);
        handler = new Handler();
        baseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);
        guestMemberData = HnppDBUtils.getGuestMemberById(baseEntityId);
        presenter = new GuestMemberProfilePresenter(this);
        updateTopBar();
        setProfileData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null) handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void updateTopBar(){
        Toolbar toolbar = findViewById(org.smartregister.family.R.id.family_toolbar);
        HnppConstants.updateAppBackground(toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        TextView toolbarTitle = findViewById(org.smartregister.chw.core.R.id.toolbar_title);
        toolbarTitle.setText(getString(R.string.return_to_guest_member));
        textViewMemberId = findViewById(R.id.textview_detail_three);
        textViewAge = findViewById(R.id.textview_age);
        textViewName = findViewById(R.id.textview_name);
        editBtn = findViewById(R.id.edit_member_btn);
        editBtn.setVisibility(View.VISIBLE);
        editBtn.setOnClickListener(this);
        imageViewProfile = findViewById(org.smartregister.chw.core.R.id.imageview_profile);
        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.viewpager);
        tabLayout.setupWithViewPager(setupViewPager(viewPager));

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit_member_btn:

                //Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
                CommonPersonObjectClient client = HnppDBUtils.createFromBaseEntityForGuestMember(baseEntityId);
                startFormForEdit(client);
                break;
        }
    }



    public void startFormForEdit(CommonPersonObjectClient client) {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    Intent intent = new Intent(GuestMemberProfileActivity.this, GuestAddMemberJsonFormActivity.class);
                    //JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
                    JSONObject jsonForm = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM, GuestMemberProfileActivity.this, client, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
                    jsonForm.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE, HnppConstants.EVENT_TYPE.GUEST_MEMBER_UPDATE_REGISTRATION);
                    jsonForm.put(org.smartregister.family.util.JsonFormUtils.ENTITY_ID,baseEntityId);

                    String ssName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.SS_NAME, false);
                    String villageName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, false);
                    HnppJsonFormUtils.updateFormWithSSName(jsonForm, SSLocationHelper.getInstance().getSsModels());
                    HnppJsonFormUtils.updateFormWithVillageName(jsonForm,ssName,villageName);
                    HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    intent.putExtra(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                    Form form = new Form();
                    form.setWizard(false);
                    if(!HnppConstants.isReleaseBuild()){
                        form.setActionBarBackground(R.color.test_app_color);

                    }else{
                        form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                    }

                    intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                    startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_GET_JSON);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    MemberHistoryFragment memberHistoryFragment;
    GuestMemberDueFragment memberDueFragment;

    @Override
    protected void setupViews() {

    }
    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        Bundle bundle = new Bundle();
        bundle.putBoolean(MemberHistoryFragment.IS_GUEST_USER,true);
        bundle.putString(BASE_ENTITY_ID,baseEntityId);
        memberHistoryFragment = MemberHistoryFragment.getInstance(bundle);
        memberDueFragment = GuestMemberDueFragment.getInstance();
        memberDueFragment.setGuestMemberData(guestMemberData);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(memberDueFragment, "সেবা ও প্যাকেজ");
        adapter.addFragment(memberHistoryFragment, this.getString(R.string.activity).toUpperCase());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);

        return viewPager;
    }


    private void setProfileData(){
        if(guestMemberData != null){
            textViewName.setText(guestMemberData.getName());
            String memberId = guestMemberData.getMemberId().replace(Constants.IDENTIFIER.FAMILY_SUFFIX,"")
                    .replace(HnppConstants.IDENTIFIER.FAMILY_TEXT,"");
            memberId = memberId.substring(memberId.length() - MEMBER_ID_SUFFIX);
            textViewMemberId.setText("ID:"+memberId);
            ((TextView)findViewById(R.id.textview_detail_one)).setText(HnppConstants.getGender(guestMemberData.getGender()));
            int age = StringUtils.isNotBlank(guestMemberData.getDob()) ? Utils.getAgeFromDate(guestMemberData.getDob()) : 0;
            textViewAge.setText(getString(R.string.age,age+""));
            if (guestMemberData.getGender().equalsIgnoreCase("M")) {
                imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_blue));
            } else if (guestMemberData.getGender().equalsIgnoreCase("F")) {
                imageViewProfile.setBorderColor(getResources().getColor(org.smartregister.chw.core.R.color.light_pink));
            }
        }

    }



    @Override
    protected void onResumption() {

    }
    public void openAncRegisterForm(){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(GuestMemberProfileActivity.this, baseEntityId, guestMemberData.getPhoneNo(),
                        HnppConstants.JSON_FORMS.ANC_FORM, null, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION,textViewName.getText().toString(),latitude,longitude);

            }
        });

    }
    public void openPregnancyRegisterForm(){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                HnppAncRegisterActivity.startHnppAncRegisterActivity(GuestMemberProfileActivity.this, baseEntityId, guestMemberData.getPhoneNo(),
                        HnppConstants.JSON_FORMS.PREGNANCY_OUTCOME_OOC, null, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION,textViewName.getText().toString(),latitude,longitude);

            }
        });

    }
    public void openHomeVisitSingleForm(String formName){
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
                startAnyFormActivity(formName,REQUEST_HOME_VISIT,latitude,longitude);
            }
        });

    }
    public void startAnyFormActivity(String formName, int requestCode, double latitude, double longitude) {
        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }

        try {
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
            HnppJsonFormUtils.addEDDField(formName,jsonForm,baseEntityId);
            HnppJsonFormUtils.addRelationalIdAsGuest(jsonForm);
            try{
                HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                HnppJsonFormUtils.addAddToStockValue(jsonForm);
            }catch (Exception e){
                e.printStackTrace();
            }
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;
             if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM_OOC) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM_OOC) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM_OOC)){
                HnppJsonFormUtils.addNoOfAnc(jsonForm);
            }
             else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_BEFORE_48_HOUR_OOC)
                     ||formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_AFTER_48_HOUR_OOC)){
                 HnppJsonFormUtils.addNoOfPnc(jsonForm);
                 int pncDay = FormApplicability.getDayPassPregnancyOutcome(baseEntityId);
                 HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"pnc_day_passed", String.valueOf(pncDay));
             }
            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)){
                if (guestMemberData.getGender().equalsIgnoreCase("F")) {
                    HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_women","true");
                }
            }
//            if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.GIRL_PACKAGE)){
//                //HnppJsonFormUtils.addMaritalStatus(jsonForm,maritalStatus);
//            }
//            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM_OOC) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM)){
              //  HnppJsonFormUtils.addLastAnc(jsonForm,baseEntityId,false);
//            } else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM)){
                //HnppJsonFormUtils.addLastPnc(jsonForm,baseEntityId,false);
//            }

//           if(formName.contains("anc"))
            HnppVisitLogRepository visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
            String height = visitLogRepository.getHeight(baseEntityId);
            if(!TextUtils.isEmpty(height)){
                HnppJsonFormUtils.addHeight(jsonForm,height);

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
            if (this != null) {
                this.startActivityForResult(intent, requestCode);
            }

        }catch (Exception e){

        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK){
            //TODO: Need to check request code
            //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            HnppConstants.isViewRefresh = true;
            if(data!=null) {
                String eventType = data.getStringExtra("event_type");
                if (!TextUtils.isEmpty(eventType) && eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                    if(memberHistoryFragment !=null){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mViewPager.setCurrentItem(1,true);
                                if(memberDueFragment !=null){
                                    memberDueFragment.updateStaticView();
                                }

                            }
                        },2000);
                    }
                    return;
                }
            }


        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){
            if(isProcessing) return;
            AtomicBoolean isSave = new AtomicBoolean(false);
            showProgressDialog(R.string.please_wait_message);

            isProcessing = true;
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
            String visitId = JsonFormUtils.generateRandomUUIDString();

            processVisits(jsonString,formSubmissionId,visitId)
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
                                showServiceDoneDialog(true);
                            }else {
                                hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });

        }
        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_JSON){
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(form);

                String[] generatedString;
                String title;
                String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);

                generatedString = HnppJsonFormUtils.getValuesFromGuestRegistrationForm(form);
                title = String.format(getString(R.string.dialog_confirm_save_guest),fullName,generatedString[0],generatedString[1]);


                HnppConstants.showSaveFormConfirmationDialog(this, title, new OnDialogOptionSelect() {
                    @Override
                    public void onClickYesButton() {
                        try{
                            showProgressBar();
                            JSONObject formWithConsent = new JSONObject(jsonString);
                            JSONObject jobkect = formWithConsent.getJSONObject("step1");
                            JSONArray field = jobkect.getJSONArray(FIELDS);
                            HnppJsonFormUtils.addConsent(field,true);
                            presenter.saveMember(formWithConsent.toString());
                        }catch (JSONException je){

                        }

                    }

                    @Override
                    public void onClickNoButton() {
                        try{
                            showProgressBar();
                            JSONObject formWithConsent = new JSONObject(jsonString);
                            JSONObject jobkect = formWithConsent.getJSONObject("step1");
                            JSONArray field = jobkect.getJSONArray(FIELDS);
                            HnppJsonFormUtils.addConsent(field,false);
                            presenter.saveMember(formWithConsent.toString());
                        }catch (JSONException je){

                        }
                    }
                });

            }catch (JSONException e){

            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }
    private Observable<Boolean> processVisits(String jsonString, String formSubmissionId, String visitId){
        return Observable.create(e-> {
            try{
                Visit visit = saveRegistration(jsonString,"visits",formSubmissionId,visitId);
                if(visit!=null){
                    FormParser.processVisitLog(visit);
                    e.onNext(true);
                    e.onComplete();
                }else{
                    e.onNext(false);
                }

            }catch (Exception ex){
                e.onNext(false);
            }
        });
    }
    Dialog dialog;
    private void showServiceDoneDialog(boolean isSuccess){
        if(dialog !=null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isProcessing = false;
                dialog = null;
                //if(isSuccess){
                    if(memberHistoryFragment !=null){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
//                        memberHistoryFragment.onActivityResult(0,0,null);
                                mViewPager.setCurrentItem(1,true);
                                if(memberDueFragment !=null){
                                    memberDueFragment.updateStaticView();
                                }

                            }
                        },2000);
                    }
               // }
            }
        });
        dialog.show();

    }
    private Visit saveRegistration(final String jsonString, String table, String formSubmissionId, String visitId) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);
        JSONObject form = new JSONObject(jsonString);
        HnppJsonFormUtils.setEncounterDateTime(form);

        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
        type = HnppJsonFormUtils.getEncounterType(type);
        baseEvent.setEntityType(type);
        baseEvent.setFormSubmissionId(formSubmissionId);
        NCUtils.addEvent(allSharedPreferences, baseEvent);
       // NCUtils.startClientProcessing();
        String visitID ="";
        if(!TextUtils.isEmpty(baseEvent.getEventId())){
            visitID = baseEvent.getEventId();
        }else{
            visitID = visitId;
        }

        Visit visit =null;

        try{
            visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        }catch (Exception e){
            e.printStackTrace();
        }
        return visit;
    }


    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateAdapter() {

    }

    @Override
    public void updateSuccessfullyFetchMessage() {
        guestMemberData = HnppDBUtils.getGuestMemberById(baseEntityId);
        setProfileData();
    }

    @Override
    public GuestMemberContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }
}
