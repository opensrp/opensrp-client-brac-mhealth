package org.smartregister.brac.hnpp.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.AllConstants;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.job.SyncServiceJob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class LivePreference extends Preference  {
    TextView live_action,test_action;
    private String appMode;
    public LivePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LivePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LivePreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        live_action = view.findViewById(R.id.live_button);
        test_action = view.findViewById(R.id.test_button);
        live_action.setOnClickListener(buttonClickListener);
        test_action.setOnClickListener(buttonClickListener);
        updateUi();
    }
    private void updateUi(){
        if(HnppConstants.isReleaseBuild()){
            live_action.setBackgroundResource(R.drawable.live_button_bg);
            live_action.setTextColor(getContext().getResources().getColor(R.color.white));
            test_action.setBackgroundResource(R.drawable.test_button_disable_bg);
            test_action.setTextColor(getContext().getResources().getColor(R.color.transparent_gray));
        }else{
            live_action.setBackgroundResource(R.drawable.live_button_disable_bg);
            live_action.setTextColor(getContext().getResources().getColor(R.color.transparent_gray));
            test_action.setBackgroundResource(R.drawable.test_button_bg);
            test_action.setTextColor(getContext().getResources().getColor(R.color.white));
        }
    }

    public View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.live_button:
                    if(HnppConstants.isReleaseBuild()) return;
                    appMode = "L";
                    createAlertDialog(v.getContext());
                    break;
                case R.id.test_button:
                    if(!HnppConstants.isReleaseBuild()) return;
                    appMode = "T";
                    createAlertDialog(v.getContext());
                    break;
            }
        }
    };

    public void createAlertDialog(Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage("Enter Password");
        alert.setCancelable(false);
        alert.setView(edittext);

        alert.setPositiveButton("সাবমিট", (dialog, whichButton) -> {
            if(edittext.getText().toString().isEmpty()){
                Toast.makeText(context,"পাসওয়ার্ড দিন",Toast.LENGTH_SHORT).show();
                return;
            }

            passwordCheck(edittext.getText().toString().trim());

        });
        alert.setNegativeButton("বাতিল", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }
    public void passwordCheck(String inputedPassword) {
        org.smartregister.util.Utils.startAsyncTask(new AsyncTask() {
            String serverPassword = "";

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    String baseUrl = Utils.getAllSharedPreferences().getPreference(AllConstants.DRISHTI_BASE_URL);
                    // Create a URL for the desired page
                    baseUrl = baseUrl.replace("opensrp/", "");
                    URL url = new URL(baseUrl + "opt/multimedia/change_apk.txt");

                    // Read all the text returned by the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                    String str;
                    while ((str = in.readLine()) != null) {
                        // str is one line of text; readLine() strips the newline character(s)
                        serverPassword += str;
                    }
                    in.close();
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(!serverPassword.trim().isEmpty() && serverPassword.equalsIgnoreCase(inputedPassword)){
                    if(appMode.equalsIgnoreCase("T")){
                        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
                        if(TextUtils.isEmpty(userName)){
                            updateAppData(false);
                        }else{
                            SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                        }

                    }else {
                        updateAppData(false);
                    }
                }else{
                    android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("পাসওয়ার্ড মিলে নাই");
                    alertDialog.setCancelable(false);

                    alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "ওকে",
                            (dialog, which) -> {

                            });
                    alertDialog.show();
                }

            }
        }, null);
    }
    public void updateAppData(boolean isFromSyncComplete){
        if(isFromSyncComplete){
            if(appMode !=null && appMode.equalsIgnoreCase("T")){
                clearApplicationData();
                HnppConstants.updateLiveTest(appMode);
                updateUi();
                HnppApplication.getHNPPInstance().appSwitch();
            }
        }else{
            clearApplicationData();
            HnppConstants.updateLiveTest(appMode);
            updateUi();
            HnppApplication.getHNPPInstance().appSwitch();
        }

    }
    public void clearApplicationData() {
        HnppApplication.getHNPPInstance().clearSharePreference();
        HnppApplication.getHNPPInstance().clearDatabase();
    }

}
