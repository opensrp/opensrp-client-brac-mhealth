package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.repository.StockRepository;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;

public class StockDashBoardModel implements DashBoardContract.Model {

    private Context context;

    public StockDashBoardModel(Context context){
        this.context = context;
    }

    public DashBoardData getAncCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EventType.ANC_HOME_VISIT);
    }
    public DashBoardData getPncCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EventType.PNC_HOME_VISIT);
    }
    public DashBoardData getAdoServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.GIRL_PACKAGE);
    }
    public DashBoardData getChildServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.IYCF_PACKAGE);
    }
    public DashBoardData getNcdServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.NCD_PACKAGE);
    }
    public DashBoardData getWomenServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE);
    }



    public DashBoardData getVisitTypeCount(String visitType){
        DashBoardData dashBoardData1 = new DashBoardData();
        String query = "select sum("+ StockRepository.STOCK_QUANTITY+") as count, sum("+StockRepository.ACHIEVEMNT_COUNT+") as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"'";

        Log.v("STOCK","visit_type:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                int quantity = cursor.getInt(0);
                int achievement = cursor.getInt(1);
                dashBoardData1.setCount(quantity-achievement);
                dashBoardData1.setEventType(visitType);
                dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }

    @Override
    public DashBoardContract.Model getDashBoardModel() {
        return this;
    }

    @Override
    public Context getContext() {
        return context;
    }
}

