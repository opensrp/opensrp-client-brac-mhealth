package org.smartregister.brac.hnpp.activity;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationSearchDetailsActivity extends SecuredActivity implements View.OnClickListener {

    protected RecyclerView recyclerView;
    private ProgressBar progressBar;

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
    }
    @Override
    public void onClick(View v) {

    }
    @Override
    protected void onResumption() {

    }
}