package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.IndicatorDashBoardModel;
import org.smartregister.brac.hnpp.model.WorkSummeryDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class WorkSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private WorkSummeryDashBoardModel model;
    private IndicatorDashBoardModel indicatorModel;

    public WorkSummeryDashBoardInteractor(AppExecutors appExecutors, WorkSummeryDashBoardModel model,IndicatorDashBoardModel indicatorModel){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.model = model;
        this.indicatorModel = indicatorModel;
    }

    @Override
    public ArrayList<DashBoardData> getListData() {
        return dashBoardDataArrayList;
    }

    private void addToDashBoardList(DashBoardData dashBoardData){
        if(dashBoardData !=null) dashBoardDataArrayList.add(dashBoardData);
    }

    @Override
    public void fetchAllData(DashBoardContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            fetchHHData("",-1,-1);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }

    //fetch data for from to month
    private void fetchHHData(String ssName, long fromMonth, long toMonth) {
        dashBoardDataArrayList.clear();

        if(HnppConstants.isPALogin()){
            addToDashBoardList(model.getEyeTestCount("",fromMonth,toMonth));
            addToDashBoardList(model.getBloodGroupingCount("",fromMonth,toMonth));
            addToDashBoardList(model.getNcdServiceCount("",fromMonth,toMonth));
            addToDashBoardList(model.getAdultForumCount("",fromMonth,toMonth));
            addToDashBoardList(model.getTotalGlassCount(fromMonth,toMonth));
        }else{
            addToDashBoardList(model.getHHCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getMemberCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getHHVisitCount(ssName,fromMonth,toMonth));
            if(indicatorModel!=null)addToDashBoardList(indicatorModel.getNoOfMemberVisited(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getElcoCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getANCRegisterCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getFirstTrimsterRegisterCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getSecondTrimsterRegisterCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getThirdTrimsterRegisterCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAnc1Count(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAnc2Count(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAnc3Count(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAncCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getDeliveryCount(ssName,fromMonth,toMonth));

            addToDashBoardList(model.getEncCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getChildFollowUpCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getNcdForumCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getNcdServiceCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getWomenForumCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getWomenServiceCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAdoForumCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAdoServiceCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getChildForumCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getChildServiceCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getAdultForumCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getPncCount(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getPncAfter48Count(ssName,fromMonth,toMonth));
            addToDashBoardList(model.getPncBefore48Count(ssName,fromMonth,toMonth));

            if(indicatorModel!=null){
                addToDashBoardList(indicatorModel.getAnotherSource(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.get4PlusAnc(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getCigerDelivery(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getNormalDelivery(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getTTPNCWomen(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getTTAncWomen(ssName,fromMonth,toMonth));
                //addToDashBoardList(indicatorModel.getPncService48Hrs(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getPnc1to2(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getPnc3to4(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getReferrelByPregnency(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getVaccineChild(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getVitaminChild(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getBcgChild(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getBrestFeedingByBirth(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getOnlyBrestFeeding(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getChildSevenMonth(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getDeathBirth(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getTotalDeath(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getMotherDeath(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getChildDeath(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getOtherDeath(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getEstimatedCoronaPatient(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getCoronaPatient(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getIsolationPatient(ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getRemoveMemberCount("সদস্য বাতিল",ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getRemoveHHCount("খানা বাতিল",ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getMigrateMemberCount("সদস্য স্থানান্তর",ssName,fromMonth,toMonth));
                addToDashBoardList(indicatorModel.getMigratedHHCount("খানা স্থানান্তর",ssName,fromMonth,toMonth));
            }

        }



    }

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,-1,-1);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }


    public void filterByFromToMonth(String ssName, long fromMonth, long toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,fromMonth,toMonth);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
