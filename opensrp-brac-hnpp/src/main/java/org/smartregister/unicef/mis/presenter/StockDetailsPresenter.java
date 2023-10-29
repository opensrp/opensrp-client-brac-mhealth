package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.StockDetailsContract;
import org.smartregister.unicef.mis.interactor.StockDetailsInteractor;
import org.smartregister.unicef.mis.model.StockDetailsModel;
import org.smartregister.unicef.mis.utils.StockDetailsData;
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
        //month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(productName,month,year,this);
    }

    @Override
    public StockDetailsContract.View getView() {
        return view;
    }
}
