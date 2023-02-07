package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.interactor.CountSummeryDashBoardInteractor;
import org.smartregister.unicef.dghs.interactor.WorkSummeryDashBoardInteractor;
import org.smartregister.unicef.dghs.model.CountSummeryDashBoardModel;
import org.smartregister.unicef.dghs.model.IndicatorDashBoardModel;
import org.smartregister.unicef.dghs.model.WorkSummeryDashBoardModel;
import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class WorkSummeryDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private WorkSummeryDashBoardInteractor interactor;

    public WorkSummeryDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new WorkSummeryDashBoardInteractor(new AppExecutors(),new WorkSummeryDashBoardModel(view.getContext()), new IndicatorDashBoardModel(view.getContext()));

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
       /* getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(ssName,month,year,this);*/

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
