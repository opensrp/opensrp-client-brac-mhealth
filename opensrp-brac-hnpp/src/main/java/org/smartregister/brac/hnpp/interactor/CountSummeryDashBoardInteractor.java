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
            fetchHHData("","","");

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    // need to maintain serial to display
    private void fetchHHData(String ssName,String month, String year) {
        addToDashBoardList(model.getHHCount(ssName,month,year));
        addToDashBoardList(model.getMemberCount(ssName,month,year));
        addToDashBoardList(model.getOOCCount(ssName,month,year));
        addToDashBoardList(model.getSimprintsCount(ssName,month,year));
        addToDashBoardList(model.getBoyChildUnder5(ssName,month,year));
        addToDashBoardList(model.getGirlChildUnder5(ssName,month,year));
        addToDashBoardList(model.getBoyChild5To9(ssName,month,year));
        addToDashBoardList(model.getGirlChild5To9(ssName,month,year));
        addToDashBoardList(model.getBoyChild10To19(ssName,month,year));
        addToDashBoardList(model.getGirlChild10To19(ssName,month,year));
        addToDashBoardList(model.getBoyChild20To50(ssName,month,year));
        addToDashBoardList(model.getGirlChild20To50(ssName,month,year));
        addToDashBoardList(model.getMenUp50(ssName,month,year));
        addToDashBoardList(model.getWoMenUp50(ssName,month,year));
        addToDashBoardList(model.getAdoGirl(ssName,month,year));
        addToDashBoardList(model.getAdoBoy(ssName,month,year));
        addToDashBoardList(model.getAdoElco(ssName,month,year));


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
