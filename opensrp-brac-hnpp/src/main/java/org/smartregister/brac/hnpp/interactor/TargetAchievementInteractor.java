package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.TargetVsAchievementModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TargetAchievementInteractor implements DashBoardContract.TargetInteractor {

    private AppExecutors appExecutors;
    private ArrayList<TargetVsAchievementData> dashBoardDataArrayList;
    private TargetVsAchievementModel model;

    public TargetAchievementInteractor(AppExecutors appExecutors, TargetVsAchievementModel model){
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
    private void fetchDataByFromToFormat( String fromDate, String toDate, String ssName, boolean isMonthWise) {
        model.setMonthWise(isMonthWise);
        if(HnppConstants.isPALogin()){
            ArrayList<TargetVsAchievementData> initialList = getInitialTargetAchievementForPA();
            ArrayList<TargetVsAchievementData> outPutList = model.getTargetVsAchievment(fromDate,toDate,ssName);
            ArrayList<TargetVsAchievementData> finalResult = mergeArrayList(initialList,outPutList);
            setArrayListData(finalResult);

        }else{
            ArrayList<TargetVsAchievementData> initialList = getInitialTargetAchievement();
            ArrayList<TargetVsAchievementData> outPutList = model.getTargetVsAchievment(fromDate,toDate,ssName);
            ArrayList<TargetVsAchievementData> finalResult = mergeArrayList(initialList,outPutList);
            setArrayListData(finalResult);
        }


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
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ELCO,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ELCO)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.METHOD_USER,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.METHOD_USER)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ADO_METHOD_USER)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CHILD_VISIT_0_6,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_VISIT_0_6)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CHILD_VISIT_7_24,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_VISIT_7_24)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CHILD_VISIT_18_36,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_VISIT_18_36)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59)));

        return visitTypeList;
    }
    private ArrayList<TargetVsAchievementData> getInitialTargetAchievementForPA(){
        ArrayList<TargetVsAchievementData> visitTypeList = new ArrayList<>();
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.FORUM_ADULT,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.FORUM_ADULT)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.ESTIMATE_HBP)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER)));
        visitTypeList.add(new TargetVsAchievementData(HnppConstants.EVENT_TYPE.CATARACT_SURGERY,HnppConstants.targetTypeMapping.get(HnppConstants.EVENT_TYPE.CATARACT_SURGERY)));

        return visitTypeList;
    }
    public void filterByFromToDate(String ssName, String fromDate, String toDate, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchDataByFromToFormat(fromDate, toDate, ssName,false);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }


    public void filterByFromToMonth(String ssName, String fromMonth, String toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchDataByFromToFormat(fromMonth, toMonth, ssName,true);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
