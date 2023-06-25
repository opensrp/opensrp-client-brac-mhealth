package org.smartregister.unicef.dghs.fragment;

import android.annotation.SuppressLint;
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

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.dghs.adapter.HnppMemberProfileDueAdapter;
import org.smartregister.unicef.dghs.adapter.OtherServiceAdapter;
import org.smartregister.unicef.dghs.contract.HnppMemberProfileContract;
import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.model.MemberProfileDueModel;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.presenter.HnppMemberProfilePresenter;
import org.smartregister.unicef.dghs.presenter.MemberOtherServicePresenter;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.unicef.dghs.utils.MemberProfileDueData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
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

import static org.smartregister.unicef.dghs.interactor.HnppMemberProfileInteractor.TAG_OPEN_ANC_HISTORY;
import static org.smartregister.unicef.dghs.utils.HnppConstants.EVENT_TYPE.CORONA_INDIVIDUAL;
import static org.smartregister.unicef.dghs.utils.HnppConstants.eventTypeFormNameMapping;
import static org.smartregister.unicef.dghs.utils.HnppConstants.eventTypeMapping;
import static org.smartregister.unicef.dghs.utils.HnppConstants.iconMapping;

public class HnppMemberProfileDueFragment extends Fragment implements View.OnClickListener, HnppMemberProfileContract.View {
    private static final int TAG_OPEN_ANC1 = 101;

    private static final int TAG_OPEN_FAMILY = 111;
    private static final int TAG_OPEN_REFEREAL = 222;
    private static final int TAG_OPEN_CORONA = 88888;
    public static final int TAG_OPEN_MEMBER_PROFILE_UPDATE = 9999;
    public static final int TAG_OPEN_ANC_REGISTRATION= 555;
    public static final int TAG_OPEN_DELIVERY = 6666;
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
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_member_profile_due,null);
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
                case TAG_OPEN_MEMBER_PROFILE_UPDATE:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openMemberProfileUpdate();
                    }
                    break;
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
                case TAG_OPEN_ANC_HISTORY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openPragnencyHistory();
                    }
                    break;
                case TAG_OPEN_REFEREAL:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openRefereal();
                    }
                    break;
                case TAG_OPEN_DELIVERY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startMalariaRegister();
                    }
                    break;
                case TAG_OPEN_ANC1:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        String eventType = (String) content.getEventType();

                            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
                                activity.openHomeVisitSingleForm(HnppConstants.JSON_FORMS.ANC_VISIT_FORM);
                            }else{
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
                case TAG_OPEN_MEMBER_PROFILE_UPDATE:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.openMemberProfileUpdate();
                    }
                    break;
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
                                && !eventType.equals(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)
                                && FormApplicability.isFirstTimeAnc(baseEntityId)){
                            activity.openHomeVisitForm();
                        }else {
                            activity.openHomeVisitSingleForm(eventTypeFormNameMapping.get(eventType));
                        }
                    }
                    break;
                case TAG_OPEN_DELIVERY:
                    if (mActivity != null && mActivity instanceof HnppFamilyOtherMemberProfileActivity) {
                        HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) mActivity;
                        activity.startMalariaRegister();
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