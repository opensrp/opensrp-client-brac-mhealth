package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.SkSelectionAdapter;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class SkSelectionActivity extends SecuredActivity implements View.OnClickListener,SyncStatusBroadcastReceiver.SyncStatusListener {
    public static final String IS_COMES_FROM_UPDATE = "is_comes_from_update";
    private  String storeUserName;

    private RecyclerView recyclerView;
    private Spinner skSpinner,ssSpinner;
    private SkSelectionAdapter adapter;
    private boolean isComesFromUpdateSync = false;
    private BroadcastReceiver locationUpdateBroadcastReceiver;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_sk_select);
        locationUpdateBroadcastReceiver = new LocationBroadcastReceiver();
        isComesFromUpdateSync = getIntent().getBooleanExtra(IS_COMES_FROM_UPDATE,false);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        skSpinner = findViewById(R.id.sk_filter_spinner);
        ssSpinner = findViewById(R.id.ss_filter_spinner);
        findViewById(R.id.add_button).setOnClickListener(this);
        findViewById(R.id.update_button).setOnClickListener(this);
        findViewById(R.id.refreshIndicatorsIcon).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        if(isComesFromUpdateSync){
            showClearDataDialog();

        }else {
            showProgressDialog("আপডেটেড লোকেশন নেওয়া হচ্ছে");
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//
//                }
//            },5000);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HnppConstants.ACTION_LOCATION_UPDATE);
        registerReceiver(locationUpdateBroadcastReceiver, intentFilter);
    }

    private void updateData(){
        hideProgressDialog();
        updateSpinner();
    }
    ArrayAdapter<String> ssSpinnerArrayAdapter;
    ArrayList<SSModel> selectedSSList  = new ArrayList<>();
    ArrayList<SSModel> ssListModel  = new ArrayList<>();
    SSModel selectedSS;
    int selectedPosition;
    private void updateSpinner(){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();


        ArrayList<String> skSpinnerArray = new ArrayList<>();


        ArrayList<SSModel> skLocationForms = SSLocationHelper.getInstance().getAllSks();
        for (SSModel ssModel : skLocationForms) {
            skSpinnerArray.add(ssModel.skName+"("+ssModel.skUserName+")");
        }
        ArrayAdapter<String> sKSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        skSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };

        ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public android.view.View getDropDownView(int position, @Nullable android.view.View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };
        skSpinner.setAdapter(sKSpinnerArrayAdapter);
        skSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position != -1) {
                    SSModel ssModel = skLocationForms.get(position);
                    ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getAllSS(ssModel.skUserName);
                    ssSpinnerArray.clear();
                    ssListModel.clear();
                    for (SSModel ssModel1 : ssLocationForms) {
                        ssSpinnerArray.add(ssModel1.username);
                        ssListModel.add(ssModel1);
                    }
                    ssSpinnerArrayAdapter = new ArrayAdapter<String>
                            (SkSelectionActivity.this, android.R.layout.simple_spinner_item,
                                    ssSpinnerArray);
                    ssSpinner.setAdapter(ssSpinnerArrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ssSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(position!=-1){
                    selectedSS = ssListModel.get(position);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
        }
        if(removeDialog !=null){
            removeDialog.dismiss();
        }
    }
    private boolean isExistInList(SSModel ssModel){
        for(SSModel model : selectedSSList){
            if(ssModel.ss_id.equalsIgnoreCase(model.ss_id)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_button:

                if(selectedSSList.size()>=5){
                    Toast.makeText(SkSelectionActivity.this,"সর্বোচ্চ ৫ জন স্বাস্থসেবিকা অ্যাড করতে পারবেন",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectedSS!=null && !isExistInList(selectedSS)){
                    selectedSSList.add(selectedSS);
                }else{
                    Toast.makeText(SkSelectionActivity.this,"অলরেডি অ্যাড করা হয়েছে",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(adapter ==null){
                    adapter = new SkSelectionAdapter(this, new SkSelectionAdapter.OnClickAdapter() {
                        @Override
                        public void onRemove(int position, SSModel content) {
                            selectedSSList.remove(content);
                        }
                    });
                    adapter.setData(selectedSSList);
                    recyclerView.setAdapter(adapter);
                }else{
                    adapter.setData(selectedSSList);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.update_button:
                if(selectedSSList.size()==0){
                    Toast.makeText(SkSelectionActivity.this,"সর্বনিন্ম ১ জন স্বাস্থ্যসেবিকাকে অ্যাড করা লাগবে",Toast.LENGTH_SHORT).show();
                    return;
                }
               boolean isUpdated =  SSLocationHelper.getInstance().isUpdated(selectedSSList);
               if(isUpdated){
                   HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                   Toast.makeText(this,R.string.syncing,Toast.LENGTH_SHORT).show();
                   finish();
               }else{
                   Toast.makeText(this,"Fail to update",Toast.LENGTH_SHORT).show();
               }
                break;
            case R.id.refreshIndicatorsIcon:
                updateSpinner();
                break;
            case R.id.backBtn:
                finish();
                HnppApplication.getHNPPInstance().forceLogout();
                break;
        }
    }
    Dialog removeDialog;
    private void showClearDataDialog(){
        removeDialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        removeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
        removeDialog.setContentView(R.layout.dialog_remove_data);
        removeDialog.findViewById(R.id.remove_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeUserName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(SkSelectionActivity.this);
//                HnppHomeVisitServiceJob.scheduleJobImmediately(HnppHomeVisitServiceJob.TAG);
                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                showProgressDialog(getString(R.string.syncing));

            }
        });
        removeDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    removeDialog.dismiss();
                }
                return true;
            }
        });
        removeDialog.show();
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
        HnppApplication.getHNPPInstance().clearSharePreference(storeUserName);
        HnppApplication.getHNPPInstance().clearDatabase();
        HnppApplication.getHNPPInstance().appSwitch();
    }
    private class LocationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getAction().equalsIgnoreCase(HnppConstants.ACTION_LOCATION_UPDATE)){
                updateData();

            }

        }
    }

}
