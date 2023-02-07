package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.model.ForumDetails;
import org.smartregister.unicef.dghs.model.Notification;

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
