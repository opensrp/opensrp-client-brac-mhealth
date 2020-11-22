package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.ServiceTargetAchievementInteractor;
import org.smartregister.brac.hnpp.interactor.TargetAchievementInteractor;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
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

    @Override
    public void fetchDashBoardData(int day, int month, int year, String ssName) {
        getView().showProgressBar();
        interactor.fetchAllData(this,day,month,year,ssName);
    }

    @Override
    public void filterData(String ssName, int day, int month, int year) {
        getView().showProgressBar();
        interactor.filterData(ssName,day,month,year,this);
    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
