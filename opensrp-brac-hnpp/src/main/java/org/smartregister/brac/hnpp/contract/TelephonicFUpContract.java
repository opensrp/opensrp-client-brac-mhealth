package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.AncFollowUpModel;

import java.util.ArrayList;

public interface TelephonicFUpContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateView();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        ArrayList<AncFollowUpModel> fetchData();
        View getView();
    }
    interface Interactor{
        ArrayList<AncFollowUpModel> getFollowUpList();
    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}