package org.smartregister.brac.hnpp.contract;

import org.smartregister.brac.hnpp.model.DashBoardData;

import java.util.ArrayList;

public interface DashBoardContract {

    public interface InteractorCallBack{

        void updateList(ArrayList<DashBoardData> dashBoardData);

    }
    public interface Interactor{

        void fetchDashBoardData(String toDate, String fromDate, InteractorCallBack interactorCallBack);

    }
    public interface Model{
        Model getDashBoardModel();

    }
    public interface Presenter{

         void fetchDashBoardData(String toDate, String fromDate);

         View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

    }

}
