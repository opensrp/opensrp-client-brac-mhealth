package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class WorkSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private WorkSummeryDashBoardModel model;

    public WorkSummeryDashBoardInteractor(AppExecutors appExecutors, WorkSummeryDashBoardModel model){
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
            fetchHHData("","");

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    // need to maintain serial to display
    private void fetchHHData(String ssName, String month) {
        if(month.equalsIgnoreCase("-1")) month = "";
        addToDashBoardList(model.getHHCount(ssName,month));
        addToDashBoardList(model.getMemberCount(ssName,month));

    }

    @Override
    public void filterData(String ssName, String month , DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,month);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }
}
