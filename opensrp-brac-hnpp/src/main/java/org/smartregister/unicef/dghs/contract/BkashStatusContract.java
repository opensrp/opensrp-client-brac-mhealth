package org.smartregister.unicef.dghs.contract;

import android.content.Context;
import org.smartregister.unicef.dghs.utils.BkashStatus;

import java.util.ArrayList;

public interface BkashStatusContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateView();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();

    }
    interface Presenter{
        void fetchBkashStatus();
        View getView();
    }
    interface Interactor{
        ArrayList<BkashStatus> getStatusList();
        void fetchBkashStatus(BkashStatusContract.InteractorCallBack callBack);

    }

    interface InteractorCallBack{
        void fetchedSuccessfully();
    }
}
