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
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class IndicatorRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String INDICATOR_TABLE = "indicator_table";
    protected static final String INDICATOR_ID = "indicator_id";
    public static final String INDICATOR_NAME = "indicator_name";
    public static final String INDICATOR_VALUE = "indicator_value";
    public static final String INDICATOR_COUNT = "indicator_count";
    public static final String SS_NAME = "ss_name";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    protected static final String YEAR = "year";
    protected static final String MONTH = "month";
    protected static final String DAY = "day";
    protected static final String START_DATE = "star_date";
    protected static final String END_DATE = "end_date";



    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + INDICATOR_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    INDICATOR_ID + " INTEGER , " +INDICATOR_NAME + " VARCHAR , " +
                    YEAR + " VARCHAR, " + MONTH+ " VARCHAR, "+DAY+" VARCHAR, "+START_DATE+" VARCHAR, "+END_DATE+" VARCHAR ,"+INDICATOR_VALUE+" VARCHAR,"+SS_NAME+" VARCHAR,"+BASE_ENTITY_ID+" VARCHAR ) ";




    public IndicatorRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return INDICATOR_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public  void updateValue(String targetName, String day, String month, String year, String ssName, String baseEntityId, String value){
        ContentValues contentValues = new ContentValues();
        targetName = getTargetName(targetName,baseEntityId);
        contentValues.put(BASE_ENTITY_ID, baseEntityId);
        contentValues.put(INDICATOR_NAME, targetName);
        contentValues.put(INDICATOR_VALUE, value);
        contentValues.put(YEAR, year);
        contentValues.put(MONTH, month);
        contentValues.put(DAY, day);
        contentValues.put(SS_NAME, ssName);
        SQLiteDatabase database = getWritableDatabase();
        if(findUnique(database,targetName,day,month,year,ssName,baseEntityId)){
            Log.v("TARGET_INSERTED","update value:"+contentValues);
            long inserted = database.insert(getLocationTableName(), null, contentValues);
        }

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+INDICATOR_NAME+" = '"+targetName+"'");
    }
    public boolean findUnique(SQLiteDatabase db, String targetName, String day, String month, String year, String ssName, String baseEntityId) {


        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " and " + INDICATOR_NAME + " = ? " + COLLATE_NOCASE+" and "+DAY+" = ?"+COLLATE_NOCASE+" and "+MONTH+" = ?"+COLLATE_NOCASE+" and "+YEAR+" = ?"+COLLATE_NOCASE+" and "+SS_NAME+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, targetName,day,month,year,ssName};
        Cursor cursor = database.query(getLocationTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        return true;
    }

    private String getTargetName(String targetName, String baseEntityId) {

        return targetName;
    }


    public boolean isExistData(int targetId){
        String sql = "select count(*) from "+getLocationTableName()+" where "+ INDICATOR_ID +" = "+targetId;
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


    protected TargetVsAchievementData readCursor(Cursor cursor) {
        int targetId = cursor.getInt(cursor.getColumnIndex(INDICATOR_ID));
        String indicatorValue = cursor.getString(cursor.getColumnIndex(INDICATOR_VALUE));
        String targetName = cursor.getString(cursor.getColumnIndex(INDICATOR_NAME));
        int indicatorCount = 0;
        try{
            indicatorCount  = cursor.getInt(cursor.getColumnIndex(INDICATOR_COUNT));
        }catch (Exception e){

        }

        TargetVsAchievementData targetVsAchievementData = new TargetVsAchievementData();
        targetVsAchievementData.setAchievementCount(indicatorCount);
        targetVsAchievementData.setTargetName(targetName);
        targetVsAchievementData.setTargetId(targetId);

        return targetVsAchievementData;
    }

}
