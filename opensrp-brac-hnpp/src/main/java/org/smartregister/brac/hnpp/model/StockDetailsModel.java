package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.smartregister.brac.hnpp.contract.StockDetailsContract;
import org.smartregister.brac.hnpp.repository.StockRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.StockData;
import org.smartregister.brac.hnpp.utils.StockDetailsData;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;

public class StockDetailsModel implements StockDetailsContract.Model{
    private Context context;

    public StockDetailsModel(Context context){
        this.context = context;
    }


    @Override
    public Context getContext() {
        return context;
    }
    public StockDetailsData getAncStockData(String month, String year){
        return getStockData(CoreConstants.EventType.ANC_HOME_VISIT,month,year,getLastBalance(CoreConstants.EventType.ANC_HOME_VISIT,month,year));
    }
    public StockDetailsData getPncStockData(String month, String year){
        return getStockData(CoreConstants.EventType.PNC_HOME_VISIT,month,year,getLastBalance(CoreConstants.EventType.PNC_HOME_VISIT,month,year));
    }
    public StockDetailsData getAdoServiceStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,month,year,getLastBalance(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,month,year));
    }
    public StockDetailsData getChildServiceStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,month,year,getLastBalance(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,month,year));
    }
    public StockDetailsData getNcdServiceStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.NCD_PACKAGE,month,year,getLastBalance(HnppConstants.EVENT_TYPE.NCD_PACKAGE,month,year));
    }
    public StockDetailsData getWomenServiceStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,month,year,getLastBalance(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,month,year));
    }
    private int getLastBalance(String visitType, String month, String year){
        int endBalance = 0;
        String query = "select sum("+ StockRepository.STOCK_QUANTITY+") as count, sum("+StockRepository.ACHIEVEMNT_COUNT+") as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.MONTH+" < '"+month+"' and "+StockRepository.YEAR+" = '"+year+"'";

        Log.v("STOCK","visit_type:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int quantity = cursor.getInt(0);
                int sell = cursor.getInt(1);
                endBalance = quantity - sell;
                cursor.moveToNext();
            }
            cursor.close();

        }

        return endBalance;
    }
    private StockDetailsData getStockData(String visitType, String month, String year,int startBalance){
        StockDetailsData stockDetailsData = new StockDetailsData();
        String query = "select sum("+ StockRepository.STOCK_QUANTITY+") as count, sum("+StockRepository.ACHIEVEMNT_COUNT+") as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.MONTH+" = '"+month+"' and "+StockRepository.YEAR+" = '"+year+"'";

        Log.v("STOCK","visit_type:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                stockDetailsData.setMonthStartBalance(startBalance);
                stockDetailsData.setNewPackage(cursor.getInt(0));
                stockDetailsData.setSell(cursor.getInt(1));
                int endBalance = (startBalance + stockDetailsData.getNewPackage()) - stockDetailsData.getSell();
                //stockDetailsData.setMonthStartBalance(stockData.getQuantity()-stockDetailsData.getSell());
                stockDetailsData.setEndBalance(endBalance);

                cursor.moveToNext();
            }
            cursor.close();

        }

        return stockDetailsData;
    }
}
