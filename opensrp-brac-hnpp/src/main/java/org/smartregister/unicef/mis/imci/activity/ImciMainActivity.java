package org.smartregister.unicef.mis.imci.activity;

import static org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.family.util.Utils;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppAncJsonFormActivity;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.imci.fragment.IMCIAssessmentDialogFragment;
import org.smartregister.unicef.mis.presenter.HnppChildProfilePresenter;
import org.smartregister.unicef.mis.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.ReferralData;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.SecuredActivity;

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


public class ImciMainActivity extends SecuredActivity {
    public static final String EXTRA_BASE_ENTITY_ID = "base_entity_id";
    public static final String EXTRA_DOB_FORMAT = "dob_format";
    public static final String EXTRA_IMCI_TYPE = "imci_type";
    public static final int IMCI_TYPE_0_2 = 1;
    public static final int IMCI_TYPE_2_59 = 2;
    public static final int REQUEST_IMCI_ACTIVITY = 1123;
    public static final int REQUEST_IMCI_SEVERE_0_2 = 1234;
    public static final int REQUEST_IMCI_DIARRHEA_0_2 = 1235;
    public static final int REQUEST_IMCI_FEEDING_0_2 = 1236;
    public static final int REQUEST_IMCI_SEVERE_2_59 = 1237;
    public static final int REQUEST_IMCI_PNEUMONIA_2_59 = 1237;
    public static final int REQUEST_IMCI_DIARRHEA_2_59 = 1238;
    public static final int REQUEST_IMCI_FEVER_2_59 = 1239;
    public static final int REQUEST_IMCI_MALNUTRITION_2_59 = 1240;
    public static final int REQUEST_IMCI_ANAEMIA_2_59 = 1241;
    public static void startIMCIActivity(Activity activity, String childBaseEntityId,String dobFormat, int requestCode, int imciType){
        Intent intent = new Intent(activity,ImciMainActivity.class);
        intent.putExtra(EXTRA_BASE_ENTITY_ID,childBaseEntityId);
        intent.putExtra(EXTRA_DOB_FORMAT,dobFormat);
        intent.putExtra(EXTRA_IMCI_TYPE,imciType);
        activity.startActivityForResult(intent,requestCode);
    }
    LinearLayout severeLL,diarrheaLL,feedingLL,dangerSignLL,pnumoniaLL,diarrhea2_59LL,feverLL,malNutritionLL,anaemiaLL;
    ImageView severeCheckIm,diarrheCheckIm,feedingCheckIm,dangerSignCheckIm,pnumoniaCheckIm,diarrhea2_59CheckIm,feverCheckIm,malNutritionCheckIm,anaemiaCheckIm;
    TextView diarrheaTxt,feedingTxt,titleTxt;
    String childBaseEntityId;
    Button nextBtn;
    HashMap<Integer,String> jsonForms = new HashMap<>();
    String dobFormat;
    int imciType = IMCI_TYPE_0_2;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_imci);
        childBaseEntityId = getIntent().getStringExtra(EXTRA_BASE_ENTITY_ID);
        dobFormat = getIntent().getStringExtra(EXTRA_DOB_FORMAT);
        imciType = getIntent().getIntExtra(EXTRA_IMCI_TYPE,IMCI_TYPE_0_2);
        nextBtn = findViewById(R.id.next_button);
        nextBtn.setEnabled(false);
        titleTxt = findViewById(R.id.title);
        if(imciType == IMCI_TYPE_0_2){
            titleTxt.setText("ছোট শিশুর (০-২ মাস) রোগ নিরূপণ");
            findViewById(R.id.ll0_2).setVisibility(View.VISIBLE);
            findViewById(R.id.ll2_59).setVisibility(View.GONE);
        }else if(imciType == IMCI_TYPE_2_59){
            titleTxt.setText("শিশুর (২-৫৯ মাস) রোগ নিরূপণ");
            findViewById(R.id.ll0_2).setVisibility(View.GONE);
            findViewById(R.id.ll2_59).setVisibility(View.VISIBLE);
        }
        severeLL = findViewById(R.id.severe_update_lay);
        diarrheaLL = findViewById(R.id.Diarrhoea_update_lay);
        feedingLL = findViewById(R.id.Feeding_update_lay);
        dangerSignLL = findViewById(R.id.severe_2_59_update_lay);
        pnumoniaLL = findViewById(R.id.pnumenia_update_lay);
        diarrhea2_59LL = findViewById(R.id.Diarrhoea_2_59_update_lay);
        feverLL = findViewById(R.id.fever_2_59_update_lay);
        malNutritionLL = findViewById(R.id.malnutrition_2_59_update_lay);
        anaemiaLL = findViewById(R.id.anaemia_2_59_update_lay);
        severeCheckIm = findViewById(R.id.severe_check_im);
        feedingCheckIm = findViewById(R.id.Feeding_check_im);
        diarrheCheckIm = findViewById(R.id.Diarrhoea_check_im);
        dangerSignCheckIm = findViewById(R.id.severe_2_59_check_im);
        pnumoniaCheckIm = findViewById(R.id.pnumenia_check_im);
        diarrhea2_59CheckIm = findViewById(R.id.Diarrhoea_2_59_check_im);

        feverCheckIm = findViewById(R.id.fever_check_im);
        malNutritionCheckIm  = findViewById(R.id.malnutrition_check_im);
        anaemiaCheckIm = findViewById(R.id.anaemia_check_im);
        diarrheaTxt = findViewById(R.id.Diarrhoea_text);
        feedingTxt = findViewById(R.id.Feeding_text);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = 1;
                for (Integer key : jsonForms.keySet()) {
                     String json = jsonForms.get(key);
                    Log.v("COUNT_JSON","count:"+count+":key:"+key);
                    if(count==3){
                        saveData(json,true);
                    }else{
                        saveData(json,false);
                        count++;
                    }
                }
            }
        });
        severeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_SEVERE_0_2,REQUEST_IMCI_SEVERE_0_2);
            }
        });
        diarrheaLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_DIARRHEA_0_2,REQUEST_IMCI_DIARRHEA_0_2);
            }
        });
        feedingLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_FEEDING_0_2,REQUEST_IMCI_FEEDING_0_2);
            }
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        dangerSignLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_SEVERE_2_59,REQUEST_IMCI_SEVERE_2_59);
            }
        });
        pnumoniaLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_PNEUMONIA_2_59,REQUEST_IMCI_PNEUMONIA_2_59);
            }
        });
        diarrhea2_59LL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_DIARRHEA_2_59,REQUEST_IMCI_DIARRHEA_2_59);
            }
        });
        feverLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_FEVER_2_59,REQUEST_IMCI_FEVER_2_59);
            }
        });
        malNutritionLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_MALNUTRITION_2_59,REQUEST_IMCI_MALNUTRITION_2_59);
            }
        });
        anaemiaLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_ANAEMIA_2_59,REQUEST_IMCI_ANAEMIA_2_59);
            }
        });
        if(imciType == IMCI_TYPE_2_59)loadIMCIType2_59_PreviousData();
        else if(imciType == IMCI_TYPE_0_2) loadIMCIType0_2_PreviousData();
    }
    private void loadIMCIType0_2_PreviousData(){
        //disableTextColor();
        List<Visit> v = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_SEVERE_0_2);
        if(v.size()>0){
            Visit imciSevereVisit = v.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciSevereVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_SEVERE_0_2,jsonForm.toString());
            updateUI(REQUEST_IMCI_SEVERE_0_2);
        }
        List<Visit> vD = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_DIARRHEA_0_2);
        if(vD.size()>0){
            Visit imciDiarrheaVisit = vD.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciDiarrheaVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_DIARRHEA_0_2,jsonForm.toString());
            updateUI(REQUEST_IMCI_DIARRHEA_0_2);
        }
        List<Visit> vF = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_DIARRHEA_0_2);
        if(vF.size()>0){
            Visit imciFeedingVisit = vF.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciFeedingVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_FEEDING_0_2,jsonForm.toString());
            updateUI(REQUEST_IMCI_FEEDING_0_2);
        }


    }
    private void loadIMCIType2_59_PreviousData(){
        //disableTextColor();
        List<Visit> v = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_SEVERE_2_59);
        if(v.size()>0){
            Visit imciSevereVisit = v.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciSevereVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_SEVERE_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_SEVERE_2_59);
        }
        List<Visit> vD = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_PNEUMONIA_2_59);
        if(vD.size()>0){
            Visit imciDiarrheaVisit = vD.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciDiarrheaVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_PNEUMONIA_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_PNEUMONIA_2_59);
        }
        List<Visit> vF = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_DIARRHEA_2_59);
        if(vF.size()>0){
            Visit imciFeedingVisit = vF.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciFeedingVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_DIARRHEA_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_DIARRHEA_2_59);
        }
        List<Visit> vFv = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_FEVER_2_59);
        if(vFv.size()>0){
            Visit imciFeverVisit = vFv.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciFeverVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_FEVER_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_FEVER_2_59);
        }
        List<Visit> vMal = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_MALNUTRITION_2_59);
        if(vMal.size()>0){
            Visit imciMalVisit = vMal.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciMalVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_MALNUTRITION_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_MALNUTRITION_2_59);
        }
        List<Visit> vAn = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByBaseEntityId(childBaseEntityId, HnppConstants.EVENT_TYPE.IMCI_ANAEMIA_2_59);
        if(vAn.size()>0){
            Visit imciAnameaVisit = vAn.get(0);
            JSONObject jsonForm = HnppJsonFormUtils.getVisitFormWithData(imciAnameaVisit.getJson(),this);
            jsonForms.put(REQUEST_IMCI_ANAEMIA_2_59,jsonForm.toString());
            updateUI(REQUEST_IMCI_ANAEMIA_2_59);
        }
    }
    String assessmentTypeId;
    public void openRefereal(String assesmentTypeId) {
        this.assessmentTypeId = assesmentTypeId;
        startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_CHILD_REFERRAL,REQUEST_HOME_VISIT);
    }
    int requestCode;
    public void startAnyFormActivity(String formName, int requestCode){
        try{
            String formStr;
            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(formName);
            this.requestCode = requestCode;
            if(requestCode == REQUEST_IMCI_FEEDING_0_2){
                HnppJsonFormUtils.addValueAtJsonForm(jsonForm,"dob", dobFormat);
            }
            if(jsonForms.get(requestCode)!=null && !jsonForms.get(requestCode).isEmpty()){
                formStr = jsonForms.get(requestCode);
            }
            else{
                formStr = jsonForm.toString();
            }
            Intent intent = new Intent(ImciMainActivity.this, HnppAncJsonFormActivity.class);

            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, formStr);


            Form form = new Form();
            form.setWizard(true);
            if(requestCode == REQUEST_HOME_VISIT){
                form.setSaveLabel(getString(R.string.save));
            }else{
                form.setSaveLabel(getString(R.string.next));
            }
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            startActivityForResult(intent, requestCode);
        }catch (Exception e){

            e.printStackTrace();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(TextUtils.isEmpty(childBaseEntityId)){
            Toast.makeText(ImciMainActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            saveData(jsonString,true);
            saveData(jsonForms.get(REQUEST_IMCI_SEVERE_0_2),false);

        }
        if(resultCode == Activity.RESULT_OK && requestCode != REQUEST_HOME_VISIT){
            updateUI(requestCode);
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            jsonForms.put(requestCode,jsonString);
            IMCIAssessmentDialogFragment.getInstance(this).setJsonData(requestCode,jsonString);
        }
    }
    boolean isProcessing = false;
    public void saveData(String jsonString, boolean needToShowServiceDoneDialog){
        //if(isProcessing) return;
        AtomicInteger isSave = new AtomicInteger(2);
        showProgressDialog(getString(R.string.please_wait_message));
        isProcessing = true;
        String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
        String visitId = JsonFormUtils.generateRandomUUIDString();
        HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);
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
                        Log.v("SAVE_VISIT","onComplete:isProcessing:"+isProcessing+":"+isSave.get());
                        isProcessing = false;
                        if(isSave.get() == 1){
                            hideProgressDialog();
                            if(needToShowServiceDoneDialog)showServiceDoneDialog(1);
                        }else if(isSave.get() == 3){
                            hideProgressDialog();
                            if(needToShowServiceDoneDialog)showServiceDoneDialog(3);
                        }else {
                            hideProgressDialog();
                            //showServiceDoneDialog(false);
                        }
                    }
                });
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

                Visit visit = HnppJsonFormUtils.saveVisit(childBaseEntityId, type, jsonStrings, formSubmissionid,visitId,jsonString);
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
    private void updateUI(int requestType){

        switch (requestType){
            case REQUEST_IMCI_SEVERE_0_2:
                severeCheckIm.setImageResource(R.drawable.success);
                severeCheckIm.setColorFilter(ContextCompat.getColor(this, R.color.others));
                //enableDiarrheaTextColor();
                break;
            case REQUEST_IMCI_DIARRHEA_0_2:
                diarrheCheckIm.setImageResource(R.drawable.success);
                diarrheCheckIm.setColorFilter(ContextCompat.getColor(this, R.color.others));
               // enableFeedingTextColor();
                break;
            case REQUEST_IMCI_FEEDING_0_2:
                feedingCheckIm.setImageResource(R.drawable.success);
                feedingCheckIm.setColorFilter(ContextCompat.getColor(this, R.color.others));
                break;
        }
        if(imciType == IMCI_TYPE_0_2 && jsonForms.size()>=2){
            nextBtn.setEnabled(true);
            nextBtn.setTextColor(getResources().getColor(R.color.white));
        }else if(imciType == IMCI_TYPE_2_59 && jsonForms.size()>=4){
            nextBtn.setEnabled(true);
            nextBtn.setTextColor(getResources().getColor(R.color.white));
        }

    }
    private void disableTextColor(){
        diarrheaLL.setBackground(getResources().getDrawable(R.drawable.white_rounded_bg));
        diarrheaTxt.setTextColor(getResources().getColor(R.color.black));
        feedingLL.setBackground(getResources().getDrawable(R.drawable.white_rounded_bg));
        feedingTxt.setTextColor(getResources().getColor(R.color.black));
    }
    private void enableDiarrheaTextColor(){
        diarrheaLL.setBackground(getResources().getDrawable(R.drawable.login_button_bg));
        diarrheaTxt.setTextColor(getResources().getColor(R.color.white));
    }
    private void enableFeedingTextColor(){
        feedingLL.setBackground(getResources().getDrawable(R.drawable.login_button_bg));
        feedingTxt.setTextColor(getResources().getColor(R.color.white));
    }
    Dialog dialog;

    private void showServiceDoneDialog(Integer isSuccess){
        if(dialog != null) return;
        dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = dialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1? getString(R.string.service_done_succ):isSuccess==3? getString(R.string.service_already_given): getString(R.string.survice_done_failed));
        Button ok_btn = dialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialog = null;
                finish();
            }
        });
        dialog.show();

    }
    private ProgressDialog progressDialog;

    private void showProgressDialog(String text) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(text);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    @Override
    protected void onResumption() {

    }
}
