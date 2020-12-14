package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.utils.ANCRegister;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class HnppVisitLogRepository extends BaseRepository {

    public static final String VISIT_LOG_TABLE_NAME = "ec_visit_log";
    public static final String VISIT_ID = "visit_id";
    public static final String VISIT_TYPE = "visit_type";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String FAMILY_ID = "family_id";
    public static final String VISIT_DATE = "visit_date";
    public static final String EVENT_TYPE = "event_type";
    public static final String VISIT_JSON = "visit_json";
    public static final String REFER_REASON = "refer_reason";
    public static final String REFER_PLACE = "refer_place";
    public static final String PREGNANT_STATUS = "pregnant_status";
    public static final String SS_NAME = "ss_name";


    public static final String[] TABLE_COLUMNS = {VISIT_ID, VISIT_TYPE,FAMILY_ID, BASE_ENTITY_ID, VISIT_DATE,EVENT_TYPE,VISIT_JSON,PREGNANT_STATUS,SS_NAME};
    private static final String VISIT_LOG_SQL = "CREATE TABLE ec_visit_log (visit_id VARCHAR,visit_type VARCHAR,base_entity_id VARCHAR NOT NULL,family_id VARCHAR NOT NULL,visit_date VARCHAR,event_type VARCHAR,visit_json TEXT,pregnant_status VARCHAR)";

    public HnppVisitLogRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
       try{
           database.execSQL(VISIT_LOG_SQL);
       }catch (SQLiteException e){

       }
    }

    public void updateFamilyLastHomeVisit(String base_entity_id,String last_home_visit){
        try{
            SQLiteDatabase database = getWritableDatabase();
            String sql = "update ec_family set last_home_visit = '"+last_home_visit+"' where " +
                    "base_entity_id = '"+base_entity_id+"' and (last_home_visit < '"+last_home_visit+"' or last_home_visit is null);";
            database.execSQL(sql);
        }catch(Exception e){
            e.printStackTrace();

        }
    }
    public long add(VisitLog visitLog) {
        long rowId = -1;
        if (visitLog == null) {
            return rowId;
        }
        try {
            SQLiteDatabase database = getWritableDatabase();

            if (visitLog.getBaseEntityId() != null && findUnique(database, visitLog) == null) {
                rowId = database.insert(VISIT_LOG_TABLE_NAME, null, createValuesFor(visitLog));
                Log.v("PROCESS_CLIENT","row insert:"+rowId+":"+visitLog.getEventType());
                return rowId;

            }


        } catch (Exception e) {
            Log.v("PROCESS_CLIENT","exception database:"+visitLog.getEventType());
            e.printStackTrace();
        }
        return rowId;

    }

    public String getHeight(String base_entity_id){
        SQLiteDatabase database = getWritableDatabase();
        Cursor cursor = database.rawQuery("select height from ec_anc_register where base_entity_id = '"+base_entity_id+"'",null);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getString(0);
        }
        return "";
    }
    public ArrayList<String> getVisitIds(){
        ArrayList<String>visit_ids = new ArrayList<String>();
        try{

            SQLiteDatabase database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select visits.visit_id from visits where visits.visit_id NOT IN (select ec_visit_log.visit_id from ec_visit_log) order by visit_date DESC",null);
            if(cursor!=null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String visit_id = cursor.getString(0);
                    visit_ids.add(visit_id);
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }catch(Exception e){

        }
        return visit_ids;
    }
    public ArrayList<VisitLog> getAllSSFormVisit(){
        SQLiteDatabase database = getWritableDatabase();

        String selection = EVENT_TYPE+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{HnppConstants.EVENT_TYPE.SS_INFO};
        net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, VISIT_DATE + " DESC", null);
        ArrayList<VisitLog> homeVisitList = getAllVisitLog(cursor);
        return homeVisitList;
    }
    public VisitLog findUnique(SQLiteDatabase db, VisitLog visitLog) {
        Log.v("PROCESS_CLIENT","findUnique");
        if (visitLog == null || TextUtils.isEmpty(visitLog.getBaseEntityId())) {
            return null;
        }
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " and " + VISIT_ID + " = ? " + COLLATE_NOCASE+" and "+EVENT_TYPE+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{visitLog.getBaseEntityId(), visitLog.getVisitId() + "",visitLog.getEventType()};
        net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, null, null);
        List<VisitLog> homeVisitList = getAllVisitLog(cursor);
        if (homeVisitList.size() > 0) {
            Log.v("PROCESS_CLIENT","not uniq:"+visitLog.getEventType());
            return homeVisitList.get(0);
        }
        return null;
    }

    private ContentValues createValuesFor(VisitLog visitLog) {
        ContentValues values = new ContentValues();
        values.put(VISIT_ID, visitLog.getVisitId());
        values.put(VISIT_TYPE, visitLog.getVisitType());
        values.put(BASE_ENTITY_ID, visitLog.getBaseEntityId());
        values.put(FAMILY_ID, visitLog.getFamilyId());
        values.put(VISIT_DATE, visitLog.getVisitDate());
        values.put(EVENT_TYPE, visitLog.getEventType());
        values.put(VISIT_JSON, visitLog.getVisitJson());
        values.put(REFER_PLACE, visitLog.getReferPlace());
        values.put(REFER_REASON, visitLog.getReferReason());
        values.put(PREGNANT_STATUS, visitLog.getPregnantStatus());
        values.put(SS_NAME, visitLog.getSsName());
        return values;
    }
    public  ArrayList<ReferralFollowUpModel> getAllReferrelFollowUp(String baseEntityId){

        ArrayList<ReferralFollowUpModel> list = new ArrayList<>();
        String query = "select "+BASE_ENTITY_ID+","+REFER_REASON+","+REFER_PLACE+" from ec_visit_log  where base_entity_id= '"+baseEntityId+"' and (event_type = '"+HnppConstants.EVENT_TYPE.MEMBER_REFERRAL+"' or " +
                " event_type = '"+HnppConstants.EVENT_TYPE.WOMEN_REFERRAL+"' or event_type = '"+HnppConstants.EVENT_TYPE.CHILD_REFERRAL+"') AND refer_reason != \"\" " +
            "and refer_reason NOT IN(select refer_reason from ec_visit_log where base_entity_id= '"+baseEntityId+"' and event_type = '"+HnppConstants.EVENT_TYPE.REFERREL_FOLLOWUP+"' and refer_reason is not null)";

        //String query = "select "+BASE_ENTITY_ID+","+REFER_REASON+","+REFER_PLACE+" from "+ VISIT_LOG_TABLE_NAME +" where base_entity_id ='"+baseEntityId+"' and "+EVENT_TYPE+" = '"+HnppConstants.EVENT_TYPE.MEMBER_REFERRAL+"'";
        android.database.Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ReferralFollowUpModel referralFollowUpModel = new ReferralFollowUpModel();
                    referralFollowUpModel.setBaseEntityId(cursor.getString(0));
                    referralFollowUpModel.setReferralReason(HnppConstants.referealResonMapping.get(cursor.getString(1)));
                    try{
                        String[] refPlace = cursor.getString(2).split(",");
                        String placeAtBangla="";
                        for (String str : refPlace){
                            if(TextUtils.isEmpty(placeAtBangla)){
                                placeAtBangla = HnppConstants.referealPlaceMapping.get(str);
                            }else{
                                placeAtBangla = HnppConstants.referealPlaceMapping.get(str)+","+placeAtBangla;
                            }


                        }
                        referralFollowUpModel.setReferralPlace(placeAtBangla);
                    }catch (Exception e){

                    }

                    list.add(referralFollowUpModel);
                    cursor.moveToNext();

                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return list;
    }
    private ArrayList<VisitLog> getAllVisitLog(Cursor cursor) {
        ArrayList<VisitLog> visitLogs = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    VisitLog visitLog = new VisitLog();
                    visitLog.setVisitId(cursor.getString(cursor.getColumnIndex(VISIT_ID)));
                    visitLog.setBaseEntityId(cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)));
                    visitLog.setEventType(cursor.getString(cursor.getColumnIndex(EVENT_TYPE)));
                    visitLog.setVisitDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(VISIT_DATE))));
                    visitLog.setVisitJson(cursor.getString(cursor.getColumnIndex(VISIT_JSON)));
                    visitLog.setVisitType(cursor.getString(cursor.getColumnIndex(VISIT_TYPE)));
                    visitLog.setPregnantStatus(cursor.getString(cursor.getColumnIndex(PREGNANT_STATUS)));
                    visitLog.setSsName(cursor.getString(cursor.getColumnIndex(SS_NAME)));
                    visitLogs.add(visitLog);
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return visitLogs;

    }
    public ANCRegister getLastANCRegister(String baseEntityID) {
        SQLiteDatabase database = getReadableDatabase();
        net.sqlcipher.Cursor cursor = null;
        try {
            if (database == null) {
                return null;
            }
            String[] ANC_TABLE_COLUMNS = {"last_menstrual_period", "edd", "no_prev_preg", "no_surv_children","height"};

            cursor = database.query(Constants.TABLES.ANC_MEMBERS, ANC_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE, new String[]{baseEntityID}, null, null, null);

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                ANCRegister ancRegister = new ANCRegister();
                ancRegister.setLastMenstrualPeriod(cursor.getString(0));
                ancRegister.setEDD(cursor.getString(1));
                ancRegister.setNoPrevPreg(cursor.getString(2));
                ancRegister.setNoSurvChildren(cursor.getString(3));
                ancRegister.setHEIGHT(cursor.getString(4));
                return ancRegister;
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;

    }
    public boolean isFirstTime(String baseEntityId){
        SQLiteDatabase database = getReadableDatabase();
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE+" and "+EVENT_TYPE+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY};
        net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, VISIT_DATE + " DESC");
        ArrayList<VisitLog> visits = getAllVisitLog(cursor);

        return  visits!=null && visits.size() == 0;
    }
    public boolean isPregnantFromElco(String baseEntityId){
        SQLiteDatabase database = getReadableDatabase();
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE+" and "+EVENT_TYPE+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, HnppConstants.EVENT_TYPE.ELCO};
        net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, VISIT_DATE + " DESC",1+"");
        ArrayList<VisitLog> visits = getAllVisitLog(cursor);
        if(visits!=null && visits.size()>0){
            String pregnantStatus = visits.get(0).pregnantStatus;
            if(!TextUtils.isEmpty(pregnantStatus) && pregnantStatus.equalsIgnoreCase("pregnant")){
                return true;
            }

        }

        return  false;
    }
    public boolean isDoneWihinTwentyFourHours(String baseEntityId, String eventTpe) {
        if(TextUtils.isEmpty(eventTpe)) return true;
        String query ="";
        if(eventTpe.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION) ||
                eventTpe.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY)){
            query  = "select event_type from ec_visit_log where (event_type ='"+HnppConstants.EVENT_TYPE.ANC1_REGISTRATION+"' OR event_type ='"+HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY+"') and base_entity_id ='"+baseEntityId+"' and (strftime('%d',datetime(visit_date/1000,'unixepoch','localtime')) = strftime('%d',datetime('now')))";

        }else{
            query = "select event_type from ec_visit_log where event_type ='"+eventTpe+"' and base_entity_id ='"+baseEntityId+"' and (strftime('%d',datetime(visit_date/1000,'unixepoch','localtime')) = strftime('%d',datetime('now')))";

        }

        String eventType="";
        android.database.Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    eventType = cursor.getString(0);
                    cursor.moveToNext();

                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return !TextUtils.isEmpty(eventType);
    }
    public  ArrayList<String> getEventsWithin24Hours(String baseEntityId){

        ArrayList<String> eventList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String query = "select event_type from ec_visit_log where base_entity_id ='"+baseEntityId+"' and (strftime('%d',datetime(visit_date/1000,'unixepoch')) = strftime('%d',datetime('now')))";
        android.database.Cursor cursor = null;
        try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    eventList.add(cursor.getString(0));
                    cursor.moveToNext();

                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return eventList;
    }
    public ArrayList<VisitLog> getAllVisitLog(String baseEntityId) {
        SQLiteDatabase database = getReadableDatabase();
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId};
        try{
            net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, VISIT_DATE + " DESC");
            return getAllVisitLog(cursor);
        }catch (Exception e){

        }
        return new ArrayList<>();

    }
    public ArrayList<VisitLog> getAllVisitLogForFamily(String familyId) {
        SQLiteDatabase database = getReadableDatabase();
        String selection = FAMILY_ID + " = ? " + COLLATE_NOCASE+" and ("+VISIT_TYPE+"!='"+HnppConstants.EVENT_TYPE.FORUM_ADULT+"' and "+VISIT_TYPE+"!='"+HnppConstants.EVENT_TYPE.FORUM_WOMEN+"' and "+VISIT_TYPE+"!='"+HnppConstants.EVENT_TYPE.FORUM_NCD+"' and "+VISIT_TYPE+"!='"+HnppConstants.EVENT_TYPE.FORUM_ADO+"' and "+VISIT_TYPE+"!='"+HnppConstants.EVENT_TYPE.FORUM_CHILD+"')";
        String[] selectionArgs = new String[]{familyId};
        try{
            net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, VISIT_DATE + " DESC");
            return getAllVisitLog(cursor);
        }catch (Exception e){

        }
        return new ArrayList<>();

    }

    public VisitLog getLatestEntry(String familyId) {
        SQLiteDatabase database = getReadableDatabase();

        return null;
    }



}
