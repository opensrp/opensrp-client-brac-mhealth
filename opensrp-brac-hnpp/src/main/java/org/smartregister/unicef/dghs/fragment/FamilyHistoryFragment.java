package org.smartregister.unicef.dghs.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppFormViewActivity;
import org.smartregister.unicef.dghs.adapter.MemberHistoryAdapter;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.presenter.FamilyHistoryPresenter;
import org.smartregister.unicef.dghs.presenter.MemberHistoryPresenter;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;

public class FamilyHistoryFragment extends Fragment implements MemberHistoryContract.View {

    private FamilyHistoryPresenter presenter;
    private RecyclerView clientsView;
    private String baseEntityId;
    private boolean isStart = true;
    private ProgressBar client_list_progress;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            presenter.fetchData(baseEntityId);
        }
    }
    public static FamilyHistoryFragment getInstance(Bundle bundle){
        FamilyHistoryFragment memberHistoryFragment = new FamilyHistoryFragment();
        Bundle args = bundle;
        if(args == null){
            args = new Bundle();
        }
        memberHistoryFragment.setArguments(args);
        return memberHistoryFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view,null);
        clientsView = view.findViewById(R.id.recycler_view);
        client_list_progress = view.findViewById(R.id.client_list_progress);
        isStart = false;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        initializePresenter();
    }

    @Override
    public void initializePresenter() {
        presenter = new FamilyHistoryPresenter(this);
        //presenter.fetchData(baseEntityId);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(presenter==null){

        }else{

        }
    }

    @Override
    public void showProgressBar() {
        client_list_progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        client_list_progress.setVisibility(View.GONE);
    }

    @Override
    public void updateAdapter() {

        MemberHistoryAdapter adapter = new MemberHistoryAdapter(getActivity(),onClickAdapter);
        adapter.setData(presenter.getMemberHistory());
        this.clientsView.setAdapter(adapter);
    }


    @Override
    public MemberHistoryContract.Presenter getPresenter() {
        return presenter;
    }

    private MemberHistoryAdapter.OnClickAdapter onClickAdapter = new MemberHistoryAdapter.OnClickAdapter() {
        @Override
        public void onClick(int position, MemberHistoryData content) {
            if(content.getEventType().equalsIgnoreCase("Vaccination")
                    || content.getEventType().equalsIgnoreCase("Recurring Service")){
                return;
            }
            startFormActivity(content);
        }
    };
    private void startFormActivity(MemberHistoryData content){
        showProgressBar();
        presenter.getVisitFormWithData(content);

    }
    @Override
    public void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm) {
        try {
            hideProgressBar();
            makeReadOnlyFields(jsonForm);
            String eventType = content.getEventType();
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
                HnppDBUtils.populatePNCChildDetails(content.getBaseEntityId(),jsonForm);
            }
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION) ||
                    eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION)
                    || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
                HnppJsonFormUtils.addLastAnc(jsonForm,baseEntityId,true);
            } else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour) ||
                    eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)){
                HnppJsonFormUtils.addLastPnc(jsonForm,baseEntityId,true);
            }
            Intent intent = new Intent(getActivity(), HnppFormViewActivity.class);
            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            if(!HnppConstants.isReleaseBuild()){
                form.setActionBarBackground(R.color.test_app_color);

            }else{
                form.setActionBarBackground(org.smartregister.family.R.color.customAppThemeBlue);

            }
            form.setHideSaveLabel(true);
            form.setSaveLabel("");
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
            if (this != null) {
                this.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void makeReadOnlyFields(JSONObject jsonObject){
        JSONObject stepOne = null;
        try {
            stepOne = jsonObject.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject fieldObject = jsonArray.getJSONObject(i);
                fieldObject.put(JsonFormUtils.READ_ONLY, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(presenter!=null)
        presenter.fetchData(baseEntityId);
    }
}