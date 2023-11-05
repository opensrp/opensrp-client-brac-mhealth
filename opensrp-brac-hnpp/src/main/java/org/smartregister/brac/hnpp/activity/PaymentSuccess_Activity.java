package org.smartregister.brac.hnpp.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;

public class PaymentSuccess_Activity extends AppCompatActivity {
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success_);

        tvResult = findViewById(R.id.tvResult);

        if (getIntent().getExtras() == null) {
            tvResult.setText("Failed to get data from bkash");
            return;
        }
        else {
            tvResult.setText(
                    "TransactionID= " + getIntent().getExtras().getString("TRANSACTION_ID") + " \n\n" +
                            "PaidAmount= " + getIntent().getExtras().getString("PAID_AMOUNT") + " \n\n" +
                            "OtherData= " + getIntent().getExtras().getString("PAYMENT_SERIALIZE") + " \n\n"
            );
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(this, PaymentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
