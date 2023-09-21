package org.smartregister.unicef.dghs.interactor;

import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.model.CountSummeryDashBoardModel;
import org.smartregister.unicef.dghs.model.ImmunizationSummeryDashBoardModel;
import org.smartregister.unicef.dghs.model.IndicatorDashBoardModel;
import org.smartregister.unicef.dghs.utils.DashBoardData;

import java.util.ArrayList;

public class ImmunizationSummeryDashBoardInteractor implements DashBoardContract.Interactor {

    private AppExecutors appExecutors;
    private ArrayList<DashBoardData> dashBoardDataArrayList;
    private ImmunizationSummeryDashBoardModel model;
    private IndicatorDashBoardModel indicatorModel;

    public ImmunizationSummeryDashBoardInteractor(AppExecutors appExecutors, ImmunizationSummeryDashBoardModel model, IndicatorDashBoardModel indicatorModel){
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
            fetchImmunizationData("",-1,-1);
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);

    }
    private void fetchImmunizationData(String blockId, long fromMonth, long toMonth){
        dashBoardDataArrayList.clear();
        addToDashBoardList(model.getTotalChildCount(HnppApplication.getInstance().getString(R.string.no_child_reg),blockId,fromMonth,toMonth));

        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_bcg),blockId,fromMonth,toMonth,"bcg"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_opv0),blockId,fromMonth,toMonth,"opv_0"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_opv1),blockId,fromMonth,toMonth,"opv_1"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_opv2),blockId,fromMonth,toMonth,"opv_2"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_opv3),blockId,fromMonth,toMonth,"opv_3"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_penta1),blockId,fromMonth,toMonth,"penta_1"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_penta2),blockId,fromMonth,toMonth,"penta_2"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_penta3),blockId,fromMonth,toMonth,"penta_3"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_pcv1),blockId,fromMonth,toMonth,"pcv_1"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_pcv2),blockId,fromMonth,toMonth,"pcv_2"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_pcv3),blockId,fromMonth,toMonth,"pcv_3"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_fipv1),blockId,fromMonth,toMonth,"fipv_1"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_fipv2),blockId,fromMonth,toMonth,"fipv_2"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_mr1),blockId,fromMonth,toMonth,"mr_1"));
        addToDashBoardList(model.getImmunizationCount(HnppApplication.getInstance().getString(R.string.no_mr2),blockId,fromMonth,toMonth,"mr_2"));

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
            fetchImmunizationData(ssName,fromMonth,toMonth);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }
}
