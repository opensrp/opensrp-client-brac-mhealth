package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.model.ForumDetails;
import org.smartregister.brac.hnpp.model.HHMemberProperty;

import java.util.ArrayList;

public interface ForumDetailsContract {

    public interface Presenter{

         void fetchAdo(String village, String claster);
         void fetchWomen(String village, String claster);
         void processChildForumEvent(ForumDetails forumDetails);
         void fetchNcd(String village, String claster);
         void searchHH(String name);

         View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();


        Context getContext();

    }

}
