package org.smartregister.brac.hnpp.activity;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.ReferralFollowupAdapter;
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.model.ReferralFollowupJsonModel;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChildFollowupActivity extends AppCompatActivity {
    public static String TYPE = "type";
    public static String BASE_ENTITY_ID = "baseEntityId";
    public static String GENDER = "gender";
    public static String BIRTH_DATE = "birthDate";
    public static String FAMILY_HEAD = "familyHead";
    public static String COMMON_PERSON = "commonPersonObj";
    public static String BUNDLE = "bundle";
    public static String IS_ONLY_SERVICE = "is_only_service";
    public static String REFERRAL_FOLLOWUP_LIST = "referral_followup_list";
    public static String IS_READ_ONLY = "is_read_only";


    public static final int REQUEST_REFERRAL = 101;

    public static final int REQUEST_REFERRAL_FOLLOWUP = 201;
    public static final int RESULT_CHILD_FOLLOW_UP = 10111;

    ImageView closeImage;
    Button saveButton;

    Button notReferredBt,noImmunizationBt,noReferFollowupGivenBt;

    LinearLayout childFollowUpLay,immunizationLay,gmpLay,referralLay,referralFollowupLay;
    ImageView childFollowupCheckIm,immunizationCheckIm,gmpCheckIm,referralCheckIm,referralFollowupCheckIm;

    RecyclerView referralFollowupRv;

    TextView referralFollowupCauseTv;

    String childBaseEntityId,formType,gender,birthDate,familyHead;
    MemberObject memberObj;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Dialog dialog;

    String childFollowUpJsonString = "";
    String childReferralJsonString;
    CommonPersonObjectClient commonPersonObjectClient;
    Bundle bundle;

    boolean isOnlyVacc = false;
    private Boolean isImmunizationTaken = null;
    private boolean isGmpTaken = false;
    ArrayList<String> jsonStringList = new ArrayList<>();
    private ArrayList<ReferralFollowupJsonModel> referralFollowupJsonList;
    ReferralFollowupAdapter adapter;
    private int clickedItemPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_child_followup);

        getIntentData();
        initView();
        viewInteraction();
        initProgressDialog();
    }

    private void initProgressDialog() {
        builder = new AlertDialog.Builder(this);
    }


    /**
     * getting intent data here
     */
    private void getIntentData() {
        Intent intent = getIntent();
        familyHead = intent.getStringExtra(BIRTH_DATE);
        memberObj = (MemberObject) intent.getSerializableExtra(FAMILY_HEAD);
        formType = intent.getStringExtra(TYPE);
        childBaseEntityId = intent.getStringExtra(BASE_ENTITY_ID);
        gender = intent.getStringExtra(GENDER);
        commonPersonObjectClient = (CommonPersonObjectClient) intent.getSerializableExtra(COMMON_PERSON);
        bundle = intent.getParcelableExtra(BUNDLE);
        isOnlyVacc = intent.getBooleanExtra(IS_ONLY_SERVICE,false);
        if(intent.getParcelableArrayListExtra(REFERRAL_FOLLOWUP_LIST) == null){
            referralFollowupJsonList = new ArrayList<>();
        }else {
            referralFollowupJsonList = intent.getParcelableArrayListExtra(REFERRAL_FOLLOWUP_LIST);
        }

    }

    /**
     * view interaction like button click
     */
    private void viewInteraction() {
        if(!referralFollowupJsonList.isEmpty()){
            referralFollowupLay.setVisibility(View.VISIBLE);
            referralFollowupRv.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ReferralFollowupAdapter(this, new ReferralFollowupAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, ReferralFollowupJsonModel content) {
                    if(content.getJson().isEmpty()){
                        clickedItemPos = position;
                        openReferealFollowUp(content);
                    }
                }
            });
            adapter.setData(referralFollowupJsonList);
            referralFollowupRv.setAdapter(adapter);
        }else {
            referralFollowupLay.setVisibility(View.GONE);
        }

        closeImage.setOnClickListener(view -> finish());

        saveButton.setOnClickListener(view -> {
            if(!childFollowUpJsonString.isEmpty() && childReferralJsonString!=null && isImmunizationTaken!=null && isGmpTaken){
                submitData();
            }else {
                Toast.makeText(ChildFollowupActivity.this,"Please fill-up all data to submit",Toast.LENGTH_SHORT).show();
            }
        });

        notReferredBt.setOnClickListener(view -> {
            childReferralJsonString = "";
            referralCheckIm.setImageResource(R.drawable.success);
            referralCheckIm.setColorFilter(ContextCompat.getColor(ChildFollowupActivity.this, android.R.color.holo_orange_dark));
            checkButtonEnableStatus();
        });

        noImmunizationBt.setOnClickListener(view -> {
            isImmunizationTaken = false;
            immunizationCheckIm.setImageResource(R.drawable.success);
            immunizationCheckIm.setColorFilter(ContextCompat.getColor(ChildFollowupActivity.this, android.R.color.holo_orange_dark));
            checkButtonEnableStatus();
        });

        childFollowUpLay.setOnClickListener(view -> {
            if(childFollowUpJsonString == null || childFollowUpJsonString.isEmpty()){
                startAnyFormActivity(formType,REQUEST_HOME_VISIT);
            }
        });

        immunizationLay.setOnClickListener(view -> {
            ChildVaccinationActivity.startChildVaccinationActivity(ChildFollowupActivity.this,bundle,commonPersonObjectClient,isOnlyVacc);
        });

        gmpLay.setOnClickListener(view -> {
            //if(!isGmpTaken){
                ChildGMPActivity.startGMPActivity(ChildFollowupActivity.this,bundle,commonPersonObjectClient);
           // }
        });

        referralLay.setOnClickListener(view -> {
            if(childReferralJsonString == null || childReferralJsonString.isEmpty()){
                startAnyFormActivity(HnppConstants.JSON_FORMS.CHILD_REFERRAL,REQUEST_REFERRAL);
            }
        });



    }

    /**
     * submit all collected data
     */
    private void submitData() {
        jsonStringList.clear();
        showProgressDialog(R.string.data_adding);
        jsonStringList.add(childFollowUpJsonString);
        if(childReferralJsonString!=null && !childReferralJsonString.isEmpty()){
            jsonStringList.add(childReferralJsonString);
        }

        for(ReferralFollowupJsonModel model : referralFollowupJsonList){
            jsonStringList.add(model.getJson());
        }
        processForm();
        //hideProgressDialog();

    }

    /**
     * process all data
     */
    void processForm(){
        processVisitFormAndSave()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        //isSave.set(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ChildFollowupActivity.this,""+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();
                        Intent intent = getIntent();
                        setResult(ChildFollowupActivity.RESULT_CHILD_FOLLOW_UP, intent);
                        finish();
                    }
                });
    }

    /**
     * initializing all views here
     */
    private void initView() {
        closeImage = findViewById(R.id.close_image_view);
        saveButton = findViewById(R.id.submit_btn);

        childFollowUpLay = findViewById(R.id.child_followup_lay);
        immunizationLay = findViewById(R.id.immunization_lay);
        gmpLay = findViewById(R.id.gmp_lay);
        referralLay = findViewById(R.id.referral_ley);
        referralFollowupLay = findViewById(R.id.referral_followup_ley);

        childFollowupCheckIm = findViewById(R.id.child_followup_check_im);
        immunizationCheckIm = findViewById(R.id.immunization_check_im);
        gmpCheckIm = findViewById(R.id.gmp_check_im);
        referralCheckIm = findViewById(R.id.referral_check_im);
        referralFollowupCheckIm = findViewById(R.id.referral_followup_check_im);

        notReferredBt = findViewById(R.id.not_referred);
        noImmunizationBt = findViewById(R.id.no_immunization_button);
        referralFollowupCauseTv = findViewById(R.id.referral_followup_cause_tv);
        referralFollowupRv = findViewById(R.id.referral_followup_rv);
    }

    /**
     * start child followup form here
     * @param formName form name to identify form type
     * @param requestCode request code to handle result
     */
    public void startAnyFormActivity(String formName, int requestCode) {
        if(!HnppApplication.getStockRepository().isAvailableStock(HnppConstants.formNameEventTypeMapping.get(formName))){
            HnppConstants.showOneButtonDialog(this,getString(R.string.dialog_stock_sell_end),"");
            return;
        }
        HnppConstants.getGPSLocation(this, (latitude, longitude) -> {
            try {
                if(TextUtils.isEmpty(childBaseEntityId)){
                    Toast.makeText(ChildFollowupActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                    finish();
                }
                HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:"+childBaseEntityId+":formName:"+formName);

                JSONObject jsonForm = FormUtils.getInstance(ChildFollowupActivity.this).getFormJson(formName);
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
                if(HnppConstants.JSON_FORMS.IYCF_PACKAGE.equalsIgnoreCase(formName)){
                    JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                    JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                    String DOB = /*((HnppChildProfilePresenter) presenter).getDateOfBirth()*/birthDate;
                    Date date = Utils.dobStringToDate(DOB);
                    String dobFormate = HnppConstants.DDMMYY.format(date);
                    updateFormField(jsonArray,"dob",dobFormate);
                    String birthWeight = HnppDBUtils.getBirthWeight(childBaseEntityId);
                    updateFormField(jsonArray,"weight",birthWeight);
                }

                if(formName.equalsIgnoreCase(HnppConstants.JSON_FORMS.BLOOD_TEST)){
                    if(gender.equalsIgnoreCase("F")){
                        HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"is_women","true");
                    }
                }
                jsonForm.put(JsonFormUtils.ENTITY_ID, memberObj.getFamilyHead());
                Intent intent = new Intent(ChildFollowupActivity.this, HnppAncJsonFormActivity.class);
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

            }catch (Exception ignored){

            }
        });


    }

    /**
     * start referral followup form
     * @param referralFollowUpModel /// item model
     */
    public void openReferealFollowUp(ReferralFollowupJsonModel referralFollowUpModel) {
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPost(double latitude, double longitude) {
                try {
                    if(TextUtils.isEmpty(childBaseEntityId)){
                        Toast.makeText(ChildFollowupActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    HnppConstants.appendLog("SAVE_VISIT", "openReferealFollowUp>>childBaseEntityId:"+childBaseEntityId);

                    JSONObject jsonForm = FormUtils.getInstance(ChildFollowupActivity.this).getFormJson(HnppConstants.JSON_FORMS.REFERREL_FOLLOWUP);
                    jsonForm.put(JsonFormUtils.ENTITY_ID, childBaseEntityId);
                    try{
                        HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    HnppJsonFormUtils.addReferrelReasonPlaceField(jsonForm,referralFollowUpModel.getReferralFollowUpModel().getReferralReason(),referralFollowUpModel.getReferralFollowUpModel().getReferralPlace());
                    Intent intent;
                    intent = new Intent(ChildFollowupActivity.this, HnppAncJsonFormActivity.class);
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
                    startActivityForResult(intent, REQUEST_REFERRAL_FOLLOWUP);

                }catch (Exception e){

                }
            }
        });


    }



    /**
     * handle all data collection result here
     * @param requestCode request code
     * @param resultCode result code
     * @param data returned data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            //handling child followup submission here
            if(requestCode == REQUEST_HOME_VISIT){
                // if(isProcessing) return;
                AtomicInteger isSave = new AtomicInteger(2);
                showProgressDialog(R.string.please_wait_message);

                // isProcessing = true;
               if(data!=null){
                   String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                   String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                   String visitId = JsonFormUtils.generateRandomUUIDString();
                   HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

                   if(jsonString!=null){
                       childFollowUpJsonString = jsonString;

                       if(!childFollowUpJsonString.isEmpty()){
                           childFollowupCheckIm.setImageResource(R.drawable.success);
                           hideProgressDialog();
                           showServiceDoneDialog();
                       }
                   }
               }
            }
            //handling referral submission here
            else if(requestCode == REQUEST_REFERRAL){
                showProgressDialog(R.string.please_wait_message);

                if(data!=null){
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                    String visitId = JsonFormUtils.generateRandomUUIDString();
                    HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

                    childReferralJsonString = jsonString==null?"":jsonString;

                    if(!childReferralJsonString.isEmpty()){
                        referralCheckIm.setImageResource(R.drawable.success);
                        referralCheckIm.setColorFilter(ContextCompat.getColor(this, R.color.others));
                        hideProgressDialog();
                        showServiceDoneDialog();
                    }else{
                        referralCheckIm.setImageResource(R.drawable.success);
                        referralCheckIm.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                        hideProgressDialog();
                        showServiceDoneDialog();
                    }
                }
            }
            else if(requestCode == REQUEST_REFERRAL_FOLLOWUP){
                showProgressDialog(R.string.please_wait_message);

                if(data!=null){
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                    String visitId = JsonFormUtils.generateRandomUUIDString();
                    HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

                    String childReferralFollowupJsonString = jsonString==null?"":jsonString;

                    if(clickedItemPos <= 0){
                        if(!childReferralFollowupJsonString.isEmpty()){
                            referralFollowupJsonList.get(clickedItemPos).setJson(childReferralFollowupJsonString);
                        }
                    }
                    hideProgressDialog();
                    showServiceDoneDialog();
                    adapter.notifyDataSetChanged();
                }
            }
        }

        //handling gmp submission status
        else  if(resultCode == ChildGMPActivity.GMP_RESULT_CODE){
            if (data != null && data.getBooleanExtra("GMP_TAKEN", false)) {
                gmpCheckIm.setImageResource(R.drawable.success);
                isGmpTaken = true;
            }
        }
        //handling vaccine submission status
        else if(resultCode == ChildVaccinationActivity.VACCINE_RESULT_CODE){
            if(data != null){
                if(isImmunizationTaken==null || !isImmunizationTaken){
                    isImmunizationTaken = data.getBooleanExtra("VACCINE_TAKEN", false);
                }

                if (isImmunizationTaken) {
                    immunizationCheckIm.setImageResource(R.drawable.success);
                    immunizationCheckIm.setColorFilter(ContextCompat.getColor(this, R.color.others));
                }else{
                    immunizationCheckIm.setImageResource(R.drawable.success);
                    immunizationCheckIm.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                }
            }
        }
        checkButtonEnableStatus();
    }

    private void checkButtonEnableStatus() {
        if(!childFollowUpJsonString.isEmpty() && childReferralJsonString!=null && isImmunizationTaken!=null && isGmpTaken){
            for(ReferralFollowupJsonModel model : referralFollowupJsonList){
                if(model.getJson().isEmpty()){
                    saveButton.setEnabled(false);
                    saveButton.setTextColor(Color.GRAY);
                    return;
                }
            }

            saveButton.setEnabled(true);
            saveButton.setTextColor(Color.WHITE);

        }else {
            saveButton.setEnabled(false);
            saveButton.setTextColor(Color.GRAY);
        }
    }

    //process prom via rxJava
    private Observable<Integer> processVisitFormAndSave(){

        return Observable.create(e-> {
            if(TextUtils.isEmpty(childBaseEntityId)) e.onNext(2);
            try {
                int successCount = 0;
                for(String jsonString : jsonStringList){
                    String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                    String visitId = JsonFormUtils.generateRandomUUIDString();

                    JSONObject form = new JSONObject(jsonString);
                    HnppJsonFormUtils.setEncounterDateTime(form);

                    String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                    type = HnppJsonFormUtils.getEncounterType(type);
                    Map<String, String> jsonStrings = new HashMap<>();
                    jsonStrings.put("First",form.toString());
                    HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":type:"+type);

                    Visit visit = HnppJsonFormUtils.saveVisit(false,false,false,"", childBaseEntityId, type, jsonStrings, "",formSubmissionId,visitId);
                    if(visit!=null && !visit.getVisitId().equals("0")){
                        HnppHomeVisitIntentService.processVisits();
                        FormParser.processVisitLog(visit);
                        HnppConstants.appendLog("SAVE_VISIT", "processVisitLog done:"+formSubmissionId+":type:"+type);

                        //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                        successCount++;


                    }/*else if(visit != null && visit.getVisitId().equals("0")){
                        e.onNext(3);
                        e.onComplete();
                    }else{
                        e.onNext(2);
                        e.onComplete();
                    }*/
                }
                if(successCount == jsonStringList.size()){
                    e.onNext(1);
                }else {
                    e.onNext(2);
                }
                e.onComplete();
            } catch (Exception ex) {
                HnppConstants.appendLog("SAVE_VISIT","exception processVisitFormAndSave >>"+ex.getMessage());
                e.onNext(1);
                e.onComplete();
            }
        });
    }
    /**
     * show progressbar
     * @param message //progress dialog message
     */
    public void showProgressDialog(int message) {
        builder.setMessage(message);
        alertDialog = builder.show();
    }

    /**
     * hide progressbar
     */
    public void hideProgressDialog() {
        alertDialog.dismiss();
    }

    /**
     * service done dialog
     */
    private void showServiceDoneDialog(){
        if(dialog != null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(R.string.survice_added);
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

    }
}