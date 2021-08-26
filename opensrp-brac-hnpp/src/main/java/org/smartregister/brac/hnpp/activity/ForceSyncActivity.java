package org.smartregister.brac.hnpp.activity;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.ForceSynItemAdapter;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.model.ForceSyncModel;
import org.smartregister.brac.hnpp.repository.HnppChwRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import timber.log.Timber;

public class ForceSyncActivity extends SecuredActivity implements SyncStatusBroadcastReceiver.SyncStatusListener{

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_force_unsync);
        findViewById(R.id.close_btn).setOnClickListener(v -> finish());
        findViewById(R.id.permission_btn).setOnClickListener( v -> getServerResponse() );
        findViewById(R.id.all_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(forseSyncAllData()){
                    SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);

                    showProgressDialog("আপনার ডাটাগুলো সার্ভার এর সাথে সিঙ্ক করা হচ্ছে ");
                    HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                }
            }
        });
        findViewById(R.id.force_sync_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(unSyncSpecificService()){
                    SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);

                    showProgressDialog("আপনার ডাটাগুলো সার্ভার এর সাথে সিঙ্ক করা হচ্ছে ");
                    HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                }else{

                }
            }
        });
        setServiceName();
        findViewById(R.id.dump_btn).setOnClickListener( v -> dumpDatabase() );

    }
    private void setServiceName(){
        AppExecutors appExecutors = new AppExecutors();
        Runnable runnable = () -> {
            ArrayList<ForceSyncModel> forceSyncModelArrayList = getAllService();

            appExecutors.mainThread().execute(() -> updateAdapter(forceSyncModelArrayList));
        };
        appExecutors.diskIO().execute(runnable);

    }
    ForceSynItemAdapter adapter;
    private void updateAdapter(ArrayList<ForceSyncModel> forceSyncModelArrayList) {
        adapter = new ForceSynItemAdapter(this, new ForceSynItemAdapter.OnClickAdapter() {
            @Override
            public void onClickItem(int position) {

            }
        });
        adapter.setData(forceSyncModelArrayList);
        ((RecyclerView)findViewById(R.id.recycler_view)).setAdapter(adapter);

    }

    ArrayList<ForceSyncModel> getAllService(){
        Cursor cursor = null;
        ArrayList<ForceSyncModel> forceSyncModelArrayList = new ArrayList<>();
        ForceSyncModel forceSyncModel = new ForceSyncModel();
        forceSyncModel.eventType = HnppConstants.EVENT_TYPE.IYCF_PACKAGE;
        forceSyncModel.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel.eventType);
        forceSyncModelArrayList.add(forceSyncModel);
        ForceSyncModel forceSyncModel1 = new ForceSyncModel();
        forceSyncModel1.eventType = HnppConstants.EVENT_TYPE.WOMEN_PACKAGE;
        forceSyncModel1.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel1.eventType);
        forceSyncModelArrayList.add(forceSyncModel1);

        ForceSyncModel forceSyncModel2 = new ForceSyncModel();
        forceSyncModel2.eventType = HnppConstants.EVENT_TYPE.NCD_PACKAGE;
        forceSyncModel2.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel2.eventType);
        forceSyncModelArrayList.add(forceSyncModel2);

        ForceSyncModel forceSyncModel3 = new ForceSyncModel();
        forceSyncModel3.eventType = HnppConstants.EVENT_TYPE.GIRL_PACKAGE;
        forceSyncModel3.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel3.eventType);
        forceSyncModelArrayList.add(forceSyncModel3);

        ForceSyncModel forceSyncModel4 = new ForceSyncModel();
        forceSyncModel4.eventType = HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
        forceSyncModel4.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel4.eventType);
        forceSyncModelArrayList.add(forceSyncModel4);

        ForceSyncModel forceSyncModel5 = new ForceSyncModel();
        forceSyncModel5.eventType = CoreConstants.EventType.ANC_HOME_VISIT;
        forceSyncModel5.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel5.eventType);
        forceSyncModelArrayList.add(forceSyncModel5);

//        String query = "select count(eventType) as count, eventType group by eventType";
//        // try {
//        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//        if(cursor !=null && cursor.getCount() > 0){
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//                ForceSyncModel forceSyncModel = new ForceSyncModel();
//                forceSyncModel.count = (cursor.getInt(0));
//                forceSyncModel.eventType = cursor.getString(1);
//                forceSyncModel.title = HnppConstants.workSummeryTypeMapping.get(forceSyncModel.eventType);
//                forceSyncModelArrayList.add(forceSyncModel);
//                cursor.moveToNext();
//            }
//            cursor.close();
//
//        }
        return forceSyncModelArrayList;
    }

    private void dumpDatabase(){
        AppExecutors appExecutors = new AppExecutors();
        ((Button)findViewById(R.id.dump_btn)).setText("ডাটাবেস ডাম্প নেওয়া হচ্ছে ");
        Runnable runnable = () -> {
            try{

                String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
                String password = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityId(userName);
                Log.v("DUMP_DB","password:"+password+":userName:"+userName);
                File Db = new File("/data/data/"+getPackageName()+"/databases/drishti.db");
                String filePath = getExternalFilesDir(null) + "/db";
                File file = new File(filePath);
                if(!file.exists()){
                    file.mkdir();
                }
                filePath = (file.getAbsolutePath() + "/"+ "drishti.db");
                File finalFile = new File(filePath);

                finalFile.setWritable(true);

                copyFile(new FileInputStream(Db), new FileOutputStream(finalFile));

            }catch (Exception e){
                e.printStackTrace();

            }

            appExecutors.mainThread().execute(() ->  ((Button)findViewById(R.id.dump_btn)).setText("ডাম্প নেওয়া শেষ হয়েছে"));
        };
        appExecutors.diskIO().execute(runnable);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{
//                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10000);
//                return;
//            }
//        }



    }
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
            Log.v("DUMP_DB","done");
        }
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
                        findViewById(R.id.service_select_panel).setVisibility(View.VISIBLE);
                        findViewById(R.id.permission_check_panel).setVisibility(View.GONE);
                        setServiceName();

                    }else{
                        Toast.makeText(ForceSyncActivity.this,"আপনার অনুমতি নেই",Toast.LENGTH_LONG).show();

                    }

                }else{
                    Toast.makeText(ForceSyncActivity.this,"আপনার অনুমতি নেই",Toast.LENGTH_LONG).show();
                }

            }
        }, null);
    }
    private boolean unSyncSpecificService(){
        if(adapter!=null){
            String condition = adapter.getSelectedServiceQuery();
            if(!TextUtils.isEmpty(condition)){
                try{
                    SQLiteDatabase db = CoreChwApplication.getInstance().getRepository().getReadableDatabase();
                    String query = "UPDATE event set syncStatus='Unsynced' where "+condition;
                    Log.v("UNSYNC_DATA","query>>"+query);
                    db.execSQL(query);
                    return true;

                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    private boolean forseSyncAllData() {
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
