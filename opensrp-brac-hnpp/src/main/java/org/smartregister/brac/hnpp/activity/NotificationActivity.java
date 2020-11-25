package org.smartregister.brac.hnpp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.NotificationAdapter;
import org.smartregister.brac.hnpp.adapter.SearchHHMemberAdapter;
import org.smartregister.brac.hnpp.contract.NotificationContract;
import org.smartregister.brac.hnpp.model.HHMemberProperty;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.presenter.NotificationPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

import static org.smartregister.brac.hnpp.activity.SearchHHMemberActivity.EXTRA_INTENT_DATA;

public class NotificationActivity extends SecuredActivity implements View.OnClickListener, NotificationContract.View {

    protected RecyclerView recyclerView;
    private NotificationPresenter presenter;
    private ProgressBar progressBar;
    private NotificationAdapter adapter;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_notification);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.search_notification).setOnClickListener(this);
        findViewById(R.id.sort_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        presenter = new NotificationPresenter(this);
        presenter.fetchNotification();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
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
    public void updateAdapter() {
        adapter = new NotificationAdapter(this, new NotificationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Notification content) {
                showDetailsDialog(content);
            }
        });
        adapter.setData(presenter.getNotificationArrayList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onResumption() {

    }
    private void showDetailsDialog(Notification notification){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.notification_details_dialog);
        TextView textViewTitle = dialog.findViewById(R.id.title_txt);
        TextView textViewDate = dialog.findViewById(R.id.date_txt);
        TextView textViewDetails = dialog.findViewById(R.id.details_txt);
        textViewTitle.setText(notification.getTitle());
        textViewDate.setText(notification.getSendDate());
        textViewDetails.setText(notification.getDetails());
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}