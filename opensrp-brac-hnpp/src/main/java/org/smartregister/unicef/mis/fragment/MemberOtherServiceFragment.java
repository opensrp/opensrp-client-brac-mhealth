package org.smartregister.unicef.mis.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.activity.HnppChildProfileActivity;
import org.smartregister.unicef.mis.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.unicef.mis.adapter.OtherServiceAdapter;
import org.smartregister.unicef.mis.contract.OtherServiceContract;
import org.smartregister.unicef.mis.presenter.MemberOtherServicePresenter;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class MemberOtherServiceFragment extends Fragment implements OtherServiceContract.View {

    private MemberOtherServicePresenter presenter;
    private RecyclerView clientsView;
    private CommonPersonObjectClient commonPersonObjectClient;
    private Handler handler;

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient){
        this.commonPersonObjectClient = commonPersonObjectClient;
    }
    private boolean isStart = true;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && !isStart){
            updateStaticView();
        }
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
        presenter = new MemberOtherServicePresenter(this);
        handler = new Handler();
        updateStaticView();
    }
    public void updateStaticView(){

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(presenter!=null&& commonPersonObjectClient!=null)presenter.fetchData(commonPersonObjectClient);
            }
        },500);

    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void updateView() {
        OtherServiceAdapter adapter = new OtherServiceAdapter(getActivity(),onClickAdapter);
        adapter.setData(presenter.getData());
        this.clientsView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler != null) handler.removeCallbacksAndMessages(null);
    }

    @Override
    public OtherServiceContract.Presenter getPresenter() {
        return presenter;
    }

    private OtherServiceAdapter.OnClickAdapter onClickAdapter = (position, content) -> startFormActivity(content);
    private void startFormActivity(OtherServiceData content){
        switch (content.getType()){
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_GIRL_PACKAGE:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.GIRL_PACKAGE);
                }

                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_NCD:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.NCD_PACKAGE);
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.NCD_PACKAGE);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_IYCF:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.IYCF_PACKAGE);
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.IYCF_PACKAGE);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_WOMEN_PACKAGE:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.WOMEN_PACKAGE);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_BLOOD:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.BLOOD_TEST);
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.BLOOD_TEST);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_EYE:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.EYE_TEST);
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.EYE_TEST);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_REFERRAL:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.MEMBER_REFERRAL_PA);
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openServiceForms(HnppConstants.JSON_FORMS.MEMBER_REFERRAL_PA);
                }
                break;
            case HnppConstants.OTHER_SERVICE_TYPE.TYPE_REFERRAL_FOLLOW_UP:
                if (getActivity() != null && getActivity() instanceof HnppFamilyOtherMemberProfileActivity) {
                    HnppFamilyOtherMemberProfileActivity activity = (HnppFamilyOtherMemberProfileActivity) getActivity();

                    activity.openReferealFollowUp(content.getReferralFollowUpModel());
                }else if (getActivity() != null && getActivity() instanceof HnppChildProfileActivity) {
                    HnppChildProfileActivity activity = (HnppChildProfileActivity) getActivity();
                    activity.openReferealFollowUp(content.getReferralFollowUpModel());
                }
                break;
        }
    }
}