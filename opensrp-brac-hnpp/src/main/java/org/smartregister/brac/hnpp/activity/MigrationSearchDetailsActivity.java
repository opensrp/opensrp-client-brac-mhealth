package org.smartregister.brac.hnpp.activity;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.adapter.NotificationAdapter;
import org.smartregister.brac.hnpp.adapter.SearchMigrationAdapter;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.contract.SearchDetailsContract;
import org.smartregister.brac.hnpp.holder.SearchMigrationViewHolder;
import org.smartregister.brac.hnpp.interactor.MigrationInteractor;
import org.smartregister.brac.hnpp.job.HnppSyncIntentServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.presenter.SearchDetailsPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;

public class MigrationSearchDetailsActivity extends SecuredActivity implements View.OnClickListener, SearchDetailsContract.View {
    public static final String EXTRA_SEARCH_CONTENT = "extra_search_content";

    protected RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView titleTextView;
    private EditText editTextSearch;
    ImageView crossBtn;
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
        editTextSearch = findViewById(R.id.search_edit_text);
        crossBtn = findViewById(R.id.cross_btn);
        crossBtn.setOnClickListener(this);
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
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(s.toString())){
                    crossBtn.setVisibility(View.VISIBLE);
                }else{
                    crossBtn.setVisibility(View.GONE);
                }
                presenter.search(s.toString());


            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.cross_btn:
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
        if(migrationSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())){
            showSSDialog();
            return;
        }
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
        TextView textViewPhoneNo = dialog.findViewById(R.id.phone_no_TV);
        if(migrationSearchContentData.getMigrationType().equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.Member.name())){
            textViewName.setText(this.getString(R.string.name,content.firstName));
            textViewAge.setText( Utils.getDuration(content.birthdate));
            textViewGender.setText(content.gender);
            textViewVillage.setText(content.cityVillage);
            if(!TextUtils.isEmpty(content.attributes.Mobile_Number)){
                textViewPhoneNo.setVisibility(View.VISIBLE);
                textViewPhoneNo.setText(this.getString(R.string.phone_no,content.attributes.Mobile_Number));
            }
        }else {
            TextView age = dialog.findViewById(R.id.age_tv);
            TextView gender = dialog.findViewById(R.id.gender_tv);
            age.setText("");
            gender.setText("");
            textViewName.setText(this.getString(R.string.house_hold_head_name,content.firstName));
            textViewAge.setText(this.getString(R.string.ss_name,content.attributes.SS_Name));
            textViewGender.setText(this.getString(R.string.member_count,content.attributes.Number_of_HH_Member));
            if(!TextUtils.isEmpty(content.attributes.HOH_Phone_Number)){
                textViewPhoneNo.setVisibility(View.VISIBLE);
                textViewPhoneNo.setText(this.getString(R.string.phone_no,content.attributes.HOH_Phone_Number));
            }
            textViewVillage.setText(content.cityVillage);
        }

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
    ArrayAdapter<String> villageSpinnerArrayAdapter;
    String mSelectedVillageName,mSelectedSSName,mSelectedVillageId;
    private void showSSDialog(){
        ArrayList<String> ssSpinnerArray = new ArrayList<>();

        ArrayList<String> villageSpinnerArray = new ArrayList<>();
        ArrayList<String> villageIdArray = new ArrayList<>();

        ArrayList<SSModel> ssLocationForms = SSLocationHelper.getInstance().getSsModels();
        for (SSModel ssModel : ssLocationForms) {
            ssSpinnerArray.add(ssModel.username);
        }

        ArrayAdapter<String> ssSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        ssSpinnerArray){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);

                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);

                return convertView;
            }
        };

         villageSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        villageSpinnerArray){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = super.getDropDownView(position, convertView,
                        parent);
                AppCompatTextView appCompatTextView = (AppCompatTextView)convertView;
                appCompatTextView.setGravity(Gravity.CENTER_VERTICAL);
                appCompatTextView.setHeight(100);
                return convertView;
            }
        };


        Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(org.smartregister.family.R.color.customAppThemeBlue)));
        dialog.setContentView(R.layout.dialog_ss_selection);
        Spinner ss_spinner = dialog.findViewById(R.id.ss_filter_spinner);
        Spinner village_spinner = dialog.findViewById(R.id.village_filter_spinner);
        village_spinner.setAdapter(villageSpinnerArrayAdapter);
        ss_spinner.setAdapter(ssSpinnerArrayAdapter);
        ss_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    villageSpinnerArray.clear();
                    villageIdArray.clear();
                    ArrayList<SSLocations> ssLocations = SSLocationHelper.getInstance().getSsModels().get(position).locations;
                    for (SSLocations ssLocations1 : ssLocations) {
                        villageSpinnerArray.add(ssLocations1.village.name.trim());
                        villageIdArray.add(ssLocations1.village.id+"");
                    }
                    villageSpinnerArrayAdapter = new ArrayAdapter<String>
                            (MigrationSearchDetailsActivity.this, android.R.layout.simple_spinner_item,
                                    villageSpinnerArray);
                    village_spinner.setAdapter(villageSpinnerArrayAdapter);
                    mSelectedSSName = ssSpinnerArray.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        village_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    mSelectedVillageName = villageSpinnerArray.get(position);
                    mSelectedVillageId = villageIdArray.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button proceed = dialog.findViewById(R.id.filter_apply_button);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                migrateHH();
                //dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void migrateHH() {
        HnppConstants.showDialogWithAction(this,getString(R.string.dialog_title), "", new Runnable() {
            @Override
            public void run() {
                new MigrationInteractor(new AppExecutors()).migrateHH(migrationSearchContentData, new MigrationContract.MigrationPostInteractorCallBack() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MigrationSearchDetailsActivity.this,"Successfully migrated,Syncing data",Toast.LENGTH_SHORT).show();
                        HnppSyncIntentServiceJob.scheduleJobImmediately(HnppSyncIntentServiceJob.TAG);
                        Intent intent = new Intent(MigrationSearchDetailsActivity.this, FamilyRegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onFail() {
                        Toast.makeText(MigrationSearchDetailsActivity.this,"Fail to migrate",Toast.LENGTH_SHORT).show();


                    }
                });

            }
        });


    }

}