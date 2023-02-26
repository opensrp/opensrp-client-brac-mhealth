package org.smartregister.unicef.dghs.nativation.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.activity.BlockUpdateActivity;
import org.smartregister.unicef.dghs.activity.DFSActivity;
import org.smartregister.unicef.dghs.activity.MigrationActivity;
import org.smartregister.unicef.dghs.activity.NewDashBoardActivity;
import org.smartregister.unicef.dghs.activity.NotificationActivity;
import org.smartregister.unicef.dghs.job.DataDeleteJob;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.activity.COVIDJsonFormActivity;
import org.smartregister.unicef.dghs.activity.ForceSyncActivity;
import org.smartregister.unicef.dghs.job.MigrationFetchJob;
import org.smartregister.unicef.dghs.job.StockFetchJob;
import org.smartregister.unicef.dghs.job.TargetFetchJob;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.util.FormUtils;
import org.smartregister.repository.EventClientRepository;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.Observable;


public class HnppNavigationPresenter extends NavigationPresenter {
    public HnppNavigationPresenter(CoreApplication application, NavigationContract.View view, NavigationModel.Flavor modelFlavor) {
        super(application, view, modelFlavor);
    }

    @Override
    protected void initialize() {
        super.initialize();
        tableMap.put(CoreConstants.DrawerMenu.ALL_MEMBER, CoreConstants.TABLE_NAME.FAMILY_MEMBER);
    }

    @Override
    public void covid19(Activity activity) {
        startAnyFormActivity(activity);
    }
    public void startAnyFormActivity(Activity activity) {
        try {
            String baseEntityId = JsonFormUtils.generateRandomUUIDString();
            JSONObject jsonForm = FormUtils.getInstance(activity.getApplicationContext()).getFormJson("covid19");

            jsonForm.put(JsonFormUtils.ENTITY_ID, baseEntityId);
            Intent intent;

            intent = new Intent(activity, COVIDJsonFormActivity.class);

            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, true);
            if (activity != null) {
                activity.startActivityForResult(intent, org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON);
            }
        }catch (Exception e){

        }
    }

    @Override
    public void updateUnSyncCount() {
        EventClientRepository eventClientRepository = HnppApplication.getHNPPInstance().getEventClientRepository();
        int cc = eventClientRepository.getUnSyncClientsCount();
        int ec = eventClientRepository.getUnSyncEventsCount();
        Log.v("UNSYNC_COUNT","cc>>"+cc+":ec>"+ec);
        getNavigationView().updateUnSyncCount(cc+ec);
    }

    @Override
    public void forceSync(Activity activity) {
        activity.startActivity(new Intent(activity, ForceSyncActivity.class));

    }

    @Override
    public void browseSSInfo(Activity activity) {
       String providerId =  HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String url = "http://hnppdfs.brac.net/SkTabLogIn?id="+providerId+"&key=62fa0f87-0710-4932-8119-8d4fe4c083e3";
        try{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);

        }catch (Exception e){
            Toast.makeText(activity,"আপনার ব্রাউজার চালু রাখুন",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void browseNotification(Activity activity) {
        activity.startActivity(new Intent(activity, NotificationActivity.class));
    }

    @Override
    public void updateLocation(Activity activity) {
        activity.startActivity(new Intent(activity, BlockUpdateActivity.class));
    }

    @Override
    public void browseMigration(Activity activity) {
        activity.startActivity(new Intent(activity, MigrationActivity.class));
    }
    @Override
    public void browsePayment(Activity activity) {
        activity.startActivity(new Intent(activity, DFSActivity.class));
    }

    @Override
    public void browseDashboard(Activity activity) {
        activity.startActivity(new Intent(activity, NewDashBoardActivity.class));
    }

    @Override
    public void sync(Activity activity) {
        startServices();
      if(!BuildConfig.DEBUG)userStatusCheck(activity);
    }
    private io.reactivex.Observable<String> updateUserStatus(){
        return  io.reactivex.Observable.create(e->{
                    try {
                        String baseUrl = CoreLibrary.getInstance().context().
                                configuration().dristhiBaseURL();
                        String endString = "/";
                        if (baseUrl.endsWith(endString)) {
                            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
                        }
                        try {
                            String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
                            String url = baseUrl + "/user/status?username=" + userName+"&version="+ BuildConfig.VERSION_NAME;
                            Log.v("USER_STATUS","url:"+url);
                            Response resp = CoreLibrary.getInstance().context().getHttpAgent().fetchWithoutAuth(url);
                            if (resp.isFailure()) {
                                throw new NoHttpResponseException(" not returned data");
                            }
                            e.onNext(resp.payload().toString());
                            e.onComplete();
                        } catch (NoHttpResponseException exception) {
                            exception.printStackTrace();
                        }
                    } catch (Exception ex) {
                        HnppConstants.appendLog("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        Log.d("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );
    }
    String response = "";
    private void userStatusCheck(Activity activity){

        updateUserStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String res) {
                        response = res;
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDialog("",activity);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("userStatusCheck","true");
                        showDialog(response,activity);

                    }
                });
    }
    private void showDialog(String status,Activity activity){

        if(!TextUtils.isEmpty(status) && status.equalsIgnoreCase("false")){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(activity).create();
                    alertDialog.setTitle("আপনার অ্যাকাউন্টটি নিষ্ক্রিয় করা হয়েছে");
                    alertDialog.setCancelable(false);

                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
                            (dialog, which) -> {
                                HnppApplication.getHNPPInstance().forceLogoutForRemoteLogin();
                            });
                    if (activity != null)
                        alertDialog.show();
                }
            });

        }else{

            startServices();
            //NotificationGeneratorJob.scheduleJobImmediately(NotificationGeneratorJob.TAG);
        }
    }
    private void startServices(){
        if(!HnppConstants.isPALogin()){
            MigrationFetchJob.scheduleJobImmediately(MigrationFetchJob.TAG);
        }
        HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
        TargetFetchJob.scheduleJobImmediately(TargetFetchJob.TAG);
        StockFetchJob.scheduleJobImmediately(StockFetchJob.TAG);
        DataDeleteJob.scheduleJobImmediately(DataDeleteJob.TAG);
    }
}
