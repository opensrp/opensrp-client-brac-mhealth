package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.model.CountSummeryDashBoardModel;
import org.smartregister.brac.hnpp.model.DashBoardModel;
import org.smartregister.brac.hnpp.model.IndicatorDashBoardModel;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class CountSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private CountSummeryDashBoardModel model;
    private IndicatorDashBoardModel indicatorModel;

    public CountSummeryDashBoardInteractor(AppExecutors appExecutors, CountSummeryDashBoardModel model,IndicatorDashBoardModel indicatorModel){
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
    // need to maintain serial to display
    private void fetchHHData(String ssName, long fromMonth, long toMonth) {
        dashBoardDataArrayList.clear();
        addToDashBoardList(model.getHHCount(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getMemberCount(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getHHVisitCount(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getOOCCount(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getSimprintsCount(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getBoyChildUnder5(ssName,fromMonth,toMonth));
        addToDashBoardList(model.getGirlChildUnder5( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getBoyChild5To9( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getGirlChild5To9( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getBoyChild10To19( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getGirlChild10To19( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getBoyChild20To50( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getGirlChild20To50( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getMenUp50( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getWoMenUp50( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getAdoGirl( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getAdoBoy( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getAdoElco( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getEddThisMonth( ssName,fromMonth,toMonth));
        addToDashBoardList(model.getRiskMother( ssName,fromMonth,toMonth));
        if(indicatorModel !=null){
            addToDashBoardList(indicatorModel.getFamilyMethodKnown( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getNoFamilyMethodUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getFillUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getFillFromSS( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getFillFromOther( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getCondomUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getIudUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getInjectionUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getNorplantUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getVasectomyUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getTubeUser( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getVerifiedBySimprints( ssName,fromMonth,toMonth));
            addToDashBoardList(indicatorModel.getIdentifiedBySimprints( ssName,fromMonth,toMonth));
        }

    }

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            //TODO not needed
            fetchHHData( ssName,-1,-1);

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
