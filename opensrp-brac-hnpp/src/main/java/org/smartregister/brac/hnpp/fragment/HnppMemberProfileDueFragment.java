package org.smartregister.brac.hnpp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.adapter.HnppMemberProfileDueAdapter;
import org.smartregister.brac.hnpp.adapter.OtherServiceAdapter;
import org.smartregister.brac.hnpp.contract.HnppMemberProfileContract;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.model.MemberProfileDueModel;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.presenter.HnppMemberProfilePresenter;
import org.smartregister.brac.hnpp.presenter.MemberOtherServicePresenter;
import org.smartregister.brac.hnpp.utils.FormApplicability;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileDueFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.brac.hnpp.utils.HnppConstants.iconMapping;

public class HnppMemberProfileDueFragment extends Fragment implements View.OnClickListener, HnppMemberProfileContract.View {
    public static String TAG = "HnppMemberProfileDueFragment";
    private static final int TAG_OPEN_ANC1 = 101;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    private static final int TAG_OPEN_ANC_REGISTRATION= 555;
    private static final int TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY = 556;


    private String baseEntityId;
    private LinearLayout otherServiceView;
    private ProgressBar loadingProgressBar;
    private CommonPersonObjectClient commonPersonObjectClient;
    private Handler handler;
    private boolean isStart = true;
    Activity mActivity;
    private HnppMemberProfilePresenter presenter;
    private RecyclerView dueRecyclerView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    public static HnppMemberProfileDueFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        HnppMemberProfileDueFragment fragment = new HnppMemberProfileDueFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }
    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient){
        this.commonPersonObjectClient = commonPersonObjectClient;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            //addStaticView();
            fetchData();
        }
    }

    public void updateStaticView() {
       // if(FormApplicability.isDueAnyForm(baseEntityId,eventType)){

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   // addStaticView();
                    fetchData();

                }
            },500);
//        }else{
//           if(otherServiceView!=null && anc1View !=null) otherServiceView.removeView(anc1View);
//        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_profile_due,null);
       // otherServiceView = view.findViewById(R.id.other_option);
        dueRecyclerView = view.findViewById(R.id.due_recycler_view);
        dueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingProgressBar = view.findViewById(R.id.client_list_progress);
        isStart = false;

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new HnppMemberProfilePresenter(this);
        baseEntityId = getArguments().getString(Constants.INTENT_KEY.BASE_ENTITY_ID);
        handler = new Handler();
        //addStaticView();
        fetchData();
    }

    private void fetchData() {
        showProgressBar();
        presenter.fetchData(commonPersonObjectClient,baseEntityId);
    }

    private HnppMemberProfileDueAdapter.OnClickAdapter onClickAdapter = (position, content) -> startFormActivity(content);

    private void startFormActivity(MemberProfileDueData content) {
        if(content.getReferralFollowUpModel() instanceof ReferralFollowUpModel){
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) content.getReferralFollowUpModel();
            if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) content.getType();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openCoronaIndividualForm();
                    }
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startAncRegister();
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openRefereal();
                    }
                    break;

                case TAG_PREGNANT_WOMAN_DIETARY_DIVERSITY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openWomanDietaryDiversity(content.from);
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        String eventType = (String) content.getEventType();
                        activity.requestedFormName = content.from;
                        if(!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)){
                            activity.openHomeVisitForm();
                        }else {
                            activity.openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof ReferralFollowUpModel){
            ReferralFollowUpModel referralFollowUpModel = (ReferralFollowUpModel) v.getTag();
            if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                activity.openReferealFollowUp(referralFollowUpModel);
            }
            return;
        }
        Integer tag = (Integer) v.getTag();
        if (tag != null) {
            switch (tag) {
                case TAG_OPEN_CORONA:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openCoronaIndividualForm();
                    }
                    break;
                case TAG_OPEN_ANC_REGISTRATION:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startAncRegister();
                    }
                    break;
                case TAG_OPEN_FAMILY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openFamilyDueTab();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openRefereal();
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        String eventType = (String) v.getTag(org.smartregister.family.R.id.VIEW_ID);
                        if(!eventType.equals(HnppConstants.EVENT_TYPE.ELCO)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour)
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)){
                            activity.openHomeVisitForm();
                        }else {
                            activity.openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler != null) handler.removeCallbacksAndMessages(null);
        mActivity = null;
    }

    @Override
    public void showProgressBar() {
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        loadingProgressBar.setVisibility(View.GONE);

    }

    @Override
    public void updateView() {
        hideProgressBar();

        HnppMemberProfileDueAdapter adapter = new HnppMemberProfileDueAdapter(getActivity(),onClickAdapter);
        adapter.setData(presenter.getData());
        this.dueRecyclerView.setAdapter(adapter);
        updateOptionMenu(presenter.getLastEventType());
    }
    private void updateOptionMenu(String eventType){
        if(TextUtils.isEmpty(eventType)) return;
    if(mActivity instanceof HnppFamilyOtherMemberProfileActivity){
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HnppFamilyOtherMemberProfileActivity aaa = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        try{
                            aaa.updatePregnancyOutcomeVisible(eventType);
                            aaa.updateAncRegisterVisible(eventType);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },500);


            }
    }

    @Override
    public HnppMemberProfileContract.Presenter getPresenter() {
        return presenter;
    }


}