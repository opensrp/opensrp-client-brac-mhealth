package org.smartregister.brac.hnpp.activity;


import static org.smartregister.chw.anc.util.JsonFormUtils.updateFormField;
import static org.smartregister.util.JsonFormUtils.FIELDS;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NewANCRegistrationActivity extends AppCompatActivity {
    public static String BASE_ENTITY_ID = "baseEntityId";
    public static final int RESULT_ANC_REGISTRATION = 111;
    public static final int REQUEST_ANC_REG = 101;
    public static final int REQUEST_HISTORY = 102;
    public static final int REQUEST_DISAGES = 103;
    public static final int REQUEST_DIVERTY = 104;
    public static final int REQUEST_ANC_VISIT = 105;
    public static final int RESULT_CHILD_FOLLOW_UP = 10111;

    ImageView closeImage;
    Button saveButton;

    Button notInterestedB;

    LinearLayout ancRegLay,physicalProbLay,historyLay,diversityLay,ancLey;
    ImageView ancRegCheckIm,physicalProbCheckIm,historyCheckIm,diversityCheckIm,ancLeyCheckIm;

    String baseEntityId;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Dialog dialog;
    ArrayList<String> jsonStringList = new ArrayList<>();
    public static void startNewAncRegistrationActivity(Activity activity, String baseEntityId){
        Intent intent = new Intent(activity, NewANCRegistrationActivity.class);
        intent.putExtra(BASE_ENTITY_ID,baseEntityId);
        activity.startActivityForResult(intent,RESULT_ANC_REGISTRATION);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_anc_registration);

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
        baseEntityId = intent.getStringExtra(BASE_ENTITY_ID);

    }

    /**
     * view interaction like button click
     */
    private void viewInteraction() {


        closeImage.setOnClickListener(view -> finish());

        saveButton.setOnClickListener(view -> {
            submitData();
        });

        notInterestedB.setOnClickListener(view -> {
            jsonStringList.add("test");
            ancLeyCheckIm.setImageResource(R.drawable.success);
            ancLeyCheckIm.setColorFilter(ContextCompat.getColor(NewANCRegistrationActivity.this, android.R.color.holo_orange_dark));
            checkButtonEnableStatus();
        });

        ancRegLay.setOnClickListener(view -> {
                startAnyFormActivity(HnppConstants.JSON_FORMS.ANC_FORM,REQUEST_ANC_REG);

        });
        historyLay.setOnClickListener(view -> {
            startAnyFormActivity(HnppConstants.JSON_FORMS.PREGNANCY_HISTORY,REQUEST_HISTORY);

        });
        physicalProbLay.setOnClickListener(view -> {
            startAnyFormActivity(HnppConstants.JSON_FORMS.GENERAL_DISEASE,REQUEST_DISAGES);

        });
        diversityLay.setOnClickListener(view -> {
            startAnyFormActivity(HnppConstants.JSON_FORMS.PREGNANT_WOMAN_DIETARY_DIVERSITY,REQUEST_DIVERTY);

        });
        ancLey.setOnClickListener(view -> {
            startAnyFormActivity(HnppConstants.JSON_FORMS.ANC1_FORM,REQUEST_ANC_VISIT);

        });

    }

    /**
     * submit all collected data
     */
    private void submitData() {
        showProgressDialog(R.string.data_adding);
        processForm();
        //hideProgressDialog();

    }
    private boolean isHighRisk(){
        for(String jsonstr: jsonStringList){
            try {
                JSONObject jsonForm = new JSONObject(jsonstr);
                JSONObject step1 = jsonForm.getJSONObject("step1");
                JSONArray fields = step1.getJSONArray(FIELDS);
                for (int i = 0; i < fields.length(); i++) {
                    try {
                        JSONObject fieldObject = fields.getJSONObject(i);
                        if("is_high_risk".equalsIgnoreCase(fieldObject.getString("key"))){
                            String str = fieldObject.getString("is_visible");
                            if (Boolean.parseBoolean(str)) {
                                return true;
                            }
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;

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
                        Toast.makeText(NewANCRegistrationActivity.this,""+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();
                        Intent intent = getIntent();
                        setResult(NewANCRegistrationActivity.RESULT_CHILD_FOLLOW_UP, intent);
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

        ancRegLay = findViewById(R.id.anc_reg_lay);
        physicalProbLay = findViewById(R.id.physical_prob_lay);
        historyLay = findViewById(R.id.history_lay);
        diversityLay = findViewById(R.id.diversity_lay);
        ancLey = findViewById(R.id.anc_ley);

        ancRegCheckIm= findViewById(R.id.anc_reg_check_im);
        physicalProbCheckIm = findViewById(R.id.physical_prob_check_im);
        historyCheckIm = findViewById(R.id.history_check_im);
        diversityCheckIm = findViewById(R.id.diversity_check_im);
        ancLeyCheckIm = findViewById(R.id.anc_check_im);

        notInterestedB = findViewById(R.id.not_interested);
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
                if(TextUtils.isEmpty(baseEntityId)){
                    Toast.makeText(NewANCRegistrationActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
                    finish();
                }
                HnppConstants.appendLog("SAVE_VISIT", "open form>>childBaseEntityId:"+baseEntityId+":formName:"+formName);

                JSONObject jsonForm = FormUtils.getInstance(NewANCRegistrationActivity.this).getFormJson(formName);
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
                try{
                    String dob = HnppDBUtils.getDOB(baseEntityId);
                    Date date = Utils.dobStringToDate(dob);
                    String dobFormate = HnppConstants.DDMMYY.format(date);
                    HnppJsonFormUtils.addJsonKeyValue(jsonForm,"dob",dobFormate);
                    String phoneNo = HnppDBUtils.getPhoneNo(baseEntityId);
                    HnppJsonFormUtils.addJsonKeyValue(jsonForm,"phone_number",phoneNo);
                }catch (Exception e){

                }

                jsonForm.put(JsonFormUtils.ENTITY_ID,baseEntityId);
                Intent intent = new Intent(NewANCRegistrationActivity.this, HnppAncJsonFormActivity.class);
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
     * handle all data collection result here
     * @param requestCode request code
     * @param resultCode result code
     * @param data returned data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK){
            //handling referral submission here
            if(requestCode == REQUEST_ANC_REG){
                if(data!=null){
                    setJsonStringList(data,ancRegCheckIm);
                }
            }
            else if(requestCode == REQUEST_HISTORY){
                if(data!=null){
                    setJsonStringList(data,historyCheckIm);
                }
            }
            else if(requestCode == REQUEST_DISAGES){
                if(data!=null){
                    setJsonStringList(data,physicalProbCheckIm);
                }
            }
            else if(requestCode == REQUEST_DIVERTY){
                if(data!=null){
                    setJsonStringList(data,diversityCheckIm);
                   boolean isHighRisk =  isHighRisk();
                   if(isHighRisk){
                       notInterestedB.setVisibility(View.INVISIBLE);
                   }
                }
            }
            else if(requestCode == REQUEST_ANC_VISIT){
                if(data!=null){
                    setJsonStringList(data,ancLeyCheckIm);
                }
            }

        }
        checkButtonEnableStatus();
    }
    private void setJsonStringList(Intent data, ImageView imageView){
        String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
        if(!TextUtils.isEmpty(jsonString)){
            jsonStringList.add(jsonString);
            imageView.setImageResource(R.drawable.success);
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.others));
        }
    }

    private void checkButtonEnableStatus() {
        if(jsonStringList.size() == 5){
            saveButton.setEnabled(true);
            saveButton.setTextColor(Color.WHITE);
        }else{
            saveButton.setEnabled(false);
            saveButton.setTextColor(Color.GRAY);
        }
    }

    //process prom via rxJava
    private Observable<Integer> processVisitFormAndSave(){

        return Observable.create(e-> {
            if(TextUtils.isEmpty(baseEntityId)) e.onNext(2);
            try {
                int successCount = 0;
                for(String jsonString : jsonStringList){
                    if(Objects.equals(jsonString, "test")) {
                        successCount++;
                        continue;
                    }
                    String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
                    String visitId = JsonFormUtils.generateRandomUUIDString();

                    JSONObject form = new JSONObject(jsonString);
                    HnppJsonFormUtils.setEncounterDateTime(form);

                    String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                    type = HnppJsonFormUtils.getEncounterType(type);
                    Map<String, String> jsonStrings = new HashMap<>();
                    jsonStrings.put("First",form.toString());
                    HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+baseEntityId+":type:"+type);

                    Visit visit = HnppJsonFormUtils.saveVisit(false,false,false,"", baseEntityId, type, jsonStrings, "",formSubmissionId,visitId);
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