package org.smartregister.unicef.mis.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;
import org.smartregister.unicef.mis.utils.OutreachContentData;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class OutreachRepository extends BaseRepository {
    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String OUTREACH_TABLE = "outreach_table";
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
    public static final String ADDRESS = "address";
    public static final String MOBILE = "mobile";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String OUTREACH_JSON = "outreach_json";
    public static final String MICROPLAN_STATUS = "microplan_status";
    public static final String IS_SYNC = "is_sync";

    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + OUTREACH_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +OUTREACH_ID + " VARCHAR , " +MICROPLAN_STATUS + " VARCHAR , " +
                    OUTREACH_NAME + " VARCHAR, " + UNION_NAME+ " VARCHAR, "+UNION_ID+" INTEGER,"+
                    OLD_WARD_NAME + " VARCHAR, " + OLD_WARD_ID+ " INTEGER, "+NEW_WARD_NAME+" VARCHAR,"+NEW_WARD_ID+" INTEGER,"+
                    BLOCK_NAME + " VARCHAR, " + BLOCK_ID+ " INTEGER, "+CENTER_TYPE+" VARCHAR,"+ADDRESS+" VARCHAR,"+
                    MOBILE + " VARCHAR, " + LATITUDE+ " DOUBLE, "+LONGITUDE+" DOUBLE,"+OUTREACH_JSON+" TEXT,"+IS_SYNC+" INT) ";


    public OutreachRepository(Repository repository) {
        super(repository);
    }

    protected String getTableName() {
        return OUTREACH_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getTableName());
    }
    public  boolean addAndUpdateOutreach(OutreachContentData outreachContentData){
        ContentValues contentValues = new ContentValues();
        contentValues.put(OUTREACH_ID, outreachContentData.outreachId);
        contentValues.put(OUTREACH_NAME, outreachContentData.outreachName);
        contentValues.put(UNION_NAME, outreachContentData.unionName);
        contentValues.put(UNION_ID, outreachContentData.unionId);
        contentValues.put(OLD_WARD_NAME, outreachContentData.oldWardName);
        contentValues.put(OLD_WARD_ID, outreachContentData.oldWardId);
        contentValues.put(NEW_WARD_NAME, outreachContentData.newWardName);
        contentValues.put(NEW_WARD_ID, outreachContentData.newWardId);
        contentValues.put(BLOCK_ID, outreachContentData.blockId);
        contentValues.put(BLOCK_NAME, outreachContentData.blockName);
        contentValues.put(CENTER_TYPE, outreachContentData.centerType);
        contentValues.put(NEW_WARD_ID, outreachContentData.newWardId);
        contentValues.put(ADDRESS, outreachContentData.address);
        contentValues.put(MOBILE, outreachContentData.mobile);
        contentValues.put(LATITUDE, outreachContentData.latitude);
        contentValues.put(LONGITUDE, outreachContentData.longitude);
        contentValues.put(OUTREACH_JSON,gson.toJson(outreachContentData));
        contentValues.put(IS_SYNC, 0);
        contentValues.put(MICROPLAN_STATUS, MicroPlanRepository.MICROPLAN_STATUS_TAG.NOT_CREATED.getValue());
        SQLiteDatabase database = getWritableDatabase();
        try{
            if(findUnique(database,outreachContentData)){
            long inserted = database.insert(getTableName(), null, contentValues);
            Log.v("OUTREACH","inserted:"+inserted+":contentValues:"+contentValues);
            return inserted>0;
            }else{
                String selection = BLOCK_ID + " = ? " + COLLATE_NOCASE;
                String[] selectionArgs = new String[]{outreachContentData.blockId+""};
                int updated = database.update(getTableName(),contentValues,selection,selectionArgs);
                Log.v("OUTREACH","updated value:"+contentValues);
                return updated>0;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+INDICATOR_NAME+" = '"+targetName+"'");
    }

    public void updateOutreachStatus(OutreachContentData outreachContentData){
        getWritableDatabase().execSQL("update "+getTableName()+" set is_sync = 1 where "+OUTREACH_ID+" = '"+outreachContentData.outreachId+"' and "+BLOCK_ID+" ='"+outreachContentData.blockId+"'");
    }
    public void updateMicroPlanStatus(int blockId, String outreachId, String status){
        getWritableDatabase().execSQL("update "+getTableName()+" set "+MICROPLAN_STATUS+" = "+status+" where "+OUTREACH_ID+" = '"+outreachId+"' and "+BLOCK_ID+" ='"+blockId+"'");
    }
    public boolean findUnique(SQLiteDatabase db, OutreachContentData outreachContentData) {
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BLOCK_ID + " = ?  and " + OUTREACH_ID + " = ? " + COLLATE_NOCASE;
        String[] selectionArgs = new String[]{outreachContentData.blockId+"", outreachContentData.outreachId};
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
    public ArrayList<OutreachContentData> getUnSyncData() {
        Cursor cursor = null;
        ArrayList<OutreachContentData> outreachContentDataList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+IS_SYNC+" is null or is_sync = '0' ";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                outreachContentDataList.add(readOutreachJsonCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("OtherVaccineContentData","locations>>>"+outreachContentDataList.size());
        return outreachContentDataList;
    }
    public ArrayList<OutreachContentData> getAllOutreachData() {
        Cursor cursor = null;
        ArrayList<OutreachContentData> outreachContentDataList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getTableName()+"  group by block_id ";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                outreachContentDataList.add(readCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("OtherVaccineContentData","locations>>>"+outreachContentDataList.size());
        return outreachContentDataList;
    }
    public OutreachContentData getOutreachInformation(int blockId){
        OutreachContentData outreachContentData = null;
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+BLOCK_ID+" = "+blockId+" group by block_id ";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                outreachContentData = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return outreachContentData;
    }

    protected OutreachContentData readOutreachJsonCursor(Cursor cursor) {
        return gson.fromJson(cursor.getString(cursor.getColumnIndex(OUTREACH_JSON)),OutreachContentData.class);
    }
    protected OutreachContentData readCursor(Cursor cursor) {
        OutreachContentData outreachContentData = new OutreachContentData();
        outreachContentData.outreachId = cursor.getString(cursor.getColumnIndex(OUTREACH_ID));
        outreachContentData.outreachName = cursor.getString(cursor.getColumnIndex(OUTREACH_NAME));
        outreachContentData.unionId = cursor.getInt(cursor.getColumnIndex(UNION_ID));
        outreachContentData.unionName = cursor.getString(cursor.getColumnIndex(UNION_NAME));
        outreachContentData.oldWardId = cursor.getInt(cursor.getColumnIndex(OLD_WARD_ID));
        outreachContentData.oldWardName = cursor.getString(cursor.getColumnIndex(OLD_WARD_NAME));
        outreachContentData.newWardId = cursor.getInt(cursor.getColumnIndex(NEW_WARD_ID));
        outreachContentData.newWardName = cursor.getString(cursor.getColumnIndex(NEW_WARD_NAME));
        outreachContentData.blockId = cursor.getInt(cursor.getColumnIndex(BLOCK_ID));
        outreachContentData.blockName = cursor.getString(cursor.getColumnIndex(BLOCK_NAME));
        outreachContentData.address = cursor.getString(cursor.getColumnIndex(ADDRESS));
        outreachContentData.mobile = cursor.getString(cursor.getColumnIndex(MOBILE));
        outreachContentData.centerType = cursor.getString(cursor.getColumnIndex(CENTER_TYPE));
        outreachContentData.microplanStatus = cursor.getString(cursor.getColumnIndex(MICROPLAN_STATUS))==null?MicroPlanRepository.MICROPLAN_STATUS_TAG.NOT_CREATED.getValue():cursor.getString(cursor.getColumnIndex(MICROPLAN_STATUS));
        outreachContentData.latitude = cursor.getDouble(cursor.getColumnIndex(LATITUDE));
        outreachContentData.longitude = cursor.getDouble(cursor.getColumnIndex(LONGITUDE));
        return outreachContentData;
    }

}
