package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.view.activity.SecuredActivity;

public class PaymentActivity extends SecuredActivity {
    private Button ancButton;
    private Button pncButton;
    private Button womenButton;
    private Button iycfButton;
    private Button ncdButton;
    private Button adolescatButton;

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

        ancButton.setOnClickListener(view -> {
            Intent intent = new Intent(PaymentActivity.this, BkashActivity.class);
            intent.putExtra("AMOUNT", String.valueOf(100.0));  //sent amount to bkash activity
            startActivity(intent);
        });
    }

    @Override
    protected void onResumption() {

    }
}