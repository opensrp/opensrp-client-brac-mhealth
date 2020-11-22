package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.CountSummeryDashBoardInteractor;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class CountSummeryDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private CountSummeryDashBoardInteractor interactor;

    public CountSummeryDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new CountSummeryDashBoardInteractor(new AppExecutors(),new CountSummeryDashBoardModel(view.getContext()));

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
    public void filterData(String ssName, String month) {
        interactor.filterData(ssName,month,this);

    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
