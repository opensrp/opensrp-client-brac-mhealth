package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.contract.StockDetailsContract;
import org.smartregister.brac.hnpp.interactor.StockDashBoardInteractor;
import org.smartregister.brac.hnpp.interactor.StockDetailsInteractor;
import org.smartregister.brac.hnpp.model.StockDashBoardModel;
import org.smartregister.brac.hnpp.model.StockDetailsModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.StockDetailsData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class StockDetailsPresenter implements StockDetailsContract.Presenter,StockDetailsContract.InteractorCallBack{

    private StockDetailsContract.View view;
    private StockDetailsInteractor interactor;

    public StockDetailsPresenter(StockDetailsContract.View view){
        this.view = view;
        interactor = new StockDetailsInteractor(new AppExecutors(),new StockDetailsModel(view.getContext()));

    }
    public ArrayList<StockDetailsData> getStockDetailsData(){
        return interactor.getStockDetailsData();
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateView();
    }


    @Override
    public void filterData(String productName, String month, String year) {
        getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(productName,month,year,this);
    }

    @Override
    public StockDetailsContract.View getView() {
        return view;
    }
}
