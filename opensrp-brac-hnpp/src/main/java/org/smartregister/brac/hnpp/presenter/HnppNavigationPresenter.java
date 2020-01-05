package org.smartregister.brac.hnpp.presenter;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.job.HomeVisitServiceJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.presenter.NavigationPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;

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
    public void sync(Activity activity) {
      userStatusCheck(activity);
    }
    private void userStatusCheck(Activity activity){
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
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
                    String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
                    baseUrl = "http://192.168.19.146:8080/opensrp";
                    String url = baseUrl + "/user/status?username=" + userName+"&version="+ BuildConfig.VERSION_NAME;
                    Log.v("USER_STATUS","url:"+url);
                    Response resp = CoreLibrary.getInstance().context().getHttpAgent().fetchWithoutAuth(url);
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
                if(o !=null ){
                    String status = (String)o;
                    showDialog(status,activity);

                }else{
                    showDialog("",activity);
                }

            }
        }, null);
    }
    private void showDialog(String status,Activity activity){


        Log.v("USER_STATUS","showDialog:"+status);
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
            HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
            SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
        }
    }
}
