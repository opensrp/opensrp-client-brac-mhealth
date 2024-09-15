package org.smartregister.unicef.mis.risky_patient.presenter;


import org.smartregister.unicef.mis.risky_patient.contract.TelephonicFUpContract;
import org.smartregister.unicef.mis.risky_patient.interactor.TelephonicFUpnteractor;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;

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
    public ArrayList<AncFollowUpModel> fetchSearchedData(String searchedText, RiskyPatientFilterType riskyPatientFilterType) {
        return interactor.getFollowUpListAfterSearch(searchedText,riskyPatientFilterType);
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
