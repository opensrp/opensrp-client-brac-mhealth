package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.interactor.CountSummeryDashBoardInteractor;
import org.smartregister.brac.hnpp.interactor.WorkSummeryDashBoardInteractor;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class WorkSummeryDashBoardPresenter implements DashBoardContract.Presenter,DashBoardContract.InteractorCallBack {

    private DashBoardContract.View view;
    private WorkSummeryDashBoardInteractor interactor;

    public WorkSummeryDashBoardPresenter(DashBoardContract.View view){
        this.view = view;
        interactor = new WorkSummeryDashBoardInteractor(new AppExecutors(),new WorkSummeryDashBoardModel(view.getContext()));

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
    public void filterData(String ssName, String month, String date) {

    }

    @Override
    public DashBoardContract.View getView() {
        return view;
    }
}
