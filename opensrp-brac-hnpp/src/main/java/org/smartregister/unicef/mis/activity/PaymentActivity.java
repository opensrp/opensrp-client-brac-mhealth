package org.smartregister.unicef.mis.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.PaymentAdapter;
import org.smartregister.unicef.mis.contract.PaymentContract;
import org.smartregister.unicef.mis.interactor.PaymentDetailsInteractor;
import org.smartregister.unicef.mis.job.HnppSyncIntentServiceJob;
import org.smartregister.unicef.mis.model.Payment;
import org.smartregister.unicef.mis.presenter.PaymentPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class PaymentActivity extends SecuredActivity implements View.OnClickListener, PaymentContract.View, SyncStatusBroadcastReceiver.SyncStatusListener {


    protected RecyclerView recyclerView;
    private TextView totalPriceTV;
    private TextView totalPriceTVGiven;
    private ProgressBar progressBar;
    private PaymentAdapter adapter;
    private PaymentPresenter presenter;
    private int totalPayable;
    private ArrayList<Payment> payments = new ArrayList<>();
    private Button confirmBtn;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.backBtn).setOnClickListener(this);
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        confirmBtn =findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        totalPriceTV = findViewById(R.id.total_price);
        totalPriceTVGiven = findViewById(R.id.total_given);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        totalPayable = 0;
        initializePresenter();
        showSyncDataDialog();


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
        if(payments !=null) payments.clear();
        adapter = new PaymentAdapter(this, new PaymentAdapter.OnClickAdapter() {
            @Override
            public void onClickItem(int position) {
                adapter.notifyItemChanged(position);
//                recyclerView.postDelayed(new Runnable()
//                {
//                    @Override
//                    public void run() {
//                        adapter.notifyItemChanged(position);
//                    }
//                },500);
                payments = adapter.getPaymentWithoutZero();
                totalPayable = adapter.getTotalPayableAmount();
                Log.v("TOTAL_PAY","totalPayable:"+totalPayable+":payments:"+payments);

                totalPriceTV.setText(totalPayable+"");
            }
        });
        payments = presenter.getPaymentData();
        adapter.setData(payments);
        recyclerView.setAdapter(adapter);
        totalPriceTVGiven.setText(payments.size() > 0 ? payments.get(payments.size() - 1).getTotalInitialAmount() + "" : 0 + "");
        totalPriceTV.setText(totalPriceTVGiven.getText().toString()+"");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void initializePresenter() {
        presenter = new PaymentPresenter(this);

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
        presenter.fetchPaymentService();
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
        switch (v.getId()) {
            case R.id.backBtn:
                finish();
                break;
            case R.id.cancel_btn:
                finish();
                break;
            case R.id.confirm_btn:
                showDetailsDialog();
                break;
        }
    }
    Dialog removeDialog;
    Button syncBtn;
    private void showSyncDataDialog() {
        removeDialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        removeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        removeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
        removeDialog.setContentView(R.layout.dialog_sync_data);
        syncBtn = removeDialog.findViewById(R.id.sync_data_btn);
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(PaymentActivity.this);
//                HnppHomeVisitServiceJob.scheduleJobImmediately(HnppHomeVisitServiceJob.TAG);
                HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                showProgressDialog(getString(R.string.syncing));
                syncBtn.setEnabled(false);
                removeDialog.dismiss();
            }
        });

        removeDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    removeDialog.dismiss();
                }
                return true;
            }
        });
        removeDialog.show();
    }

    private ProgressDialog dialog;

    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        if(removeDialog !=null){
            syncBtn.setEnabled(true);
            removeDialog.dismiss();
        }
    }

    private void showDetailsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.payment_dialog);
        TextView totalTV = dialog.findViewById(R.id.totalTV);
        TextView givenTV = dialog.findViewById(R.id.givenTV);
        TextView remainTV = dialog.findViewById(R.id.remainTV);

        totalTV.setText(totalPriceTVGiven.getText().toString() + " " + "Taka");
        remainTV.setText((Integer.valueOf(totalPriceTVGiven.getText().toString()) - (Integer.valueOf(totalPriceTV.getText().toString()))) + " " + "Taka");
        givenTV.setText(totalPriceTV.getText().toString()+"" + " " + "Taka");

        dialog.findViewById(R.id.send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    String amount = String.valueOf(paymentET.getText());
                Intent intent = new Intent(PaymentActivity.this, BkashActivity.class);
                intent.putExtra("AMOUNT",amount);  //sent amount to bkash activity
                startActivity(intent);
                finish();*/

                HnppConstants.showTermConditionDialog(PaymentActivity.this, getString(R.string.dialog_title_payment), "", new Runnable() {
                    @Override
                    public void run() {
                        payments = adapter.getPaymentWithoutZero();
                        showProgressDialog("Please wait");
                        new PaymentDetailsInteractor(new AppExecutors()).paymentDetailsPost(payments, Integer.valueOf(totalPriceTV.getText().toString()), new PaymentContract.PaymentPostInteractorCallBack() {

                            @Override
                            public void onSuccess(ArrayList<String> responses) {
                                hideProgressDialog();
                                //Toast.makeText(PaymentActivity.this, "Successfully posted,Payment data", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PaymentActivity.this,BkashActivity.class);
                                intent.putExtra("url",responses.get(0));
                                intent.putExtra("trxId",responses.get(1));
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onFail(String message) {
                                hideProgressDialog();
                                Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onSuccess(String message) {
                                hideProgressDialog();
                            }
                        });

                    }
                });

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);

    }
}