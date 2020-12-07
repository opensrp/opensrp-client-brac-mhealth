package org.smartregister.brac.hnpp.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import timber.log.Timber;

public class ForceSyncActivity extends SecuredActivity implements SyncStatusBroadcastReceiver.SyncStatusListener{
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_force_unsync);
        findViewById(R.id.close_btn).setOnClickListener(v -> finish());
        findViewById(R.id.force_sync_btn).setOnClickListener( v -> getServerResponse() );
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
                            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
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
}
