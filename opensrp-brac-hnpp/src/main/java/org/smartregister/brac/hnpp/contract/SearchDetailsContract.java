package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.Migration;

import java.util.ArrayList;

public interface SearchDetailsContract {
    interface View{
        Presenter getPresenter();
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        Context getContext();
    }
    interface Presenter{
        void fetchData(String villageId, String gender, String age);
        ArrayList<Migration> getMemberList();
        View getView();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<Migration> list);
    }
    interface Interactor{
        void fetchData(String villageId, String gender, String age, SearchDetailsContract.InteractorCallBack callBack);
    }
}
