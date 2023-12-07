package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.utils.BkashStatus;

import java.util.ArrayList;

public interface RoutinFUpContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateView();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        ArrayList<AncFollowUpModel> fetchRoutinFUp();
        View getView();
    }
    interface Interactor{
        ArrayList<AncFollowUpModel> getFollowUpList();
    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}