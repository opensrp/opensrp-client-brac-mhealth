package org.smartregister.unicef.dghs.nativation.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.DFSActivity;
import org.smartregister.unicef.dghs.activity.GrowthReportActivity;
import org.smartregister.unicef.dghs.activity.MigrationActivity;
import org.smartregister.unicef.dghs.activity.NotificationActivity;
import org.smartregister.unicef.dghs.activity.QRScannerActivity;
import org.smartregister.unicef.dghs.activity.SearchActivity;
import org.smartregister.unicef.dghs.job.DataDeleteJob;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.activity.COVIDJsonFormActivity;
import org.smartregister.unicef.dghs.activity.ForceSyncActivity;
import org.smartregister.unicef.dghs.job.VaccineDueUpdateServiceJob;
import org.smartregister.unicef.dghs.nativation.interactor.NavigationInteractor;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.repository.EventClientRepository;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class HnppNavigationPresenter implements NavigationContract.Presenter {

    private NavigationContract.Model mModel;
    private NavigationContract.Interactor mInteractor;
    private WeakReference<NavigationContract.View> mView;
    protected HashMap<String, String> tableMap = new HashMap<>();

    public HnppNavigationPresenter(HnppApplication application, NavigationContract.View view, NavigationModel.Flavor modelFlavor) {
        mView = new WeakReference<>(view);

        mInteractor = NavigationInteractor.getInstance();
        mInteractor.setApplication(application);

        mModel = NavigationModel.getInstance();
        mModel.setNavigationFlavor(modelFlavor);

        initialize();
    }

    protected void initialize() {
        tableMap.put(CoreConstants.DrawerMenu.ALL_FAMILIES, CoreConstants.TABLE_NAME.FAMILY);
        tableMap.put(CoreConstants.DrawerMenu.ALL_MEMBER,  CoreConstants.TABLE_NAME.FAMILY_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ELCO_CLIENT,"test");
        tableMap.put(CoreConstants.DrawerMenu.ADULT,"adult");
        tableMap.put(CoreConstants.DrawerMenu.ADO,"ado");
        tableMap.put(CoreConstants.DrawerMenu.IYCF,"iycf");
        tableMap.put(CoreConstants.DrawerMenu.WOMEN,"women");
        tableMap.put(CoreConstants.DrawerMenu.FORUM,"");
        tableMap.put(CoreConstants.DrawerMenu.GUEST_MEMBER,"");
        tableMap.put(CoreConstants.DrawerMenu.CHILD_CLIENTS, CoreConstants.TABLE_NAME.CHILD);
        tableMap.put(CoreConstants.DrawerMenu.ANC_CLIENTS, CoreConstants.TABLE_NAME.ANC_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ANC, CoreConstants.TABLE_NAME.ANC_MEMBER);
        tableMap.put(CoreConstants.DrawerMenu.ANC_RISK, "anc_risk");
        tableMap.put(CoreConstants.DrawerMenu.PNC_RISK, "pnc_risk");
        tableMap.put(CoreConstants.DrawerMenu.ELCO_RISK, "elco_risk");
        tableMap.put(CoreConstants.DrawerMenu.CHILD_RISK, "child_risk");
        tableMap.put(CoreConstants.DrawerMenu.ADULT_RISK, "adult_risk");
        tableMap.put(CoreConstants.DrawerMenu.PNC, CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME);
        tableMap.put(CoreConstants.DrawerMenu.REFERRALS, CoreConstants.TABLE_NAME.TASK);
        tableMap.put(CoreConstants.DrawerMenu.MALARIA, CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION);
        tableMap.put(CoreConstants.DrawerMenu.ALL_MEMBER, CoreConstants.TABLE_NAME.FAMILY_MEMBER);
    }

    @Override
    public void sendAlert(Activity activity) {
        sendAlertForVaccine().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String res) {
                        alertResponse = res;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {

                            new AlertDialog.Builder(activity)
                                    .setTitle("Alert!")
                                    .setMessage(alertResponse)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();

                    }
                });
    }

    @Override
    public List<NavigationOption> getOptions() {
        return mModel.getNavigationItems();
    }

    @Override
    public void covid19(Activity activity) {
        startAnyFormActivity(activity);
    }
    public void startAnyFormActivity(Activity activity) {
        try {
            String baseEntityId = JsonFormUtils.generateRandomUUIDString();
            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject("covid19");

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
    public void scanQR(Activity activity) {
        activity.startActivity(new Intent(activity, QRScannerActivity.class));
    }

    @Override
    public void browseSSInfo(Activity activity) {
        activity.startActivity(new Intent(activity, SearchActivity.class));
//       String providerId =  HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
//        String url = "http://hnppdfs.brac.net/SkTabLogIn?id="+providerId+"&key=62fa0f87-0710-4932-8119-8d4fe4c083e3";
//        try{
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            activity.startActivity(browserIntent);
//
//        }catch (Exception e){
//            Toast.makeText(activity, R.string.active_your_browser,Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void browseNotification(Activity activity) {
        activity.startActivity(new Intent(activity, NotificationActivity.class));
    }

    @Override
    public void updateLocation(Activity activity) {
        //activity.startActivity(new Intent(activity, BlockUpdateActivity.class));
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
//        activity.startActivity(new Intent(activity, NewDashBoardActivity.class));
        activity.startActivity(new Intent(activity, GrowthReportActivity.class));
    }

    @Override
    public void sync(Activity activity) {
        startServices();
      if(!BuildConfig.DEBUG)userStatusCheck(activity);
    }
    private io.reactivex.Observable<String> sendAlertForVaccine(){
        return  io.reactivex.Observable.create(e->{
                    try {
                        String baseUrl = CoreLibrary.getInstance().context().
                                configuration().dristhiBaseURL();
                        String endString = "/";
                        if (baseUrl.endsWith(endString)) {
                            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
                        }
                        String url = baseUrl + "/rest/event/announcement";
                        Log.v("USER_STATUS","url:"+url);
                        Response resp = CoreLibrary.getInstance().context().getHttpAgent().fetch(url);
                        if (resp.isFailure()) {
                            e.onError(new NoHttpResponseException("no route found"));
                            e.onComplete();
                            return;
                        }
                        e.onNext(new JSONObject(resp.payload().toString()).getString("msg"));
                        e.onComplete();
                    } catch (Exception ex) {
                        e.onNext(ex.getMessage());//error
                        e.onComplete();
                    }

                }
        );
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
                        HnppConstants.appendLog("SAVE_VISIT","updateUserStatus :"+ex.getMessage());
                        Log.d("SAVE_VISIT","processVisitLog exception occured :"+ex.getMessage());
                        e.onNext("");//error
                        e.onComplete();
                    }

                }
        );
    }
    String response = "",alertResponse = "";
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
                    alertDialog.setTitle(activity.getString(R.string.acount_deactiving));
                    alertDialog.setCancelable(false);

                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.ok_en),
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
//        if(!HnppConstants.isPALogin()){
//            MigrationFetchJob.scheduleJobImmediately(MigrationFetchJob.TAG);
//        }
        HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
        PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
        VaccineDueUpdateServiceJob.scheduleJobImmediately(VaccineDueUpdateServiceJob.TAG);
//        TargetFetchJob.scheduleJobImmediately(TargetFetchJob.TAG);
//        StockFetchJob.scheduleJobImmediately(StockFetchJob.TAG);
        DataDeleteJob.scheduleJobImmediately(DataDeleteJob.TAG);
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
                    }

                    @Override
                    public void onComplete() {
                        Log.v("OTHER_VACCINE","completed");
                    }
                });
    }
    @Override
    public NavigationContract.View getNavigationView() {
        return mView.get();
    }
    @Override
    public void refreshNavigationCount() {

        int x = 0;
        while (x < mModel.getNavigationItems().size()) {

            final int finalX = x;
            NavigationOption option = mModel.getNavigationItems().get(x);
            if(option.isNeedToExpand()){
                mInteractor.getRegisterCount(tableMap.get(option.getNavigationSubModel().getType()), new NavigationContract.InteractorCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        mModel.getNavigationItems().get(finalX).getNavigationSubModel().setSubCount(result);
                        getNavigationView().refreshCount();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
            mInteractor.getRegisterCount(tableMap.get(option.getMenuTitle()), new NavigationContract.InteractorCallback<Integer>() {
                @Override
                public void onResult(Integer result) {
                    mModel.getNavigationItems().get(finalX).setRegisterCount(result);
                    getNavigationView().refreshCount();
                }

                @Override
                public void onError(Exception e) {
                    // getNavigationView().displayToast(activity, "Error retrieving count for " + tableMap.get(mModel.getNavigationItems().get(finalX).getMenuTitle()));
                    Timber.e("Error retrieving count for %s", tableMap.get(mModel.getNavigationItems().get(finalX).getMenuTitle()));
                }
            });
            x++;
        }

    }



    @Override
    public void refreshLastSync() {
        // get last sync date
        getNavigationView().refreshLastSync(mInteractor.sync());
    }

    @Override
    public void displayCurrentUser() {
        getNavigationView().refreshCurrentUser(mModel.getCurrentUser());
    }

}
