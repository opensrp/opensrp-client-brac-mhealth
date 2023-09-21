package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.location.UpdateLocationModel;

import java.util.ArrayList;

public interface UpdateLocationContract {

    public interface Presenter{
        ArrayList<UpdateLocationModel> getPaurashavaList();

        ArrayList<UpdateLocationModel> getUnion();

        ArrayList<UpdateLocationModel> getOldWard();

        ArrayList<UpdateLocationModel> getWard();

        void processPaurasava();

        void processUnion(String paurasavaId);

        void processOldWard(String unionId);

        void processWard(String oldWardId);

        void updateLocation();

        View getView();
    }

    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updatePaurashovaAdapter();

        void updateUnionAdapter();

        void updateOldWardAdapter();

        void updateWardAdapter();

        void onBlockUpdated(boolean status);


        Context getContext();

    }

}
