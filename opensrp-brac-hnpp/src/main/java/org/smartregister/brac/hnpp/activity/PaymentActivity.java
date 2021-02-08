package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.NotificationAdapter;
import org.smartregister.brac.hnpp.adapter.PaymentAdapter;
import org.smartregister.brac.hnpp.model.Payment;
import org.smartregister.brac.hnpp.presenter.NotificationPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class PaymentActivity extends SecuredActivity implements View.OnClickListener{
   protected RecyclerView recyclerView;
   private static TextView totalPriceTV;
   private ProgressBar progressBar;
   private PaymentAdapter adapter;
   ArrayList<Payment> paymentArrayList;
   Payment payment,payment2,payment3;
   static int totalPayment;

   public interface listener {
      void addsum( int amount);
   }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        totalPriceTV = findViewById(R.id.total_price);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        payment = new Payment();
        payment2 = new Payment();
        payment3 = new Payment();
        adapter = new PaymentAdapter(this);
        paymentArrayList = new ArrayList<>();

        payment.setPackageName("IYCF");
        payment.setUnitPrice(150);
        payment.setQuantity(4);

        payment2.setPackageName("ANC");
        payment2.setUnitPrice(200);
        payment2.setQuantity(2);

        payment3.setPackageName("PNC");
        payment3.setUnitPrice(200);
        payment3.setQuantity(4);

        paymentArrayList.add(payment);
        paymentArrayList.add(payment2);
        paymentArrayList.add(payment3);


        adapter.setData(paymentArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        adapter.setListener(new listener() {
            @Override
            public void addsum( int amount) {
                totalPriceTV.setText(amount+"");
            }
        });
    }

    public static void addTotalPayment(int total){
        totalPayment = totalPayment + total;
        totalPriceTV.setText(totalPayment+"");
    }
    private void setupView() {
       /* ancButton  = findViewById(R.id.ancID);
        pncButton = findViewById(R.id.pncID);
        womenButton = findViewById(R.id.womenID);
        iycfButton = findViewById(R.id.iycfID);
        ncdButton = findViewById(R.id.ncdID);
        adolescatButton = findViewById(R.id.adolescatID);
        eyetestButton = findViewById(R.id.eyetestID);
        bloodgroupButton = findViewById(R.id.eyetestID);

        if(HnppConstants.isPALogin()){
            findViewById(R.id.eyetestID).setVisibility(View.VISIBLE);
            findViewById(R.id.bloodgroupID).setVisibility(View.VISIBLE);
            findViewById(R.id.adolescatID).setVisibility(View.GONE);
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
        bloodgroupButton.setOnClickListener(this);*/
    }

    @Override
    protected void onResumption() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
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