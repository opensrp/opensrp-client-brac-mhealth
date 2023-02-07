package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.model.Migration;

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
        void fetchData(String type,String districtId, String villageId, String gender,String startAge, String age);
        ArrayList<Migration> getMemberList();
        View getView();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<Migration> list);
    }
    interface Interactor{
        void fetchData(String type,String districtId, String villageId, String gender, String startAge, String age, SearchDetailsContract.InteractorCallBack callBack);
    }
}
