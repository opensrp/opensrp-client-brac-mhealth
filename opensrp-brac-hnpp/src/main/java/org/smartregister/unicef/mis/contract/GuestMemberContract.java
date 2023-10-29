package org.smartregister.unicef.mis.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.unicef.mis.utils.GuestMemberData;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

import java.util.ArrayList;

public interface GuestMemberContract {

    interface View{
        void showProgressBar();
        void hideProgressBar();
        void updateAdapter();
        void updateSuccessfullyFetchMessage();
        Presenter getPresenter();
        Context getContext();
        void openProfile(String baseEntityId);
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
        void saveMember(String jsonString, boolean isEdited);
        void fetchData();
        void filterData(String query, String ssName);
        void updateSHRIdFromServer();
        View getView();
    }
    interface Interactor{

        void processAndSaveRegistration(final String jsonString,  final InteractorCallBack callBack, boolean isEdited);

        void fetchData(Context context, InteractorCallBack callBack);

        void filterData(Context context, String query, String ssName, InteractorCallBack callBack);

        ArrayList<GuestMemberData> getAllGuestMemberList();

        void updateSHRIdFromServer(Context context, InteractorCallBack callBack);
    }

    interface InteractorCallBack{
        void updateAdapter();
        void successfullySaved();
        void openRegisteredProfile(String baseEntityId);
    }
}
