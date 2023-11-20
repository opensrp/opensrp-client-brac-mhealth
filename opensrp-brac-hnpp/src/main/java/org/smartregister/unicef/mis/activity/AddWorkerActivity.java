package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.DistributionData;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.SuperVisorData;
import org.smartregister.unicef.mis.utils.WorkerData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddWorkerActivity extends SecuredActivity implements View.OnClickListener {
    private static final String PUT_EXTRA_MICRO_PLAN = "micro_plan_extra";
    MicroPlanEpiData microPlanEpiData;

    public static void startAddWorkerActivity(Activity activity, MicroPlanEpiData microPlanEpiData){
        Intent intent = new Intent(activity, AddWorkerActivity.class);
        intent.putExtra(PUT_EXTRA_MICRO_PLAN,microPlanEpiData);
        activity.startActivity(intent);
    }
    EditText worker1NameTxt,worker1MobileTxt,worker2NameTxt,worker2MobileTxt;
    EditText supervisor1NameTxt,supervisor1MobileTxt,supervisor2NameTxt,supervisor2MobileTxt;
    Spinner worker1DesignationSpinner,worker2DesignationSpinner,supervisor1DesignationSpinner,supervisor2DesignationSpinner;
    @Override
    protected void onCreation() {
        setContentView(R.layout.add_worker_info);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        findViewById(R.id.fab_add_worker_2).setOnClickListener(this);
        findViewById(R.id.fab_add_supervisor_2).setOnClickListener(this);
        initUi();
        microPlanEpiData = (MicroPlanEpiData) getIntent().getSerializableExtra(PUT_EXTRA_MICRO_PLAN);
        if(microPlanEpiData!=null){
            if(microPlanEpiData.workerData!=null){
                populatedUI();
            }
            if(microPlanEpiData.isViewMode){
                findViewById(R.id.submit_btn).setEnabled(false);
                findViewById(R.id.submit_btn).setAlpha(.3f);
            }
        }
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
        worker1NameTxt.setText(fullName);

    }
    private void populatedUI(){
        worker1NameTxt.setText(microPlanEpiData.workerData.worker1Name);
        worker1MobileTxt.setText(microPlanEpiData.workerData.worker1MobileNo);
        worker2NameTxt.setText(microPlanEpiData.workerData.worker2Name);
        worker2MobileTxt.setText(microPlanEpiData.workerData.worker2MobileNo);
        supervisor1NameTxt.setText(microPlanEpiData.superVisorData.superVisor1Name);
        supervisor1MobileTxt.setText(microPlanEpiData.superVisorData.superVisor1MobileNo);
        supervisor2NameTxt.setText(microPlanEpiData.superVisorData.superVisor2Name);
        supervisor2MobileTxt.setText(microPlanEpiData.superVisorData.superVisor2MobileNo);
        worker1DesignationSpinner.setSelection(((ArrayAdapter<String>)worker1DesignationSpinner.getAdapter()).getPosition(microPlanEpiData.workerData.worker1Designation));
        worker2DesignationSpinner.setSelection(((ArrayAdapter<String>)worker2DesignationSpinner.getAdapter()).getPosition(microPlanEpiData.workerData.worker2Designation));
        supervisor1DesignationSpinner.setSelection(((ArrayAdapter<String>)supervisor1DesignationSpinner.getAdapter()).getPosition(microPlanEpiData.superVisorData.superVisor1Designation));
        supervisor2DesignationSpinner.setSelection(((ArrayAdapter<String>)supervisor2DesignationSpinner.getAdapter()).getPosition(microPlanEpiData.superVisorData.superVisor2Designation));

    }
    private void initUi() {
        worker1NameTxt = findViewById(R.id.worker_1_name);
        worker1MobileTxt = findViewById(R.id.worker_1_mobile);
        worker2NameTxt = findViewById(R.id.worker_2_name);
        worker2MobileTxt = findViewById(R.id.worker_2_mobile);
        supervisor1NameTxt = findViewById(R.id.supervisor_1_name);
        supervisor1MobileTxt = findViewById(R.id.supervisor_1_mobile);
        supervisor2NameTxt = findViewById(R.id.supervisor_2_name);
        supervisor2MobileTxt = findViewById(R.id.supervisor_2_mobile);
        worker1DesignationSpinner = findViewById(R.id.worker_1_deg_spinner);
        worker2DesignationSpinner = findViewById(R.id.worker_2_deg_spinner);
        supervisor1DesignationSpinner = findViewById(R.id.supervisor_1_deg_spinner);
        supervisor2DesignationSpinner = findViewById(R.id.supervisor_2_deg_spinner);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.fab_add_worker_2:
                findViewById(R.id.worker_2_info).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_worker_2).setVisibility(View.VISIBLE);
                break;
            case R.id.fab_add_supervisor_2:
                findViewById(R.id.supervisor_2_info).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_supervisor_2).setVisibility(View.VISIBLE);
                break;
            case R.id.previous_btn:
                finish();
                break;
            case R.id.submit_btn:
                WorkerData workerData = new WorkerData();
                workerData.worker1Name = worker1NameTxt.getText().toString();
                workerData.worker1MobileNo = worker1MobileTxt.getText().toString();
                workerData.worker1Designation = worker1DesignationSpinner.getSelectedItem().toString();
                if(findViewById(R.id.worker_2_info).getVisibility() == View.VISIBLE){
                    workerData.worker2Name = worker2NameTxt.getText().toString();
                    workerData.worker2MobileNo = worker2MobileTxt.getText().toString();
                    workerData.worker2Designation = worker2DesignationSpinner.getSelectedItem().toString();
                }
                microPlanEpiData.workerData = workerData;
                SuperVisorData superVisorData = new SuperVisorData();
                superVisorData.superVisor1Name = supervisor1NameTxt.getText().toString();
                superVisorData.superVisor1MobileNo = supervisor1MobileTxt.getText().toString();
                superVisorData.superVisor1Designation = supervisor1DesignationSpinner.getSelectedItem().toString();
                if(findViewById(R.id.supervisor_2_info).getVisibility() == View.VISIBLE){
                    superVisorData.superVisor2Name = supervisor2NameTxt.getText().toString();
                    superVisorData.superVisor2MobileNo = supervisor2MobileTxt.getText().toString();
                    superVisorData.superVisor2Designation = supervisor2DesignationSpinner.getSelectedItem().toString();
                }
                microPlanEpiData.superVisorData = superVisorData;
                saveMicroPlanData();

                break;
        }


    }
    private void saveMicroPlanData(){
        showProgressDialog("saving.....");
        saveMicroPlanDataAtDB().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean isInserted) {
                        hideProgressDialog();
                        if(isInserted){
                            Toast.makeText(AddWorkerActivity.this,"Save Microplan successfully",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(AddWorkerActivity.this,AddMicroplanActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();

                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();

                    }
                });
    }
    private Observable<Boolean> saveMicroPlanDataAtDB(){
        return Observable.create(e-> {
            try{
                boolean isInserted = HnppApplication.getMicroPlanRepository().addAndUpdateMicroPlan(microPlanEpiData);
                e.onNext(isInserted);
                e.onComplete();


            }catch (Exception ex){
                e.onNext(false);
            }
        });
    }
    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }


    @Override
    protected void onResumption() {

    }
}
