package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity.REQUEST_HOME_VISIT;

public class SSInfoActivity extends SecuredActivity {
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AppExecutors appExecutors;
    private CardView addNewCard;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_ss_info);
        progressBar = findViewById(R.id.client_list_progress);
        recyclerView = findViewById(R.id.recycler_view);
        addNewCard = findViewById(R.id.add_new);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSSForm();
            }
        });
        appExecutors = new AppExecutors();
        loadPreviousData();

    }

    @Override
    protected void onResumption() {

    }

    private void startSSForm() {
        Intent intent = new Intent(this, HNPPJsonWizardFormActivity.class);
        try{
            JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.SS_FORM);
            HnppJsonFormUtils.updateFormWithSSName(jsonForm, SSLocationHelper.getInstance().getSsModels());
            HnppJsonFormUtils.addYear(jsonForm);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
            Form form = new Form();
            form.setName(getString(R.string.menu_ss_info));
            form.setWizard(true);
            form.setActionBarBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);
            form.setNavigationBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);
            form.setHideSaveLabel(true);
            form.setNextLabel(getString(R.string.next));
            form.setPreviousLabel(getString(R.string.previous));
            form.setSaveLabel(getString(R.string.save));
            form.setBackIcon(R.mipmap.ic_cross_white);
            //
//            Form form = new Form();
//            form.setName(getString(R.string.menu_ss_info));
//            form.setHideSaveLabel(true);
//            form.setSaveLabel(getString(R.string.save));
//            form.setNextLabel(getString(R.string.next));
//            form.setPreviousLabel(getString(R.string.previous));
//            form.setWizard(true);
////            if(!HnppConstants.isReleaseBuild()){
////                form.setActionBarBackground(R.color.test_app_color);
////
////            }else{
////                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);
////
////            }
//            form.setNavigationBackground(!HnppConstants.isReleaseBuild()?R.color.test_app_color:org.smartregister.family.R.color.customAppThemeBlue);
            form.setHomeAsUpIndicator(org.smartregister.family.R.mipmap.ic_cross_white);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            this.startActivityForResult(intent, REQUEST_HOME_VISIT);

        }catch (Exception e){

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_HOME_VISIT){
            progressBar.setVisibility(View.VISIBLE);
            Runnable runnable = () ->{
                try{
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);

                   HnppJsonFormUtils.processAndSaveSSForm(jsonString);
                }catch (Exception e){

                }
             appExecutors.mainThread().execute(() ->{
                 progressBar.setVisibility(View.GONE);
                 VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
                 new Handler().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         loadPreviousData();
                     }
                 },1000);

             });
            };
            appExecutors.diskIO().execute(runnable);


        }
    }

    private void loadPreviousData(){
        progressBar.setVisibility(View.VISIBLE);
        Runnable runnable = () -> {
            ArrayList<VisitLog> visitLogs = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getAllSSFormVisit();
            ArrayList<MemberHistoryData> historyDataArrayList  = new ArrayList<>();

            for(VisitLog visitLog : visitLogs){

                try {
                    MemberHistoryData historyData = new MemberHistoryData();
                    String eventType = visitLog.getEventType();
                    historyData.setEventType(eventType);
                    historyData.setVisitDetails(visitLog.getVisitJson());

                    JSONObject jsonForm = new JSONObject(historyData.getVisitDetails());
                    String ssName= HnppJsonFormUtils.getSSNameFromForm(jsonForm);
                    String title = getString(R.string.menu_ss_info)+"\nস্বাস্থ্য সেবিকার নামঃ"+ssName;
                    historyData.setTitle(title);
                    historyData.setImageSource(R.drawable.childrow_history);



                    historyData.setVisitDate(visitLog.getVisitDate());
                    historyDataArrayList.add(historyData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            appExecutors.mainThread().execute(() -> {
                updateAdapter(historyDataArrayList);
//                String value = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(HnppConstants.KEY_IS_SAME_MONTH);
//                if(TextUtils.isEmpty(value)){
//                    addNewCard.setVisibility(View.VISIBLE);
//                }else{
//                    addNewCard.setVisibility(View.GONE);
//                }
            });
        };
        appExecutors.diskIO().execute(runnable);


    }

    private void updateAdapter(ArrayList<MemberHistoryData> visitLogArrayList) {
        progressBar.setVisibility(View.GONE);
        MemberHistoryAdapter adapter = new MemberHistoryAdapter(this, new MemberHistoryAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, MemberHistoryData content) {
                openDetails(content);
            }
        });
        adapter.setData(visitLogArrayList);
        recyclerView.setAdapter(adapter);

    }

    private void openDetails(MemberHistoryData content) {
        try {
            JSONObject jsonForm = new JSONObject(content.getVisitDetails());
            HnppJsonFormUtils.makeReadOnlyFields(jsonForm);
            String ssId= HnppJsonFormUtils.getSSIdFromForm(jsonForm);
             HnppJsonFormUtils.getYearMonthFromForm(jsonForm);

            HnppJsonFormUtils.updateFormWithSSName(jsonForm,SSLocationHelper.getInstance().getSsModels(),ssId);

            Intent intent = new Intent(this, HnppFormViewActivity.class);
            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            form.setActionBarBackground( HnppConstants.isReleaseBuild()?R.color.customAppThemeBlue:R.color.alert_urgent_red);
            form.setHideSaveLabel(true);
            form.setSaveLabel("");
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
            if (this != null) {
                this.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
