package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.utils.MemberProfileDueData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
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
                       Context context,
                       String baseEntityId,
                       HnppMemberProfileContract.InteractorCallBack callBack);
        String getLastEvent();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<MemberProfileDueData> memberProfileDueData);
    }
}
