package org.smartregister.brac.hnpp.interactor;


import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class ForumTargetAchievementInteractor implements DashBoardContract.ForumTargetInteractor {

    private AppExecutors appExecutors;
    private ArrayList<TargetVsAchievementData> dashBoardDataArrayList;
    private ArrayList<TargetVsAchievementData> forumArrayList;
    private ArrayList<TargetVsAchievementData> avgForumArrayList;
    private TargetVsAchievementModel model;

    public ForumTargetAchievementInteractor(AppExecutors appExecutors, TargetVsAchievementModel model){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        forumArrayList = new ArrayList<>();
        avgForumArrayList = new ArrayList<>();
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

//        Runnable runnable = () -> {
//            fetchData(day,month,year,ssName);
//
//            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
//        };
//        appExecutors.diskIO().execute(runnable);

    }

    private void fetchData( String day, String month, String year, String ssName) {
        if(HnppConstants.isPALogin()){

            fetchForumDataByFromToFormat(ssName,day,month,year);
            fetchAvgForumDataByFromToFormat(ssName,day,month,year);

            TargetVsAchievementData adoForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_ADO);
            TargetVsAchievementData avgAdoForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM);
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
        }else{
            fetchForumDataByFromToFormat(ssName,day,month,year);
            fetchAvgForumDataByFromToFormat(ssName,day,month,year);

            TargetVsAchievementData adoForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_ADO);
            TargetVsAchievementData avgAdoForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM);
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

            TargetVsAchievementData ncdForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_NCD);
            TargetVsAchievementData avgNcdForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM);
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

            TargetVsAchievementData childForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_CHILD);
            TargetVsAchievementData avgChildForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM);
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

            TargetVsAchievementData womenForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_WOMEN);
            TargetVsAchievementData avgWomenForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM);
            if(womenForum.getAchievementCount() !=0){
                womenForum.setAvgAchievmentCount(avgWomenForum.getAvgAchievmentCount() / womenForum.getAchievementCount());

            }
            womenForum.setAvgTargetCount(avgWomenForum.getAvgTargetCount());
            if(avgWomenForum.getAvgTargetCount() != 0){
                int percentage = (int) ((avgWomenForum.getAvgAchievmentCount() * 100)/avgWomenForum.getAvgTargetCount());
                avgWomenForum.setAvgAchievementPercentage(percentage);
            }
            womenForum.setAvgAchievementPercentage(avgWomenForum.getAvgAchievementPercentage());
            setData(womenForum);

            TargetVsAchievementData adultForum = getSpecificForumData(HnppConstants.EVENT_TYPE.FORUM_ADULT);
            TargetVsAchievementData avgAdultForum = getSpecificAvgForumData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM);
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

    private void fetchForumDataByFromToFormat( String ssName, String day, String month, String year) {
        ArrayList<TargetVsAchievementData> initialList = getInitialForumTargetAchievement();
        List<String> visitType = new ArrayList<>();
        for(TargetVsAchievementData targetVsAchievementData: initialList){
            visitType.add(targetVsAchievementData.getEventType());
        }
        ArrayList<TargetVsAchievementData> outPutList = model.getForumsTargetVsAchievement(visitType,day,month,year,ssName);
        ArrayList<TargetVsAchievementData> finalResult = mergeArrayList(initialList,outPutList);
        forumArrayList.clear();
        forumArrayList.addAll(finalResult);
    }
    private void fetchAvgForumDataByFromToFormat( String ssName, String day, String month, String year) {
        ArrayList<TargetVsAchievementData> initialList = getInitialAvgForumTargetAchievement();
        List<String> visitType = new ArrayList<>();
        for(TargetVsAchievementData targetVsAchievementData: initialList){
            visitType.add(targetVsAchievementData.getEventType());
        }
        ArrayList<TargetVsAchievementData> outPutList = model.getAvgTargetVsAchievmentByVisitType(visitType,day,month,year,ssName);
        ArrayList<TargetVsAchievementData> finalResult = mergeArrayList(initialList,outPutList);
        avgForumArrayList.clear();
        avgForumArrayList.addAll(finalResult);
    }
    private TargetVsAchievementData getSpecificForumData(String targetName){
            for(TargetVsAchievementData targetVsAchievementData:forumArrayList){
                if(targetVsAchievementData!=null && targetVsAchievementData.getEventType().equalsIgnoreCase(targetName)){
                    return targetVsAchievementData;
                }
            }

        return new TargetVsAchievementData();
    }
    private TargetVsAchievementData getSpecificAvgForumData(String targetName){
            for(TargetVsAchievementData targetVsAchievementData:avgForumArrayList){
                if(targetVsAchievementData!=null && targetVsAchievementData.getEventType().equalsIgnoreCase(targetName)){
                    return targetVsAchievementData;
                }
            }
        return new TargetVsAchievementData();
    }


    private ArrayList<TargetVsAchievementData> mergeArrayList(ArrayList<TargetVsAchievementData> initialList,ArrayList<TargetVsAchievementData> outputList){
        ArrayList<TargetVsAchievementData> finalList = new ArrayList<>();
        for(int i = 0 ;i < initialList.size() ;i++){
            TargetVsAchievementData data =  isContain(initialList.get(i),outputList);
            if(data != null){
                finalList.add(data);
            }else{
                finalList.add(initialList.get(i));
            }
        }
        return finalList;
    }
    private TargetVsAchievementData isContain(TargetVsAchievementData initial,ArrayList<TargetVsAchievementData> list){
        for(int i = 0; i< list.size() ; i++){
            if(list.get(i).getEventType().equalsIgnoreCase(initial.getEventType())){
                return list.get(i);
            }
        }
        return null;
    }
    private ArrayList<TargetVsAchievementData> getInitialForumTargetAchievement(){
        ArrayList<TargetVsAchievementData> visitTypeList = new ArrayList<>();
        if(HnppConstants.isPALogin()){
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_ADULT,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_ADULT)));

        }else{
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_ADO,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_ADO)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_NCD,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_NCD)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_CHILD,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_CHILD)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_WOMEN,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_WOMEN)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_ADULT,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_ADULT)));

        }

        return visitTypeList;
    }
    private ArrayList<TargetVsAchievementData> getInitialAvgForumTargetAchievement(){
        ArrayList<TargetVsAchievementData> visitTypeList = new ArrayList<>();
        if(HnppConstants.isPALogin()){
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM)));
        }else{
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM)));
            visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM)));

        }

        return visitTypeList;
    }
}
