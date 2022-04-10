package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.Notification;
import org.smartregister.brac.hnpp.model.Survey;
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
public class SurveyHistoryRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String SURVEY_HISTORY_TABLE = "survey_history_table";
    protected static final String UUID = "uuid";
    protected static final String FORM_NAME = "form_name";
    public static final String DATE = "date_time";
    public static final String SS_NAME = "ss_name";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String TIME_STAMP = "time_stamp";
    public static final String SURVEY_TYPE = "type";
    public static final String FORM_ID = "form_id";



    private static final String CREATE_SURVEY_TABLE =
            "CREATE TABLE " + SURVEY_HISTORY_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    UUID + " VARCHAR , " +FORM_NAME + " VARCHAR , " + TIME_STAMP+" LONG,"+ SURVEY_TYPE+" VARCHAR,"+FORM_ID+" VARCHAR,"+
                    DATE + " VARCHAR, "+SS_NAME+" VARCHAR,"+BASE_ENTITY_ID+" VARCHAR ) ";




    public SurveyHistoryRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return SURVEY_HISTORY_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_SURVEY_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public boolean findUnique(SQLiteDatabase db, String uuid, String baseEntityId) {
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " and " + UUID + " = ? " + COLLATE_NOCASE;
        String[] selectionArgs = new String[]{baseEntityId, uuid};
        Cursor cursor = database.query(getLocationTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        if(cursor!=null) cursor.close();
        return true;
    }

    public boolean isExistData(String uuid){
        String sql = "select count(*) from "+getLocationTableName()+" where "+ UUID +" = '"+uuid+"'";
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
    public void addOrUpdate(Survey survey, String type) {
        if(!isExistData(survey.uuid)){
            String ssName = "";
            if(type.equalsIgnoreCase(HnppConstants.SURVEY_KEY.HH_TYPE)){
               ssName = HnppDBUtils.getSSNameByHHID(survey.baseEntityId);
            }else if(type.equalsIgnoreCase(HnppConstants.SURVEY_KEY.MM_TYPE)){
                ssName = HnppDBUtils.getSSName(survey.baseEntityId);
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(FORM_NAME, survey.formName);
            contentValues.put(UUID, survey.uuid);
            contentValues.put(BASE_ENTITY_ID, survey.baseEntityId);
            contentValues.put(DATE, survey.dateTime);
            contentValues.put(SS_NAME, ssName);
            contentValues.put(TIME_STAMP, survey.timestamp);
            contentValues.put(SURVEY_TYPE, survey.type);
            contentValues.put(FORM_ID, survey.formId);
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("SURVEY_HISTORY","inserterd:"+inserted+":ContentValues:"+contentValues);
        }else{
            Log.v("SURVEY_HISTORY","exists!!!!!!!!!");
        }


    }
    public ArrayList<Survey> getSurveyList(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<Survey> surveyArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+BASE_ENTITY_ID+" ='"+baseEntityId+"' order by "+DATE+" desc", null);
            while (cursor.moveToNext()) {
                surveyArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return surveyArrayList;
    }
    protected Survey readCursor(Cursor cursor) {
        String formName = cursor.getString(cursor.getColumnIndex(FORM_NAME));
        String uuid = cursor.getString(cursor.getColumnIndex(UUID));
        String date = cursor.getString(cursor.getColumnIndex(DATE));
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        long timeStamp = cursor.getLong(cursor.getColumnIndex(TIME_STAMP));
        String ssName = cursor.getString(cursor.getColumnIndex(SS_NAME));
        String formId = cursor.getString(cursor.getColumnIndex(FORM_ID));
        Survey survey = new Survey();
        survey.dateTime = date;
        survey.timestamp = timeStamp;
        survey.baseEntityId = baseEntityId;
        survey.uuid = uuid;
        survey.formName = formName;
        survey.ssName = ssName;
        survey.formId = formId;
        return survey;
    }

}
