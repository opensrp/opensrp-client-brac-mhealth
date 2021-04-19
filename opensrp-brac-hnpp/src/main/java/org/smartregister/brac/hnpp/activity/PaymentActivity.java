package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.PaymentAdapter;
import org.smartregister.brac.hnpp.contract.PaymentContract;
import org.smartregister.brac.hnpp.interactor.PaymentDetailsInteractor;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.presenter.PaymentPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.PaymentDetails;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class PaymentActivity extends SecuredActivity implements View.OnClickListener, PaymentContract.View, SyncStatusBroadcastReceiver.SyncStatusListener{
   protected RecyclerView recyclerView;
   private static TextView totalPriceTV;
   private static TextView totalPriceTVGiven;
   private ProgressBar progressBar;
   private PaymentAdapter adapter;
   private PaymentPresenter presenter;
   static int totalPayment;
   static int givenPayment;
   static ArrayList<PaymentDetails> paymentDetails;;



    public interface listener {
      void addsum( int amount);
      void addsumpay(int amount);
      void getPaymentDetailsObject(ArrayList<PaymentDetails> paymentDetailsArrayList);
   }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.confirm_btn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        totalPriceTV = findViewById(R.id.total_price);
        totalPriceTVGiven = findViewById(R.id.total_given);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        paymentDetails = new ArrayList<>();
        initializePresenter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isOnline()) {
                showSyncDataDialog();
                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
            }
            else
            {
                checkNetworkConnection();
            }
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
    public void updateAdapter() {
        adapter = new PaymentAdapter(this);
        adapter.setData(presenter.getPaymentData());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        adapter.setListener(new listener() {
            @Override
            public void addsum( int amount) {
                totalPayment = amount;
                totalPriceTVGiven.setText(amount+"");
            }

            @Override
            public void addsumpay(int amount) {
                givenPayment = amount;
                totalPriceTV.setText(amount+"");
            }

            @Override
            public void getPaymentDetailsObject(ArrayList<PaymentDetails> paymentDetailsArrayList) {
                paymentDetails = paymentDetailsArrayList;
            }
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    public void initializePresenter() {
        presenter = new PaymentPresenter(this);
        presenter.fetchPaymentService();
    }

    @Override
    public void onSyncStart() {
        showProgressDialog(getString(R.string.syncing));
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {

    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        hideProgressDialog();
    }
    @Override
    public PaymentContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Context getContext() {
        return this;
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
            case R.id.cancel_btn:
                finish();
                break;
            case R.id.confirm_btn:
                showDetailsDialog(totalPayment,givenPayment,paymentDetails);
                break;
        }
    }
    private void showSyncDataDialog(){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
        dialog.setContentView(R.layout.dialog_sync_data);
        dialog.findViewById(R.id.sync_data_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                showProgressDialog(getString(R.string.syncing));
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private ProgressDialog dialog;
    private void showProgressDialog(String text){
        if(dialog == null){
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }
    private void hideProgressDialog(){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
    private void showDetailsDialog(int totallAmount, int givenAmount, ArrayList<PaymentDetails> paymentDetailsArrayList){
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.payment_dialog);
        int remainAmount = totallAmount - givenAmount;
        TextView totalTV = dialog.findViewById(R.id.totalTV);
        TextView givenTV = dialog.findViewById(R.id.givenTV);
        TextView remainTV = dialog.findViewById(R.id.remainTV);

        totalTV.setText(totallAmount+" "+"Taka");
        remainTV.setText(remainAmount+" "+"Taka");
        givenTV.setText(givenAmount+" "+"Taka");
        dialog.findViewById(R.id.send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    String amount = String.valueOf(paymentET.getText());
                Intent intent = new Intent(PaymentActivity.this, BkashActivity.class);
                intent.putExtra("AMOUNT",amount);  //sent amount to bkash activity
                startActivity(intent);
                finish();*/
                if(paymentDetails != null){
                    HnppConstants.showDialogWithAction(PaymentActivity.this,getString(R.string.dialog_title_payment), "", new Runnable() {
                        @Override
                        public void run() {
                            new PaymentDetailsInteractor(new AppExecutors()).paymentDetailsPost(paymentDetailsArrayList, givenAmount, new PaymentContract.PaymentPostInteractorCallBack() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(PaymentActivity.this,"Successfully posted,Payment data",Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFail() {
                                    Toast.makeText(PaymentActivity.this,"Fail to post",Toast.LENGTH_SHORT).show();


                                }
                            });

                        }
                    });
                }
            }
        });
        dialog.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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