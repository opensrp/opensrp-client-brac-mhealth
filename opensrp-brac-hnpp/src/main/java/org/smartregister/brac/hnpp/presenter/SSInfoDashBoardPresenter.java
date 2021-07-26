package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.SSInfoDashBoardInteractor;
import org.smartregister.brac.hnpp.interactor.WorkSummeryDashBoardInteractor;
import org.smartregister.brac.hnpp.model.IndicatorDashBoardModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class SSInfoDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private SSInfoDashBoardInteractor interactor;

    public SSInfoDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new SSInfoDashBoardInteractor(new AppExecutors(), new IndicatorDashBoardModel(view.getContext()));

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
    public void filterData(String ssName, String to, String from) {
       /* getView().showProgressBar();
        if(month.equalsIgnoreCase("-1")) month ="";
        if(year.equalsIgnoreCase("-1")) year ="";
        month = HnppConstants.addZeroForMonth(month);
        interactor.filterData(ssName,month,year,this);*/
        interactor.filterData(ssName,to,from,this);

    }



    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
