package org.smartregister.unicef.mis.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.adapter.PaymentHistoryAdapter;
import org.smartregister.unicef.mis.contract.PaymentHistoryContract;
import org.smartregister.unicef.mis.model.PaymentHistory;
import org.smartregister.unicef.mis.presenter.PaymentHistoryPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class PaymentHistoryActivity extends SecuredActivity implements View.OnClickListener,PaymentHistoryContract.View,  SyncStatusBroadcastReceiver.SyncStatusListener {

    private ProgressDialog dialog;
    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PaymentHistoryAdapter paymentHistoryadapter;
    private PaymentHistoryPresenter presenter;
    private ArrayList<PaymentHistory> paymentHistoryList;
    private Button fromDateBtn,toDateBtn;
    private TextView totalPaymentTxt;
    protected int fromDay, fromMonth, fromYear, toDay, toMonth, toYear;
    private String fromDate, toDate;
    Calendar calendar;
    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_payment_history);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar_payment_history));
        recyclerView = findViewById(R.id.recycler_view_history);
        findViewById(R.id.refreshIndicatorsIcon).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar_pmnt_hstr);
        findViewById(R.id.back_btn_hstry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initDatePicker();
        initializePresenter();
    }
    private void initDatePicker(){
        fromDateBtn = findViewById(R.id.from);
        totalPaymentTxt = findViewById(R.id.total_given);
        toDateBtn = findViewById(R.id.to);
        fromDateBtn.setOnClickListener(this);
        toDateBtn.setOnClickListener(this);
        findViewById(R.id.clear_filter).setOnClickListener(this);
        LocalDate currentDateMinus6Months = LocalDate.now().minusMonths(6);

        fromYear = currentDateMinus6Months.getYear();
        fromMonth = currentDateMinus6Months.getMonthOfYear();
        fromDay = currentDateMinus6Months.getDayOfMonth();
        fromDate = fromYear +"-"+HnppConstants.addZeroForMonth(fromMonth+"")+"-"+ HnppConstants.addZeroForMonth(fromDay+"");
        calendar = Calendar.getInstance();
        toYear = calendar.get(Calendar.YEAR);
        toMonth = calendar.get(Calendar.MONTH)+1;
        toDay = calendar.get(Calendar.DAY_OF_MONTH);
        toDate   = toYear+"-"+HnppConstants.addZeroForMonth(toMonth+"")+"-"+ HnppConstants.addZeroForMonth(toDay+"");
        fromDateBtn.setText(fromDate);
        toDateBtn.setText(toDate);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refreshIndicatorsIcon:
                presenter.fetchAllData();
                break;
            case R.id.clear_filter:
                fromDateBtn.setText(getString(R.string.all_text));
                toDateBtn.setText(getString(R.string.all_text));

                presenter.fetchLocalData();
                break;
            case R.id.from:

                DatePickerDialog fromDateDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        fromDay = dayOfMonth;
                        fromMonth = mnt +1;
                        fromYear = yr;

                        fromDate = fromYear+"-"+HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+ HnppConstants.addZeroForMonth(dayOfMonth+"") ;

                        fromDateBtn.setText(fromDate);
                        filterByFromToDate();
                    }
                },fromYear,(fromMonth-1),fromDay);
                LocalDate currentDate = LocalDate.now();
                LocalDate currentDateMinus6Months = currentDate.minusMonths(6);

                fromDateDialog.getDatePicker().setMinDate(currentDateMinus6Months.toDate().getTime());
                fromDateDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
                fromDateDialog.show();
                break;
            case R.id.to:
                DatePickerDialog toDateDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        toDay = dayOfMonth;
                        toMonth = mnt +1;
                        toYear = yr;

                        toDate = toYear + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        toDateBtn.setText(toDate);
                        filterByFromToDate();
                    }
                },toYear,(toMonth-1),toDay);
                toDateDialog.getDatePicker().setMaxDate(calendar.getTime().getTime());
                toDateDialog.getDatePicker().setMinDate(LocalDate.now().minusMonths(6).toDate().getTime());
                toDateDialog.show();
                break;
        }
    }
    public void filterByFromToDate() {
        presenter.filterByFromToDate(fromDate,toDate);
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
        paymentHistoryadapter = new PaymentHistoryAdapter(this);
        paymentHistoryList = presenter.getPaymentData();
        paymentHistoryadapter.setData(paymentHistoryList);
        recyclerView.setAdapter(paymentHistoryadapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        paymentHistoryadapter.notifyDataSetChanged();
        totalPaymentTxt.setText(presenter.getTotalPayment()+"");
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


}