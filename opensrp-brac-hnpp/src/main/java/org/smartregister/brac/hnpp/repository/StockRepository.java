package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.StockData;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

public class StockRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String STOCK_TABLE = "stock_table";
    protected static final String STOCK_ID = "stock_id";
    protected static final String STOCK_PRODUCT_ID = "product_id";
    public static final String STOCK_PRODUCT_NAME = "product_name";
    public static final String STOCK_QUANTITY = "stock_quantity";
    public static final String STOCK_TIMESTAMP = "stock_timestamp";
    protected static final String STOCK_EXPIREY_DATE = "expirey_date";
    protected static final String STOCK_RECEIVE_DATE = "receive_date";
    public static final String YEAR = "year";
    public static final String MONTH = "month";
    public static final String ACHIEVEMNT_DAY = "achievement_day";
    public static final String ACHIEVEMNT_COUNT = "achievemnt_count";
    public static final String SS_NAME = "ss_name";
    public static final String BASE_ENTITY_ID = "base_entity_id";



    private static final String CREATE_STOCK_TABLE=
            "CREATE TABLE " + STOCK_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    STOCK_ID + " INTEGER , " +STOCK_PRODUCT_ID + " INTEGER , " +BASE_ENTITY_ID + " INTEGER , " +ACHIEVEMNT_COUNT + " INTEGER , " +STOCK_PRODUCT_NAME + " VARCHAR , " + STOCK_QUANTITY+ " INTEGER,"+
                    YEAR + " VARCHAR, " +MONTH + " VARCHAR, " + ACHIEVEMNT_DAY+ " VARCHAR, "+STOCK_TIMESTAMP+" VARCHAR, "+SS_NAME+" VARCHAR, "+STOCK_EXPIREY_DATE+" VARCHAR, "+STOCK_RECEIVE_DATE+" VARCHAR ) ";




    public StockRepository(Repository repository) {
        super();
    }

    protected String getLocationTableName() {
        return STOCK_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_STOCK_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }
   public  void updateValue(String targetName, String day, String month, String year, String ssName, String baseEntityId,long timeStamp){
        updateValue(targetName,day,month,year,ssName,baseEntityId,1,timeStamp);

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+TARGET_NAME+" = '"+targetName+"'");
    }
    public  void updateValue(String productName, String day, String month, String year, String ssName, String baseEntityId, int count,long timeStamp){
        ContentValues contentValues = new ContentValues();
        productName = getTargetName(productName,baseEntityId);
        if(TextUtils.isEmpty(productName)) return;
        contentValues.put(BASE_ENTITY_ID, baseEntityId);
        contentValues.put(ACHIEVEMNT_DAY, day);
        contentValues.put(STOCK_PRODUCT_NAME, productName);
        contentValues.put(ACHIEVEMNT_COUNT, count);
        contentValues.put(STOCK_TIMESTAMP, timeStamp);
        contentValues.put(YEAR, year);
        contentValues.put(MONTH, month);
        contentValues.put(SS_NAME, ssName);
        SQLiteDatabase database = getWritableDatabase();
        if(findUnique(database,productName,day,month,year,ssName,baseEntityId)){
            Log.v("TARGET_INSERTED","update value:"+contentValues);
            long inserted = database.insert(getLocationTableName(), null, contentValues);
        }

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+TARGET_NAME+" = '"+targetName+"'");
    }
    public boolean findUnique(SQLiteDatabase db, String targetName, String day, String month, String year, String ssName, String baseEntityId) {
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " and " + STOCK_PRODUCT_NAME + " = ? " + COLLATE_NOCASE+" and "+ACHIEVEMNT_DAY+" = ?"+COLLATE_NOCASE+" and "+MONTH+" = ?"+COLLATE_NOCASE+" and "+YEAR+" = ?"+COLLATE_NOCASE+" and "+SS_NAME+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, targetName,day,month,year,ssName};
        net.sqlcipher.Cursor cursor = database.query(getLocationTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        return true;
    }

     private String getTargetName(String targetName, String baseEntityId) {
        if(!TextUtils.isEmpty(targetName)){
            switch (targetName){
                case HnppConstants.EventType.ANC_HOME_VISIT:
                case HnppConstants.EVENT_TYPE.ANC1_REGISTRATION:
                case HnppConstants.EVENT_TYPE.ANC2_REGISTRATION:
                case HnppConstants.EVENT_TYPE.ANC3_REGISTRATION:
                    return CoreConstants.EventType.ANC_HOME_VISIT;
                case HnppConstants.EventType.PNC_HOME_VISIT:
                case HnppConstants.EVENT_TYPE.PNC_REGISTRATION:
                    return CoreConstants.EventType.PNC_HOME_VISIT;
                case HnppConstants.EVENT_TYPE.GIRL_PACKAGE:
                case HnppConstants.EVENT_TYPE.IYCF_PACKAGE:
                case HnppConstants.EVENT_TYPE.NCD_PACKAGE:
                case HnppConstants.EVENT_TYPE.WOMEN_PACKAGE:
                    return targetName;
                case HnppConstants.EVENT_TYPE.GLASS:
                case HnppConstants.EVENT_TYPE.SUN_GLASS:
                case HnppConstants.EVENT_TYPE.SV_1:
                case HnppConstants.EVENT_TYPE.SV_1_5:
                case HnppConstants.EVENT_TYPE.SV_2:
                case HnppConstants.EVENT_TYPE.SV_2_5:
                case HnppConstants.EVENT_TYPE.SV_3:
                case HnppConstants.EVENT_TYPE.BF_1:
                case HnppConstants.EVENT_TYPE.BF_1_5:
                case HnppConstants.EVENT_TYPE.BF_2:
                case HnppConstants.EVENT_TYPE.BF_2_5:
                case HnppConstants.EVENT_TYPE.BF_3:
                    return targetName;

            }


        }
        return "";
    }
    public  boolean isAvailableStock(String eventTyype){
//        if(TextUtils.isEmpty(eventTyype)) return true;
//        String targetName = getTargetName(eventTyype,"");
//        if(!TextUtils.isEmpty(targetName)){
//            return HnppDBUtils.isAvailableStock(targetName);
//        }
        return true;
    }

    public void addOrUpdate(StockData stockData) {
       // if(!isExistData(stockData.getStockId())){
            ContentValues contentValues = new ContentValues();
            contentValues.put(STOCK_ID, stockData.getStockId());
            contentValues.put(STOCK_PRODUCT_ID, stockData.getProductId());
            contentValues.put(STOCK_PRODUCT_NAME, stockData.getProductName());
            contentValues.put(YEAR, stockData.getYear());
            contentValues.put(MONTH, stockData.getMonth());
            contentValues.put(STOCK_EXPIREY_DATE, stockData.getExpireyDate());
            contentValues.put(STOCK_RECEIVE_DATE, stockData.getReceiveDate());
            contentValues.put(STOCK_QUANTITY, stockData.getQuantity());
            contentValues.put(STOCK_TIMESTAMP, stockData.getTimestamp());
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("STOCK_FETCH","inserterd:"+inserted);
//        }else{
//            Log.v("STOCK_FETCH","exists!!!!!!!!!");
//        }


    }
    public boolean isExistData(int stockId){
        String sql = "select count(*) from "+getLocationTableName()+" where "+STOCK_ID+" = "+stockId;
        Cursor cursor = null;
        boolean isExist = false;

        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    if(cursor.getInt(0) >0){
                        isExist = true;
                    }
                    cursor.moveToNext();
                }

            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return isExist;
    }
   /* public StockData getStockDetailsByName(String productName) {
        Cursor cursor = null;
        TargetVsAchievementData targetVsAchievementData = new TargetVsAchievementData();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+TARGET_NAME+" = '"+targetName+"", null);
            while (cursor.moveToNext()) {
                targetVsAchievementData = readCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return targetVsAchievementData;
    }*/

    public ArrayList<StockData> getStockDetailsById(String stockId) {
        Cursor cursor = null;
        ArrayList<StockData> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+STOCK_ID+" = '"+stockId+"", null);
            while (cursor.moveToNext()) {
                locations.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;
    }
    public ArrayList<StockData> getAllStock() {
        Cursor cursor = null;
        ArrayList<StockData> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName(), null);
            while (cursor.moveToNext()) {
                locations.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;
    }

    protected StockData readCursor(Cursor cursor) {
        int stockId = cursor.getInt(cursor.getColumnIndex(STOCK_ID));
        int productId = cursor.getInt(cursor.getColumnIndex(STOCK_PRODUCT_ID));
        int quantity = cursor.getInt(cursor.getColumnIndex(STOCK_QUANTITY));
        int month = cursor.getInt(cursor.getColumnIndex(MONTH));
        int year = cursor.getInt(cursor.getColumnIndex(YEAR));
        String timestamp = cursor.getString(cursor.getColumnIndex(STOCK_TIMESTAMP));
        String productName = cursor.getString(cursor.getColumnIndex(STOCK_PRODUCT_NAME));
        String expireyDate = cursor.getString(cursor.getColumnIndex(STOCK_EXPIREY_DATE));
        String receiveDate = cursor.getString(cursor.getColumnIndex(STOCK_RECEIVE_DATE));

        StockData stockData = new StockData();
        stockData.setStockId(stockId);
        stockData.setProductId(productId);
        stockData.setQuantity(quantity);
        stockData.setMonth(Integer.toString(month));
        stockData.setYear(Integer.toString(year));
        stockData.setTimestamp(Long.parseLong(timestamp));
        stockData.setProductName(productName);
        stockData.setExpireyDate(expireyDate);
        stockData.setReceiveDate(receiveDate);

        return stockData;
    }

}
