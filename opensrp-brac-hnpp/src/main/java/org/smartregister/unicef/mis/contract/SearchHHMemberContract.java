package org.smartregister.unicef.mis.contract;

import android.content.Context;

import org.smartregister.unicef.mis.model.HHMemberProperty;

import java.util.ArrayList;

public interface SearchHHMemberContract {

    public interface Presenter{

         void fetchHH(String village, String claster);
         void fetchAdo(String village, String claster);
         void fetchWomen(String village, String claster);
         void fetchChild(String village, String claster);
         void fetchNcd(String village, String claster);
        void fetchAdult(String village, String claster);
         void searchHH( String name);

         View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter(ArrayList<HHMemberProperty> list);

        Context getContext();

    }

}
