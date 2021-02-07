package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;

import java.util.ArrayList;

public interface DashBoardContract {

    public interface InteractorCallBack{



        void fetchedSuccessfully();

    }
    public interface Interactor{

        ArrayList<DashBoardData> getListData();

        void fetchAllData(DashBoardContract.InteractorCallBack callBack);

        void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack);


    }
    public interface TargetInteractor {

        ArrayList<TargetVsAchievementData> getTargetListData();

        void fetchAllData(DashBoardContract.InteractorCallBack callBack, String day, String month, String year, String ssName);

        void filterData(String ssName, String day, String month, String year, DashBoardContract.InteractorCallBack callBack);
        void filterByFromToDate(String ssName, String fromDate, String toDate, DashBoardContract.InteractorCallBack callBack);

    }
    public interface Model{

        Model getDashBoardModel();

        Context getContext();

    }
    public interface Presenter{

         void fetchDashBoardData();

         void filterData(String ssName, String month, String year);

         View getView();

    }
    public interface TargetPresenter{

        void fetchDashBoardData(String day, String month, String year, String ssName);

        void filterData(String ssName, String day, String month, String year);
        void filterByFromToDate(String fromDate, String toDate, String ssName);
        View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

        Context getContext();

    }

}
