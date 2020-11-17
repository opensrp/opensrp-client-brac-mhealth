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

        void filterData(String ssName, String month, String date,DashBoardContract.InteractorCallBack callBack);


    }
    public interface TargetInteractor {

        ArrayList<TargetVsAchievementData> getTargetListData();

        void fetchAllData(DashBoardContract.InteractorCallBack callBack, int day, int month, int year);

        void filterData(String ssName, int day, int month, int year, String date,DashBoardContract.InteractorCallBack callBack);


    }
    public interface Model{

        Model getDashBoardModel();

        Context getContext();

    }
    public interface Presenter{

         void fetchDashBoardData();

         void filterData(String ssName, String month, String date);

         View getView();

    }
    public interface TargetPresenter{

        void fetchDashBoardData(int day, int month, int year);

        void filterData(String ssName, String month, String date);

        View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateAdapter();

        Context getContext();

    }

}
