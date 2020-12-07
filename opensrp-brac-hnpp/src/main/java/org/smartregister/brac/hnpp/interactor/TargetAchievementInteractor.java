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
    public void fetchAllData(DashBoardContract.InteractorCallBack callBack, int day, int month, int year, String ssName) {

        Runnable runnable = () -> {
            fetchData(day,month,year,ssName);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }

    private void fetchData( int day, int month, int year, String ssName) {
        String m = HnppConstants.addZeroForMonth(month+"");
        if(!TextUtils.isEmpty(m)){
            month = Integer.parseInt(m);
        }
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