package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.fragment.GuestMemberDueFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.activity.SecuredActivity;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.brac.hnpp.utils.HnppConstants.MEMBER_ID_SUFFIX;
import static org.smartregister.brac.hnpp.utils.HnppJsonFormUtils.makeReadOnlyFields;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID;

public class GuestMemberProfileActivity extends BaseProfileActivity implements View.OnClickListener{

    String baseEntityId;
    private GuestMemberData guestMemberData;
    private TextView textViewMemberId,textViewName,textViewAge;
    private Button editBtn;
    private CircleImageView imageViewProfile;
    private ViewPager mViewPager;
    private ViewPagerAdapter adapter;

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
        baseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);
        guestMemberData = HnppDBUtils.getGuestMemberById(baseEntityId);
        updateTopBar();
        setProfileData();

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
        try {
            Intent intent = new Intent(this, GuestAddMemberJsonFormActivity.class);
            //JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
            JSONObject jsonForm = HnppJsonFormUtils.getAutoPopulatedJsonEditFormString(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM, this, client, HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
            String ssName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.SS_NAME, false);
            String villageName = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), HnppConstants.KEY.VILLAGE_NAME, false);
            HnppJsonFormUtils.updateFormWithSSName(jsonForm, SSLocationHelper.getInstance().getSsModels());
            HnppJsonFormUtils.updateFormWithVillageName(jsonForm,ssName,villageName);
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
            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;
             if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC1_FORM_OOC) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC2_FORM_OOC) || formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.ANC3_FORM_OOC)){
                HnppJsonFormUtils.addNoOfAnc(jsonForm);
            }
            else if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.PNC_FORM_OOC)){
                HnppJsonFormUtils.addNoOfPnc(jsonForm);
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
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            HnppConstants.isViewRefresh = true;
            if(data!=null) {
                String eventType = data.getStringExtra("event_type");
                if (!TextUtils.isEmpty(eventType) && eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION)) {
                    if(memberHistoryFragment !=null){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mViewPager.setCurrentItem(1,true);
                                if(memberDueFragment !=null){
                                    memberDueFragment.updateStaticView();
                                }

                            }
                        },2000);
                    }
                }
            }


        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);


            try {
                saveRegistration(jsonString,"visits");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(memberHistoryFragment !=null){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        memberHistoryFragment.onActivityResult(0,0,null);
                        mViewPager.setCurrentItem(1,true);
                        if(memberDueFragment !=null){
                            memberDueFragment.updateStaticView();
                        }

                    }
                },2000);
            }

        }


        super.onActivityResult(requestCode, resultCode, data);

    }
    private void saveRegistration(final String jsonString, String table) throws Exception {
        AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
        Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, table);
        JSONObject form = new JSONObject(jsonString);
        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
        type = HnppJsonFormUtils.getEncounterType(type);
        baseEvent.setEntityType(type);
        NCUtils.addEvent(allSharedPreferences, baseEvent);
       // NCUtils.startClientProcessing();
        String visitID ="";
        if(!TextUtils.isEmpty(baseEvent.getEventId())){
            visitID = baseEvent.getEventId();
        }else{
            visitID = org.smartregister.util.JsonFormUtils.generateRandomUUIDString();
        }
        Log.v("ANC_HISTORY","visitId:"+visitID);

        Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
        visit.setPreProcessedJson(new Gson().toJson(baseEvent));
        try{
            // visit.setParentVisitID(visitRepository().getParentVisitEventID(visit.getBaseEntityId(), HnppConstants.EVENT_TYPE.SS_INFO, visit.getDate()));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
