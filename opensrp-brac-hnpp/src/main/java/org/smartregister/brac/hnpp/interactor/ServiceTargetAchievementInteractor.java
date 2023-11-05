package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class ServiceTargetAchievementInteractor implements DashBoardContract.TargetInteractor {

    private AppExecutors appExecutors;
    private ArrayList<TargetVsAchievementData> dashBoardDataArrayList;
    private TargetVsAchievementModel model;

    public ServiceTargetAchievementInteractor(AppExecutors appExecutors, TargetVsAchievementModel model){
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
    public void setArrayListData(ArrayList<TargetVsAchievementData> targetVsAchievementData){
        dashBoardDataArrayList.clear();
        if(targetVsAchievementData !=null) dashBoardDataArrayList.addAll(targetVsAchievementData);
    }
    public void filterByFromToDate(String ssName, String fromDate, String toDate, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            model.setMonthWise(false);
            fetchDataByFromToFormat(fromDate, toDate, ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }


    public void filterByFromToMonth(String ssName, String fromMonth, String toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            model.setMonthWise(true);
            fetchDataByFromToFormat(fromMonth, toMonth, ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
    private void fetchDataByFromToFormat( String fromDate, String toDate, String ssName) {
        ArrayList<TargetVsAchievementData> initialList = getInitialTargetAchievement();
        ArrayList<TargetVsAchievementData> outPutList = model.getTargetVsAchievment(fromDate,toDate,ssName);
        ArrayList<TargetVsAchievementData> finalResult = mergeArrayList(initialList,outPutList);
        setArrayListData(finalResult);
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
    private ArrayList<TargetVsAchievementData> getInitialTargetAchievement(){
        ArrayList<TargetVsAchievementData> visitTypeList = new ArrayList<>();
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ANC_SERVICE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ANC_SERVICE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.PNC_SERVICE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.PNC_SERVICE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.NCD_PACKAGE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.NCD_PACKAGE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.IYCF_PACKAGE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.GIRL_PACKAGE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.EYE_TEST,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.EYE_TEST)));
        return visitTypeList;
    }
}
