package org.smartregister.unicef.mis.contract;

import android.content.Context;

import org.smartregister.unicef.mis.model.ForumDetails;

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
