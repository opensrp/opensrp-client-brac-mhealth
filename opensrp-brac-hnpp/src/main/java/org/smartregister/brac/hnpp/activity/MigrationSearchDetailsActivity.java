package org.smartregister.brac.hnpp.activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.adapter.NotificationAdapter;
import org.smartregister.brac.hnpp.adapter.SearchMigrationAdapter;
import org.smartregister.brac.hnpp.contract.SearchDetailsContract;
import org.smartregister.brac.hnpp.holder.SearchMigrationViewHolder;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.presenter.SearchDetailsPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.SecuredActivity;

public class MigrationSearchDetailsActivity extends SecuredActivity implements View.OnClickListener, SearchDetailsContract.View {
    public static final String EXTRA_SEARCH_CONTENT = "extra_search_content";

    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private SearchDetailsPresenter presenter;
    private SearchMigrationAdapter adapter;

    private MigrationSearchContentData migrationSearchContentData;

    public static void startMigrationSearchActivity(Activity activity, MigrationSearchContentData migrationSearchContentData){
        Intent intent = new Intent(activity,MigrationSearchDetailsActivity.class);
        intent.putExtra(EXTRA_SEARCH_CONTENT,migrationSearchContentData);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_migration_search_details);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        titleTextView = findViewById(R.id.textview_detail_two);
        findViewById(R.id.search_migration).setOnClickListener(this);
        findViewById(R.id.sort_btn).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);

        migrationSearchContentData =(MigrationSearchContentData) getIntent().getSerializableExtra(EXTRA_SEARCH_CONTENT);

        presenter = new SearchDetailsPresenter(this);
        if(migrationSearchContentData !=null){
            if(migrationSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name())){
                titleTextView.setText(getString(R.string.member_migration));
            }else {
                titleTextView.setText(getString(R.string.hh_migration));
            }
            presenter.fetchData(migrationSearchContentData.getMigrationType(),migrationSearchContentData.getVillageId(), migrationSearchContentData.getGender(), migrationSearchContentData.getAge());
        }

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
    public SearchDetailsContract.Presenter getPresenter() {
        return presenter;
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
            public void onItemClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Migration content) {
                content.cityVillage = content.addresses.get(0).getCityVillage();
                showDetailsDialog(content);
            }

            @Override
            public void onClick(SearchMigrationViewHolder viewHolder, int adapterPosition, Migration content) {
                PopupMenu popup = new PopupMenu(getContext(), viewHolder.imageViewMenu);
                popup.inflate(R.menu.popup_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.migration_menu:
                                migrationSearchContentData.setBaseEntityId(content.baseEntityId);
                                openFamilyListActivity();
                                return true;
                            case R.id.migration_details:
                                content.cityVillage = content.addresses.get(0).getCityVillage();

                                showDetailsDialog(content);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                //displaying the popup
                popup.show();
            }

        });
        adapter.setData(presenter.getMemberList());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
    }
    private void openFamilyListActivity(){
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(MigrationSearchDetailsActivity.EXTRA_SEARCH_CONTENT,migrationSearchContentData);
        startActivity(intent);

    }

    @Override
    public Context getContext() {
        return this;
    }

    private void showDetailsDialog(Migration content){
        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.migration_member_details_dialog);
        TextView textViewName = dialog.findViewById(R.id.name_TV);
        TextView textViewAge = dialog.findViewById(R.id.age_TV);
        TextView textViewGender = dialog.findViewById(R.id.gender_TV);
        TextView textViewVillage = dialog.findViewById(R.id.village_TV);
        textViewName.setText(content.firstName);
        textViewAge.setText( Utils.getDuration(content.birthdate));
        textViewGender.setText(content.gender);
        textViewVillage.setText(content.cityVillage);
        dialog.findViewById(R.id.cross_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.migration_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                migrationSearchContentData.setBaseEntityId(content.baseEntityId);
                openFamilyListActivity();
            }
        });
        dialog.show();

    }

}