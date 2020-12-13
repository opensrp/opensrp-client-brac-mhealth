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
            fetchHHData("","","");

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    // need to maintain serial to display
    private void fetchHHData(String ssName, String month, String year) {
        if(HnppConstants.isPALogin()){
            addToDashBoardList(model.getEyeTestCount(ssName,month,year));
            addToDashBoardList(model.getBloodGroupingCount(ssName,month,year));
            addToDashBoardList(model.getNcdServiceCount(ssName,month,year));
            addToDashBoardList(model.getNcdForumCount(ssName,month,year));
        }else{
            addToDashBoardList(model.getHHCount(ssName,month,year));
            addToDashBoardList(model.getMemberCount(ssName,month,year));
            addToDashBoardList(model.getElcoCount(ssName,month,year));
            addToDashBoardList(model.getANCRegisterCount(ssName,month,year));
            addToDashBoardList(model.getAnc1Count(ssName,month,year));
            addToDashBoardList(model.getAnc2Count(ssName,month,year));
            addToDashBoardList(model.getAnc3Count(ssName,month,year));
            addToDashBoardList(model.getAncCount(ssName,month,year));
            addToDashBoardList(model.getDeliveryCount(ssName,month,year));
            addToDashBoardList(model.getPncCount(ssName,month,year));
            addToDashBoardList(model.getEncCount(ssName,month,year));
            addToDashBoardList(model.getChildFollowUpCount(ssName,month,year));
            addToDashBoardList(model.getNcdForumCount(ssName,month,year));
            addToDashBoardList(model.getNcdServiceCount(ssName,month,year));
            addToDashBoardList(model.getWomenForumCount(ssName,month,year));
            addToDashBoardList(model.getWomenServiceCount(ssName,month,year));
            addToDashBoardList(model.getAdoForumCount(ssName,month,year));
            addToDashBoardList(model.getAdoServiceCount(ssName,month,year));
            addToDashBoardList(model.getChildForumCount(ssName,month,year));
            addToDashBoardList(model.getChildServiceCount(ssName,month,year));
            addToDashBoardList(model.getAdultForumCount(ssName,month,year));
        }



    }

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,month,year);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }
}
