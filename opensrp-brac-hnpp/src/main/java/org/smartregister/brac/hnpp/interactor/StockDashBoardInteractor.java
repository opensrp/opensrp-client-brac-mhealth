package org.smartregister.brac.hnpp.interactor;

import android.util.Log;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.StockDashBoardModel;
import org.smartregister.brac.hnpp.model.StockDetailsModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.Calendar;

public class StockDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private StockDetailsModel model;

    public StockDashBoardInteractor(AppExecutors appExecutors, StockDetailsModel model){
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
        Calendar calendar = Calendar.getInstance();
        month = calendar.get(Calendar.MONTH)+1+"";
        String year = calendar.get(Calendar.YEAR)+"";
        Log.v("STOCK_DETAILS","sdfasdf"+month+":year:"+year);

        addToDashBoardList(model.getAncStockData(month,year));
        addToDashBoardList(model.getPncStockData(month,year));
        addToDashBoardList(model.getNcdServiceStockData(month,year));
        addToDashBoardList(model.getWomenServiceStockData(month,year));
        addToDashBoardList(model.getAdoServiceStockData(month,year));
        addToDashBoardList(model.getChildServiceStockData(month,year));
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
