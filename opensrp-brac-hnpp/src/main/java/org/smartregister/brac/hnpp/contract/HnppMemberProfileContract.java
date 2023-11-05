package org.smartregister.brac.hnpp.contract;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.utils.MemberProfileDueData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;

public interface HnppMemberProfileContract {
    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateView();
        HnppMemberProfileContract.Presenter getPresenter();
        Context getContext();
    }
    interface Presenter{
        void fetchData(CommonPersonObjectClient  commonPersonObjectClient,String baseEntityId);
        ArrayList<MemberProfileDueData> getData();
        View getView();
    }
    interface Model{
        ArrayList<MemberProfileDueData> getData();
    }

    interface Interactor{
        void fetchData(CommonPersonObjectClient commonPersonObjectClient,
                       Fragment fragment,
                       String baseEntityId,
                       HnppMemberProfileContract.InteractorCallBack callBack);
        String getLastEvent();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<MemberProfileDueData> memberProfileDueData);
    }
}
