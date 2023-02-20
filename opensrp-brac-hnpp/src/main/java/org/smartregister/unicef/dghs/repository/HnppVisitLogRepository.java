package org.smartregister.unicef.dghs.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.model.ReferralFollowUpModel;
import org.smartregister.unicef.dghs.utils.ANCRegister;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.VisitLog;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import java.util.ArrayList;
import java.util.HashMap;
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
    public static final String BLOCK_NAME = "block_name";

    public static final String[] TABLE_COLUMNS = {VISIT_ID, VISIT_TYPE,FAMILY_ID, BASE_ENTITY_ID, VISIT_DATE,EVENT_TYPE,VISIT_JSON,PREGNANT_STATUS,BLOCK_NAME};
    private static final String VISIT_LOG_SQL = "CREATE TABLE ec_visit_log (visit_id VARCHAR,visit_type VARCHAR,base_entity_id VARCHAR NOT NULL,refer_reason VARCHAR,refer_place VARCHAR" +
            ",family_id VARCHAR NOT NULL,visit_date VARCHAR,event_type VARCHAR,visit_json TEXT,pregnant_status VARCHAR,block_name VARCHAR)";

    public HnppVisitLogRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
       try{
           database.execSQL(VISIT_LOG_SQL);
       }catch (SQLiteException e){

       }
    }

    public void updateFamilyFromHomeVisit(ContentValues values,String base_entity_id,String last_home_visit){
        try{
            SQLiteDatabase database = getWritableDatabase();
            values.put("last_home_visit",last_home_visit);
            supportListOfAsset(values);
            String selection = "base_entity_id = '"+base_entity_id+"' and (last_home_visit < '"+last_home_visit+"' or last_home_visit is null)";
            int isUpdated = database.update("ec_family",values,selection,null);
        }catch(Exception e){
            e.printStackTrace();

        }
    }

    /*
        This method reshape the multiple question input abc,xye output like ["abc","xyz"]
     */
    private void supportListOfAsset(ContentValues values) {
        if(values.get("list_of_assets")!=null){
            String valuesWillComma = (String)values.get("list_of_assets");
            String newValue ="";
            String[] spiltArray = valuesWillComma.split(",");
            StringBuilder builder = new StringBuilder();
            if(spiltArray.length ==1){
                builder.append("\"");
                builder.append(valuesWillComma);
                builder.append("\"");
                newValue = builder.toString();
            }else{
                for(String value:spiltArray){
                    builder = new StringBuilder();
                    builder.append(newValue);
                    builder.append("\"");
                    builder.append(value);
                    builder.append("\"");
                    builder.append(",");
                    newValue = builder.toString();
                }
            }
            if(newValue.endsWith(",")) newValue = newValue.substring(0,newValue.length()-1);
            newValue = "["+newValue+"]";
            Log.v("HH_VISIT","supportListOfAsset>>newValue:"+newValue+":valuesWillComma"+valuesWillComma);
            values.put("list_of_assets",newValue);
        }
    }

    public HashMap<String, String>  tableHasColumn(HashMap<String, String> details) {
        HashMap<String, String> existColumn = new HashMap<>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(ec_family)",null);
        if(cursor!=null){
            int cursorCount = cursor.getCount();
            for (int i = 1; i < cursorCount; i++ ) {
                cursor.moveToPosition(i);
                String storedSqlColumnName = cursor.getString(cursor.getColumnIndex("name"));
                try{
                    String value = details.get(storedSqlColumnName);
                    if(!TextUtils.isEmpty(value)){
                        existColumn.put(storedSqlColumnName,value);
                    }
                }catch (Exception e){
                    e.printStackTrace();

                }
            }
        }

        if(cursor!=null) cursor.close();
        return existColumn;
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
//    public ArrayList<String> getNeedToUpdateAlreadyProccedVisit(){
//        ArrayList<String>visit_ids = new ArrayList<String>();
//        Cursor cursor = null;
//        try{
//
//            SQLiteDatabase database = getWritableDatabase();
//            cursor= database.rawQuery("select visits.visit_id from visits where visits.processed ='2' ",null);
//            if(cursor!=null && cursor.getCount() > 0) {
//                cursor.moveToFirst();
//                while (!cursor.isAfterLast()) {
//                    String visit_id = cursor.getString(0);
//                    visit_ids.add(visit_id);
//                    cursor.moveToNext();
//                }
//            }
//        }catch(Exception e){
//
//        }
//        finally {
//            if(cursor!=null) cursor.close();
//        }
//        return visit_ids;
//    }
    public ArrayList<String> getPregnancyOutcomeEvents(){
        ArrayList<String>visit_ids = new ArrayList<String>();
        try{

            SQLiteDatabase database = getWritableDatabase();
            Cursor cursor = database.rawQuery("select visit_id from visits where visit_type ='Pregnancy Outcome' ",null);
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
    public ArrayList<Visit> getVisitByVisitId(String visitId){
        SQLiteDatabase database = getWritableDatabase();

        String selection = VISIT_ID+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{visitId};
        net.sqlcipher.Cursor cursor = database.query("visits", null, selection, selectionArgs, null, null, VISIT_DATE + " DESC", null);
        ArrayList<Visit> homeVisits = getVisits(cursor);
        return homeVisits;
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
        values.put(BLOCK_NAME, visitLog.getBlockName());
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
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
    private ArrayList<Visit> getVisits(Cursor cursor) {
        ArrayList<Visit> visitLogs = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Visit visitLog = new Visit();
                    visitLog.setVisitId(cursor.getString(cursor.getColumnIndex(VISIT_ID)));
                    visitLog.setBaseEntityId(cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)));
                    visitLog.setJson(cursor.getString(cursor.getColumnIndex("visit_json")));
                    visitLog.setVisitType(cursor.getString(cursor.getColumnIndex(VISIT_TYPE)));
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
                    visitLog.setBlockName(cursor.getString(cursor.getColumnIndex(BLOCK_NAME)));
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
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE+" and "+EVENT_TYPE+" = ? or event_type is null"+COLLATE_NOCASE;
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
    public boolean isDoneWihinTwentyFourHours(String baseEntityId, String eventType) {
        if(TextUtils.isEmpty(eventType)) return true;

        String visitType = getCorrespondingVisitType(eventType);
        String query = "select visit_type from visits where visit_type ='"+visitType+"' and base_entity_id ='"+baseEntityId+"' and ((strftime('%s',datetime('now')) - strftime('%s',datetime(visit_date/1000,'unixepoch','localtime')))/3600)<24";
        Log.v("DUE_VISIT",""+query);
        android.database.Cursor cursor = null;
        boolean isExist = false;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    isExist = true;
                    cursor.moveToNext();

                }
            }
        }catch (Exception e){

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        return isExist;
    }

    private String getCorrespondingVisitType(String eventType) {
        switch (eventType){
            case HnppConstants.EVENT_TYPE.ANC1_REGISTRATION:
            case HnppConstants.EVENT_TYPE.ANC2_REGISTRATION:
            case HnppConstants.EVENT_TYPE.ANC3_REGISTRATION:
            case HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY:
            case HnppConstants.EVENT_TYPE.ANC_GENERAL_DISEASE:
                return CoreConstants.EventType.ANC_HOME_VISIT;
            case HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC:
                return HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour;
            case HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC:
                return HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
            default:
                return eventType;
        }
    }

    public boolean isDoneWihinChildInfoLogic(String baseEntityId, String eventTpe) {
        if(TextUtils.isEmpty(eventTpe)) return true;
        if(isDoneWihinTwentyFourHours(baseEntityId,eventTpe)) return true;
        String query ="";
        if(eventTpe.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12) ||
                eventTpe.equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS)){
            query  = "select event_type from ec_visit_log where event_type ='"+eventTpe+"' and base_entity_id ='"+baseEntityId+"'";

        }else{
            //query = "select event_type from ec_visit_log where event_type ='"+eventTpe+"' and base_entity_id ='"+baseEntityId+"' and (strftime('%d',datetime(visit_date/1000,'unixepoch','localtime')) = strftime('%d',datetime('now')))";
            query = "select event_type, CAST((julianday(DATE('now'))-julianday(DATE(ROUND(visit_date / 1000), 'unixepoch')))as INTEGER) as d from ec_visit_log where event_type ='"+eventTpe+"' and base_entity_id ='"+baseEntityId+"' and ((d >= '180' AND d <= '210') or (d >= '331' AND d <= '366') or (d >= '515' AND d <= '545'))";
        }


        String eventType="";
        android.database.Cursor cursor = null;
        try {
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
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
            net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null, " rowid DESC");
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
            net.sqlcipher.Cursor cursor = database.query(VISIT_LOG_TABLE_NAME, TABLE_COLUMNS, selection, selectionArgs, null, null,   " rowid DESC");
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
