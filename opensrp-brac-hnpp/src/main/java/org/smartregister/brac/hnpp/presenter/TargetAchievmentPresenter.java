package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.TargetAchievementInteractor;
import org.smartregister.brac.hnpp.interactor.WorkSummeryDashBoardInteractor;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
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

    @Override
    public void fetchDashBoardData(String day, String month, String year,String ssName) {
        getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        //month = HnppConstants.addZeroForMonth(month);
        interactor.fetchAllData(this,day,month,year,ssName);
    }

    @Override
    public void filterData(String ssName, String day, String month, String year) {
        getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        interactor.filterData(ssName,day,month,year,this);
    }

    public void filterByFromToDate(long fromDate, long toDate, String ssName) {
        getView().showProgressBar();
        interactor.filterByFromToDate(ssName,fromDate,toDate,this);
    }


    public void filterByFromToMonth(long fromMonth, long toMonth, String ssName) {
        getView().showProgressBar();
        interactor.filterByFromToMonth(ssName,fromMonth,toMonth,this);
    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
