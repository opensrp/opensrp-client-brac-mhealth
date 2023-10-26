package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.HHVisitDurationModel;
import org.smartregister.brac.hnpp.model.HHVisitInfoModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanvir on 05/10/23.
 */
public class HouseHoldVisitInfoRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "id";
    protected static final String HH_BASE_ENTITY_ID = "hh_base_entity_id";
    protected static final String MEMBER_BASE_ENTITY_ID = "member_base_entity_id";
    protected static final String PAGE_EVENT_TYPE = "page_event_type";
    protected static final String EVENT_TYPE = "event_type";
    protected static final String INFO_COUNT = "info_count";
    protected static final String IS_DONE = "is_done";
/*    protected static final String SERVICE_NAME = "service_name";
    protected static final String VALUE = "value"; //duration*/

    protected static final String HH_VISIT_INFO_TABLE = "hh_visit_info";

    //protected static final String[] COLUMNS = new String[]{ID, SERVICE_ID, SERVICE_NAME,VALUE};

    private static final String CREATE_HH_INFO_TABLE =
            "CREATE TABLE " + HH_VISIT_INFO_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    HH_BASE_ENTITY_ID + " VARCHAR , " +MEMBER_BASE_ENTITY_ID + " VARCHAR , "+PAGE_EVENT_TYPE + " VARCHAR ,"
                    +EVENT_TYPE + " VARCHAR ," +INFO_COUNT + " INTEGER , "+IS_DONE + " INTEGER) ";



    public HouseHoldVisitInfoRepository(Repository repository) {
        super(repository);
    }

    protected String getHhVisitInfoTableName() {
        return HH_VISIT_INFO_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HH_INFO_TABLE);
    }
    public void dropTable(){
       try{
           getWritableDatabase().execSQL("delete from "+getHhVisitInfoTableName());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void addOrUpdateHHDataType(HHVisitInfoModel model) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(HH_BASE_ENTITY_ID, model.hhBaseEntityId);
        contentValues.put(MEMBER_BASE_ENTITY_ID, model.memberBaseEntityId);
        contentValues.put(EVENT_TYPE, model.eventType);
        contentValues.put(PAGE_EVENT_TYPE, model.pageEventType);
        contentValues.put(INFO_COUNT, model.infoCount);
        contentValues.put(IS_DONE, model.isDone);
       try{
           int status = getReadableDatabase().update(getHhVisitInfoTableName(),contentValues,
                   HH_BASE_ENTITY_ID +" = ?" + " and "+PAGE_EVENT_TYPE+" = ?" +" and "+EVENT_TYPE + " = ?",
                   new String[]{model.hhBaseEntityId,model.pageEventType,model.eventType});
           if(status <= 0){
               long inserted = getWritableDatabase().replace(getHhVisitInfoTableName(), null, contentValues);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void addOrUpdateHhMemmerData(HHVisitInfoModel model) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(HH_BASE_ENTITY_ID, model.hhBaseEntityId);
        contentValues.put(MEMBER_BASE_ENTITY_ID, model.memberBaseEntityId);
        contentValues.put(EVENT_TYPE, model.eventType);
        contentValues.put(PAGE_EVENT_TYPE, model.pageEventType);
        contentValues.put(INFO_COUNT, model.infoCount);
        contentValues.put(IS_DONE, model.isDone);
        try{
            int status = getReadableDatabase().update(getHhVisitInfoTableName(),contentValues,
                    HH_BASE_ENTITY_ID +" = ?" + " and "+PAGE_EVENT_TYPE+" = ?" +" and "+EVENT_TYPE + " = ?"+" and "+MEMBER_BASE_ENTITY_ID + " = ?",
                    new String[]{model.hhBaseEntityId,model.pageEventType,model.eventType,model.memberBaseEntityId});
            if(status <= 0){
                long inserted = getWritableDatabase().replace(getHhVisitInfoTableName(), null, contentValues);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<HHVisitInfoModel> getHhVisitInfoByHH(String hhBaseEntityId, String pageEventType) {
        List<HHVisitInfoModel> hhVisitInfoModelList = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " +
                getHhVisitInfoTableName() + " where " + PAGE_EVENT_TYPE + " = '" + pageEventType+"'"+" and "+HH_BASE_ENTITY_ID+" = '"+ hhBaseEntityId +"'", null)) {

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    HHVisitInfoModel hhVisitInfoModel = readCursor(cursor);
                    hhVisitInfoModelList.add(hhVisitInfoModel);
                    cursor.moveToNext();

                }
            }

        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        }
        return hhVisitInfoModelList;
    }
    public List<HHVisitInfoModel> getMemberDueInfo(String hhBaseEntityId,String memberBaseEntityid, String pageEventType) {
        List<HHVisitInfoModel> hhVisitInfoModelList = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " +
                getHhVisitInfoTableName() + " where " + PAGE_EVENT_TYPE + " = '" + pageEventType+"'"+" and "+HH_BASE_ENTITY_ID+" = '"+ hhBaseEntityId +"' and "+MEMBER_BASE_ENTITY_ID+" = '"+ memberBaseEntityid +"'", null)) {

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    HHVisitInfoModel hhVisitInfoModel = readCursor(cursor);
                    hhVisitInfoModelList.add(hhVisitInfoModel);
                    cursor.moveToNext();

                }
            }

        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        }
        return hhVisitInfoModelList;
    }

    public List<HHVisitInfoModel> getMemberDueByHH(String hhBaseEntityId,String memberBaseEntityId) {
        List<HHVisitInfoModel> hhVisitInfoModelList = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " +
                getHhVisitInfoTableName() + " where (" + PAGE_EVENT_TYPE + " = '" + HnppConstants.EVENT_TYPE.HH_MEMBER_DUE +"' or "+PAGE_EVENT_TYPE+" = '"+HnppConstants.EVENT_TYPE.HH_CHILD_DUE+"')"+" and "+HH_BASE_ENTITY_ID+" = '"+ hhBaseEntityId +"' and "+MEMBER_BASE_ENTITY_ID+" = '"+ memberBaseEntityId +"'", null)) {

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    HHVisitInfoModel hhVisitInfoModel = readCursor(cursor);
                    hhVisitInfoModelList.add(hhVisitInfoModel);
                    cursor.moveToNext();

                }
            }

        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        }
        return hhVisitInfoModelList;
    }


    public List<HHVisitInfoModel> getHhVisitInfoByMember(String memberBaseEntityId) {
        List<HHVisitInfoModel> hhVisitInfoModelList = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " +
                getHhVisitInfoTableName() + " where "
                +" and "+MEMBER_BASE_ENTITY_ID+" = '"+ memberBaseEntityId +"'", null)) {

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    HHVisitInfoModel hhVisitInfoModel = readCursor(cursor);
                    hhVisitInfoModelList.add(hhVisitInfoModel);
                    cursor.moveToNext();

                }
            }

        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        }
        return hhVisitInfoModelList;
    }

   public boolean deleteDataByHH(String baseEntityId){
        int delete = getReadableDatabase().delete(getHhVisitInfoTableName(),HH_BASE_ENTITY_ID +" = ?",new String[]{baseEntityId});
        return delete > 0;
    }


    protected HHVisitInfoModel readCursor(Cursor cursor) {
        String eventType = cursor.getString(cursor.getColumnIndex(EVENT_TYPE));
        String pageEventType = cursor.getString(cursor.getColumnIndex(PAGE_EVENT_TYPE));
        String hhBaseEntityId = cursor.getString(cursor.getColumnIndex(HH_BASE_ENTITY_ID));
        String memberBaseEntityId = cursor.getString(cursor.getColumnIndex(MEMBER_BASE_ENTITY_ID));
        int infoCount = cursor.getInt(cursor.getColumnIndex(INFO_COUNT));
        int isDone = cursor.getInt(cursor.getColumnIndex(IS_DONE));


        HHVisitInfoModel hhVisitDurationModel = new HHVisitInfoModel();
        hhVisitDurationModel.eventType = eventType;
        hhVisitDurationModel.pageEventType = pageEventType;
        hhVisitDurationModel.hhBaseEntityId = hhBaseEntityId;
        hhVisitDurationModel.memberBaseEntityId = memberBaseEntityId;
        hhVisitDurationModel.infoCount = infoCount;
        hhVisitDurationModel.isDone = isDone;
        return hhVisitDurationModel;
    }

}
