package org.smartregister.brac.hnpp.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import org.joda.time.LocalDate;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class PaymentHistoryActivity extends SecuredActivity implements View.OnClickListener,PaymentHistoryContract.View,  SyncStatusBroadcastReceiver.SyncStatusListener {

    private ProgressDialog dialog;
    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PaymentHistoryAdapter paymentHistoryadapter;
    private PaymentHistoryPresenter presenter;
    private ArrayList<PaymentHistory> paymentHistoryList;
    private Button fromDateBtn,toDateBtn;
    protected int day, month, year, fromDay, fromMonth, fromYear, toDay, toMonth, toYear;
    private String fromDate, toDate, currentDate;
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
        toDateBtn = findViewById(R.id.to);
        fromDateBtn.setOnClickListener(this);
        toDateBtn.setOnClickListener(this);
        findViewById(R.id.clear_filter).setOnClickListener(this);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH)+1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        currentDate   = year+"-"+ HnppConstants.addZeroForMonth(month+"")+"-"+HnppConstants.addZeroForMonth(day+"");
        fromDate = currentDate;
        toDate = currentDate;
        fromDateBtn.setText(getString(R.string.all_text));
        toDateBtn.setText(getString(R.string.all_text));
        month = -1;
        year = -1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.refreshIndicatorsIcon:
                presenter.fetchPaymentService();
                break;
            case R.id.clear_filter:
                fromDateBtn.setText(getString(R.string.all_text));
                toDateBtn.setText(getString(R.string.all_text));
                month = -1;
                year = -1;
                presenter.fetchLocalData();
                break;
            case R.id.from:
                if(fromMonth == -1) fromMonth = calendar.get(Calendar.MONTH)+1;
                if(fromYear == -1) fromYear = calendar.get(Calendar.YEAR);

                DatePickerDialog fromDateDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        fromDay = dayOfMonth;
                        fromMonth = mnt +1;
                        fromYear = yr;

                        fromDate = fromYear + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        fromDateBtn.setText(fromDate);
                        updateFromFilter();
                        filterByFromToDate();
                    }
                },year,(month-1),day);
                LocalDate currentDate = LocalDate.now();
                LocalDate currentDateMinus6Months = currentDate.minusMonths(6);

                fromDateDialog.getDatePicker().setMaxDate(currentDateMinus6Months.toDate().getTime());
                fromDateDialog.show();
                break;
            case R.id.to:
                if(toMonth == -1) toMonth = calendar.get(Calendar.MONTH)+1;
                if(toYear == -1) toYear = calendar.get(Calendar.YEAR);

                DatePickerDialog toDateDialog = new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int yr, int mnt, int dayOfMonth) {

                        toDay = dayOfMonth;
                        toMonth = mnt +1;
                        toYear = yr;

                        toDate = toYear + "-" + HnppConstants.addZeroForMonth((mnt+1)+"")+"-"+HnppConstants.addZeroForMonth(dayOfMonth+"");

                        toDateBtn.setText(toDate);
                        updateToFilter();
                        filterByFromToDate();
                    }
                },year,(month-1),day);
                LocalDate cDate = LocalDate.now();
                LocalDate localDates = cDate.minusMonths(6);

                toDateDialog.getDatePicker().setMaxDate(localDates.toDate().getTime());
                toDateDialog.show();
                break;
        }
    }
    public void filterByFromToDate() {
        String fromDateFormat = fromYear+"-"+fromMonth+"-"+fromDay;
        String toDateFormat = toYear+"-"+toMonth+"-"+toDay;
        presenter.filterByFromToDate(fromDateFormat,toDateFormat);
    }
    private void updateFromFilter() {
        if (TextUtils.isEmpty(fromDateBtn.getText().toString())) {
            toDate = currentDate;
            fromDateBtn.setText(toDate+"");
        }
    }
    private void updateToFilter() {
        if (TextUtils.isEmpty(toDateBtn.getText().toString())) {
            toDate = currentDate;
            toDateBtn.setText(toDate+"");
        }
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


}