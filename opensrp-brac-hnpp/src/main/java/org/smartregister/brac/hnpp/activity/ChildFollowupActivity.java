package org.smartregister.brac.hnpp.activity;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import org.smartregister.brac.hnpp.listener.OnPostDataWithGps;
import org.smartregister.brac.hnpp.presenter.HnppChildProfilePresenter;
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
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.activity.SecuredActivity;

import java.util.Date;
import java.util.HashMap;
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
    public static String IS_ONLY_VACC = "is_only_vacc";

    ImageView closeImage;
    TextView saveText;

    LinearLayout childFollowUpLay,immunizationLay,gmpLay,referralLay;
    ImageView childFollowupCheckIm,immunizationCheckIm,gmpCheckIm,referralCheckIm;

    String childBaseEntityId,formType,gender,birthDate,familyHead;
    MemberObject memberObj;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Dialog dialog;

    String childFollowUpJsonString = "";
    CommonPersonObjectClient commonPersonObjectClient;
    Bundle bundle;

    boolean isOnlyVacc = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_child_followup);

        getIntentData();
        initView();
        viewInteraction();
        initProgressDialog();
    }

    private void initProgressDialog() {
        builder = new AlertDialog.Builder(this);
    }



    private void getIntentData() {
        Intent intent = getIntent();
        familyHead = intent.getStringExtra(BIRTH_DATE);
        memberObj = (MemberObject) intent.getSerializableExtra(FAMILY_HEAD);
        formType = intent.getStringExtra(TYPE);
        childBaseEntityId = intent.getStringExtra(BASE_ENTITY_ID);
        gender = intent.getStringExtra(GENDER);
        commonPersonObjectClient = (CommonPersonObjectClient) intent.getSerializableExtra(COMMON_PERSON);
        bundle = intent.getParcelableExtra(BUNDLE);
        isOnlyVacc = intent.getBooleanExtra(IS_ONLY_VACC,false);
    }

    private void viewInteraction() {
        closeImage.setOnClickListener(view -> finish());

        saveText.setOnClickListener(view -> {

        });

        childFollowUpLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(childFollowUpJsonString.isEmpty()){
                    startAnyFormActivity(formType,REQUEST_HOME_VISIT);
                }
            }
        });

        immunizationLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChildVaccinationActivity.startChildVaccinationActivity(ChildFollowupActivity.this,bundle,commonPersonObjectClient,isOnlyVacc);
            }
        });

        gmpLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChildGMPActivity.startGMPActivity(ChildFollowupActivity.this,bundle,commonPersonObjectClient);
            }
        });
    }

    private void initView() {
        closeImage = findViewById(R.id.close_image_view);
        saveText = findViewById(R.id.submit_tv);

        childFollowUpLay = findViewById(R.id.child_followup_lay);
        immunizationLay = findViewById(R.id.immunization_lay);
        gmpLay = findViewById(R.id.gmp_lay);
        referralLay = findViewById(R.id.referral_ley);

        childFollowupCheckIm = findViewById(R.id.child_followup_check_im);
        immunizationCheckIm = findViewById(R.id.immunization_check_im);
        gmpCheckIm = findViewById(R.id.gmp_check_im);
        referralCheckIm = findViewById(R.id.referral_check_im);
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
        HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
            @Override
            public void onPost(double latitude, double longitude) {
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

                }catch (Exception e){

                }
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_HOME_VISIT){
                // if(isProcessing) return;
                AtomicInteger isSave = new AtomicInteger(2);
                showProgressDialog(R.string.please_wait_message);

                // isProcessing = true;
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                String visitId = JsonFormUtils.generateRandomUUIDString();
                HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);

                childFollowUpJsonString = jsonString;

                if(!childFollowUpJsonString.isEmpty()){
                    childFollowupCheckIm.setImageResource(R.drawable.success);
                    hideProgressDialog();
                    showServiceDoneDialog();
                }
            }

            if (data != null && data.getBooleanExtra("VACCINE_TAKEN", false)) {
                immunizationCheckIm.setImageResource(R.drawable.success);
            }
/*
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
                                //showServiceDoneDialog(1);
                            }else if(isSave.get() == 3){
                                hideProgressDialog();
                                //showServiceDoneDialog(3);
                            }else {
                                hideProgressDialog();
                                //showServiceDoneDialog(false);
                            }
                        }
                    });*/
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

        }
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

    /**
     * show progressbar
     * @param message
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

    private void showServiceDoneDialog(){
        if(dialog != null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(R.string.survice_added);
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}