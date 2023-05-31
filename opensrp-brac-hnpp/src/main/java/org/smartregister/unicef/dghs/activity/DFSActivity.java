package org.smartregister.unicef.dghs.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class DFSActivity extends SecuredActivity implements View.OnClickListener {
    private static final String EXTRA_IS_COMES_FROM = "is_comes_from";
    private boolean isComesFromPaymentDone;

    public static void startPaymentActivity(Activity activity, boolean isComesFromPaymentDone){
        Intent intent = new Intent(activity,DFSActivity.class);
        intent.putExtra(EXTRA_IS_COMES_FROM,isComesFromPaymentDone);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_dfs);
        findViewById(R.id.new_payment).setOnClickListener(this);
        findViewById(R.id.history).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        isComesFromPaymentDone = getIntent().getBooleanExtra(EXTRA_IS_COMES_FROM,false);
        if(isComesFromPaymentDone){
            startActivity(new Intent(this, PaymentHistoryActivity.class));
            return;
        }
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_payment:
                if(!HnppConstants.isConnectedToInternet(this)){
                    HnppConstants.checkNetworkConnection(this);
                    return;
                }
                startActivity(new Intent(this,PaymentActivity.class));
                break;
            case R.id.history:
                startActivity(new Intent(this,PaymentHistoryActivity.class));
                break;
            case R.id.backBtn:
                finish();
                break;
        }

    }



}
