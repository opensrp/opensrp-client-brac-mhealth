package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.contract.SpecialFUpContract;
import org.smartregister.brac.hnpp.interactor.RoutinFUpnteractor;
import org.smartregister.brac.hnpp.interactor.SpecialFUpnteractor;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;

import java.util.ArrayList;

public class SpecialFUpPresenter implements SpecialFUpContract.Presenter, SpecialFUpContract.InteractorCallBack {
    private SpecialFUpContract.View view;
    private SpecialFUpContract.Interactor interactor;

    public SpecialFUpPresenter(SpecialFUpContract.View view){
        this.view = view;
        interactor = new SpecialFUpnteractor();
    }

    @Override
    public ArrayList<AncFollowUpModel> fetchSpecialFUp() {
        return interactor.getFollowUpList();
    }

    @Override
    public ArrayList<AncFollowUpModel> fetchSearchedSpecialFUp(String searchedText) {
        return interactor.getFollowUpListAfterSearch(searchedText);
    }

    @Override
    public SpecialFUpContract.View getView() {
        return view;
    }

    @Override
    public void fetchedSuccessfully() {
        getView().hideProgressBar();
        getView().updateView();
    }
}
