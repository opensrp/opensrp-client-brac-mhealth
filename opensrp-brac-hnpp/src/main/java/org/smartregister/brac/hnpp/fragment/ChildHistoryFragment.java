package org.smartregister.brac.hnpp.fragment;

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
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppFormViewActivity;
import org.smartregister.brac.hnpp.adapter.MemberHistoryAdapter;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.presenter.ChildHistoryPresenter;
import org.smartregister.brac.hnpp.presenter.MemberHistoryPresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;

public class ChildHistoryFragment extends Fragment implements MemberHistoryContract.View {

    private ChildHistoryPresenter presenter;
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
    public void setBaseEntityId(String baseEntityId){
        this.baseEntityId = baseEntityId;
    }

    public static ChildHistoryFragment getInstance(Bundle bundle){
        ChildHistoryFragment memberHistoryFragment = new ChildHistoryFragment();
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
        initializePresenter();
    }

    @Override
    public void initializePresenter() {
        presenter = new ChildHistoryPresenter(this);
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
            if(content.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.VACCINATION)
            || content.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.SERVICES)){

               // Toast.makeText(getActivity(),content.getVisitDetails(),Toast.LENGTH_SHORT).show();
                return;

            }
            startFormActivity(content);
        }
    };

    @Override
    public void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm) {
        try {
            hideProgressBar();
            makeReadOnlyFields(jsonForm);

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

    private void startFormActivity(MemberHistoryData content){
        showProgressBar();
        presenter.getVisitFormWithData(content);

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