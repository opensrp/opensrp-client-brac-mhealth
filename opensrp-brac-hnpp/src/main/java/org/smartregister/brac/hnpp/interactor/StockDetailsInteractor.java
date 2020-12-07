package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.StockDetailsContract;
import org.smartregister.brac.hnpp.model.StockDetailsModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.StockDetailsData;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class StockDetailsInteractor implements StockDetailsContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<StockDetailsData> dashBoardDataArrayList;
    private StockDetailsModel model;

    public StockDetailsInteractor(AppExecutors appExecutors, StockDetailsModel model){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.model = model;
    }


    private void addToDashBoardList(StockDetailsData dashBoardData){
        if(dashBoardData !=null) dashBoardDataArrayList.add(dashBoardData);
    }
    // need to maintain serial to display
    private void fetchStockDetails(String productName, String month, String year) {
        if(productName.equalsIgnoreCase(CoreConstants.EventType.ANC_HOME_VISIT)){
            addToDashBoardList(model.getAncStockData(month,year));
        }
        else if(productName.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)){
            addToDashBoardList(model.getPncStockData(month,year));
        }
        else if(productName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GIRL_PACKAGE)){
            addToDashBoardList(model.getAdoServiceStockData(month,year));
        }
        else if(productName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.IYCF_PACKAGE)){
            addToDashBoardList(model.getChildServiceStockData(month,year));
        }
        else if(productName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
            addToDashBoardList(model.getNcdServiceStockData(month,year));
        }
        else if(productName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)){
            addToDashBoardList(model.getWomenServiceStockData(month,year));
        }

    }

    @Override
    public ArrayList<StockDetailsData> getStockDetailsData() {
        return dashBoardDataArrayList;
    }

    @Override
    public void filterData(String productName, String month, String year, StockDetailsContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchStockDetails(productName,month,year);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}