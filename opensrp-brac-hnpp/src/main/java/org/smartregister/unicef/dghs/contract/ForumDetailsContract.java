package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.model.ForumDetails;
import org.smartregister.unicef.dghs.model.HHMemberProperty;

import java.util.ArrayList;

public interface ForumDetailsContract {

    public interface Presenter{

         void processAdoForum(ForumDetails forumDetails);
         void processWomenForum(ForumDetails forumDetails);
         void processChildForumEvent(ForumDetails forumDetails);
         void processNcdForum(ForumDetails forumDetails);
        void processAdultForum(ForumDetails forumDetails);
         void searchHH(String name);

         View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void showSuccessMessage(String message);

        void  showFailedMessage(String message);

        Context getContext();

    }

}
