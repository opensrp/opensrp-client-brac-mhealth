package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.Notification;

public interface NotificationContract {

    public interface Presenter{
        void processNotification();

        View getView();
    }

    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

        Context getContext();

    }

}
