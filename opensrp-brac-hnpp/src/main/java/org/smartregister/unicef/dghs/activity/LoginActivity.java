package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.smartregister.unicef.dghs.BuildConfig;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.job.CampFetchJob;
import org.smartregister.unicef.dghs.job.HnppPncCloseJob;
import org.smartregister.unicef.dghs.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.dghs.job.MigrationFetchJob;
import org.smartregister.unicef.dghs.job.PullGuestMemberIdServiceJob;
import org.smartregister.unicef.dghs.job.PullHouseholdIdsServiceJob;
import org.smartregister.unicef.dghs.job.SSLocationFetchJob;
import org.smartregister.unicef.dghs.location.SaveDistrictTask;
import org.smartregister.unicef.dghs.presenter.LoginPresenter;
import org.smartregister.unicef.dghs.repository.DistrictListRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.family.util.Constants;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.InValidateSyncDataServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import java.util.concurrent.TimeUnit;

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
//            userNameText.setText("doli@ha.5");//userNameText.setText("baby@ha.4");
//            passwordText.setText("Mis@4321");
//        }
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
            HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
            PullHouseholdIdsServiceJob.scheduleJobImmediately(PullHouseholdIdsServiceJob.TAG);
            if(!HnppConstants.isPALogin()){
                MigrationFetchJob.scheduleJobImmediately(MigrationFetchJob.TAG);
            }
            CampFetchJob.scheduleJobImmediately(CampFetchJob.TAG);
            HnppPncCloseJob.scheduleJobImmediately(HnppPncCloseJob.TAG);
            VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
            VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);


        }
        if(HnppConstants.isNeedToCallInvalidApi()){
            InValidateSyncDataServiceJob.scheduleJob(InValidateSyncDataServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.INVALID_SYNC_DURATION_MINUTES),15l);
        }


    }

}