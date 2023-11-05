package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.ServiceTargetAchievementInteractor;
import org.smartregister.brac.hnpp.interactor.TargetAchievementInteractor;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class ServiceTargetAchievmentPresenter implements DashBoardContract.TargetPresenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private ServiceTargetAchievementInteractor interactor;

    public ServiceTargetAchievmentPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new ServiceTargetAchievementInteractor(new AppExecutors(),new TargetVsAchievementModel(view.getContext()));

    }
    public ArrayList<TargetVsAchievementData> getDashBoardData(){
        return interactor.getTargetListData();
    }
    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateAdapter();

    }


    public void filterByFromToDate(String fromDate, String toDate, String ssName) {
        getView().showProgressBar();
        interactor.filterByFromToDate(ssName,fromDate,toDate,this);
    }


    public void filterByFromToMonth(String fromMonth, String toMonth, String ssName) {
        getView().showProgressBar();
        interactor.filterByFromToMonth(ssName,fromMonth,toMonth,this);
    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
