package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationActivity extends SecuredActivity implements View.OnClickListener{


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_migration);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        if (isOnline()) {
            findViewById(R.id.migration_member_btn).setOnClickListener(this);
            findViewById(R.id.migration_khana_btn).setOnClickListener(this);
        }
        else
        {
            checkNetworkConnection();
        }


    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.migration_member_btn:
                showDetailsDialog();
                break;
            case R.id.migration_khana_btn:
                showDetailsDialog();
                break;
        }
    }
    private void showDetailsDialog(){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.migration_dialog);
       /* Button yesBtn = dialog.findViewById(R.id.yes_btn);
        Button noBtn = dialog.findViewById(R.id.no_btn);
        Button dontKnowSearchBtn = dialog.findViewById(R.id.dont_know_search_btn);*/

        dialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MigrationActivity.this,MigrationSearchDetailsActivity.class));
            }
        });
        dialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.dont_know_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(MigrationActivity.this,MigrationFilterSearchActivity.class));
            }
        });
        dialog.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            return false;
        }
        return true;
    }

    public void checkNetworkConnection(){
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}