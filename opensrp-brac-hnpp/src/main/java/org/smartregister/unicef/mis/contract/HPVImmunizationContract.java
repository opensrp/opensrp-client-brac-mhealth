package org.smartregister.unicef.mis.contract;

import android.content.Context;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.unicef.mis.location.HALocation;
import org.smartregister.unicef.mis.location.HPVLocation;
import org.smartregister.unicef.mis.model.GlobalSearchResult;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;

import java.util.ArrayList;

public interface HPVImmunizationContract {
    interface View{
        Presenter getPresenter();
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        Context getContext();
    }
    interface Presenter{
        void fetchData(GlobalSearchContentData globalSearchContentData);
        ArrayList<Client> getMemberList();
        GlobalSearchResult getGlobalSearchResult();
        View getView();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<HPVLocation> list);
        void enrolSuccessfully(String message);
        void enrolFail(String message);
        void onUpdateOtherVaccine(OtherVaccineContentData otherVaccineContentData);
    }
    interface Interactor{
        void fetchCenterList(String baseEntityId, HPVImmunizationContract.InteractorCallBack callBack);
        void postEnrolmentData(String baseEntityId, HPVLocation location, HPVImmunizationContract.InteractorCallBack callBack);
        void fetchOtherVaccineData(String baseEntityId, HPVImmunizationContract.InteractorCallBack callBack);
    }
}
