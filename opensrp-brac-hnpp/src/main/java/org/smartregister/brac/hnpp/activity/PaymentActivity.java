package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class PaymentActivity extends SecuredActivity implements View.OnClickListener{
    private Button ancButton;
    private Button pncButton;
    private Button womenButton;
    private Button iycfButton;
    private Button ncdButton;
    private Button adolescatButton;
    private Button eyetestButton;
    private Button bloodgroupButton;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment);
        setupView();
    }

    private void setupView() {
        ancButton  = findViewById(R.id.ancID);
        pncButton = findViewById(R.id.pncID);
        womenButton = findViewById(R.id.womenID);
        iycfButton = findViewById(R.id.iycfID);
        ncdButton = findViewById(R.id.ncdID);
        adolescatButton = findViewById(R.id.adolescatID);
        eyetestButton = findViewById(R.id.eyetestID);
        bloodgroupButton = findViewById(R.id.eyetestID);

        if(HnppConstants.isPALogin()){
            findViewById(R.id.eyetestID).setVisibility(View.VISIBLE);
            findViewById(R.id.eyetestID).setVisibility(View.VISIBLE);
            findViewById(R.id.ancID).setVisibility(View.GONE);
            findViewById(R.id.pncID).setVisibility(View.GONE);
            findViewById(R.id.womenID).setVisibility(View.GONE);
            findViewById(R.id.iycfID).setVisibility(View.GONE);
        }

        ancButton.setOnClickListener(this);
        pncButton.setOnClickListener(this);
        womenButton.setOnClickListener(this);
        iycfButton.setOnClickListener(this);
        ncdButton.setOnClickListener(this);
        adolescatButton.setOnClickListener(this);
        eyetestButton.setOnClickListener(this);
        bloodgroupButton.setOnClickListener(this);
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ancID:
                showDetailsDialog();
                break;
            case R.id.pncID:
                showDetailsDialog();
                break;
            case R.id.womenID:
                showDetailsDialog();
                break;
            case R.id.iycfID:
                showDetailsDialog();
                break;
            case R.id.ncdID:
                showDetailsDialog();
                break;
            case R.id.adolescatID:
                showDetailsDialog();
                break;
            case R.id.eyetestID:
                showDetailsDialog();
                break;
            case R.id.bloodgroupID:
                showDetailsDialog();
                break;
        }
    }

    private void showDetailsDialog(){
        Intent intent = new Intent(PaymentActivity.this, BkashActivity.class);
        startActivity(intent);
        finish();
//        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.payment_dialog);
//        EditText paymentET = dialog.findViewById(R.id.payment_et);
//        dialog.findViewById(R.id.payment_Btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String amount = String.valueOf(paymentET.getText());
//                Intent intent = new Intent(PaymentActivity.this, BkashActivity.class);
//                intent.putExtra("AMOUNT",amount);  //sent amount to bkash activity
//                startActivity(intent);
//                finish();
//            }
//        });
//        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//        dialog.show();

    }
}