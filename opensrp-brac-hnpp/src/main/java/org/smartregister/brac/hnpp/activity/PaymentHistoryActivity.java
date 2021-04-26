package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.PaymentHistoryAdapter;
import org.smartregister.brac.hnpp.contract.PaymentHistoryContract;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.brac.hnpp.presenter.PaymentHistoryPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class PaymentHistoryActivity extends SecuredActivity implements PaymentHistoryContract.View,  SyncStatusBroadcastReceiver.SyncStatusListener {

    private ProgressDialog dialog;
    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PaymentHistoryAdapter paymentHistoryadapter;
    private PaymentHistoryPresenter presenter;
    private ArrayList<PaymentHistory> paymentHistoryList;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment_history);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_payment_history));
        recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar_pmnt_hstr);
        findViewById(R.id.back_btn_hstry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initializePresenter();
    }

    @Override
    protected void onResumption() {
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
      // todo//
        Log.e(PaymentHistoryActivity.class.getSimpleName(), "updateAdapter called");
        paymentHistoryadapter = new PaymentHistoryAdapter(this);
        paymentHistoryList = presenter.getPaymentData();
        sortListByDate(paymentHistoryList);
        paymentHistoryadapter.setData(paymentHistoryList);
        recyclerView.setAdapter(paymentHistoryadapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        paymentHistoryadapter.notifyDataSetChanged();

    }

    @Override
    public void initializePresenter() {
        presenter = new PaymentHistoryPresenter(this);
        presenter.fetchPaymentService();
    }

    @Override
    public PaymentHistoryContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }
    private void showProgressDialog(String text) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage(text);
            dialog.setCancelable(false);
            dialog.show();
        }

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
    private void hideProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void sortListByDate(ArrayList<PaymentHistory> paymentHistoryList)
    {
        Collections.sort(this.paymentHistoryList, new Comparator<PaymentHistory>() {
            public int compare(PaymentHistory obj1, PaymentHistory obj2) {
                DateFormat dateString = new SimpleDateFormat("MM-dd-yyyy");
                Date dateOne = null, dateTwo = null;
                try {
                    dateOne = dateString.parse(obj1.getPaymentDate());
                    dateTwo = dateString.parse(obj2.getPaymentDate());
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                return dateTwo.compareTo(dateOne);
            }
        });
    }
}