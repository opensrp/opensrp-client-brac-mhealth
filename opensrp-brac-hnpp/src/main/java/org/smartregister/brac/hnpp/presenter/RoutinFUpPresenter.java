package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.BkashStatusContract;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.interactor.BkashStatusInteractor;
import org.smartregister.brac.hnpp.interactor.RoutinFUpnteractor;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.utils.BkashStatus;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class RoutinFUpPresenter implements RoutinFUpContract.Presenter, RoutinFUpContract.InteractorCallBack {
    private RoutinFUpContract.View view;
    private RoutinFUpContract.Interactor interactor;

    public RoutinFUpPresenter(RoutinFUpContract.View view){
        this.view = view;
        interactor = new RoutinFUpnteractor();
    }

    @Override
    public ArrayList<FollowUpModel> fetchRoutinFUp() {
        return interactor.getFollowUpList();
    }

    @Override
    public RoutinFUpContract.View getView() {
        return view;
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateView();
    }
}
