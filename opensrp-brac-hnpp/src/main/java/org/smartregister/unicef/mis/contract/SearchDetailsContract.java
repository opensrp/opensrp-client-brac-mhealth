package org.smartregister.unicef.mis.contract;

import android.content.Context;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.unicef.mis.model.GlobalSearchResult;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;

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
        void fetchData(GlobalSearchContentData globalSearchContentData);
        ArrayList<Client> getMemberList();
        GlobalSearchResult getGlobalSearchResult();
        View getView();
    }
    interface InteractorCallBack{
        void onUpdateList(ArrayList<Client> list);
        void setGlobalSearchResult(GlobalSearchResult globalSearchResult);
        void onUpdateOtherVaccine(OtherVaccineContentData otherVaccineContentData);
    }
    interface Interactor{
        void fetchData(GlobalSearchContentData globalSearchContentData, SearchDetailsContract.InteractorCallBack callBack);
        void fetchOtherVaccineData(OtherVaccineContentData otherVaccineContentData,SearchDetailsContract.InteractorCallBack callBack);

        default void fetchAddMemberSearchData(GlobalSearchContentData globalSearchContentData, SearchDetailsContract.InteractorCallBack callBack) {

        }
    }
}
