package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.AddMicroPlanAdapter;
import org.smartregister.unicef.mis.model.ForceSyncModel;
import org.smartregister.unicef.mis.sync.FormParser;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddMicroplanActivity extends SecuredActivity implements View.OnClickListener{

    private Spinner statusSpinner,yearSpinner;
    private RecyclerView recyclerView;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_add_microplan);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.filter_btn).setOnClickListener(this);
        statusSpinner = findViewById(R.id.status_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        recyclerView = findViewById(R.id.recycler_view);
        generateYear();

        loadMicroPlanData();
    }

    private void generateYear() {
        ArrayList<String> yearList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int startyear = currentYear - 2;
        int endyear = currentYear + 2;
        for(int i = endyear;i>=startyear;i--){
            yearList.add(i+"");
        }

        ArrayAdapter<String> oldWardSpinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                yearList);
        yearSpinner.setAdapter(oldWardSpinnerArrayAdapter);
        yearSpinner.setSelection(((ArrayAdapter<String>)yearSpinner.getAdapter()).getPosition(currentYear+""));

    }

    private void loadMicroPlanData(){
        showProgressDialog("Loading.....");
        getMicroPlanFromDB(statusSpinner.getSelectedItem().toString(),Integer.parseInt(yearSpinner.getSelectedItem().toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<MicroPlanEpiData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<MicroPlanEpiData> microPlanEpiDatas) {
                        hideProgressDialog();
                        updateAdapter(microPlanEpiDatas);
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
    private Observable<ArrayList<MicroPlanEpiData>> getMicroPlanFromDB(String status, int year){
        return Observable.create(e-> {
            try{
                ArrayList<MicroPlanEpiData> microPlanEpiData = HnppApplication.getMicroPlanRepository().getAllMicroPlanEpiData(status,year);
                if(microPlanEpiData!=null){
                    e.onNext(microPlanEpiData);
                    e.onComplete();
                }else{
                    e.onNext(new ArrayList<>());
                }

            }catch (Exception ex){
                e.onNext(new ArrayList<>());
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
    private void updateAdapter(ArrayList<MicroPlanEpiData> microPlanEpiData){
        AddMicroPlanAdapter adapter = new AddMicroPlanAdapter(this, new AddMicroPlanAdapter.OnClickAdapter() {
            @Override
            public void onViewClick(int position, MicroPlanEpiData content) {
                content.isViewMode = true;
                AddCenterDetailsActivity.startAddCenterDetailsActivity(AddMicroplanActivity.this,content);
            }

            @Override
            public void onEditClick(int position, MicroPlanEpiData content) {
                content.isViewMode = false;
                AddCenterDetailsActivity.startAddCenterDetailsActivity(AddMicroplanActivity.this,content);
            }

            @Override
            public void onAddClick(int position, MicroPlanEpiData content) {
                content.isViewMode = false;
                content.year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
                AddCenterDetailsActivity.startAddCenterDetailsActivity(AddMicroplanActivity.this,content);


            }
        });
        adapter.setData(microPlanEpiData);
        recyclerView.setAdapter(adapter);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.filter_btn:
                loadMicroPlanData();
                break;
        }
    }
}