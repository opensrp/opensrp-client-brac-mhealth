package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.interactor.RoutinFUpnteractor;
import org.smartregister.brac.hnpp.interactor.TelephonicFUpnteractor;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;

import java.util.ArrayList;

public class TelephonicFUpPresenter implements TelephonicFUpContract.Presenter, TelephonicFUpContract.InteractorCallBack {
    private TelephonicFUpContract.View view;
    private TelephonicFUpContract.Interactor interactor;

    public TelephonicFUpPresenter(TelephonicFUpContract.View view){
        this.view = view;
        interactor = new TelephonicFUpnteractor();
    }

    @Override
    public ArrayList<AncFollowUpModel> fetchData() {
        return interactor.getFollowUpList();
    }

    @Override
    public TelephonicFUpContract.View getView() {
        return view;
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateView();
    }
}
