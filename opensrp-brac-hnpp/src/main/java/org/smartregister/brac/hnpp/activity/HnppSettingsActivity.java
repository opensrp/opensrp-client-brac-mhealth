package org.smartregister.brac.hnpp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.utils.PermissionUtils;

import org.smartregister.brac.hnpp.BuildConfig;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.widget.LivePreference;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.LangUtils;
import org.smartregister.util.UrlUtil;
import org.smartregister.view.activity.SettingsActivity;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class HnppSettingsActivity extends PreferenceActivity {

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        super.attachBaseContext(base);
    }
    MyPreferenceFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.verifyPermissionGranted(permissions, grantResults, Manifest.permission.READ_PHONE_STATE)) {

            fragment.updateData(true);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SyncStatusBroadcastReceiver.SyncStatusListener {
        private TelephonyManager mTelephonyManager;
        private LivePreference livePreference;
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }
        public void updateData(boolean fromPermission){
            if(fromPermission){
                mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

            }
            Preference editTextPref = (EditTextPreference) findPreference("APP_VERSION");
            editTextPref.setSummary(BuildConfig.VERSION_NAME);
            Preference editTextPref2 = (EditTextPreference) findPreference("IMEI_NO");
            String devieImei = HnppConstants.getDeviceId(mTelephonyManager,getActivity(),true);
            editTextPref2.setSummary(devieImei);
        }

        @SuppressLint("HardwareIds")
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
            addPreferencesFromResource(R.xml.preferences);
            livePreference =(LivePreference) findPreference("change_pin");
            updateData(false);
            Preference baseUrlPreference = findPreference("DRISHTI_BASE_URL");
            if (baseUrlPreference != null) {
                final EditTextPreference baseUrlEditTextPreference = (EditTextPreference) baseUrlPreference;
                baseUrlEditTextPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final Dialog dialog = baseUrlEditTextPreference.getDialog();
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newValue = baseUrlEditTextPreference.getEditText().getText().toString();
                                if (newValue != null && UrlUtil.isValidUrl(newValue)) {
                                    baseUrlEditTextPreference.onClick(null, DialogInterface.BUTTON_POSITIVE);
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), R.string.invalid_url_massage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return false;
                    }
                });
                baseUrlEditTextPreference.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Timber.i("baseUrl before text change");
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        Timber.i("baseUrl on text change");
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String text = editable.toString();
                        boolean validUrl = UrlUtil.isValidUrl(text);
                        if(!validUrl) {
                            if(text.isEmpty()){
                                baseUrlEditTextPreference.getEditText().setError(getString(R.string.msg_empty_url));
                            }
                            else{
                                baseUrlEditTextPreference.getEditText().setError(getString(R.string.invalid_url_massage));
                            }
                        }
                        else{
                            baseUrlEditTextPreference.getEditText().setError(null);
                        }

                    }
                });

                baseUrlEditTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue != null) {
                            updateUrl(newValue.toString());
                        }
                        return true;
                    }
                });
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getActivity(), new String[]{
                            Manifest.permission.READ_PHONE_STATE}, PermissionUtils.PHONE_STATE_PERMISSION);
                    return;
                } else {
                    mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                }
            } else {
                mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            }
            updateData(false);
        }

        private void updateUrl(String baseUrl) {
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                URL url = new URL(baseUrl);

                String base = url.getProtocol() + "://" + url.getHost();
                int port = url.getPort();

                Timber.i("Base URL: %s", base);
                Timber.i("Port: %s", port);

                allSharedPreferences.saveHost(base);
                allSharedPreferences.savePort(port);

                Timber.i("Saved URL: %s", allSharedPreferences.fetchHost(""));
                Timber.i("Port: %s", allSharedPreferences.fetchPort(0));
            } catch (MalformedURLException e) {
                Timber.e("Malformed Url: %s", baseUrl);
            }
        }

        @Override
        public void onSyncStart() {
            showProgressDialog();
        }

        @Override
        public void onSyncInProgress(FetchStatus fetchStatus) {

        }

        @Override
        public void onSyncComplete(FetchStatus fetchStatus) {
            hideProgressDialog();
            livePreference.updateAppData(true);
        }

        @Override
        public void onDestroy() {
            SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
            super.onDestroy();
        }

        @Override
        public void onDestroyView() {
            SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
            super.onDestroyView();
        }
        private ProgressDialog dialog;
        private void showProgressDialog(){
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getActivity().getString(R.string.action_start_sync));
            dialog.setCancelable(false);
            dialog.show();
        }
        private void hideProgressDialog(){
            if(dialog !=null && dialog.isShowing()){
                dialog.setMessage(getActivity().getString(R.string.sync_complete));
                dialog.dismiss();
            }
        }
    }
}
