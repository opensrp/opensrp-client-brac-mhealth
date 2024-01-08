package org.smartregister.unicef.mis.interactor;

import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.model.GMPDashBoardModel;
import org.smartregister.unicef.mis.model.ImmunizationSummeryDashBoardModel;
import org.smartregister.unicef.mis.model.IndicatorDashBoardModel;
import org.smartregister.unicef.mis.utils.DashBoardData;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.util.ArrayList;

public class GMPDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private GMPDashBoardModel model;

    public GMPDashBoardInteractor(AppExecutors appExecutors, GMPDashBoardModel model){
        this.appExecutors = appExecutors;
        dashBoardDataArrayList = new ArrayList<>();
        this.model = model;
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
            fetchGMPData("",-1,-1);
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    private void fetchGMPData(String blockId, long fromMonth, long toMonth){
        if(fromMonth ==-1) {
            toMonth = -1;
        }
        else{
            toMonth = toMonth+ HnppConstants.SIX_HOUR;
        }

        /*
        Monthly reporting dashboard
o D1 = Number of children 0-23 months newly enrolled (unique cases) in this month
o D2 = Total number of children 0-23 months attended (both newly enrolled and followed-up) in this month
o N1 = Number of children 0-23 months weight measured in this month
o N2 = Number of children 0-23 months length/height measured in this month
o N3 = Number of children 0-23 months MUAC measured in this month
o S1 = Number of children 0-23 months were underweight (<-2SD)
o S2 = Number of children 0-23 months were severely underweight (<-3SD)
o S3 = Number of children 0-23 months were stunted (<-2SD) diagnosed at facility
o S4 = Number of children 0-23 months were severely stunted (<-3SD) diagnosed at facility
o S5 = Number of children 6-23 months with MUAC between >11.5 cm and <12.5 cm
o S6 = Number of children 6-23 months with MUAC < 11.5 cm
o S7 = Number of children 0-23 months diagnosed with growth faltering
o G1 = Number of children 0-23 months referred from outreach to the Community Clinic for further diagnosis
o G2 = Number of mothers and caregivers of children 0-23 months received counseling on IYCF and early childhood development
o G3 = Number of children aged 0-23 months visited the Community Clinics referred from outreach
o G4= Number of children aged 0-23 months visited the Upazila Health Complex referred from CC and or from outreach
o
         */
        dashBoardDataArrayList.clear();
        //D1 Number of children 0-23 months newly enrolled (unique cases) in this month
        addToDashBoardList(model.getUniqueInThisMonthCount("Number of children newly enrolled in this month",fromMonth,toMonth));
        addToDashBoardList(model.getGMPCount("Total number of children attended",fromMonth,toMonth));

        addToDashBoardList(model.getWeightCount("Number of children weight measured",fromMonth,toMonth));
        addToDashBoardList(model.getHeightCount("Number of children length/height measured",fromMonth,toMonth));
        addToDashBoardList(model.getMUACCount("Number of children MUAC measured",fromMonth,toMonth));
        addToDashBoardList(model.getUnderWeightCount("Number of children were underweight",fromMonth,toMonth));
        addToDashBoardList(model.getSeverelyUnderWeightCount("Number of children were severely underweight",fromMonth,toMonth));
        addToDashBoardList(model.getStuntedCount("Number of children were stunted",fromMonth,toMonth));
        addToDashBoardList(model.getSeverelyStuntedCount("Number of children were severely stunted",fromMonth,toMonth));
        addToDashBoardList(model.getMuacMamCount("Number of children with MUAC MAM",fromMonth,toMonth));
        addToDashBoardList(model.getMuacSamCount("Number of children with MUAC SAM",fromMonth,toMonth));
        addToDashBoardList(model.getGrowthFaltering("Number of children diagnosed with growth faltering",fromMonth,toMonth));

        addToDashBoardList(model.getChildRefCount(HnppApplication.getInstance().getString(R.string.no_child_ref),fromMonth,toMonth));
        addToDashBoardList(model.getChildGmpCounselingCount(HnppApplication.getInstance().getString(R.string.no_gmp_counceling),fromMonth,toMonth));
        addToDashBoardList(model.getChildRefFollowupCount(HnppApplication.getInstance().getString(R.string.no_child_ref_followup),fromMonth,toMonth));
        addToDashBoardList(model.getRefVisitedCount("Number of children visited the UHC",fromMonth,toMonth));
        addToDashBoardList(model.getCCVisitedCount("Number of children visited the Community Clinics referred from outreach",fromMonth,toMonth));

//
//
//        addToDashBoardList(model.getTotalChildCount(HnppApplication.getInstance().getString(R.string.no_child_reg),blockId,fromMonth,toMonth));

    }
    // need to maintain serial to display

    @Override
    public void filterData(String ssName, String month , String year, DashBoardContract.InteractorCallBack callBack) {
//        dashBoardDataArrayList.clear();
//        Runnable runnable = () -> {
//            //TODO not needed
//            fetchHHData( ssName,-1,-1);
//
//            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
//        };
//        appExecutors.diskIO().execute(runnable);


    }
    public void filterByFromToMonth(String ssName, long fromMonth, long toMonth, DashBoardContract.InteractorCallBack callBack) {
        dashBoardDataArrayList.clear();
        Runnable runnable = () -> {
            fetchGMPData(ssName,fromMonth,toMonth);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

}
