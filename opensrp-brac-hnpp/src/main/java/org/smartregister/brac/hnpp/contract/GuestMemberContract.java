package org.smartregister.brac.hnpp.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.brac.hnpp.utils.GuestMemberData;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
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
        View getView();
    }
    interface Interactor{

        void processAndSaveRegistration(final String jsonString,  final InteractorCallBack callBack);

        void fetchData(Context context, InteractorCallBack callBack);
        ArrayList<GuestMemberData> getAllGuestMemberList();
    }

    interface InteractorCallBack{
        void updateAdapter();
        void successfullySaved();
    }
}
