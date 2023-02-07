package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.interactor.TargetAchievementInteractor;
import org.smartregister.unicef.dghs.interactor.WorkSummeryDashBoardInteractor;
import org.smartregister.unicef.dghs.model.TargetVsAchievementModel;
import org.smartregister.unicef.dghs.model.WorkSummeryDashBoardModel;
import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class TargetAchievmentPresenter implements DashBoardContract.TargetPresenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private TargetAchievementInteractor interactor;

    public TargetAchievmentPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new TargetAchievementInteractor(new AppExecutors(),new TargetVsAchievementModel(view.getContext()));

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
