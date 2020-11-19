package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.DashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class CountSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private CountSummeryDashBoardModel model;

    public CountSummeryDashBoardInteractor(AppExecutors appExecutors, CountSummeryDashBoardModel model){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.model = model;
    }

    @Override
    public ArrayList<DashBoardData> getListData() {
        return dashBoardDataArrayList;
    }

    private void addToDashBoardList(DashBoardData dashBoardData){
        if(dashBoardData !=null) dashBoardDataArrayList.add(dashBoardData);
    }

    @Override
    public void fetchAllData(DashBoardContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            fetchHHData("");

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    // need to maintain serial to display
    private void fetchHHData(String ssName) {
        addToDashBoardList(model.getHHCount(ssName));
        addToDashBoardList(model.getMemberCount(ssName));

    }

    @Override
    public void filterData(String ssName, String month , DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }
}
