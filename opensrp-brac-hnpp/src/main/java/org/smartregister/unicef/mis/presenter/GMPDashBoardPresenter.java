package org.smartregister.unicef.mis.presenter;

import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.interactor.GMPDashBoardInteractor;
import org.smartregister.unicef.mis.interactor.ImmunizationSummeryDashBoardInteractor;
import org.smartregister.unicef.mis.model.GMPDashBoardModel;
import org.smartregister.unicef.mis.model.ImmunizationSummeryDashBoardModel;
import org.smartregister.unicef.mis.model.IndicatorDashBoardModel;
import org.smartregister.unicef.mis.utils.DashBoardData;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;

public class GMPDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private GMPDashBoardInteractor interactor;

    public GMPDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new GMPDashBoardInteractor(new AppExecutors(),new GMPDashBoardModel(view.getContext()));

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
    public void filterData(String ssName, String month,String year) {
        getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(ssName,month,year,this);

    }
    public void filterByFromToMonth(long fromMonth, long toMonth, String ssName) {
        getView().showProgressBar();
        interactor.filterByFromToMonth(ssName,fromMonth,toMonth,this);
    }
    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
