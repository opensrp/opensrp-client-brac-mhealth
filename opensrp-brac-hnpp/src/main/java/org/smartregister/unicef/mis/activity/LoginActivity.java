package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.job.GlobalLocationFetchJob;
import org.smartregister.unicef.mis.job.HnppPncCloseJob;
import org.smartregister.unicef.mis.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.mis.job.PullGuestMemberIdServiceJob;
import org.smartregister.unicef.mis.job.SSLocationFetchJob;
import org.smartregister.unicef.mis.job.VaccineDueUpdateServiceJob;
import org.smartregister.unicef.mis.job.ZScoreRefreshServiceJob;
import org.smartregister.unicef.mis.location.SaveDistrictTask;
import org.smartregister.unicef.mis.presenter.LoginPresenter;
import org.smartregister.unicef.mis.repository.DistrictListRepository;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.family.util.Constants;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.LangUtils;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

        HnppApplication.initContext(this);
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
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String language = LangUtils.getLanguage(this);

        if(TextUtils.isEmpty(userName) && language.equalsIgnoreCase("en")){
            LangUtils.saveLanguage(this, "bn");
            HnppApplication.getInstance().getResources().getConfiguration().setLocale(new Locale("bn"));

            Runtime.getRuntime().exit(0);
            Intent intent = new Intent(this,org.smartregister.unicef.mis.activity.LoginActivity.class);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


    }

    @SuppressLint("SetTextI18n")
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
//        if(BuildConfig.DEBUG){
//            userNameText.setText("saif101");//userNameText.setText("md3539632@gmail.com");
//            passwordText.setText("123456");//Bangladesh#123
//        }
        ((TextView) findViewById(R.id.login_build_text_view)).setText("Version " + getVersion() + ", Built on: " + getBuildDate());

        if(!BuildConfig.DEBUG)updateAppVersion();
    }
    public String getBuildDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(new Date(BuildConfig.BUILD_TIMESTAMP));
    }
    private String getVersion()  {
        PackageInfo pInfo = null;
        try {
            pInfo = LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";

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
    private void updateAppVersion(){
        HnppConstants.getAppVersionFromServer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(String s) {
                        Log.v("VERSION_CODE","onNext>>s:"+s);
                        if(!TextUtils.isEmpty(s)){
                            PackageInfo pInfo = null;
                            try {
                                pInfo = LoginActivity.this.getPackageManager().getPackageInfo(getPackageName(), 0);
                                String version = pInfo.versionName;
                                if(version.equalsIgnoreCase(s)){
                                    return;
                                }
                                Log.v("VERSION_CODE","onNext>>version:"+version);
                                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                                alertDialog.setTitle("এপটির নতুন ভার্সন এসেছে ");
                                alertDialog.setMessage("অনুগ্রহ করে ভার্শনটি আপডেট করুন");
                                alertDialog.setCancelable(false);
                                alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "UPDATE",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                    }


                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                if (mActivity != null)
                                    alertDialog.show();
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                        }

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
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
            Utils.startAsyncTask(new SaveDistrictTask(), null);
            PullGuestMemberIdServiceJob.scheduleJobImmediately(PullGuestMemberIdServiceJob.TAG);

        }

        getToFamilyList(remote);
        finish();
    }


    private void getToFamilyList(boolean remote) {
        boolean isExist = new DistrictListRepository(HnppApplication.getInstance().getRepository()).isExistData();
        if(!isExist){
            Utils.startAsyncTask(new SaveDistrictTask(), null);

        }
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(Constants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
        boolean isConnected = HnppConstants.isConnectedToInternet(this);
        if(isConnected){
            PullUniqueIdsServiceJob.scheduleJobImmediately(PullUniqueIdsServiceJob.TAG);
            SSLocationFetchJob.scheduleJobImmediately(SSLocationFetchJob.TAG);
            GlobalLocationFetchJob.scheduleJobImmediately(GlobalLocationFetchJob.TAG);
            try{
                postHPVData();
            }catch (Exception e){

            }
        }
        if(HnppConstants.isNeedToCallInvalidApi()){
            InValidateSyncDataServiceJob.scheduleJob(InValidateSyncDataServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.INVALID_SYNC_DURATION_MINUTES),15l);
        }
        HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);
        VaccineDueUpdateServiceJob.scheduleJobImmediately(VaccineDueUpdateServiceJob.TAG);
        HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
        ZScoreRefreshServiceJob.scheduleJobImmediately(ZScoreRefreshServiceJob.TAG);

    }
    private void postHPVData(){
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

}