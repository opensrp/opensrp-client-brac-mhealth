package org.smartregister.unicef.mis.risky_patient.contract;

import android.content.Context;

import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;

import java.util.ArrayList;


public interface TelephonicFUpContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateView();
        void noDataFound();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        ArrayList<AncFollowUpModel> fetchData();
        ArrayList<AncFollowUpModel> fetchSearchedData(String searchedText, RiskyPatientFilterType riskyPatientFilterType);
        View getView();
    }
    interface Interactor{
        ArrayList<AncFollowUpModel> getFollowUpList();
        ArrayList<AncFollowUpModel> getFollowUpListAfterSearch(String searchedText, RiskyPatientFilterType riskyPatientFilterType);
    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}
