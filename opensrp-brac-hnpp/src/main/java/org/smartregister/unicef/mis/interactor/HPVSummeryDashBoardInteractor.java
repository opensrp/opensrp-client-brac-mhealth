package org.smartregister.unicef.mis.interactor;

import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.model.HPVSummeryDashBoardModel;
import org.smartregister.unicef.mis.model.ImmunizationSummeryDashBoardModel;
import org.smartregister.unicef.mis.model.IndicatorDashBoardModel;
import org.smartregister.unicef.mis.utils.DashBoardData;

import java.util.ArrayList;

public class HPVSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private HPVSummeryDashBoardModel model;

    public HPVSummeryDashBoardInteractor(AppExecutors appExecutors, HPVSummeryDashBoardModel model){
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
            fetchImmunizationData("",-1,-1);
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    private void fetchImmunizationData(String blockId, long fromMonth, long toMonth){
        dashBoardDataArrayList.clear();
        dashBoardDataArrayList = model.getHPVCount("","",fromMonth,toMonth,"HPV");

    }
    // need to maintain serial to display

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
//        dashBoardDataArrayList.clear();
//        Runnable runnable = () -> {
//            //TODO not needed
//            fetchHHData( ssName,-1,-1);
//
//            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
//        };
//        appExecutors.diskIO().execute(runnable);


    }
    public void filterByFromToMonth(String ssName, long fromMonth, long toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchImmunizationData(ssName,fromMonth,toMonth);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
