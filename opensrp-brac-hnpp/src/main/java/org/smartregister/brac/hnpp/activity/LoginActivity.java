package org.smartregister.brac.hnpp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vijay.jsonwizard.utils.PermissionUtils;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.job.PullHouseholdIdsServiceJob;
import org.smartregister.brac.hnpp.job.SSLocationFetchJob;
import org.smartregister.brac.hnpp.presenter.LoginPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.exception.NoHttpResponseException;
import org.smartregister.family.util.Constants;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;



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
        //isDeviceVerifyiedCheck();
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
            return true;
        }
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
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }

        getToFamilyList(remote);

        finish();
    }
    public void isDeviceVerifyiedCheck() {
        if(HnppConstants.isDeviceVerified()){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        }
        String devieImei = HnppConstants.getDeviceId(mTelephonyManager,this,false);
        if(TextUtils.isEmpty(devieImei)){
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
                            String url = baseUrl + "/deviceverify/get?imei=" + devieImei;
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
                    showDialog(status,devieImei);

                }else{
                    showDialog("",devieImei);
                }

            }
        }, null);
    }
    private void showDialog(String status, String devieImei){
        hideProgressDialog();
        Log.v("IMEI_URL","showDialog:"+status);
        if(TextUtils.isEmpty(status) || !status.equalsIgnoreCase("true")){
            HnppConstants.updateDeviceVerified(false);
            android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("এই ডিভাইস টি রেজিস্টার করা হয় নি।ডিভাইস id:"+devieImei);
            alertDialog.setCancelable(false);

            alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
                    (dialog, which) -> {
                        if (mActivity != null) mActivity.finish();
                    });
            if (mActivity != null)
                alertDialog.show();
        }else{
            HnppConstants.updateDeviceVerified(true);
        }
    }
    private ProgressDialog dialog;
    private void showProgressDialog(){
        dialog = new ProgressDialog(this);
        dialog.setMessage("ডিভাইস টি রেজিস্টার কিনা ছেক করা হচ্ছে");
        dialog.setCancelable(false);
        dialog.show();
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
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(Constants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
        try{
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            SSLocationFetchJob.scheduleJobImmediately(SSLocationFetchJob.TAG);
        }catch (Exception e){

        }

    }

}