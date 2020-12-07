package org.smartregister.brac.hnpp.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppFormViewActivity;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.presenter.MemberHistoryPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;

import java.util.HashMap;

public class MemberHistoryFragment extends Fragment implements MemberHistoryContract.View {

    public static final String IS_GUEST_USER = "IS_GUEST_USER";

    private MemberHistoryPresenter presenter;
    private RecyclerView clientsView;
    private String baseEntityId;
    private boolean isStart = true;
    private boolean isGuestUser = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            presenter.fetchData(baseEntityId);
        }
    }

    public static MemberHistoryFragment getInstance(Bundle bundle){
        MemberHistoryFragment memberHistoryFragment = new MemberHistoryFragment();
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
        isStart = false;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        isGuestUser = getArguments().getBoolean(IS_GUEST_USER,false);
        initializePresenter();
    }

    @Override
    public void initializePresenter() {
        presenter = new MemberHistoryPresenter(this);
        presenter.fetchData(baseEntityId);
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

    }

    @Override
    public void hideProgressBar() {

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
            startFormActivity(content);
        }
    };

    private void startFormActivity(MemberHistoryData content){
        try {
            JSONObject jsonForm = new JSONObject(content.getVisitDetails());
            String eventType = content.getEventType();
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
                HnppDBUtils.populatePNCChildDetails(content.getBaseEntityId(),jsonForm);
            }
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION) ||
                    eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION)
                    || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
                if(isGuestUser){
                    HnppJsonFormUtils.addNoOfAnc(jsonForm);
                }else{
                    HnppJsonFormUtils.addLastAnc(jsonForm,baseEntityId,true);
                }

            } else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION) ||
                    eventType.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)){
                if(isGuestUser){
                    HnppJsonFormUtils.addNoOfPnc(jsonForm);
                }else{
                    HnppJsonFormUtils.addLastPnc(jsonForm,baseEntityId,true);
                }

            }
            makeReadOnlyFields(jsonForm);

            Intent intent = new Intent(getActivity(), HnppFormViewActivity.class);
            intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

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
            intent.putExtra(org.smartregister.family.util.Constants.WizardFormActivity.EnableOnCloseDialog, false);
            if (this != null) {
                this.startActivity(intent);
            }
        } catch (JSONException e) {
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
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(presenter!=null)
//        presenter.fetchData(baseEntityId);
//    }
}