package org.smartregister.unicef.mis.imci.contract;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.unicef.mis.utils.MemberHistoryData;
import org.smartregister.unicef.mis.utils.VisitHistory;

import java.util.ArrayList;

public interface IMCIAssessmentContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void initializePresenter();
        Presenter getPresenter();
        Context getContext();
        void startFormWithVisitData(MemberHistoryData content, JSONObject jsonForm);
        void updateANCTitle();
    }
    interface Presenter{
        void fetchData(String baseEntityId);
        void getVisitFormWithData(MemberHistoryData content);
        ArrayList<MemberHistoryData> getMemberHistory();
        View getView();
    }
    interface PresenterANC{
        void fetchANCData(String baseEntityId);
        void fetchCurrentTimeLineData(String baseEntityId);
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<MemberHistoryData> list);
        void updateFormWithData(MemberHistoryData content, JSONObject jsonForm);
    }
    interface InteractorCallBackANC{
        void onUpdateAncList(ArrayList<VisitHistory> list);
    }
    interface Interactor{
        void fetchData(Context context,String baseEntityId, IMCIAssessmentContract.InteractorCallBack callBack);
        void getVisitFormWithData(Context context,MemberHistoryData content, IMCIAssessmentContract.InteractorCallBack callBack);
    }
    interface InteractorANC{
        void fetchAncData(Context context,String baseEntityId, IMCIAssessmentContract.InteractorCallBackANC callBack);
        void fetchCurrentTimeLineData(Context context,String baseEntityId, IMCIAssessmentContract.InteractorCallBack callBack);
    }
}
