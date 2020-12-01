package org.smartregister.brac.hnpp.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.adapter.GuestMemberAdapter;
import org.smartregister.brac.hnpp.contract.GuestMemberContract;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.presenter.GuestMemberPresenter;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.SecuredActivity;

public class GuestMemberActivity extends SecuredActivity implements GuestMemberContract.View, View.OnClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private GuestMemberAdapter adapter;
    private GuestMemberPresenter presenter;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_guest_member);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.fab_add_member).setOnClickListener(this);
        findViewById(R.id.backBtn).setOnClickListener(this);
        presenter = new GuestMemberPresenter(this);
        presenter.fetchData();

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


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backBtn:
                finish();
                break;
            case R.id.fab_add_member:
                try{
                    Intent intent = new Intent(this, GuestAddMemberJsonFormActivity.class);
                    JSONObject jsonForm = FormUtils.getInstance(this).getFormJson(HnppConstants.JSON_FORMS.GUEST_MEMBER_FORM);
                    HnppJsonFormUtils.updateFormWithSSName(jsonForm, SSLocationHelper.getInstance().getSsModels());
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

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                presenter.saveMember(jsonString);
            }catch (Exception e){

            }
        }
    }

    private void openProfile(String baseEntityId){

        GuestMemberProfileActivity.startGuestMemberProfileActivity(this,baseEntityId);

    }
}
