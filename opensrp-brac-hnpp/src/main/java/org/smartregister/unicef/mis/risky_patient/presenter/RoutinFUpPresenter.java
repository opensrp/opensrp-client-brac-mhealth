package org.smartregister.unicef.mis.risky_patient.presenter;


import org.smartregister.unicef.mis.risky_patient.contract.RoutinFUpContract;
import org.smartregister.unicef.mis.risky_patient.interactor.RoutinFUpInteractor;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;

import java.util.ArrayList;

public class RoutinFUpPresenter implements RoutinFUpContract.Presenter, RoutinFUpContract.InteractorCallBack {
    private RoutinFUpContract.View view;
    private RoutinFUpContract.Interactor interactor;

    public RoutinFUpPresenter(RoutinFUpContract.View view){
        this.view = view;
        interactor = new RoutinFUpInteractor();
    }

    @Override
    public ArrayList<AncFollowUpModel> fetchRoutinFUp() {
        return interactor.getFollowUpList();
    }

    @Override
    public ArrayList<AncFollowUpModel> fetchSearchedRoutinFUp(String searchText, RiskyPatientFilterType riskyPatientFilterType) {
        return interactor.getFollowUpListAfterSearch(searchText,riskyPatientFilterType);
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
