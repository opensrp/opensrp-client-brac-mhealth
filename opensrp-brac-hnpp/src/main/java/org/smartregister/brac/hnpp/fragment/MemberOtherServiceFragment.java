package org.smartregister.brac.hnpp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.activity.HnppChildProfileActivity;
import org.smartregister.brac.hnpp.activity.HnppFamilyOtherMemberProfileActivity;
import org.smartregister.brac.hnpp.adapter.OtherServiceAdapter;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.presenter.MemberOtherServicePresenter;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class MemberOtherServiceFragment extends Fragment implements OtherServiceContract.View {

    private MemberOtherServicePresenter presenter;
    private RecyclerView clientsView;
    private CommonPersonObjectClient commonPersonObjectClient;

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient){
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view,null);
        clientsView = view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new MemberOtherServicePresenter(this);
        presenter.fetchData(commonPersonObjectClient);
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
        }
    }
}