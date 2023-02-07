package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.HnppMemberProfileContract;
import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.fragment.HnppMemberProfileDueFragment;
import org.smartregister.unicef.dghs.interactor.HnppMemberProfileInteractor;
import org.smartregister.unicef.dghs.interactor.MemberOtherServiceInteractor;
import org.smartregister.unicef.dghs.utils.MemberProfileDueData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class HnppMemberProfilePresenter implements HnppMemberProfileContract.Presenter, HnppMemberProfileContract.InteractorCallBack {
    private HnppMemberProfileContract.View view;
    private ArrayList<MemberProfileDueData> data;
    private HnppMemberProfileContract.Interactor interactor;

    public HnppMemberProfilePresenter(HnppMemberProfileContract.View view) {
        this.view = view;
        data = new ArrayList<>();
        interactor = new HnppMemberProfileInteractor(new AppExecutors());
    }

    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient,String baseEntityId) {
        interactor.fetchData(commonPersonObjectClient,getView().getContext(),baseEntityId,this);
    }

    @Override
    public ArrayList<MemberProfileDueData> getData() {
        return data;
    }
    public String getLastEventType(){
       return interactor.getLastEvent();
    }

    @Override
    public HnppMemberProfileDueFragment getView() {
        return (HnppMemberProfileDueFragment) view;
    }

    @Override
    public void onUpdateList(ArrayList<MemberProfileDueData> memberProfileDueData) {
        this.data.clear();
        this.data = memberProfileDueData;
        if(getView() != null) getView().updateView();
    }
}
