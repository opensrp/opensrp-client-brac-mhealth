package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.DataSyncByBaseEntityServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.view.activity.SecuredActivity;

import java.util.List;

import timber.log.Timber;

public class ForceSyncActivity extends SecuredActivity implements SyncStatusBroadcastReceiver.SyncStatusListener{
    private BroadcastReceiver invalidDataBroadcastReceiver;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_force_unsync);
        findViewById(R.id.invalid_data).setOnClickListener(v -> checkInvalidData());
        findViewById(R.id.force_sync_btn).setOnClickListener( v -> getServerResponse() );
        findViewById(R.id.data_sync_by_id).setOnClickListener( v -> syncDataById() );
    }

    private void syncDataById() {
        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DATA_SYNC");
        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
        DataSyncByBaseEntityServiceJob.scheduleJobImmediately(DataSyncByBaseEntityServiceJob.TAG);
    }

    private void checkInvalidData() {
        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        List<JSONObject> invalidClients = eventClientRepository.getUnValidatedClients(100);
        List<JSONObject> invalidEvents = eventClientRepository.getUnValidatedEvents(100);
        showInvalidCountDialog(invalidClients,invalidEvents);


    }
    private void showInvalidCountDialog(List<JSONObject> invalidClients, List<JSONObject> invalidEvents ){
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_invalid_data);
        TextView countTxt = dialog.findViewById(R.id.count_tv);
        StringBuilder builder = new StringBuilder();
        builder.append("No Of Invalid Client: "+invalidClients.size());
        builder.append("\n");
        builder.append("No Of Invalid Events: "+invalidEvents.size());
        countTxt.setText(builder.toString());


        Button syncBtn = dialog.findViewById(R.id.invalid_sync_btn);
        Button closeBtn = dialog.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(invalidClients.size()==0 && invalidEvents.size()==0){
                    Toast.makeText(ForceSyncActivity.this,"কোনো ইনভ্যালিড ডাটা পাওয়া যায়নি",Toast.LENGTH_SHORT).show();
                    return;
                }
                invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("INVALID_SYNC");
                registerReceiver(invalidDataBroadcastReceiver, intentFilter);
                showProgressDialog("ইনভ্যালিড ডাটা সিঙ্ক করা হচ্ছে....");
                dialog.dismiss();
                InValidateSyncDataServiceJob.scheduleJobImmediately(InValidateSyncDataServiceJob.TAG);
            }
        });
        dialog.show();

    }

    private void getServerResponse(){
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog("আপনার অনুমতি আছে কিনা চেক করা হচ্ছে ...");
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                String baseUrl = CoreLibrary.getInstance().context().
                        configuration().dristhiBaseURL();
                String endString = "/";
                if (baseUrl.endsWith(endString)) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
                }
                try {
                    String url = baseUrl + "/is_resync?username=" + CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
                    Log.v("FORCE_SYNC_URL","url:"+url);
                    Response resp = CoreLibrary.getInstance().context().getHttpAgent().fetch(url);
                    if (resp.isFailure()) {
                        throw new NoHttpResponseException(" not returned data");
                    }
                    return resp.payload().toString();
                } catch (NoHttpResponseException e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                hideProgressDialog();
                if(o !=null ){
                    String status = (String)o;
                    if(!TextUtils.isEmpty(status) && status.equalsIgnoreCase("yes")){
                        if(forseUnsyncData()){
                            SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);

                            showProgressDialog("আপনার ডাটাগুলো সার্ভার এর সাথে সিঙ্ক করা হচ্ছে ");
                            SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                        }
                    }else{
                        Toast.makeText(ForceSyncActivity.this,"আপনার অনুমতি নেই",Toast.LENGTH_LONG).show();

                    }

                }else{
                    Toast.makeText(ForceSyncActivity.this,"আপনার অনুমতি নেই",Toast.LENGTH_LONG).show();
                }

            }
        }, null);
    }

    private boolean forseUnsyncData() {
        try{
            SQLiteDatabase db = CoreChwApplication.getInstance().getRepository().getReadableDatabase();
            db.execSQL("UPDATE client set syncStatus='Unsynced' where syncStatus='Synced'");
            db.execSQL("UPDATE event set syncStatus='Unsynced',serverVersion= 0");
            return true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private ProgressDialog dialog;
    private void showProgressDialog(String message){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage(message);
            dialog.setCancelable(false);
            dialog.show();
        }

    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
    @Override
    public void onDestroy() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
        if(invalidDataBroadcastReceiver!=null)unregisterReceiver(invalidDataBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onSyncStart() {

    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {

        hideProgressDialog();
        Toast.makeText(this,getString(R.string.sync_complete),Toast.LENGTH_SHORT).show();
        finish();
    }
    private class InvalidSyncBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressDialog();
            if(intent != null && intent.getAction().equalsIgnoreCase("INVALID_SYNC")){
                String value = intent.getStringExtra("EXTRA_INVALID_SYNC");
                Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
            }
            if(intent != null && intent.getAction().equalsIgnoreCase("DATA_SYNC")){
                String value = intent.getStringExtra("EXTRA_DATA_SYNC");
                Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
            }

        }
    }
}
