package org.smartregister.brac.hnpp.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.adapter.NotificationAdapter;
import org.smartregister.brac.hnpp.adapter.SearchMigrationAdapter;
import org.smartregister.brac.hnpp.contract.SearchDetailsContract;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.presenter.SearchDetailsPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationSearchDetailsActivity extends SecuredActivity implements View.OnClickListener, SearchDetailsContract.View {
    private static final String EXTRA_VILLAGE_ID = "extra_village_id";
    private static final String EXTRA_GENDER= "gender";
    private static final String EXTRA_AGE = "age";

    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SearchDetailsPresenter presenter;
    private SearchMigrationAdapter adapter;

    private String villageId,gender,age;

    public static void startMigrationSearchActivity(Activity activity, String villageId, String gender, String age){
        Intent intent = new Intent(activity,MigrationSearchDetailsActivity.class);
        intent.putExtra(EXTRA_VILLAGE_ID,villageId);
        intent.putExtra(EXTRA_GENDER,gender);
        intent.putExtra(EXTRA_AGE,age);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_migration_search_details);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        findViewById(R.id.search_migration).setOnClickListener(this);
        findViewById(R.id.sort_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);
        presenter = new SearchDetailsPresenter(this);
        presenter.fetchData();
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
        adapter = new SearchMigrationAdapter(this, new SearchMigrationAdapter.OnClickAdapter() {
            @Override
            public void onClick(int position, Migration content) {

            }
        });
        adapter.setData(presenter.getMemberList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }

    @Override
    public Context getContext() {
        return this;
    }

}