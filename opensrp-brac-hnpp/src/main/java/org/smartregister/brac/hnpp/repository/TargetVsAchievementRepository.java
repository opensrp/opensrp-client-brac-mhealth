package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
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
    protected static final String YEAR = "year";
    protected static final String MONTH = "month";
    protected static final String DAY = "day";
    protected static final String START_DATE = "star_date";
    protected static final String END_DATE = "end_date";



    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + TARGET_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    TARGET_ID + " INTEGER , " +TARGET_NAME + " VARCHAR , " + TARGET_COUNT+ " INTEGER,"+
                    YEAR + " VARCHAR, " + MONTH+ " VARCHAR, "+DAY+" VARCHAR, "+START_DATE+" VARCHAR, "+END_DATE+" VARCHAR ,"+ACHIEVEMNT_COUNT+" INTEGER ) ";




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
    public  void updateValue(String targetName){
        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1 where "+TARGET_NAME+" = '"+targetName+"'");
    }

    public void addOrUpdate(TargetVsAchievementData targetVsAchievementData) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TARGET_ID, targetVsAchievementData.getTargetId());
        contentValues.put(TARGET_COUNT, targetVsAchievementData.getTargetCount());
        contentValues.put(TARGET_NAME, targetVsAchievementData.getTargetName());
        contentValues.put(ACHIEVEMNT_COUNT, targetVsAchievementData.getAchievementCount());
        contentValues.put(YEAR, targetVsAchievementData.getYear());
        contentValues.put(MONTH, targetVsAchievementData.getMonth());
        contentValues.put(DAY, targetVsAchievementData.getDate());
        contentValues.put(START_DATE, targetVsAchievementData.getStartDate());
        contentValues.put(END_DATE, targetVsAchievementData.getEndDate());
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);

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
