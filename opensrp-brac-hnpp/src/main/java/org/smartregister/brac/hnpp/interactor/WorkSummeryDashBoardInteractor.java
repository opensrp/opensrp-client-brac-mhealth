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
            fetchHHData("","","");

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    // need to maintain serial to display
    private void fetchHHData(String ssName, String month, String year) {
        dashBoardDataArrayList.clear();

        if(HnppConstants.isPALogin()){
            addToDashBoardList(model.getEyeTestCount(ssName,month,year));
            addToDashBoardList(model.getBloodGroupingCount(ssName,month,year));
            addToDashBoardList(model.getNcdServiceCount(ssName,month,year));
            addToDashBoardList(model.getAdultForumCount(ssName,month,year));
        }else{
            addToDashBoardList(model.getHHCount(ssName,month,year));
            addToDashBoardList(model.getMemberCount(ssName,month,year));
            addToDashBoardList(model.getElcoCount(ssName,month,year));
            addToDashBoardList(model.getANCRegisterCount(ssName,month,year));
            addToDashBoardList(model.getAnc1Count(ssName,month,year));
            addToDashBoardList(model.getAnc2Count(ssName,month,year));
            addToDashBoardList(model.getAnc3Count(ssName,month,year));
            addToDashBoardList(model.getAncCount(ssName,month,year));
            addToDashBoardList(model.getDeliveryCount(ssName,month,year));
            addToDashBoardList(model.getPncCount(ssName,month,year));
            addToDashBoardList(model.getEncCount(ssName,month,year));
            addToDashBoardList(model.getChildFollowUpCount(ssName,month,year));
            addToDashBoardList(model.getNcdForumCount(ssName,month,year));
            addToDashBoardList(model.getNcdServiceCount(ssName,month,year));
            addToDashBoardList(model.getWomenForumCount(ssName,month,year));
            addToDashBoardList(model.getWomenServiceCount(ssName,month,year));
            addToDashBoardList(model.getAdoForumCount(ssName,month,year));
            addToDashBoardList(model.getAdoServiceCount(ssName,month,year));
            addToDashBoardList(model.getChildForumCount(ssName,month,year));
            addToDashBoardList(model.getChildServiceCount(ssName,month,year));
            addToDashBoardList(model.getAdultForumCount(ssName,month,year));
            if(indicatorModel!=null){
                addToDashBoardList(indicatorModel.getAnotherSource(ssName,month,year));
                addToDashBoardList(indicatorModel.get4PlusAnc(ssName,month,year));
                addToDashBoardList(indicatorModel.getCigerDelivery(ssName,month,year));
                addToDashBoardList(indicatorModel.getNormalDelivery(ssName,month,year));
                addToDashBoardList(indicatorModel.getTTWomen(ssName,month,year));
                addToDashBoardList(indicatorModel.getPncService48Hrs(ssName,month,year));
                addToDashBoardList(indicatorModel.getPnc1to2(ssName,month,year));
                addToDashBoardList(indicatorModel.getPnc3to4(ssName,month,year));
                addToDashBoardList(indicatorModel.getReferrelByPregnency(ssName,month,year));
                addToDashBoardList(indicatorModel.getVaccineChild(ssName,month,year));
                addToDashBoardList(indicatorModel.getVitaminChild(ssName,month,year));
                addToDashBoardList(indicatorModel.getBrestFeedingByBirth(ssName,month,year));
                addToDashBoardList(indicatorModel.getOnlyBrestFeeding(ssName,month,year));
                addToDashBoardList(indicatorModel.getChildSevenMonth(ssName,month,year));
                addToDashBoardList(indicatorModel.getDeathBirth(ssName,month,year));
                addToDashBoardList(indicatorModel.getTotalDeath(ssName,month,year));
                addToDashBoardList(indicatorModel.getMotherDeath(ssName,month,year));
                addToDashBoardList(indicatorModel.getChildDeath(ssName,month,year));
                addToDashBoardList(indicatorModel.getOtherDeath(ssName,month,year));
                addToDashBoardList(indicatorModel.getEstimatedCoronaPatient(ssName,month,year));
                addToDashBoardList(indicatorModel.getCoronaPatient(ssName,month,year));
                addToDashBoardList(indicatorModel.getIsolationPatient(ssName,month,year));
                addToDashBoardList(indicatorModel.getRemoveMemberCount("সদস্য বাতিল",ssName,month,year));
                addToDashBoardList(indicatorModel.getRemoveHHCount("খানা বাতিল",ssName,month,year));
                addToDashBoardList(indicatorModel.getMigrateMemberCount("সদস্য স্থানান্তর",ssName,month,year));
                addToDashBoardList(indicatorModel.getMigratedHHCount("খানা স্থানান্তর",ssName,month,year));
            }


        }



    }

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchHHData(ssName,month,year);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);


    }
}
