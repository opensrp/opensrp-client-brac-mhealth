package org.smartregister.unicef.mis.repository;

import android.content.ContentValues;
import android.content.Intent;
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
import org.smartregister.unicef.mis.utils.ReferralData;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class ReferralRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String REFERRAL_TABLE = "referral_table";
    protected static final String REFERRAL_ID = "referral_id";
    public static final String REFERRAL_CAUSE = "referral_cause";
    public static final String REFERRAL_PLACE = "referral_place";
    public static final String REFERRAL_DATE = "referral_date";
    public static final String REFERRAL_EVENT = "referral_event";
    public static final String REFERRAL_STATUS = "referral_status";
    public static final String BASE_ENTITY_ID = "base_entity_id";

    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + REFERRAL_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +REFERRAL_ID + " VARCHAR , " +BASE_ENTITY_ID+" VARCHAR ,"+
                    REFERRAL_CAUSE + " VARCHAR, " + REFERRAL_PLACE+ " VARCHAR, "+REFERRAL_DATE+" LONG,"+REFERRAL_EVENT+" VARCHAR,"+REFERRAL_STATUS+" INT) ";


    public ReferralRepository(Repository repository) {
        super(repository);
    }

    protected String getTableName() {
        return REFERRAL_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getTableName());
    }
    public  void addAndUpdateReferral(ReferralData referralData){
        ContentValues contentValues = new ContentValues();
        contentValues.put(REFERRAL_ID, referralData.referralId);
        contentValues.put(REFERRAL_CAUSE, referralData.referralCause);
        contentValues.put(REFERRAL_PLACE, referralData.referralPlace);
        contentValues.put(REFERRAL_DATE, referralData.referralDate);
        contentValues.put(REFERRAL_EVENT,referralData.referralEvent);
        contentValues.put(REFERRAL_STATUS, referralData.referralStatus);
        contentValues.put(BASE_ENTITY_ID, referralData.baseEntityId);
        SQLiteDatabase database = getWritableDatabase();
        try{

                long inserted = database.insert(getTableName(), null, contentValues);
                Log.v("REFERRAL_FOLLOWUP","inserted:"+inserted+":contentValues"+contentValues);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateReferralStatus(String referralId){
        getWritableDatabase().execSQL("update "+getTableName()+" set "+REFERRAL_STATUS+" = '1' where "+BASE_ENTITY_ID+" = '"+referralId+"'");
    }


    public ArrayList<ReferralData> getReferralData(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<ReferralData> referralDataList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+BASE_ENTITY_ID+" ='"+baseEntityId+"'";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                referralDataList.add(readCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("OtherVaccineContentData","locations>>>"+referralDataList.size());
        return referralDataList;
    }
    public ReferralData getIsReferralDataById(String baseEntityId) {
        Cursor cursor = null;
        ReferralData referralData = null;
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+BASE_ENTITY_ID+" ='"+baseEntityId+"' and "+REFERRAL_STATUS+" = 0 order by "+REFERRAL_DATE+" DESC";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                referralData = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return referralData;
    }
    public ReferralData getReferralDataById(String referralId) {
        Cursor cursor = null;
        ReferralData referralData = new ReferralData();
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+REFERRAL_ID+" ='"+referralId+"'";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                referralData = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return referralData;
    }

    protected ReferralData readCursor(Cursor cursor) {
        ReferralData referralData = new ReferralData();
        referralData.referralCause =  cursor.getString(cursor.getColumnIndex(REFERRAL_CAUSE));
        referralData.referralPlace = cursor.getString(cursor.getColumnIndex(REFERRAL_PLACE));
        referralData.referralDate = cursor.getLong(cursor.getColumnIndex(REFERRAL_DATE));
        referralData.referralEvent = cursor.getString(cursor.getColumnIndex(REFERRAL_EVENT));
        referralData.referralStatus = Integer.parseInt(cursor.getString(cursor.getColumnIndex(REFERRAL_STATUS)));
        referralData.baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));

        return referralData;
    }

}
