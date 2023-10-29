package org.smartregister.unicef.mis.presenter;

import org.smartregister.unicef.mis.contract.BkashStatusContract;
import org.smartregister.unicef.mis.interactor.BkashStatusInteractor;
import org.smartregister.unicef.mis.utils.BkashStatus;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class BkashStatusPresenter implements BkashStatusContract.Presenter, BkashStatusContract.InteractorCallBack {
    private BkashStatusContract.View view;
    private BkashStatusContract.Interactor interactor;

    public BkashStatusPresenter(BkashStatusContract.View view){
        this.view = view;
        interactor = new BkashStatusInteractor(new AppExecutors());
    }


    @Override
    public void fetchBkashStatus() {
        getView().showProgressBar();
        interactor.fetchBkashStatus(this);
    }

    @Override
    public BkashStatusContract.View getView() {
        return view;
    }
    public ArrayList<BkashStatus> getBkashStatusData(){
        return interactor.getStatusList();
    }
    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateView();
    }
}
