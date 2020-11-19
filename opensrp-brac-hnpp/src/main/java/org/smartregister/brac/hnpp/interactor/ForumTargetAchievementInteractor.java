package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class ForumTargetAchievementInteractor implements DashBoardContract.TargetInteractor {

    private AppExecutors appExecutors;
    private ArrayList<TargetVsAchievementData> dashBoardDataArrayList;
    private TargetVsAchievementModel model;

    public ForumTargetAchievementInteractor(AppExecutors appExecutors, TargetVsAchievementModel model){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.model = model;
    }

    @Override
    public ArrayList<TargetVsAchievementData> getTargetListData() {
        return dashBoardDataArrayList;
    }

    public void setData(TargetVsAchievementData targetVsAchievementData){
        if(targetVsAchievementData !=null) dashBoardDataArrayList.add(targetVsAchievementData);
    }

    @Override
    public void fetchAllData(DashBoardContract.InteractorCallBack callBack, int day, int month, int year, String ssName) {

        Runnable runnable = () -> {
            fetchData(day,month,year,ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }

    private void fetchData( int day, int month, int year, String ssName) {

        TargetVsAchievementData adoForum = model.getAdoForumTarget(day,month,year,ssName);
        TargetVsAchievementData avgAdoForum = model.getAvgAdoTarget(day,month,year,ssName);
        adoForum.setAvgAchievmentCount(avgAdoForum.getAvgAchievmentCount());
        adoForum.setAvgTargetCount(avgAdoForum.getAvgTargetCount());
        adoForum.setAvgAchievementPercentage(avgAdoForum.getAvgAchievementPercentage());
        setData(adoForum);

        TargetVsAchievementData ncdForum = model.getNcdForumTarget(day,month,year,ssName);
        TargetVsAchievementData avgNcdForum = model.getAvgNcdTarget(day,month,year,ssName);
        ncdForum.setAvgAchievmentCount(avgNcdForum.getAvgAchievmentCount());
        ncdForum.setAvgTargetCount(avgNcdForum.getAvgTargetCount());
        ncdForum.setAvgAchievementPercentage(avgNcdForum.getAvgAchievementPercentage());
        setData(ncdForum);

        TargetVsAchievementData childForum = model.getChildForumTarget(day,month,year,ssName);
        TargetVsAchievementData avgChildForum = model.getAvgChildTarget(day,month,year,ssName);
        childForum.setAvgAchievmentCount(avgChildForum.getAvgAchievmentCount());
        childForum.setAvgTargetCount(avgChildForum.getAvgTargetCount());
        childForum.setAvgAchievementPercentage(avgChildForum.getAvgAchievementPercentage());
        setData(childForum);

        TargetVsAchievementData womenForum = model.getWomenForumTarget(day,month,year,ssName);
        TargetVsAchievementData avgWomenForum = model.getAvgWomenTarget(day,month,year,ssName);
        womenForum.setAvgAchievmentCount(avgWomenForum.getAvgAchievmentCount());
        womenForum.setAvgTargetCount(avgWomenForum.getAvgTargetCount());
        womenForum.setAvgAchievementPercentage(avgWomenForum.getAvgAchievementPercentage());
        setData(womenForum);

        TargetVsAchievementData adultForum = model.getAdultForumTarget(day,month,year,ssName);
        TargetVsAchievementData avgAdultForum = model.getAvgAdultTarget(day,month,year,ssName);
        adultForum.setAvgAchievmentCount(avgAdultForum.getAvgAchievmentCount());
        adultForum.setAvgTargetCount(avgAdultForum.getAvgTargetCount());
        adultForum.setAvgAchievementPercentage(avgAdultForum.getAvgAchievementPercentage());
        setData(adultForum);
    }

    @Override
    public void filterData(String ssName, int day, int month, int year,DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchData(day,month,year,ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
