package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.LangUtils;
import org.smartregister.util.UrlUtil;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

public class HnppSettingsActivity extends PreferenceActivity {

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }
    MyPreferenceFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();

    }




    public static class MyPreferenceFragment extends PreferenceFragment{
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

        }
        public void updateData(){

            Preference editTextPref = (EditTextPreference) findPreference("APP_VERSION");
            editTextPref.setSummary(BuildConfig.VERSION_NAME);
            Preference editTextPref2 = (EditTextPreference) findPreference("IMEI_NO");
            String devieImei = HnppConstants.getDeviceId(getActivity());
            editTextPref2.setSummary(devieImei);
        }

        @SuppressLint("HardwareIds")
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
            updateData();
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

            updateData();
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
    }
}
