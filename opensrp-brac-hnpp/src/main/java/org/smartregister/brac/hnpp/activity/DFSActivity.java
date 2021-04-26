package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.view.View;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class DFSActivity extends SecuredActivity implements View.OnClickListener {
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_dfs);
        findViewById(R.id.new_payment).setOnClickListener(this);
        findViewById(R.id.history).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));

    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_payment:
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
