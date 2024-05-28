package org.smartregister.brac.hnpp.activity;

import static org.smartregister.brac.hnpp.utils.HnppConstants.HH_SORTED_BY;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.android.job.JobManager;
import com.vijay.jsonwizard.utils.PermissionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.job.HHVisitDurationFetchJob;
import org.smartregister.brac.hnpp.job.HnppPncCloseJob;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.job.MigrationFetchJob;
import org.smartregister.brac.hnpp.job.NotificationGeneratorJob;
import org.smartregister.brac.hnpp.job.PullGuestMemberIdServiceJob;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.SSLocationFetchJob;
import org.smartregister.brac.hnpp.job.StockFetchJob;
import org.smartregister.brac.hnpp.job.SurveyHistoryJob;
import org.smartregister.brac.hnpp.job.TargetFetchJob;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SaveDistrictTask;
import org.smartregister.brac.hnpp.presenter.LoginPresenter;
import org.smartregister.brac.hnpp.repository.DistrictListRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.family.util.Constants;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.service.HTTPAgent;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {
    public static final String TAG = BaseLoginActivity.class.getCanonicalName();

    private EditText userNameText,passwordText;
    private View userNameView, passwordView;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userNameText = findViewById(R.id.login_user_name_edit_text);
        passwordText = findViewById(R.id.login_password_edit_text);
        userNameView = findViewById(R.id.login_user_name_view);
        passwordView = findViewById(R.id.login_password_view);
//        if(BuildConfig.DEBUG){
//            passwordText.setText("brac2019");
//        }


        userNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    findViewById(R.id.login_login_btn).setAlpha(1.0f);
                }

            }
        });
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    findViewById(R.id.login_login_btn).setAlpha(1.0f);
                }

            }
        });

        userNameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasfocus) {
                if (hasfocus) {
                    userNameView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.hnpp_accent));
                } else {
                    userNameView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));

                }
            }
        });

        passwordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View arg0, boolean hasfocus) {
                if (hasfocus) {
                    passwordView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.hnpp_accent));
                } else {
                    passwordView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                }
            }
        });

        HnppConstants.deleteLogFile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(Boolean bool) {
                        Log.v("NEXT_DELETE_LOG",""+bool);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("NEXT_DELETE_ERROR",""+e);
                    }

                    @Override
                    public void onComplete() {
                        Log.v("NEXT_DELETE_COMPLETE","completed");
                    }
                });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
                    goToHome(false);
         }
        fillUserIfExists();
        findViewById(R.id.login_login_btn).setAlpha(1.0f);
        mActivity = this;
        HnppConstants.updateAppBackgroundOnResume(findViewById(R.id.login_layout));
        if(!BuildConfig.DEBUG)isDeviceVerifyiedCheck();
        if(BuildConfig.DEBUG){
//            userNameText.setText("01313049998");
//            passwordText.setText("9998");
            //  PA user
//            userNameText.setText("01787699880");
//            passwordText.setText("9880");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Settings");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("Settings")) {
            startActivity(new Intent(this, HnppSettingsActivity.class));
            //startActivity(new Intent(this, NotificationActivity.class));
            return true;
        }
        //startActivity(new Intent(this, NotificationActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == org.smartregister.R.id.login_login_btn) {
            if(HnppConstants.isWrongDate()){
                new AlertDialog.Builder(this).setMessage(getString(R.string.wrong_date_msg))
                        .setTitle(R.string.wrong_date_title).setCancelable(false)
                        .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }
            String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
            if(!TextUtils.isEmpty(userName) && !userName.equalsIgnoreCase(userNameText.getText().toString())){
                showClearDataMessage();
                return;
            }

            v.setAlpha(0.3f);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoginPresenter.attemptLogin(userNameText.getText().toString(), passwordText.getText().toString());
                }
            },500);

        }
    }

    private void fillUserIfExists() {
            String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
            if(!TextUtils.isEmpty(userName)){
                userNameText.setText(userName.trim());
            }else{
                userNameText.setText("");
            }
    }
    private void showClearDataMessage(){
        new AlertDialog.Builder(this).setMessage(getString(R.string.clear_data))
                .setTitle(R.string.title_clear_data).setCancelable(false)
                .setPositiveButton(R.string.yes_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        String sortedBy = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(HH_SORTED_BY);
        if(TextUtils.isEmpty(sortedBy)){
            HnppConstants.sSortedBy = HnppConstants.SORT_BY.SORT_BY_REGIGTRATION;
        }else{
            HnppConstants.sSortedBy = Integer.parseInt(sortedBy);
        }
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);

            Utils.startAsyncTask(new SaveDistrictTask(), null);
            PullGuestMemberIdServiceJob.scheduleJobImmediately(PullGuestMemberIdServiceJob.TAG);
            SurveyHistoryJob.scheduleJobImmediately(SurveyHistoryJob.TAG);

        }

        getToFamilyList(remote);
        finish();
    }

    public void isDeviceVerifyiedCheck() {
        if(HnppConstants.isDeviceVerified()){
           // Log.d("IMEI_URL","verified");
            return;
        }
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) this, new String[]{
                        Manifest.permission.READ_PHONE_STATE}, PermissionUtils.PHONE_STATE_PERMISSION);
                return;
            } else {
                mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            }
        } else {
            mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        }*/
        String deviceId = HnppConstants.getDeviceId(this,false);
        Log.d("deviceId",deviceId);
        if(TextUtils.isEmpty(deviceId)){
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("এই ডিভাইস টির IMEI পাওয়া যাইনি");
            alertDialog.setCancelable(false);

            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
                    (dialog, which) -> {
                        if (mActivity != null) mActivity.finish();
                    });
            if (mActivity != null && alertDialog != null)
                alertDialog.show();
            return;
        }
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
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
                    // baseUrl = "http://mhealthtest.brac.net:8080/opensrp";
                            String url = baseUrl + "/deviceverify/get?imei=" + deviceId;
                            Log.v("IMEI_URL","url:"+url);
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
                    showDialog(status,deviceId);

                }else{
                    showDialog("",deviceId);
                }

            }
        }, null);
    }
    private void showDialog(String status, String devieImei){
        hideProgressDialog();
        Log.v("IMEI_URL","showDialog:"+status+devieImei);
        if(TextUtils.isEmpty(status) || !status.equalsIgnoreCase("true")){
            HnppConstants.updateDeviceVerified(false,devieImei);
//            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(mActivity).create();
//            alertDialog.setMessage(devieImei);
//            alertDialog.setTitle("এই ডিভাইস টি রেজিস্টার করা হয় নি।ডিভাইস id:");
//            alertDialog.setCancelable(false);
//
//            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
//                    (dialog, which) -> {
//                        if (mActivity != null) mActivity.finish();
//                    });
//            if (mActivity != null)
//                alertDialog.show();
            //
            AlertDialog alertDialog;
            AlertDialog.Builder builder;
// The TextView to show your Text
            TextView showText = new TextView(this);
            showText.setGravity(Gravity.CENTER);
            showText.setTextSize(20);
            showText.setText(devieImei);
// Add the Listener
            showText.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    // Copy the Text to the clipboard
                    ClipboardManager manager =
                            (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    TextView showTextParam = (TextView) v;
                    manager.setText( showTextParam.getText() );
                    // Show a message:
                    Toast.makeText(v.getContext(), "Device id copied",
                                    Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
// Build the Dialog
            builder = new AlertDialog.Builder(this);
            builder.setView(showText);
            alertDialog = builder.create();
// Some eye-candy
            alertDialog.setTitle("এই ডিভাইস টি রেজিস্টার করা হয় নি।ডিভাইস আইডি টি হলো");
            alertDialog.setCancelable(false);
            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
                    (dialog, which) -> {
                        if (mActivity != null) mActivity.finish();
                    });
            if (mActivity != null)
                alertDialog.show();
        }else{
            Log.v("devieImei",devieImei);
            HnppConstants.updateDeviceVerified(true,devieImei);
        }
    }
    private ProgressDialog dialog;
    private void showProgressDialog(){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage("ডিভাইস টি রেজিস্টার কিনা চেক করা হচ্ছে");
            dialog.setCancelable(false);
            dialog.show();
        }

    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
    private TelephonyManager mTelephonyManager;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.READ_PHONE_STATE)) {
            isDeviceVerifyiedCheck();
        }
    }

    private void getToFamilyList(boolean remote) {
        boolean isExist = new DistrictListRepository(HnppApplication.getInstance().getRepository()).isExistData();
        if(!isExist){
            Utils.startAsyncTask(new SaveDistrictTask(), null);

        }

        boolean isConnected = HnppConstants.isConnectedToInternet(this);
        if(isConnected){
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            SSLocationFetchJob.scheduleJobImmediately(SSLocationFetchJob.TAG);
            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
            if(!HnppConstants.isPALogin()){
                MigrationFetchJob.scheduleJobImmediately(MigrationFetchJob.TAG);
                HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
                VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
                VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);
                HHVisitDurationFetchJob.scheduleJobImmediately(HHVisitDurationFetchJob.TAG);
                PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            }

        }
        if(HnppConstants.isNeedToCallInvalidApi()){
            InValidateSyncDataServiceJob.scheduleJob(InValidateSyncDataServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.INVALID_SYNC_DURATION_MINUTES),15l);
        }
        if(HnppConstants.isPALogin()){
            startActivity(new Intent(this, PANewHomeActivity.class));
        }else{
            Intent intent = new Intent(this, FamilyRegisterActivity.class);
            intent.putExtra(Constants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
            startActivity(intent);
        }
    }

}