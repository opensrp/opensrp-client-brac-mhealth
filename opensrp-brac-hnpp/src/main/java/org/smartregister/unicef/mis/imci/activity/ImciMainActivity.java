package org.smartregister.unicef.mis.imci.activity;

import static org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppAncJsonFormActivity;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.imci.fragment.IMCIAssessmentDialogFragment;
import org.smartregister.unicef.mis.service.HnppHomeVisitIntentService;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.ReferralData;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class ImciMainActivity extends SecuredActivity {
    public static final String EXTRA_BASE_ENTITY_ID = "base_entity_id";
    public static final int REQUEST_IMCI_ACTIVITY = 1123;
    private static final int REQUEST_IMCI_SEVERE_0_2 = 1234;
    public static void startIMCIActivity(Activity activity, String childBaseEntityId, int requestCode){
        Intent intent = new Intent(activity,ImciMainActivity.class);
        intent.putExtra(EXTRA_BASE_ENTITY_ID,childBaseEntityId);
        activity.startActivityForResult(intent,requestCode);
    }
    LinearLayout severeLL;
    ImageView severeCheckIm;
    String childBaseEntityId;
    Button nextBtn;
    HashMap<Integer,String> jsonForms = new HashMap<>();

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_imci);
        childBaseEntityId = getIntent().getStringExtra(EXTRA_BASE_ENTITY_ID);
        nextBtn = findViewById(R.id.next_button);
        nextBtn.setEnabled(false);
        severeLL = findViewById(R.id.severe_update_lay);
        severeCheckIm = findViewById(R.id.severe_check_im);
        severeLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnyFormActivity(HnppConstants.JSON_FORMS.IMCI_SEVERE_0_2,REQUEST_IMCI_SEVERE_0_2);
            }
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    public void startAnyFormActivity(String formName, int requestCode){
        try{
            String formStr;
            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(formName);
            if(jsonForms.get(REQUEST_IMCI_SEVERE_0_2)!=null && !jsonForms.get(REQUEST_IMCI_SEVERE_0_2).isEmpty()){
                formStr = jsonForms.get(REQUEST_IMCI_SEVERE_0_2);
            }else{
                formStr = jsonForm.toString();
            }
            Intent intent = new Intent(ImciMainActivity.this, HnppAncJsonFormActivity.class);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, formStr);

            Form form = new Form();
            form.setWizard(true);
            form.setSaveLabel(getString(R.string.next));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(TextUtils.isEmpty(childBaseEntityId)){
            Toast.makeText(ImciMainActivity.this, "baseentityid null", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMCI_SEVERE_0_2){
            updateUI(REQUEST_IMCI_SEVERE_0_2);
            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
            jsonForms.put(REQUEST_IMCI_SEVERE_0_2,jsonString);
            IMCIAssessmentDialogFragment.getInstance(this).setJsonData(jsonString);
//            AtomicInteger isSave = new AtomicInteger(2);
//            showProgressDialog(getString(R.string.please_wait_message));
//            String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
//            jsonForms.put(REQUEST_IMCI_SEVERE_0_2,jsonString);
//            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
//            String visitId = JsonFormUtils.generateRandomUUIDString();
//            HnppConstants.appendLog("SAVE_VISIT", "save form>>childBaseEntityId:"+childBaseEntityId+":formSubmissionId:"+formSubmissionId);
//
//            processVisitFormAndSave(jsonString,formSubmissionId,visitId,REQUEST_IMCI_SEVERE_0_2)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Observer<Integer>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(Integer integer) {
//                            isSave.set(integer);
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            hideProgressDialog();
//                        }
//
//                        @Override
//                        public void onComplete() {
//                            hideProgressDialog();
//                            updateUI(REQUEST_IMCI_SEVERE_0_2);
//                        }
//                    });
        }
    }
    private Observable<Integer> processVisitFormAndSave(String jsonString, String formSubmissionid, String visitId,int requestCode){
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
                break;
        }
        if(jsonForms.size()>=3){
            nextBtn.setEnabled(true);
            nextBtn.setTextColor(getResources().getColor(R.color.white));
        }

    }
    Dialog dialog;
    boolean isProcessing = false;
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
                isProcessing = false;
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
