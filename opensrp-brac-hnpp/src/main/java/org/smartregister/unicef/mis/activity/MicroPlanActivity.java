package org.smartregister.unicef.mis.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class MicroPlanActivity extends SecuredActivity implements View.OnClickListener{


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_microplan);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_layout));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.add_outreach_btn).setOnClickListener(this);
        findViewById(R.id.add_epi_btn).setOnClickListener(this);



    }

    @Override
    protected void onResumption() {

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.add_outreach_btn:
                startActivity(new Intent(MicroPlanActivity.this,OutreachActivity.class));
                break;
            case R.id.add_epi_btn:
                AddSessionActivity.startAddSessionActivity(MicroPlanActivity.this,null);

               // startActivity(new Intent(MicroPlanActivity.this,AddMicroplanActivity.class));


                break;
        }
    }
}