package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.HnppMemberProfileContract;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.fragment.HnppMemberProfileDueFragment;
import org.smartregister.brac.hnpp.fragment.HouseHoldMemberDueFragment;
import org.smartregister.brac.hnpp.interactor.HnppMemberProfileInteractor;
import org.smartregister.brac.hnpp.interactor.MemberOtherServiceInteractor;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
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
        interactor.fetchData(commonPersonObjectClient,getView(),baseEntityId,this);
    }

    public void fetchDataForHh(CommonPersonObjectClient commonPersonObjectClient,String baseEntityId) {
        interactor.fetchData(commonPersonObjectClient,getHHView(),baseEntityId,this);
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
        if(view instanceof HnppMemberProfileDueFragment){
            return (HnppMemberProfileDueFragment) view;
        }
       return null;
    }

    public HouseHoldMemberDueFragment getHHView() {
        if(view instanceof HouseHoldMemberDueFragment){
            return (HouseHoldMemberDueFragment) view;
        }
        return null;
    }

    @Override
    public void onUpdateList(ArrayList<MemberProfileDueData> memberProfileDueData) {
        this.data.clear();
        this.data = memberProfileDueData;

        if(getView() != null){
            getView().updateView();
        }else if(getHHView() != null){
            getHHView().updateView();
        }
    }
}
