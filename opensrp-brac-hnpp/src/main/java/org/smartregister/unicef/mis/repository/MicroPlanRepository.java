package org.smartregister.unicef.mis.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.location.HALocation;
import org.smartregister.unicef.mis.utils.DistributionData;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.MicroPlanTypeData;
import org.smartregister.unicef.mis.utils.OutreachContentData;
import org.smartregister.unicef.mis.utils.SessionPlanData;
import org.smartregister.unicef.mis.utils.SuperVisorData;
import org.smartregister.unicef.mis.utils.WorkerData;
import org.smartregister.util.DateTimeTypeConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class MicroPlanRepository extends BaseRepository {
    public enum MICROPLAN_STATUS_TAG {
        NOT_CREATED("Not created"),
        PENDING("Pending"),
        APPROVED("Approved"),
        RETAKE("Sent back");
        String value;
        MICROPLAN_STATUS_TAG(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
    }
    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String MICRO_PLAN_TABLE = "microplan_table";
    protected static final String OUTREACH_ID = "outreach_id";
    public static final String OUTREACH_NAME = "outreach_name";
    public static final String UNION_NAME = "union_name";
    public static final String UNION_ID = "union_id";
    public static final String OLD_WARD_NAME = "old_ward_name";
    public static final String OLD_WARD_ID = "old_ward_id";
    public static final String NEW_WARD_NAME = "new_ward_name";
    public static final String NEW_WARD_ID = "new_ward_id";
    public static final String BLOCK_NAME = "block_name";
    public static final String BLOCK_ID = "block_id";
    public static final String CENTER_TYPE = "center_type";
    public static final String HH_NO = "hh_no";
    public static final String MICROPLAN_TYPE_DATA = "type_data";
    public static final String DISTRIBUTION_DATA = "distribution_data";
    public static final String SESSION_PLAN = "session_plan";
    public static final String WORKER_INFO = "worker_info";
    public static final String SUPERVISOR_INFO = "supervisor_info";
    public static final String MICROPLAN_STATUS = "status";
    public static final String YEAR = "year";
    public static final String IS_SYNC = "is_sync";

    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + MICRO_PLAN_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +OUTREACH_ID + " VARCHAR , "  +
                    OUTREACH_NAME + " VARCHAR, " + HH_NO+ " VARCHAR, "+ UNION_NAME+ " VARCHAR, "+UNION_ID+" INTEGER,"+
    OLD_WARD_NAME + " VARCHAR, " + OLD_WARD_ID+ " INTEGER, "+NEW_WARD_NAME+" VARCHAR,"+NEW_WARD_ID+" INTEGER,"+

    BLOCK_NAME + " VARCHAR, " + BLOCK_ID+ " INTEGER, "+CENTER_TYPE+" VARCHAR,"+MICROPLAN_STATUS+" VARCHAR,"+
                    YEAR + " INTEGER, " + MICROPLAN_TYPE_DATA+ " TEXT, "+DISTRIBUTION_DATA+" TEXT,"+SESSION_PLAN+" TEXT,"+WORKER_INFO+" TEXT,"+SUPERVISOR_INFO+" TEXT,"+IS_SYNC+" INT) ";


    public MicroPlanRepository(Repository repository) {
        super(repository);
    }

    protected String getTableName() {
        return MICRO_PLAN_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getTableName());
    }
    public  boolean addAndUpdateMicroPlan(MicroPlanEpiData microPlanEpiData){
        ContentValues contentValues = new ContentValues();
        contentValues.put(OUTREACH_ID, microPlanEpiData.outreachId);
        contentValues.put(OUTREACH_NAME, microPlanEpiData.outreachName);
        contentValues.put(UNION_NAME, microPlanEpiData.unionName);
        contentValues.put(UNION_ID, microPlanEpiData.unionId);
        contentValues.put(OLD_WARD_NAME, microPlanEpiData.oldWardName);
        contentValues.put(OLD_WARD_ID, microPlanEpiData.oldWardId);
        contentValues.put(NEW_WARD_NAME, microPlanEpiData.newWardName);
        contentValues.put(NEW_WARD_ID, microPlanEpiData.newWardId);
        contentValues.put(BLOCK_ID, microPlanEpiData.blockId);
        contentValues.put(BLOCK_NAME, microPlanEpiData.blockName);
        contentValues.put(CENTER_TYPE, microPlanEpiData.centerType);
        contentValues.put(MICROPLAN_TYPE_DATA, gson.toJson(microPlanEpiData.microPlanTypeData));
        contentValues.put(DISTRIBUTION_DATA, gson.toJson(microPlanEpiData.distributionData));
        contentValues.put(SESSION_PLAN, gson.toJson(microPlanEpiData.sessionPlanData));
        contentValues.put(WORKER_INFO, gson.toJson(microPlanEpiData.workerData));
        contentValues.put(SUPERVISOR_INFO, gson.toJson(microPlanEpiData.superVisorData));
        contentValues.put(YEAR,microPlanEpiData.year);
        contentValues.put(IS_SYNC, 0);
        contentValues.put(MICROPLAN_STATUS, MICROPLAN_STATUS_TAG.PENDING.getValue());
        SQLiteDatabase database = getWritableDatabase();
        try{
            if(findUnique(database,microPlanEpiData)){
            long inserted = database.insert(getTableName(), null, contentValues);
                Log.v("OUTREACH","inserted:"+inserted+":contentValues:"+contentValues);

                return inserted>0;
            }else{
                String selection = BLOCK_ID + " = ?  and " + YEAR + " = ? " + COLLATE_NOCASE;
                String[] selectionArgs = new String[]{microPlanEpiData.blockId+"", microPlanEpiData.year+""};
                database.update(getTableName(),contentValues,selection,selectionArgs);
                Log.v("OUTREACH","failed value:"+contentValues);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;


//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+INDICATOR_NAME+" = '"+targetName+"'");
    }

    public void updateMicroPlanSyncStatus(MicroPlanEpiData microPlanEpiData){
        getWritableDatabase().execSQL("update "+getTableName()+" set is_sync = 1 where "+YEAR+" = '"+microPlanEpiData.year+"' and "+BLOCK_ID+" ='"+microPlanEpiData.blockId+"'");
    }
    public void updateMicroPlanStatus(int blockId, int year, String status){
        getWritableDatabase().execSQL("update "+getTableName()+" set "+MICROPLAN_STATUS+" = "+status+" where "+YEAR+" = '"+year+"' and "+BLOCK_ID+" ='"+blockId+"'");
    }
    public boolean findUnique(SQLiteDatabase db, MicroPlanEpiData microPlanEpiData) {
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BLOCK_ID + " = ?  and " + YEAR + " = ? " + COLLATE_NOCASE;
        String[] selectionArgs = new String[]{microPlanEpiData.blockId+"", microPlanEpiData.year+""};
        Cursor cursor = database.query(getTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        if(cursor!=null) cursor.close();
        return true;
    }

    public int getUnSyncCount() {
        Cursor cursor = null;
        int unsyncCount = 0;
        try {
            String sql = "SELECT count(*) FROM " + getTableName()+" where "+IS_SYNC+" is null or is_sync = '0' ";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                unsyncCount = Integer.parseInt(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return unsyncCount;
    }
    public ArrayList<MicroPlanEpiData> getUnSyncData() {
        Cursor cursor = null;
        ArrayList<MicroPlanEpiData> microPlanTypeData = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+IS_SYNC+" is null or is_sync = '0' ";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                microPlanTypeData.add(readCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("OtherVaccineContentData","locations>>>"+microPlanTypeData.size());
        return microPlanTypeData;
    }
    public MicroPlanEpiData getMicroPlan(String status, int blockId, int year) {
        Cursor cursor = null;
        MicroPlanEpiData microPlanEpiData = null;

        try {
            String sql;
            if(TextUtils.isEmpty(status) || status.equalsIgnoreCase("all")){
                sql= "SELECT * FROM " + getTableName()+"  where "+BLOCK_ID+" = "+blockId+" and "+YEAR+" = "+year+"";
            }else{
                sql= "SELECT * FROM " + getTableName()+"  where "+BLOCK_ID+" = "+blockId+" and "+YEAR+" = "+year+" and "+MICROPLAN_STATUS+" = '"+status+"'";

            }
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                microPlanEpiData = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return microPlanEpiData;
    }

    protected MicroPlanEpiData readCursor(Cursor cursor) {
        MicroPlanEpiData microPlanEpiData = new MicroPlanEpiData();
        microPlanEpiData.outreachId = cursor.getString(cursor.getColumnIndex(OUTREACH_ID));
        microPlanEpiData.outreachName = cursor.getString(cursor.getColumnIndex(OUTREACH_NAME));
        microPlanEpiData.unionId = cursor.getInt(cursor.getColumnIndex(UNION_ID));
        microPlanEpiData.unionName = cursor.getString(cursor.getColumnIndex(UNION_NAME));
        microPlanEpiData.oldWardId = cursor.getInt(cursor.getColumnIndex(OLD_WARD_ID));
        microPlanEpiData.oldWardName = cursor.getString(cursor.getColumnIndex(OLD_WARD_NAME));
        microPlanEpiData.newWardId = cursor.getInt(cursor.getColumnIndex(NEW_WARD_ID));
        microPlanEpiData.newWardName = cursor.getString(cursor.getColumnIndex(NEW_WARD_NAME));
        microPlanEpiData.blockId = cursor.getInt(cursor.getColumnIndex(BLOCK_ID));
        microPlanEpiData.blockName = cursor.getString(cursor.getColumnIndex(BLOCK_NAME));
        microPlanEpiData.houseHoldNo = cursor.getString(cursor.getColumnIndex(HH_NO));
        microPlanEpiData.centerType = cursor.getString(cursor.getColumnIndex(CENTER_TYPE));
        microPlanEpiData.microPlanStatus = cursor.getString(cursor.getColumnIndex(MICROPLAN_STATUS))==null? MICROPLAN_STATUS_TAG.NOT_CREATED.getValue():cursor.getString(cursor.getColumnIndex(MICROPLAN_STATUS));
        microPlanEpiData.microPlanTypeData = gson.fromJson(cursor.getString(cursor.getColumnIndex(MICROPLAN_TYPE_DATA)),MicroPlanTypeData.class);
        microPlanEpiData.distributionData = gson.fromJson(cursor.getString(cursor.getColumnIndex(DISTRIBUTION_DATA)), DistributionData.class);
        microPlanEpiData.sessionPlanData = gson.fromJson(cursor.getString(cursor.getColumnIndex(SESSION_PLAN)), SessionPlanData.class);
        microPlanEpiData.workerData = gson.fromJson(cursor.getString(cursor.getColumnIndex(WORKER_INFO)), WorkerData.class);
        microPlanEpiData.superVisorData = gson.fromJson(cursor.getString(cursor.getColumnIndex(SUPERVISOR_INFO)), SuperVisorData.class);
        microPlanEpiData.year = cursor.getInt(cursor.getColumnIndex(YEAR));
        return microPlanEpiData;
    }
    public ArrayList<MicroPlanEpiData> getAllMicroPlanEpiData(String status, int year){
        ArrayList<MicroPlanEpiData> microPlanEpiData = new ArrayList<>();
        ArrayList<HALocation> getAllHALocation = HnppApplication.getHALocationRepository().getAllLocation();
        for(HALocation haLocation: getAllHALocation){
            MicroPlanEpiData microPlanEpiData1 = new MicroPlanEpiData();
            microPlanEpiData1.unionId = haLocation.union.id;
            microPlanEpiData1.unionName = haLocation.union.name;
            microPlanEpiData1.oldWardId = haLocation.old_ward.id;
            microPlanEpiData1.oldWardName = haLocation.old_ward.name;
            microPlanEpiData1.newWardId = haLocation.ward.id;
            microPlanEpiData1.newWardName = haLocation.ward.name;
            microPlanEpiData1.blockId = haLocation.block.id;
            microPlanEpiData1.blockName = haLocation.block.name;
            OutreachContentData outreachContentData = HnppApplication.getOutreachRepository().getOutreachInformation(microPlanEpiData1.blockId);
            if(outreachContentData!=null){
                microPlanEpiData1.outreachName =  outreachContentData.outreachName;
                microPlanEpiData1.outreachId = outreachContentData.outreachId;
                microPlanEpiData1.centerType = outreachContentData.centerType;
            }
            MicroPlanEpiData microPlan= getMicroPlan(status,microPlanEpiData1.blockId,year);
            if(microPlan==null){
                microPlanEpiData1.microPlanStatus = MICROPLAN_STATUS_TAG.NOT_CREATED.getValue();
            }else{
                microPlanEpiData1 = microPlan;
            }
            microPlanEpiData.add(microPlanEpiData1);

        }
        return microPlanEpiData;
    }

}
