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
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class TargetVsAchievementRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String TARGET_TABLE = "target_table";
    protected static final String TARGET_ID = "target_id";
    public static final String TARGET_NAME = "target_name";
    public static final String TARGET_COUNT = "target_count";
    public static final String ACHIEVEMNT_COUNT = "achievemnt_count";
    public static final String SS_NAME = "ss_name";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    protected static final String YEAR = "year";
    protected static final String MONTH = "month";
    protected static final String DAY = "day";
    protected static final String START_DATE = "star_date";
    protected static final String END_DATE = "end_date";



    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + TARGET_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    TARGET_ID + " INTEGER , " +TARGET_NAME + " VARCHAR , " + TARGET_COUNT+ " INTEGER,"+
                    YEAR + " VARCHAR, " + MONTH+ " VARCHAR, "+DAY+" VARCHAR, "+START_DATE+" VARCHAR, "+END_DATE+" VARCHAR ,"+ACHIEVEMNT_COUNT+" INTEGER,"+SS_NAME+" VARCHAR,"+BASE_ENTITY_ID+" VARCHAR ) ";




    public TargetVsAchievementRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return TARGET_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }
    public  void updateValue(String targetName, String day, String month, String year, String ssName, String baseEntityId){
        updateValue(targetName,day,month,year,ssName,baseEntityId,1);

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+TARGET_NAME+" = '"+targetName+"'");
    }
    public  void updateValue(String targetName, String day, String month, String year, String ssName, String baseEntityId, int count){
        ContentValues contentValues = new ContentValues();
        targetName = getTargetName(targetName);
        contentValues.put(BASE_ENTITY_ID, targetName);
        contentValues.put(TARGET_NAME, getTargetName(targetName));
        contentValues.put(ACHIEVEMNT_COUNT, count);
        contentValues.put(YEAR, year);
        contentValues.put(MONTH, month);
        contentValues.put(DAY, day);
        contentValues.put(SS_NAME, ssName);
        SQLiteDatabase database = getWritableDatabase();
        if(findUnique(database,targetName,day,month,year,ssName,baseEntityId)){
            Log.v("TARGET_INSERTED","update value:"+contentValues);
            long inserted = database.insert(getLocationTableName(), null, contentValues);
        }

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+TARGET_NAME+" = '"+targetName+"'");
    }
    public boolean findUnique(SQLiteDatabase db, String targetName, String day, String month, String year, String ssName, String baseEntityId) {


        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " and " + TARGET_NAME + " = ? " + COLLATE_NOCASE+" and "+DAY+" = ?"+COLLATE_NOCASE+" and "+MONTH+" = ?"+COLLATE_NOCASE+" and "+YEAR+" = ?"+COLLATE_NOCASE+" and "+SS_NAME+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, targetName,day,month,year,ssName};
        net.sqlcipher.Cursor cursor = database.query(getLocationTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        return true;
    }

    private String getTargetName(String targetName) {
        if(!TextUtils.isEmpty(targetName)){
            if(targetName.equalsIgnoreCase(HnppConstants.EventType.ANC_HOME_VISIT)
                    || targetName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)
                || targetName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION)
                    || targetName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
                targetName = HnppConstants.EVENT_TYPE.ANC_SERVICE;
            }else if(targetName.equalsIgnoreCase(HnppConstants.EventType.PNC_HOME_VISIT)
                    || targetName.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
                targetName = HnppConstants.EVENT_TYPE.PNC_SERVICE;
            } else if(targetName.equalsIgnoreCase(HnppConstants.EventType.ANC_REGISTRATION)){
                targetName = HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED;
            }

        }
        return targetName;
    }

    public void addOrUpdate(TargetVsAchievementData targetVsAchievementData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TARGET_ID, targetVsAchievementData.getTargetId());
        contentValues.put(TARGET_COUNT, targetVsAchievementData.getTargetCount());
        contentValues.put(TARGET_NAME, targetVsAchievementData.getTargetName());
        contentValues.put(ACHIEVEMNT_COUNT, targetVsAchievementData.getAchievementCount());
        contentValues.put(YEAR, targetVsAchievementData.getYear());
        contentValues.put(MONTH, targetVsAchievementData.getMonth());
        contentValues.put(DAY, targetVsAchievementData.getDay());
        contentValues.put(START_DATE, targetVsAchievementData.getStartDate());
        contentValues.put(END_DATE, targetVsAchievementData.getEndDate());
        long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);

    }
    public TargetVsAchievementData getTargetDetailsByName(String targetName) {
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
    }

    public ArrayList<TargetVsAchievementData> getTargetDetailsById(String targetId) {
        Cursor cursor = null;
        ArrayList<TargetVsAchievementData> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+TARGET_ID+" = '"+targetId+"", null);
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

    protected TargetVsAchievementData readCursor(Cursor cursor) {
        int targetId = cursor.getInt(cursor.getColumnIndex(TARGET_ID));
        int targetCount = cursor.getInt(cursor.getColumnIndex(TARGET_COUNT));
        int achievemntCount = cursor.getInt(cursor.getColumnIndex(ACHIEVEMNT_COUNT));
        String targetName = cursor.getString(cursor.getColumnIndex(TARGET_NAME));

        TargetVsAchievementData targetVsAchievementData = new TargetVsAchievementData();
        targetVsAchievementData.setAchievementCount(achievemntCount);
        targetVsAchievementData.setTargetName(targetName);
        targetVsAchievementData.setTargetId(targetId);
        targetVsAchievementData.setTargetCount(targetCount);

        return targetVsAchievementData;
    }

}
