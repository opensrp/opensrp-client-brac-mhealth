package org.smartregister.unicef.dghs.model;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.contract.StockDetailsContract;
import org.smartregister.unicef.dghs.repository.StockRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.StockData;
import org.smartregister.unicef.dghs.utils.StockDetailsData;

import org.smartregister.chw.core.utils.CoreConstants;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class StockDetailsModel implements StockDetailsContract.Model{
    private Context context;

    public StockDetailsModel(Context context){
        this.context = context;
    }


    @Override
    public Context getContext() {
        return context;
    }
    //for PA
    public StockDetailsData getAdultPackageStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.NCD_PACKAGE,month,year,getLastBalance(HnppConstants.EVENT_TYPE.NCD_PACKAGE,month,year));
    }
//    public StockDetailsData getBloodGroupStockData(String month, String year){
//        return getStockData(HnppConstants.EVENT_TYPE.BLOOD_GROUP,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BLOOD_GROUP,month,year));
//    }
    public StockDetailsData getTotalGlassStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.GLASS,month,year,getLastBalance(HnppConstants.EVENT_TYPE.GLASS,month,year));
    }
    public StockDetailsData getSunGlassStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SUN_GLASS,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SUN_GLASS,month,year));
    }
    public StockDetailsData getSV1StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SV_1,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SV_1,month,year));
    }
    public StockDetailsData getSV1_5StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SV_1_5,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SV_1_5,month,year));
    }
    public StockDetailsData getSV2_5StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SV_2_5,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SV_2_5,month,year));
    }
    public StockDetailsData getSV2StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SV_2,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SV_2,month,year));
    }
    public StockDetailsData getSV3StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.SV_3,month,year,getLastBalance(HnppConstants.EVENT_TYPE.SV_3,month,year));
    }
    public StockDetailsData getBF1StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.BF_1,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BF_1,month,year));
    }
    public StockDetailsData getBF1_5StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.BF_1_5,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BF_1_5,month,year));
    }
    public StockDetailsData getBF2StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.BF_2,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BF_2,month,year));
    }
    public StockDetailsData getBF2_5StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.BF_2_5,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BF_2_5,month,year));
    }
    public StockDetailsData getBF3StockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.BF_3,month,year,getLastBalance(HnppConstants.EVENT_TYPE.BF_3,month,year));
    }
    //
    public StockDetailsData getAncStockData(String month, String year){
        return getStockData(CoreConstants.EventType.ANC_HOME_VISIT,month,year,getLastBalance(CoreConstants.EventType.ANC_HOME_VISIT,month,year));
    }
    public StockDetailsData getPncStockData(String month, String year){
        return getStockData(HnppConstants.EVENT_TYPE.PNC_REGISTRATION,month,year,getLastBalance(HnppConstants.EVENT_TYPE.PNC_REGISTRATION,month,year));
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
        String query="";
//        if(month.equalsIgnoreCase("1")){
//           query = "select sum("+ StockRepository.STOCK_QUANTITY+") as count, sum("+StockRepository.ACHIEVEMNT_COUNT+") as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.MONTH+" <= '12' and "+StockRepository.YEAR+" < '"+year+"'";
//
//        }else {
//            query = "select sum("+ StockRepository.STOCK_QUANTITY+") as count, sum("+StockRepository.ACHIEVEMNT_COUNT+") as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.MONTH+" < '"+month+"' and "+StockRepository.YEAR+" <= '"+year+"'";
//
//        }

        if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GLASS)){
            query = "select sum(coalesce("+ StockRepository.STOCK_QUANTITY+",0)) as count, sum(coalesce("+StockRepository.ACHIEVEMNT_COUNT+",0)) as acount from "+StockRepository.STOCK_TABLE+" where ("
                    +StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1_5+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2_5+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_3+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1_5+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2_5+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_3+"' or " +
                    StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SUN_GLASS+"')" +
                    "and "+StockRepository.STOCK_TIMESTAMP+" < "+HnppConstants.getLongDateFormatForStock(year,month);//47809000= 1970/01/01
        }
        else {
            query = "select sum(coalesce("+ StockRepository.STOCK_QUANTITY+",0)) as count, sum(coalesce("+StockRepository.ACHIEVEMNT_COUNT+",0)) as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.STOCK_TIMESTAMP+" < "+HnppConstants.getLongDateFormatForStock(year,month);//47809000= 1970/01/01
        }
        Log.v("LAST_BALANCE_STOCK","query:"+query);

        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int quantity = cursor.getInt(0);
                int sell = cursor.getInt(1);
                endBalance = quantity - sell;
                Log.v("LAST_BALANCE_STOCK","endBalance:"+endBalance);
                cursor.moveToNext();
            }
            cursor.close();

        }

        return endBalance;
    }


    private StockDetailsData getStockData(String visitType, String month, String year,int startBalance){
        StockDetailsData stockDetailsData = new StockDetailsData();
        String query="";
       if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.GLASS)){
           query = "select sum(coalesce("+ StockRepository.STOCK_QUANTITY+",0)) as count, sum(coalesce("+StockRepository.ACHIEVEMNT_COUNT+",0)) as acount from "+StockRepository.STOCK_TABLE+" where ("
                   +StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1_5+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2_5+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_3+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1_5+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2_5+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_3+"' or "+
                   StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SUN_GLASS+"')"+
                   "and "+StockRepository.MONTH+" = '"+month+"' and "+StockRepository.YEAR+" = '"+year+"'";
       }
       else {
           query = "select sum(coalesce("+ StockRepository.STOCK_QUANTITY+",0)) as count, sum(coalesce("+StockRepository.ACHIEVEMNT_COUNT+",0)) as acount from "+StockRepository.STOCK_TABLE+" where "+StockRepository.STOCK_PRODUCT_NAME+" = '"+visitType+"' and "+StockRepository.MONTH+" = '"+month+"' and "+StockRepository.YEAR+" = '"+year+"'";
       }

        Log.v("getStockData","getStockData:"+query+":startBalance:"+startBalance);

        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                stockDetailsData.setMonthStartBalance(startBalance);
                stockDetailsData.setNewPackage(cursor.getInt(0));
                stockDetailsData.setSell(cursor.getInt(1));
                int endBalance = (startBalance + stockDetailsData.getNewPackage()) - stockDetailsData.getSell();
                //stockDetailsData.setMonthStartBalance(stockData.getQuantity()-stockDetailsData.getSell());
                stockDetailsData.setEndBalance(endBalance);
                stockDetailsData.setCount(endBalance);
                stockDetailsData.setEventType(visitType);
                if(HnppConstants.isPALogin()&&visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NCD_PACKAGE)){
                        stockDetailsData.setTitle("প্রাপ্তবয়স্ক প্যাকেজ");

                }
                else{
                    stockDetailsData.setTitle(HnppConstants.workSummeryTypeMapping.get(stockDetailsData.getEventType()));

                }

                try{
                    stockDetailsData.setImageSource((int)HnppConstants.iconMapping.get(stockDetailsData.getEventType()));
                }catch (Exception e){

                }

                cursor.moveToNext();
            }
            cursor.close();

        }

        return stockDetailsData;
    }
}
