package org.smartregister.unicef.mis.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppFormViewActivity;
import org.smartregister.unicef.mis.adapter.MemberHistoryAdapter;
import org.smartregister.unicef.mis.contract.MemberHistoryContract;
import org.smartregister.unicef.mis.presenter.MemberHistoryPresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppJsonFormUtils;
import org.smartregister.unicef.mis.utils.MemberHistoryData;

import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.unicef.mis.utils.VisitHistory;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.util.ArrayList;

public class MemberHistoryFragment extends Fragment implements MemberHistoryContract.View, View.OnClickListener {

    public static final String IS_GUEST_USER = "IS_GUEST_USER";
    public static final String IS_ANC_HISTORY = "IS_ANC";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    private MemberHistoryPresenter presenter;
    private RecyclerView clientsView;
    private LinearLayout otherServiceView;
    private String baseEntityId;
    private boolean isStart = true;
    private boolean isGuestUser = false;
    private ProgressBar client_list_progress;
    boolean isAncHistory = false;
    boolean isNeedANCTitle= false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            if(isNeedANCTitle){
                presenter.fetchANCData(baseEntityId);
            }else{
                presenter.fetchData(baseEntityId);
            }

        }
    }
    public void setIsNeedAncTitle(boolean isNeedANCTitle){
        this.isNeedANCTitle = isNeedANCTitle;
    }

    public void setAncHistory(boolean ancHistory) {
        isAncHistory = ancHistory;
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_recycler_view,null);
        clientsView = view.findViewById(R.id.recycler_view);
        client_list_progress = view.findViewById(R.id.client_list_progress);
        otherServiceView = view.findViewById(R.id.other_option);
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
        if(isNeedANCTitle){
            presenter.fetchANCData(baseEntityId);
        }else{
            presenter.fetchData(baseEntityId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        if(isAncHistory){
            adapter.setData(presenter.getANCHistory());
        }else {
            adapter.setData(presenter.getMemberHistory());
        }
        this.clientsView.setAdapter(adapter);
    }

    @Override
    public void updateANCTitle() {
        ArrayList<VisitHistory> visitHistories = presenter.getVisitHistory();
        if(visitHistories.size()>0){
            otherServiceView.setVisibility(View.VISIBLE);
            otherServiceView.removeAllViews();
        }
        int count = visitHistories.size();
        for (VisitHistory visitHistory:visitHistories) {
            @SuppressLint("InflateParams") View followupView = LayoutInflater.from(getActivity()).inflate(R.layout.view_member_due,null);
            ImageView fImg = followupView.findViewById(R.id.image_view);
            CustomFontTextView fName =  followupView.findViewById(R.id.patient_name_age);
            followupView.findViewById(R.id.status).setVisibility(View.INVISIBLE);
            fImg.setImageResource(R.mipmap.ic_anc_pink);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(getTitle(count));
            stringBuilder.append("\n");
//            stringBuilder.append("LMP:"+visitHistory.getLmpDate());
            stringBuilder.append(getActivity().getString(R.string.edd)+visitHistory.getEddDate());
            fName.setText(stringBuilder.toString());
            fName.setFontVariant(FontVariant.REGULAR);
            fName.setTypeface(fName.getTypeface(), Typeface.NORMAL);
            followupView.setTag(visitHistory);
            followupView.setOnClickListener(this);
            otherServiceView.addView(followupView);
            count--;
        }

    }
    private String getTitle(int no){
        switch (no){
            case 1:
                return getString(R.string.first_preg_history);
            case 2:
                return getString(R.string.secont_preg_history);
            case 3:
                return getString(R.string.third_preg_history);

        }
        return "";
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

    @Override
    public void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm) {
        try {
            hideProgressBar();
            String eventType = content.getEventType();
//            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
//                HnppDBUtils.populatePNCChildDetails(content.getBaseEntityId(),jsonForm);
//            }

            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
                HnppJsonFormUtils.addNcdSugerPressure(baseEntityId,jsonForm);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void startFormActivity(MemberHistoryData content){
        showProgressBar();
        presenter.getVisitFormWithData(content);

    }

    public void makeReadOnlyFields(JSONObject jsonObject){
        try {
            int count = jsonObject.getInt("count");
            for(int i= 1;i<=count;i++){
                JSONObject steps = jsonObject.getJSONObject("step"+i);
                JSONArray ja = steps.getJSONArray(JsonFormUtils.FIELDS);

                for (int k = 0; k < ja.length(); k++) {
                    JSONObject fieldObject =ja.getJSONObject(k);
                    fieldObject.put(JsonFormUtils.READ_ONLY, true);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof VisitHistory){
            VisitHistory visitHistory =(VisitHistory) v.getTag();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INTENT_KEY.BASE_ENTITY_ID,baseEntityId);
            bundle.putLong(MemberHistoryFragment.START_TIME,visitHistory.getStartVisitDate());
            bundle.putLong(MemberHistoryFragment.END_TIME,visitHistory.getEndVisitDate());
            MemberHistoryDialogFragment.getInstance(getActivity(),bundle);

        }

    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(presenter!=null)
//        presenter.fetchData(baseEntityId);
//    }
}