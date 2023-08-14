package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.growthmonitoring.listener.HeightActionListener;
import org.smartregister.growthmonitoring.listener.MUACActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.immunization.listener.ServiceActionListener;
import org.smartregister.immunization.listener.VaccinationActionListener;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.GuestMemberAdapter;
import org.smartregister.unicef.dghs.contract.GuestMemberContract;
import org.smartregister.unicef.dghs.listener.OnPostDataWithGps;
import org.smartregister.unicef.dghs.location.HALocationHelper;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.presenter.GuestMemberPresenter;
import org.smartregister.unicef.dghs.repository.GlobalLocationRepository;
import org.smartregister.unicef.dghs.utils.GuestMemberData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.BaseProfileActivity;

import java.util.ArrayList;

public class GuestMemberActivity extends BaseProfileActivity implements GuestMemberContract.View, View.OnClickListener{

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GuestMemberAdapter adapter;
    private Spinner ssSpinner;
    private GuestMemberPresenter presenter;
    private String query ="";
    private EditText editTextSearch;

    @Override
    protected void initializePresenter() {

    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {

    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_guest_member);
        HnppConstants.updateAppBackground(findViewById(R.id.action_bar));
        ssSpinner = findViewById(R.id.ss_filter_spinner);
        editTextSearch = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.fab_add_member).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        presenter = new GuestMemberPresenter(this);
        findViewById(R.id.global_search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalSearchActivity.startMigrationFilterActivity(GuestMemberActivity.this,HnppConstants.MIGRATION_TYPE.Member.name());

            }
        });
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString();
                filterData();

            }
        });
        presenter.updateSHRIdFromServer();


    }

    private void filterData() {
        presenter.filterData(query,"");

    }

    @Override
    protected void onResumption() {
        presenter.fetchData();
    }
    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void updateAdapter() {
        if(adapter == null){
            adapter = new GuestMemberAdapter(this, new GuestMemberAdapter.OnClickAdapter() {
                @Override
                public void onClick(int position, GuestMemberData content) {
                    openProfile(content.getBaseEntityId());
                }
            });
            adapter.setData(getPresenter().getData());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.setData(getPresenter().getData());
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void updateSuccessfullyFetchMessage() {
        Toast.makeText(this,"Saved successfully",Toast.LENGTH_LONG).show();
        presenter.fetchData();

    }

    @Override
    public GuestMemberContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.fab_add_member:
//                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
//                    @Override
//                    public void onPost(double latitude, double longitude) {
                        try{
                            Intent intent = new Intent(GuestMemberActivity.this, GuestAddMemberJsonFormActivity.class);
                            JSONObject jsonForm = HnppJsonFormUtils.getJsonObject(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
                            JSONArray divJsonArray = new JSONArray();
                            ArrayList<GlobalLocationModel> divModels = HnppApplication.getGlobalLocationRepository().getLocationByTagId(GlobalLocationRepository.LOCATION_TAG.DIVISION.getValue());
                            for (GlobalLocationModel globalLocationModel:divModels){
                                divJsonArray.put(globalLocationModel.name);
                            }
                            HnppJsonFormUtils.updateFormWithDivision(jsonForm, divJsonArray);
                            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                            Form form = new Form();
                            form.setWizard(false);
                            form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                            startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//                    }
//                });


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject formWithConsent = new JSONObject(jsonString);
                HnppJsonFormUtils.setEncounterDateTime(formWithConsent);
                presenter.saveMember(formWithConsent.toString(),false);

            }catch (JSONException e){

            }
        }
    }
    @Override
    public void openProfile(String baseEntityId){

        GuestMemberProfileActivity.startGuestMemberProfileActivity(this,baseEntityId);

    }

}
