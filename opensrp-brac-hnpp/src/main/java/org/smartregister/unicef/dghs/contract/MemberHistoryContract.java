package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;

import java.util.ArrayList;

public interface MemberHistoryContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void initializePresenter();
        Presenter getPresenter();
        void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm);
    }
    interface Presenter{
        void fetchData(String baseEntityId);
        void getVisitFormWithData(MemberHistoryData content);
        ArrayList<MemberHistoryData> getMemberHistory();
        View getView();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<MemberHistoryData> list);
        void updateFormWithData(MemberHistoryData content, JSONObject jsonForm);
    }
    interface Interactor{
        void fetchData(Context context,String baseEntityId, MemberHistoryContract.InteractorCallBack callBack);
        void getVisitFormWithData(Context context,MemberHistoryData content, MemberHistoryContract.InteractorCallBack callBack);
    }
}
