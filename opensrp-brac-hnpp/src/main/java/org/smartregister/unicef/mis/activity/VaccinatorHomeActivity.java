package org.smartregister.unicef.mis.activity;


import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.LAST_SYNC_HPV;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.CoreLibrary;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VaccinatorHomeActivity extends SecuredActivity implements View.OnClickListener {

    private ImageView logoutBtn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_vaccinator_services);
        findViewById(R.id.qrscan_view).setOnClickListener(this);
        findViewById(R.id.hpv_search_view).setOnClickListener(this);
        findViewById(R.id.sync_text_txt).setOnClickListener(this);
        findViewById(R.id.refreshIndicatorsIcon).setOnClickListener(this);
        findViewById(R.id.unsync_count_txt).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.dashboard).setOnClickListener(this);
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        updateUnSyncCount();
        ((TextView) findViewById(R.id.login_build_text_view)).setText("Version " + getVersion() + ", Built on: " + getBuildDate());
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String firstName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);
        //  ((TextView) findViewById(R.id.user_name_txt)).setText(firstName+"\n"+userName);
        ((TextView) findViewById(R.id.username_text_view)).setText(firstName+"\n"+userName);
    }
    int ec;
    @SuppressLint("SetTextI18n")
    private void updateUnSyncCount(){
        ec = HnppApplication.getOtherVaccineRepository().getUnSyncCount();
        ((TextView)findViewById(R.id.unsync_count_txt)).setText(ec+"");
        ((TextView) findViewById(R.id.last_sync_text_view)).setText(getString(R.string.last_sync,getLastSyncDateTime()));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ec>0){
            try{
                postHPVData();
            }catch (Exception e){

            }
        }


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

    private String getLastSyncDateTime(){
        long lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_HPV)==null?System.currentTimeMillis():Long.parseLong(CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_HPV));
        return HnppConstants.DDMMYY.format(lastSynTime);
    }
    public String getBuildDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(BuildConfig.BUILD_TIMESTAMP));
    }
    private void updateData(){
        hideProgressDialog();

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();

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

            case R.id.qrscan_view:
                startActivity(new Intent(this, QRScannerActivity.class));

                break;
            case R.id.hpv_search_view:
                startActivity(new Intent(this, SearchActivity.class));
                break;
            case R.id.sync_text_txt:
            case R.id.refreshIndicatorsIcon:
            case R.id.unsync_count_txt:
                if(ec==0){
                    Toast.makeText(this,"No data found",Toast.LENGTH_LONG).show();
                }
                postHPVData();
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
                try{
                    postHPVData();
                }catch (Exception e){

                }
            }
        });
        serviceDialog.show();

    }
    private void postHPVData(){
        onSyncStart();
        HnppConstants.postOtherVaccineData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(String s) {
                        Log.v("OTHER_VACCINE","onNext>>s:"+s);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("OTHER_VACCINE",""+e);
                        Toast.makeText(VaccinatorHomeActivity.this,"Sync Failed",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.v("OTHER_VACCINE","completed");
                        onSyncComplete();
                    }
                });
    }
    public void onSyncStart() {
        showProgressDialog(getString(R.string.syncing));
    }

    public void onSyncComplete() {
        Toast.makeText(VaccinatorHomeActivity.this,"Sync Successfully",Toast.LENGTH_LONG).show();
        hideProgressDialog();
        updateUnSyncCount();
        if(isFromBackPress){
            finish();
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
