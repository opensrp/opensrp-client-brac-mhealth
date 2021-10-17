package org.smartregister.brac.hnpp.interactor;

import android.util.Log;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.StockDetailsModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
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
        //month = HnppConstants.addZeroForMonth(month);
        Log.v("STOCK_DETAILS","sdfasdf"+month+":year:"+year);
        if(HnppConstants.isPALogin()){
            addToDashBoardList(model.getAdultPackageStockData(month,year));
            addToDashBoardList(model.getTotalGlassStockData(month,year));
            addToDashBoardList(model.getSunGlassStockData(month,year));
            addToDashBoardList(model.getSV1StockData(month,year));
            addToDashBoardList(model.getSV1_5StockData(month,year));
            addToDashBoardList(model.getSV2StockData(month,year));
            addToDashBoardList(model.getSV2_5StockData(month,year));
            addToDashBoardList(model.getSV3StockData(month,year));
            addToDashBoardList(model.getBF1StockData(month,year));
            addToDashBoardList(model.getBF1_5StockData(month,year));
            addToDashBoardList(model.getBF2StockData(month,year));
            addToDashBoardList(model.getBF2_5StockData(month,year));
            addToDashBoardList(model.getBF3StockData(month,year));
        }else{
            addToDashBoardList(model.getAncStockData(month,year));
            addToDashBoardList(model.getPncStockData(month,year));

            addToDashBoardList(model.getNcdServiceStockData(month,year));
            addToDashBoardList(model.getWomenServiceStockData(month,year));
            addToDashBoardList(model.getAdoServiceStockData(month,year));
            addToDashBoardList(model.getChildServiceStockData(month,year));
        }


    }

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,month);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }
}
