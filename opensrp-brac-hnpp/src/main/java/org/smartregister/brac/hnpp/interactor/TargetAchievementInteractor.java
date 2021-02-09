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
            setData(model.getAdultForum(day,month,year,ssName));
            setData(model.getAttendancAdultForum(day,month,year,ssName));
            setData(model.getServiceCountAdultForum(day,month,year,ssName));
            setData(model.getMarkedPresbyopia(day,month,year,ssName));
            setData(model.getPresbyopiaCorrection(day,month,year,ssName));
            setData(model.getEstimateDiabetes(day,month,year,ssName));
            setData(model.getEstimateHBS(day,month,year,ssName));
            setData(model.getCataractSurgery(day,month,year,ssName));
            setData(model.getCataractSurgeryRefer(day,month,year,ssName));
        }else{
            setData(model.getHHVisitTarget(day,month,year,ssName));
            setData(model.getElcoTarget(day,month,year,ssName));
            setData(model.getMethodUserTarget(day,month,year,ssName));
            setData(model.getAdoMethodUserTarget(day,month,year,ssName));
            setData(model.getPregnencyIdentiTarget(day,month,year,ssName));
            setData(model.getDeliveryTarget(day,month,year,ssName));
            setData(model.getInstitutionDeliveryTarget(day,month,year,ssName));
            setData(model.get0to6ChildVisitTarget(day,month,year,ssName));
            setData(model.get7to24ChildVisitTarget(day,month,year,ssName));
            setData(model.get18to36ChildVisitTarget(day,month,year,ssName));
            setData(model.get0to59ChildImmunizationTarget(day,month,year,ssName));
        }


    }
    private void fetchDataByFromToDate( String fromDate, String toDate, String ssName) {
        if(HnppConstants.isPALogin()){
            setData(model.getAdultForum(fromDate,toDate,ssName));
            setData(model.getAttendancAdultForum(fromDate,toDate,ssName));
            setData(model.getServiceCountAdultForum(fromDate,toDate,ssName));
            setData(model.getMarkedPresbyopia(fromDate,toDate,ssName));
            setData(model.getPresbyopiaCorrection(fromDate,toDate,ssName));
            setData(model.getEstimateDiabetes(fromDate,toDate,ssName));
            setData(model.getEstimateHBS(fromDate,toDate,ssName));
            setData(model.getCataractSurgery(fromDate,toDate,ssName));
            setData(model.getCataractSurgeryRefer(fromDate,toDate,ssName));
        }else{
            setData(model.getHHVisitTarget(fromDate,toDate,ssName));
            setData(model.getElcoTarget(fromDate,toDate,ssName));
            setData(model.getMethodUserTarget(fromDate,toDate,ssName));
            setData(model.getAdoMethodUserTarget(fromDate,toDate,ssName));
            setData(model.getPregnencyIdentiTarget(fromDate,toDate,ssName));
            setData(model.getDeliveryTarget(fromDate,toDate,ssName));
            setData(model.getInstitutionDeliveryTarget(fromDate,toDate,ssName));
            setData(model.get0to6ChildVisitTarget(fromDate,toDate,ssName));
            setData(model.get7to24ChildVisitTarget(fromDate,toDate,ssName));
            setData(model.get18to36ChildVisitTarget(fromDate,toDate,ssName));
            setData(model.get0to59ChildImmunizationTarget(fromDate,toDate,ssName));
        }


    }
    private void fetchDataByFromToMonth( String fromMonth, String toMonth, String ssName) {
        if(HnppConstants.isPALogin()){
            setData(model.getAdultForum(fromMonth,toMonth,ssName));
            setData(model.getAttendancAdultForum(fromMonth,toMonth,ssName));
            setData(model.getServiceCountAdultForum(fromMonth,toMonth,ssName));
            setData(model.getMarkedPresbyopia(fromMonth,toMonth,ssName));
            setData(model.getPresbyopiaCorrection(fromMonth,toMonth,ssName));
            setData(model.getEstimateDiabetes(fromMonth,toMonth,ssName));
            setData(model.getEstimateHBS(fromMonth,toMonth,ssName));
            setData(model.getCataractSurgery(fromMonth,toMonth,ssName));
            setData(model.getCataractSurgeryRefer(fromMonth,toMonth,ssName));
        }else{
            setData(model.getHHVisitTarget(fromMonth,toMonth,ssName));
            setData(model.getElcoTarget(fromMonth,toMonth,ssName));
            setData(model.getMethodUserTarget(fromMonth,toMonth,ssName));
            setData(model.getAdoMethodUserTarget(fromMonth,toMonth,ssName));
            setData(model.getPregnencyIdentiTarget(fromMonth,toMonth,ssName));
            setData(model.getDeliveryTarget(fromMonth,toMonth,ssName));
            setData(model.getInstitutionDeliveryTarget(fromMonth,toMonth,ssName));
            setData(model.get0to6ChildVisitTarget(fromMonth,toMonth,ssName));
            setData(model.get7to24ChildVisitTarget(fromMonth,toMonth,ssName));
            setData(model.get18to36ChildVisitTarget(fromMonth,toMonth,ssName));
            setData(model.get0to59ChildImmunizationTarget(fromMonth,toMonth,ssName));
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

    @Override
    public void filterByFromToDate(String ssName, String fromDate, String toDate, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchDataByFromToDate(fromDate, toDate, ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void filterByFromToMonth(String ssName, String fromMonth, String toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchDataByFromToMonth(fromMonth, toMonth, ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
