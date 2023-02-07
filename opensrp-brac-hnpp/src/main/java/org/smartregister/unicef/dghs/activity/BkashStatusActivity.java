package org.smartregister.unicef.dghs.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.PaymentAdapter;
import org.smartregister.unicef.dghs.contract.BkashStatusContract;
import org.smartregister.unicef.dghs.presenter.BkashStatusPresenter;
import org.smartregister.unicef.dghs.presenter.PaymentPresenter;
import org.smartregister.unicef.dghs.utils.BkashStatus;
import org.smartregister.unicef.dghs.utils.StockDetailsData;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class BkashStatusActivity extends SecuredActivity implements View.OnClickListener, BkashStatusContract.View{
    private TextView transactionTV;
    private TextView quantityTV;
    private TextView totalTV;
    private TextView statusTV;
    private Button okBtn;
    private ProgressBar progressBar;
    private BkashStatusPresenter presenter;
    String trnsactionId;
    public static final String BKASH_TRANSACTION_ID = "bkash_transaction_id";

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_bkash_status);
        findViewById(R.id.okBtn).setOnClickListener(this);
        trnsactionId = getIntent().getStringExtra("trxId");
        CoreLibrary.getInstance().context().allSharedPreferences().savePreference(BKASH_TRANSACTION_ID,trnsactionId);
        transactionTV = findViewById(R.id.trnsactionTV);
        quantityTV = findViewById(R.id.qtyTV);
        totalTV = findViewById(R.id.totalTV);
        statusTV = findViewById(R.id.statusTV);
        progressBar = findViewById(R.id.progress_bar);
        initializePresenter();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.okBtn:
                Intent intent = new Intent(BkashStatusActivity.this,DFSActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }


    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void updateView() {
        ArrayList<BkashStatus> bkashStatusArrayList =  presenter.getBkashStatusData();
        if(bkashStatusArrayList.size()>0){
            BkashStatus bkashStatus = bkashStatusArrayList.get(0);
            transactionTV.setText(bkashStatus.getTrxId()+"");
            quantityTV.setText(bkashStatus.getQty()+"");
            totalTV.setText(bkashStatus.getTotalAmount()+"");
            statusTV.setText(bkashStatus.getStatus()+"");

        }
    }

    @Override
    public void initializePresenter() {
        presenter = new BkashStatusPresenter(this);
        presenter.fetchBkashStatus();
    }

    @Override
    public BkashStatusContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onResumption() {

    }
    @Override
    public Context getContext() {
        return this;
    }


}