package org.smartregister.brac.hnpp.presenter;

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
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.activity.MigrationActivity;
import org.smartregister.brac.hnpp.activity.NotificationActivity;
import org.smartregister.brac.hnpp.activity.PaymentActivity;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.job.HomeVisitServiceJob;
import org.smartregister.brac.hnpp.activity.COVIDJsonFormActivity;
import org.smartregister.brac.hnpp.activity.ForceSyncActivity;
import org.smartregister.brac.hnpp.job.NotificationGeneratorJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.StockFetchJob;
import org.smartregister.brac.hnpp.job.TargetFetchJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.presenter.NavigationPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.util.FormUtils;

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

//            JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
//            JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
            HnppJsonFormUtils.updateFormWithSSNameAndSelf(jsonForm, SSLocationHelper.getInstance().getSsModels());
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
    public void browseMigration(Activity activity) {
        activity.startActivity(new Intent(activity, MigrationActivity.class));
    }
    @Override
    public void browsePayment(Activity activity) {
        activity.startActivity(new Intent(activity, PaymentActivity.class));
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
            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            TargetFetchJob.scheduleJobImmediately(TargetFetchJob.TAG);
            StockFetchJob.scheduleJobImmediately(StockFetchJob.TAG);
            //NotificationGeneratorJob.scheduleJobImmediately(NotificationGeneratorJob.TAG);
        }
    }
}
