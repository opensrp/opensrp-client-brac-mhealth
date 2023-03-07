package org.smartregister.unicef.dghs.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.adapter.GuestMemberAdapter;
import org.smartregister.unicef.dghs.contract.GuestMemberContract;
import org.smartregister.unicef.dghs.listener.OnPostDataWithGps;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.unicef.dghs.presenter.GuestMemberPresenter;
import org.smartregister.unicef.dghs.utils.GuestMemberData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.OnDialogOptionSelect;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.BaseProfileActivity;

import java.util.ArrayList;

import static com.vijay.jsonwizard.constants.JsonFormConstants.FIELDS;

public class GuestMemberActivity extends BaseProfileActivity implements GuestMemberContract.View, View.OnClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GuestMemberAdapter adapter;
    private Spinner ssSpinner;
    private GuestMemberPresenter presenter;
    private String ssName="";
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


    }

    private void filterData() {
        presenter.filterData(query,ssName);

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
                HnppConstants.getGPSLocation(this, new OnPostDataWithGps() {
                    @Override
                    public void onPost(double latitude, double longitude) {
                        try{
                            Intent intent = new Intent(GuestMemberActivity.this, GuestAddMemberJsonFormActivity.class);
                            JSONObject jsonForm = FormUtils.getInstance(GuestMemberActivity.this).getFormJson(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
                            HnppJsonFormUtils.updateFormWithWardName(jsonForm, GeoLocationHelper.getInstance().getWardList());
                            HnppJsonFormUtils.updateLatitudeLongitude(jsonForm,latitude,longitude,"");
                            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
                            Form form = new Form();
                            form.setWizard(false);
                            if(!HnppConstants.isReleaseBuild()){
                                form.setActionBarBackground(R.color.test_app_color);

                            }else{
                                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

                            }

                            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

                            startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String[] generatedString;
                String title;
                String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();

                String fullName = HnppApplication.getInstance().getContext().allSharedPreferences().getANMPreferredName(userName);

                generatedString = HnppJsonFormUtils.getValuesFromGuestRegistrationForm(form);
                title = String.format(getString(R.string.dialog_confirm_save_guest),fullName,generatedString[0],generatedString[1]);


                HnppConstants.showSaveFormConfirmationDialog(this, title, new OnDialogOptionSelect() {
                    @Override
                    public void onClickYesButton() {
                       try{
                           showProgressBar();
                           JSONObject formWithConsent = new JSONObject(jsonString);
                           JSONObject jobkect = formWithConsent.getJSONObject("step1");
                           JSONArray field = jobkect.getJSONArray(FIELDS);
                           HnppJsonFormUtils.addConsent(field,true);
                           presenter.saveMember(formWithConsent.toString());
                       }catch (JSONException je){

                       }

                    }

                    @Override
                    public void onClickNoButton() {
                        try{
                            showProgressBar();
                            JSONObject formWithConsent = new JSONObject(jsonString);
                            JSONObject jobkect = formWithConsent.getJSONObject("step1");
                            JSONArray field = jobkect.getJSONArray(FIELDS);
                            HnppJsonFormUtils.addConsent(field,false);
                            presenter.saveMember(formWithConsent.toString());
                        }catch (JSONException je){

                        }
                    }
                });

            }catch (JSONException e){

            }
        }
    }

    private void openProfile(String baseEntityId){

        GuestMemberProfileActivity.startGuestMemberProfileActivity(this,baseEntityId);

    }
}
