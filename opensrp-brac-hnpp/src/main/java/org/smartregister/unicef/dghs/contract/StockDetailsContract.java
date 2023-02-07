package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.StockDetailsData;
import org.smartregister.unicef.dghs.utils.TargetVsAchievementData;

import java.util.ArrayList;

public interface StockDetailsContract {

    public interface InteractorCallBack{
        void fetchedSuccessfully();

    }
    public interface Interactor{

        ArrayList<StockDetailsData> getStockDetailsData();


        void filterData(String ssName, String month , String year, StockDetailsContract.InteractorCallBack callBack);


    }
    public interface Model{


        Context getContext();

    }
    public interface Presenter{

         //void fetchDashBoardData();

         void filterData(String ssName, String month,String year);

         View getView();

    }
    public interface TargetPresenter{

        void fetchDashBoardData(int day, int month, int year, String ssName);

        void filterData(String ssName, int day, int month, int year);

        View getView();

    }
    public interface View{

        void showProgressBar();

        void hideProgressBar();

        void updateView();

        Context getContext();

    }

}
