package org.smartregister.brac.hnpp.activity;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;
import static org.smartregister.brac.hnpp.service.SSLocationFetchIntentService.WITHOUT_SK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SkSelectionAdapter;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.HnppHomeVisitIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PANewHomeActivity extends SecuredActivity implements View.OnClickListener,SyncStatusBroadcastReceiver.SyncStatusListener {
    private  String storeUserName;

    private BroadcastReceiver locationUpdateBroadcastReceiver;
    private AppExecutors appExecutors;
    private ImageView logoutBtn;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_pa_services);
        locationUpdateBroadcastReceiver = new LocationBroadcastReceiver();
        findViewById(R.id.vb_view).setOnClickListener(this);
        findViewById(R.id.ncd_view).setOnClickListener(this);
        findViewById(R.id.eye_test_view).setOnClickListener(this);
        findViewById(R.id.history_forum).setOnClickListener(this);
        findViewById(R.id.sync_text_txt).setOnClickListener(this);
        findViewById(R.id.refreshIndicatorsIcon).setOnClickListener(this);
        findViewById(R.id.unsync_count_txt).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.dashboard).setOnClickListener(this);
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        appExecutors = new AppExecutors();
        updateSpinner();
        updateUnSyncCount();
        ((TextView) findViewById(R.id.login_build_text_view)).setText("Version " + getVersion() + ", Built on: " + getBuildDate());
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String firstName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
        //  ((TextView) findViewById(R.id.user_name_txt)).setText(firstName+"\n"+userName);
        ((TextView) findViewById(R.id.username_text_view)).setText(firstName+"\n"+userName);
    }
    @SuppressLint("SetTextI18n")
    private void updateUnSyncCount(){
        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        //int cc = eventClientRepository.getUnSyncClientsCount();
        int ec = eventClientRepository.getUnSyncEventsCount();
        ((TextView)findViewById(R.id.unsync_count_txt)).setText(ec+"");
        ((TextView) findViewById(R.id.last_sync_text_view)).setText(getString(R.string.last_sync,getLastSyncDateTime()));

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HnppConstants.ACTION_LOCATION_UPDATE);
        registerReceiver(locationUpdateBroadcastReceiver, intentFilter);
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(PANewHomeActivity.this);

    }
    private String getVersion()  {
        PackageInfo pInfo = null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";

    }
    public String getLastServerVersion(){
        return String.valueOf(ECSyncHelper.getInstance(this).getLastSyncTimeStamp());
    }
    private String getLastSyncDateTime(){
        long lastSync = ECSyncHelper.getInstance(this).getLastCheckTimeStamp();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa, MMM d", Locale.getDefault());
        return MessageFormat.format(" {0}", sdf.format(lastSync));
    }
    public String getBuildDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(BuildConfig.BUILD_TIMESTAMP));
    }
    private void updateData(){
        hideProgressDialog();
        updateSpinner();
    }
    ArrayList<String> skNames = new ArrayList<>();
    boolean withoutSk = false;
    private void updateSpinner(){
        skNames.clear();
        ArrayList<SSModel> skLocationForms = SSLocationHelper.getInstance().getAllSks();
        String withoutsk = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(WITHOUT_SK);
        if(!TextUtils.isEmpty(withoutsk) && withoutsk.equalsIgnoreCase("PA")){
            withoutSk = true;
        }
        for (SSModel ssModel : skLocationForms) {
            if(withoutSk){
                if(!isExistInSkList(ssModel.locations.get(0).city_corporation_upazila.name)){
                    skNames.add(ssModel.locations.get(0).city_corporation_upazila.name);
                }
            }else{
                skNames.add(ssModel.skName);
            }

        }
    }
    private boolean isExistInSkList(String upozila){
        for(String skname : skNames){
            if(skname.equalsIgnoreCase(upozila)){
                return true;
            }
        }
        return false;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        unregisterReceiver(locationUpdateBroadcastReceiver);

    }




    private ProgressDialog dialog;
    private void showProgressDialog(String text){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onResumption() {

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.history_forum:
                startActivity(new Intent(this,PANewHistoryActivity.class));
                break;
            case R.id.vb_view:
                startAnyForm(HnppConstants.JSON_FORMS.PA_VB,REQUEST_HOME_VISIT);

                break;
            case R.id.ncd_view:
                startAnyForm(HnppConstants.JSON_FORMS.PA_NCD,REQUEST_HOME_VISIT);
                break;
            case R.id.eye_test_view:
                startAnyForm(HnppConstants.JSON_FORMS.PA_EYE_TEST,REQUEST_HOME_VISIT);
                break;
            case R.id.sync_text_txt:
            case R.id.refreshIndicatorsIcon:
            case R.id.unsync_count_txt:
                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
                break;
            case R.id.dashboard:
                startActivity(new Intent(this,NewDashBoardActivity.class));
                break;
            case R.id.logoutBtn:
                HnppApplication.getHNPPInstance().forceLogout();
                Toast.makeText(this, this.getResources().getText(org.smartregister.chw.core.R.string.action_log_out), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void startAnyForm(String formName,int requestCode){
        try {
            HnppConstants.appendLog("SAVE_VISIT","processJsonForm>>>skNames:"+skNames);

            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(formName);
            HnppJsonFormUtils.updateFormWithSKName(jsonForm, skNames,withoutSk);
            Intent intent = new Intent(this, HNPPJsonWizardFormActivity.class);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){

            AtomicInteger isSave = new AtomicInteger(2); /// 1-> Success / 2-> Regular error  3-> Already submitted visit error
            showProgressDialog(getString(R.string.please_wait_message));
            String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
            String formSubmissionId = JsonFormUtils.generateRandomUUIDString();
            String visitId = JsonFormUtils.generateRandomUUIDString();

            processVisitFormAndSave(formSubmissionId,jsonString,formSubmissionId,visitId)
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
                            updateUnSyncCount();
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


        }
    }
    Dialog serviceDialog;
    private void showServiceDoneDialog(Integer isSuccess){
        if(serviceDialog!=null) return;
        serviceDialog = new Dialog(this);
        serviceDialog.setCancelable(false);
        serviceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        serviceDialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = serviceDialog.findViewById(R.id.title_tv);
        titleTv.setText(isSuccess==1?"সার্ভিসটি দেওয়া সম্পূর্ণ হয়েছে":isSuccess==3?"সার্ভিসটি ইতিমধ্যে দেওয়া হয়েছে":"সার্ভিসটি দেওয়া সফল হয়নি। পুনরায় চেষ্টা করুন ");
        Button ok_btn = serviceDialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDialog.dismiss();
                serviceDialog = null;
            }
        });
        serviceDialog.show();

    }
    private void showDataSyncDialog(){
        Dialog serviceDialog = new Dialog(this);
        serviceDialog.setCancelable(false);
        serviceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        serviceDialog.setContentView(R.layout.dialog_with_one_button);
        TextView titleTv = serviceDialog.findViewById(R.id.title_tv);
        titleTv.setText("আপনার ডিভাইস এ আনসিঙ্ক ডাটা আছে। ডাটা সিঙ্ক করুন ");
        Button ok_btn = serviceDialog.findViewById(R.id.ok_btn);

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceDialog.dismiss();
                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
            }
        });
        serviceDialog.show();

    }
    private Observable<Integer> processVisitFormAndSave(String baseEntityId, String jsonString, String formSubmissionId, String visitId){

        return  Observable.create(e->{
                    try {
                        JSONObject form = new JSONObject(jsonString);
                        HnppJsonFormUtils.setEncounterDateTime(form);

                        Log.v("DATEEEE",""+form.getJSONObject("metadata").getJSONObject("today").getString("value"));

                        String  type = form.getString(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE);
                        type = HnppJsonFormUtils.getEncounterType(type);
                        Map<String, String> jsonStrings = new HashMap<>();
                        jsonStrings.put("First",form.toString());

                        Visit visit = HnppJsonFormUtils.savePAVisit(type, jsonStrings, baseEntityId,formSubmissionId,visitId);

                        if(visit!=null && !visit.getVisitId().equals("0")){
                            HnppHomeVisitIntentService.processVisits();
                            FormParser.processVisitLog(visit);
                            HnppConstants.appendLog("SAVE_VISIT","processVisitLog done formSubmissionId:"+formSubmissionId+":type:"+type);

                            // return true;
                            e.onNext(1);//success
                            e.onComplete();

                        }else if(visit!=null && visit.getVisitId().equals("0")){
                            e.onNext(3);//already exist
                            e.onComplete();
                        }else{
                            //return false;
                            e.onNext(2);//error
                            e.onComplete();
                        }
                    } catch (Exception ex) {
                        HnppConstants.appendLog("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        Log.d("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        e.onNext(2);//error
                        e.onComplete();
                    }
                    // return false;
                    // e.onNext(false);

                }
        );


    }
    @Override
    public void onSyncStart() {
        showProgressDialog(getString(R.string.syncing));
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        hideProgressDialog();
        updateUnSyncCount();
        if(isFromBackPress){
            finish();
        }
    }
    private class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getAction().equalsIgnoreCase(HnppConstants.ACTION_LOCATION_UPDATE)){
                updateData();

            }

        }
    }
    boolean isFromBackPress = false;
    @Override
    public void onBackPressed() {
        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        int ec = eventClientRepository.getUnSyncEventsCount();
        if(ec==0){
            super.onBackPressed();
        }else{
            isFromBackPress = true;
            showDataSyncDialog();
        }

    }
}
