package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
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
        month = HnppConstants.addZeroForMonth(month);
        addToDashBoardList(model.getHHCount(ssName,month));
        addToDashBoardList(model.getMemberCount(ssName,month));
        addToDashBoardList(model.getANCRegisterCount(ssName,month));
        addToDashBoardList(model.getAnc1Count(ssName,month));
        addToDashBoardList(model.getAnc2Count(ssName,month));
        addToDashBoardList(model.getAnc3Count(ssName,month));
        addToDashBoardList(model.getAncCount(ssName,month));
        addToDashBoardList(model.getDeliveryCount(ssName,month));
        addToDashBoardList(model.getPncCount(ssName,month));
        addToDashBoardList(model.getEncCount(ssName,month));
        addToDashBoardList(model.getChildFollowUpCount(ssName,month));
        addToDashBoardList(model.getNcdForumCount(ssName,month));
        addToDashBoardList(model.getNcdServiceCount(ssName,month));
        addToDashBoardList(model.getWomenForumCount(ssName,month));
        addToDashBoardList(model.getWomenServiceCount(ssName,month));
        addToDashBoardList(model.getAdoForumCount(ssName,month));
        addToDashBoardList(model.getAdoServiceCount(ssName,month));
        addToDashBoardList(model.getChildForumCount(ssName,month));
        addToDashBoardList(model.getChildServiceCount(ssName,month));
        addToDashBoardList(model.getAdultForumCount(ssName,month));

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
