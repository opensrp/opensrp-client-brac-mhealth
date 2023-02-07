package org.smartregister.unicef.dghs.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.unicef.dghs.utils.GuestMemberData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;

public interface GuestMemberContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void updateSuccessfullyFetchMessage();
        Presenter getPresenter();
        Context getContext();
    }
    interface Model{
        ArrayList<GuestMemberData> getData();
        Pair<Client, Event> processRegistration(String jsonString);
        void saveRegistration(Pair<Client, Event> pair);
        void loadData();
        Context getContext();
    }

    interface Presenter{
        ArrayList<GuestMemberData> getData();
        void saveMember(String jsonString);
        void fetchData();
        void filterData(String query, String ssName);
        View getView();
    }
    interface Interactor{

        void processAndSaveRegistration(final String jsonString,  final InteractorCallBack callBack);

        void fetchData(Context context, InteractorCallBack callBack);

        void filterData(Context context, String query, String ssName, InteractorCallBack callBack);

        ArrayList<GuestMemberData> getAllGuestMemberList();
    }

    interface InteractorCallBack{
        void updateAdapter();
        void successfullySaved();
    }
}
