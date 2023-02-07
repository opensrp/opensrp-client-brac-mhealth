package org.smartregister.unicef.dghs.interactor;

import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.model.IndicatorDashBoardModel;
import org.smartregister.unicef.dghs.model.WorkSummeryDashBoardModel;
import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class SSInfoDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private IndicatorDashBoardModel indicatorModel;

    public SSInfoDashBoardInteractor(AppExecutors appExecutors, IndicatorDashBoardModel indicatorModel){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.indicatorModel = indicatorModel;
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

    //fetch data for from to month
    private void fetchHHData(String ssName, String fromMonth, String toMonth) {
        dashBoardDataArrayList.clear();

            if(indicatorModel!=null){
                addToDashBoardList(indicatorModel.getHHVisitCount(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getPregnancyIdentity(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getAfter1hrBirthAdviceCount(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getServiceTakenMemberCount(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getIncomeFromMedicineCount(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getGlassSellCount(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getPresentEpiCount(ssName,fromMonth,toMonth));
            }

    }

    @Override
    public void filterData(String ssName, String to , String from, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,from,to);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }


//    public void filterByFromToMonth(String ssName, long fromMonth, long toMonth, DashBoardContract.InteractorCallBack callBack) {
//        dashBoardDataArrayList.clear();
//        Runnable runnable = () -> {
//            fetchHHData(ssName,fromMonth,toMonth);
//
//            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
//        };
//        appExecutors.diskIO().execute(runnable);
//    }
}
