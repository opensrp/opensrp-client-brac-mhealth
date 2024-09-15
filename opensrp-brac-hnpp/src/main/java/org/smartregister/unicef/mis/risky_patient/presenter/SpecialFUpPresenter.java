package org.smartregister.unicef.mis.risky_patient.presenter;


import org.smartregister.unicef.mis.risky_patient.contract.SpecialFUpContract;
import org.smartregister.unicef.mis.risky_patient.interactor.SpecialFUpnteractor;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;

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
    public ArrayList<AncFollowUpModel> fetchSearchedSpecialFUp(String searchedText, RiskyPatientFilterType riskyPatientFilterType) {
        return interactor.getFollowUpListAfterSearch(searchedText,riskyPatientFilterType);
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
