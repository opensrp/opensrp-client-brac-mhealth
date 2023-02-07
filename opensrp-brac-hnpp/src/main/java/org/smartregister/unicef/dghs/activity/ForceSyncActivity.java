package org.smartregister.unicef.dghs.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.ForceSynItemAdapter;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.model.ForceSyncModel;
import org.smartregister.unicef.dghs.repository.HnppChwRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.job.CompareDataServiceJob;
import org.smartregister.job.DataSyncByBaseEntityServiceJob;
import org.smartregister.job.ForceSyncDataServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.intent.ForceSyncIntentService;
import org.smartregister.view.activity.SecuredActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ForceSyncActivity extends SecuredActivity implements SyncStatusBroadcastReceiver.SyncStatusListener{
    private BroadcastReceiver invalidDataBroadcastReceiver;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_force_unsync);
        findViewById(R.id.invalid_data).setOnClickListener(v -> checkInvalidData());
        findViewById(R.id.sync_unsync_btn).setOnClickListener( v -> forceSyncData() );
        findViewById(R.id.data_sync_by_id).setOnClickListener( v -> syncDataById() );
        findViewById(R.id.compare_btn).setOnClickListener( v -> compareData() );
        findViewById(R.id.close_btn).setOnClickListener(v -> finish());
        findViewById(R.id.permission_btn).setOnClickListener( v -> getServerResponse() );
//        findViewById(R.id.all_data_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(forseSyncAllData()){
//                    SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);
//
//                    showProgressDialog("আপনার ডাটাগুলো সার্ভার এর সাথে সিঙ্ক করা হচ্ছে ");
//                    HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
//                }
//            }
//        });
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
        //setServiceName();
        findViewById(R.id.dump_btn).setOnClickListener( v -> dumpDatabase() );

    }
    private void setServiceName(){
        getAllService()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ForceSyncModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<ForceSyncModel> forceSyncModels) {
                        updateAdapter(forceSyncModels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        /*AppExecutors appExecutors = new AppExecutors();
        Runnable runnable = () -> {
            ArrayList<ForceSyncModel> forceSyncModelArrayList = getAllService();

            appExecutors.mainThread().execute(() -> updateAdapter(forceSyncModelArrayList));
        };
        appExecutors.diskIO().execute(runnable);*/

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

    Observable<ArrayList<ForceSyncModel>> getAllService(){
       return  Observable.create(e->{
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
          e.onNext(forceSyncModelArrayList);
          e.onComplete();
       });
    }

    private void dumpDatabase(){
        //AppExecutors appExecutors = new AppExecutors();
        ((Button)findViewById(R.id.dump_btn)).setText("ডাটাবেস ডাম্প নেওয়া হচ্ছে ");
        Observable.create(e-> {
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
                try {
                    File logFile = new File(file.getAbsolutePath() + "/"+ "keys.txt");
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(password);
                    buf.newLine();
                    buf.close();
                } catch (IOException ex) {

                }
                File finalFile = new File(filePath);

                finalFile.setWritable(true);

                copyFile(new FileInputStream(Db), new FileOutputStream(finalFile));
                e.onComplete();

            }catch (Exception ex){
                ex.printStackTrace();
                e.onError(ex);

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        ((Button)findViewById(R.id.dump_btn)).setText("ডাম্প নেওয়া শেষ হয়েছে");
                    }
                });
   /*     Runnable runnable = () -> {
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
                try {
                    File logFile = new File(file.getAbsolutePath() + "/"+ "keys.txt");
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(password);
                    buf.newLine();
                    buf.close();
                } catch (IOException e) {

                }
                File finalFile = new File(filePath);

                finalFile.setWritable(true);

                copyFile(new FileInputStream(Db), new FileOutputStream(finalFile));

            }catch (Exception e){
                e.printStackTrace();

            }

            appExecutors.mainThread().execute(() ->  ((Button)findViewById(R.id.dump_btn)).setText("ডাম্প নেওয়া শেষ হয়েছে"));
        };
        appExecutors.diskIO().execute(runnable);*/
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

    private void compareData() {
        new AlertDialog.Builder(this).setMessage("ডাটা কম্পেয়ার উইথ সার্ভার")
                .setTitle("আপনার ডিভাইস এর ডাটা গুলো সার্ভার এর সাথে ম্যাচ আছে কিনা চেক করার জন্য পাঠাতে চান ?")
                .setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction("COMPARE_DATA");
                        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
                        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
                        CompareDataServiceJob.scheduleJobImmediately(CompareDataServiceJob.TAG);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.no_button_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private void syncDataById() {
        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("DATA_SYNC");
        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
        DataSyncByBaseEntityServiceJob.scheduleJobImmediately(DataSyncByBaseEntityServiceJob.TAG);
    }
    private void forceSyncData() {
        invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ForceSyncIntentService.ACTION_SYNC);
        registerReceiver(invalidDataBroadcastReceiver, intentFilter);
        showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);
        ForceSyncDataServiceJob.scheduleJobImmediately(ForceSyncDataServiceJob.TAG);
    }
    private void checkInvalidData() {
        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        int cc = eventClientRepository.getInvalidClientsCount();
        int ec = eventClientRepository.getInvalidEventsCount();
        showInvalidCountDialog(cc,ec,false);


    }
//    private void checkServerVersionNullData() {
//        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
//        int cc = eventClientRepository.getInvalidClientsCount();
//        int ec = eventClientRepository.getInvalidEventsCount(true);
//        showInvalidCountDialog(cc,ec,true);
//
//
//    }
    private void showInvalidCountDialog(int cc, int ec,boolean isFromServerCheck ){
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_invalid_data);
        TextView ccountTxt = dialog.findViewById(R.id.client_count_tv);
        ccountTxt.setText("No Of Invalid Client: "+cc);
        TextView ecountTxt = dialog.findViewById(R.id.event_count_tv);
        ecountTxt.setText("No Of Invalid Event: "+ec);
        Button clientShowBtn = dialog.findViewById(R.id.client_btn);
        Button eventShowBtn = dialog.findViewById(R.id.event_btn);
        Button syncBtn = dialog.findViewById(R.id.invalid_sync_btn);
        Button closeBtn = dialog.findViewById(R.id.close_btn);

        clientShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                InvalidDataDisplayActivity.startInvalidActivity(InvalidDataDisplayActivity.TYPE_CLIENT,ForceSyncActivity.this);
            }
        });
        eventShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                InvalidDataDisplayActivity.startInvalidActivity(InvalidDataDisplayActivity.TYPE_EVENT,ForceSyncActivity.this);

            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cc==0 && ec==0){
                    Toast.makeText(ForceSyncActivity.this,"কোনো ইনভ্যালিড ডাটা পাওয়া যায়নি",Toast.LENGTH_SHORT).show();
                    return;
                }
                invalidDataBroadcastReceiver = new InvalidSyncBroadcast();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("INVALID_SYNC");
                registerReceiver(invalidDataBroadcastReceiver, intentFilter);
                showProgressDialog("ইনভ্যালিড ডাটা সিঙ্ক করা হচ্ছে....");
                dialog.dismiss();
                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(ForceSyncActivity.this);
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


    private ProgressDialog dialog;
    private void showProgressDialog(String message){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage(message);
            dialog.setCancelable(true);
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
        Toast.makeText(this,"সিঙ্ক কমপ্লিট। আরো ইনভ্যালিড ডাটা থাকলে সিঙ্ক করুন",Toast.LENGTH_SHORT).show();
        //finish();
    }
    private class InvalidSyncBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(isFinishing()) return;
                hideProgressDialog();
                if(intent != null && intent.getAction().equalsIgnoreCase("INVALID_SYNC")){
                    String value = intent.getStringExtra("EXTRA_INVALID_SYNC");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                    showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
                    HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                }
                if(intent != null && intent.getAction().equalsIgnoreCase("DATA_SYNC")){
                    String value = intent.getStringExtra("EXTRA_DATA_SYNC");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                }
                if(intent != null && intent.getAction().equalsIgnoreCase("COMPARE_DATA")){
                    String value = intent.getStringExtra("EXTRA_COMPARE_DATA");
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                }
                if(intent != null && intent.getAction().equalsIgnoreCase(ForceSyncIntentService.ACTION_SYNC)){
                    String value = intent.getStringExtra(ForceSyncIntentService.EXTRA_SYNC);
                    Toast.makeText(ForceSyncActivity.this,value,Toast.LENGTH_SHORT).show();
                    showProgressDialog("ডাটা সিঙ্ক করা হচ্ছে....");
                    HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                }
            }catch (Exception e){

            }

        }
    }
}
