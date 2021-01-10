package org.smartregister.brac.hnpp.interactor;


import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
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
    public void fetchAllData(DashBoardContract.InteractorCallBack callBack, String day, String month, String year, String ssName) {

        Runnable runnable = () -> {
            fetchData(day,month,year,ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }

    private void fetchData( String day, String month, String year, String ssName) {
        if(HnppConstants.isPALogin()){
//            TargetVsAchievementData ncdForum = model.getNcdForumTarget(day,month,year,ssName);
//            TargetVsAchievementData avgNcdForum = model.getAvgNcdTarget(day,month,year,ssName);
//            if(ncdForum.getAchievementCount() !=0){
//                ncdForum.setAvgAchievmentCount(avgNcdForum.getAvgAchievmentCount() / ncdForum.getAchievementCount());
//            }
//
//            ncdForum.setAvgTargetCount(avgNcdForum.getAvgTargetCount());
//            if(avgNcdForum.getAvgTargetCount() != 0){
//                int percentage = (int) ((avgNcdForum.getAvgAchievmentCount() * 100)/avgNcdForum.getAvgTargetCount());
//                avgNcdForum.setAvgAchievementPercentage(percentage);
//            }
//            ncdForum.setAvgAchievementPercentage(avgNcdForum.getAvgAchievementPercentage());
//            setData(ncdForum);
            TargetVsAchievementData adultForum = model.getAdultForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgAdultForum = model.getAvgAdultPATarget(day,month,year,ssName);
            if(adultForum.getAchievementCount() != 0 ){
                adultForum.setAvgAchievmentCount(avgAdultForum.getAvgAchievmentCount() /adultForum.getAchievementCount() );
            }

            adultForum.setAvgTargetCount(avgAdultForum.getAvgTargetCount());
            if(avgAdultForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgAdultForum.getAvgAchievmentCount() * 100)/avgAdultForum.getAvgTargetCount());
                avgAdultForum.setAvgAchievementPercentage(percentage);
            }
            adultForum.setAvgAchievementPercentage(avgAdultForum.getAvgAchievementPercentage());
            setData(adultForum);
        }else{
            TargetVsAchievementData adoForum = model.getAdoForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgAdoForum = model.getAvgAdoTarget(day,month,year,ssName);
            if(adoForum.getAchievementCount() !=0){
                adoForum.setAvgAchievmentCount(avgAdoForum.getAvgAchievmentCount()/adoForum.getAchievementCount());

            }
            adoForum.setAvgTargetCount(avgAdoForum.getAvgTargetCount());
            if(avgAdoForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgAdoForum.getAvgAchievmentCount() * 100)/avgAdoForum.getAvgTargetCount());
                avgAdoForum.setAvgAchievementPercentage(percentage);
            }
            adoForum.setAvgAchievementPercentage(avgAdoForum.getAvgAchievementPercentage());
            setData(adoForum);

            TargetVsAchievementData ncdForum = model.getNcdForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgNcdForum = model.getAvgNcdTarget(day,month,year,ssName);
            if(ncdForum.getAchievementCount() !=0){
                ncdForum.setAvgAchievmentCount(avgNcdForum.getAvgAchievmentCount() / ncdForum.getAchievementCount());
            }

            ncdForum.setAvgTargetCount(avgNcdForum.getAvgTargetCount());
            if(avgNcdForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgNcdForum.getAvgAchievmentCount() * 100)/avgNcdForum.getAvgTargetCount());
                avgNcdForum.setAvgAchievementPercentage(percentage);
            }
            ncdForum.setAvgAchievementPercentage(avgNcdForum.getAvgAchievementPercentage());
            setData(ncdForum);

            TargetVsAchievementData childForum = model.getChildForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgChildForum = model.getAvgChildTarget(day,month,year,ssName);
            if(childForum.getAchievementCount() !=0){
                childForum.setAvgAchievmentCount(avgChildForum.getAvgAchievmentCount() / childForum.getAchievementCount());

            }
            childForum.setAvgTargetCount(avgChildForum.getAvgTargetCount());
            if(avgChildForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgChildForum.getAvgAchievmentCount() * 100)/avgChildForum.getAvgTargetCount());
                avgChildForum.setAvgAchievementPercentage(percentage);
            }
            childForum.setAvgAchievementPercentage(avgChildForum.getAvgAchievementPercentage());
            setData(childForum);

            TargetVsAchievementData womenForum = model.getWomenForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgWomenForum = model.getAvgWomenTarget(day,month,year,ssName);
            if(womenForum.getAchievementCount() !=0){
                avgWomenForum.setAvgAchievmentCount(avgWomenForum.getAvgAchievmentCount() / womenForum.getAchievementCount());

            }
            womenForum.setAvgTargetCount(avgWomenForum.getAvgTargetCount());
            if(avgWomenForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgWomenForum.getAvgAchievmentCount() * 100)/avgWomenForum.getAvgTargetCount());
                avgWomenForum.setAvgAchievementPercentage(percentage);
            }
            womenForum.setAvgAchievementPercentage(avgWomenForum.getAvgAchievementPercentage());
            setData(womenForum);

            TargetVsAchievementData adultForum = model.getAdultForumTarget(day,month,year,ssName);
            TargetVsAchievementData avgAdultForum = model.getAvgAdultTarget(day,month,year,ssName);
            if(adultForum.getAchievementCount() != 0 ){
                adultForum.setAvgAchievmentCount(avgAdultForum.getAvgAchievmentCount() /adultForum.getAchievementCount() );
            }

            adultForum.setAvgTargetCount(avgAdultForum.getAvgTargetCount());
            if(avgAdultForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgAdultForum.getAvgAchievmentCount() * 100)/avgAdultForum.getAvgTargetCount());
                avgAdultForum.setAvgAchievementPercentage(percentage);
            }
            adultForum.setAvgAchievementPercentage(avgAdultForum.getAvgAchievementPercentage());
            setData(adultForum);
        }


    }

    @Override
    public void filterData(String ssName, String day, String month, String year,DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchData(day,month,year,ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
