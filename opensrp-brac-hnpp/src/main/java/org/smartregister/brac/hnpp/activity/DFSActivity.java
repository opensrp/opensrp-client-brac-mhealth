package org.smartregister.brac.hnpp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
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
                    checkNetworkConnection();
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


    public void checkNetworkConnection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.no_internet_title);
        builder.setMessage(R.string.no_internet_msg);
        builder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
