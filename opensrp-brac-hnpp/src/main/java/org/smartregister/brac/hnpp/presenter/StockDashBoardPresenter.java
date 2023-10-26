package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.StockDashBoardInteractor;
import org.smartregister.brac.hnpp.model.StockDetailsModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class StockDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private StockDashBoardInteractor interactor;

    public StockDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new StockDashBoardInteractor(new AppExecutors(),new StockDetailsModel(view.getContext()));

    }
    public ArrayList<DashBoardData> getDashBoardData(){
        return interactor.getListData();
    }
    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateAdapter();

    }

    @Override
    public void fetchDashBoardData() {
        getView().showProgressBar();
        interactor.fetchAllData(this);

    }

    @Override
    public void filterData(String ssName, String month, String year) {
        getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
       // month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(ssName,month,year,this);

    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}

