package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.RiskyPatientFilterType;

import java.util.ArrayList;

public interface SpecialFUpContract {

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
        ArrayList<AncFollowUpModel> fetchSpecialFUp();
        ArrayList<AncFollowUpModel> fetchSearchedSpecialFUp(String searchedText, RiskyPatientFilterType riskyPatientFilterType);
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
