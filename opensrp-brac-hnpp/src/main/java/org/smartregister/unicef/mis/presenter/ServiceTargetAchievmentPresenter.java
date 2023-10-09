package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.interactor.ServiceTargetAchievementInteractor;
import org.smartregister.unicef.mis.model.TargetVsAchievementModel;
import org.smartregister.unicef.mis.utils.TargetVsAchievementData;
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
