package org.smartregister.unicef.mis.imci.repository;

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
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.imci.model.IMCIReport;
import org.smartregister.unicef.mis.location.CampModel;
import org.smartregister.unicef.mis.model.PaymentHistory;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by mahmud on 11/23/18.
 */
public class IMCIReportRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String IMCI_TYPE = "imci_type";
    public static final String ASSESSMENT_RESULT_TYPE = "assessment_result_type";
    public static final String TREATMENT = "treatment";
    public static final String ASSESSMENT_RESULT = "assessment_result";
    public static final String ASSESSMENT_TIMESTAMP = "assessment_timestamp";
    protected static final String TABLE_NAME = "imci_report_tbl";


    private static final String CREATE_IMCI_REPORT_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    BASE_ENTITY_ID + " VARCHAR , "  + IMCI_TYPE + " VARCHAR, " +
                    ASSESSMENT_RESULT_TYPE+ " VARCHAR, "+ TREATMENT+" VARCHAR, "+
                    ASSESSMENT_RESULT+" VARCHAR, "+ ASSESSMENT_TIMESTAMP+" LONG ) ";
    public IMCIReportRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return TABLE_NAME;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_IMCI_REPORT_TABLE);
    }
    public void dropTable(){
       try{
           getWritableDatabase().execSQL("delete from "+getLocationTableName());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void addOrUpdate(IMCIReport imciReport) {
        if(!isExistData(imciReport.getBaseEntityId(),imciReport.getImciType())){
            ContentValues contentValues = new ContentValues();
            contentValues.put(BASE_ENTITY_ID, imciReport.getBaseEntityId());
            contentValues.put(IMCI_TYPE, imciReport.getImciType());
            contentValues.put(ASSESSMENT_RESULT_TYPE, imciReport.getAssessmentResultType());
            contentValues.put(TREATMENT,imciReport.getTreatment());
            contentValues.put(ASSESSMENT_RESULT, imciReport.getAssessmentResult());
            contentValues.put(ASSESSMENT_TIMESTAMP, imciReport.getAssessmentTimeStamp());
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("IMCI_REPORT","inserterd:"+inserted+":contentValues:"+contentValues);
        }else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(BASE_ENTITY_ID, imciReport.getBaseEntityId());
            contentValues.put(IMCI_TYPE, imciReport.getImciType());
            contentValues.put(ASSESSMENT_RESULT_TYPE, imciReport.getAssessmentResultType());
            contentValues.put(TREATMENT,imciReport.getTreatment());
            contentValues.put(ASSESSMENT_RESULT, imciReport.getAssessmentResult());
            contentValues.put(ASSESSMENT_TIMESTAMP, imciReport.getAssessmentTimeStamp());
            int isUpdated = getWritableDatabase().update(getLocationTableName(), contentValues,
                    BASE_ENTITY_ID + " = ?  and "+IMCI_TYPE +" = ?", new String[]{imciReport.getBaseEntityId(),imciReport.getImciType()});
            Log.v("IMCI_REPORT","updated:"+isUpdated+":content:"+contentValues);
        }


    }
    public boolean isExistData(String baseEntityId, String imciType){
        String sql = "select count(*) from "+getLocationTableName()+" where "+BASE_ENTITY_ID+" = '"+baseEntityId+"' and "+IMCI_TYPE+" = '"+imciType+"'";
        Log.v("IMCI_REPORT","sql:"+sql);
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
    public int getIMCIReportCount(String baseEntityId){

        android.database.Cursor cursor = null;
        int count=0;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT count(*) FROM " + getLocationTableName()+" where "+BASE_ENTITY_ID+" ='"+baseEntityId+"' order by "+ASSESSMENT_TIMESTAMP+" ASC", null);
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            if(cursor!=null)cursor.close();
            return count;
        } catch (Exception e) {
            Timber.e(e);
        }
        return count;
    }
    public ArrayList<IMCIReport> getIMCIReportList(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<IMCIReport> imciReportList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+BASE_ENTITY_ID+" ='"+baseEntityId+"' order by "+ASSESSMENT_TIMESTAMP+" ASC", null);
            while (cursor.moveToNext()) {
                imciReportList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return imciReportList;
    }
    protected IMCIReport readCursor(Cursor cursor) {
        // String paymentId = cursor.getString(cursor.getColumnIndex(PAYMENT_ID));
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        String imciType = cursor.getString(cursor.getColumnIndex(IMCI_TYPE));
        String assessmentResultType = cursor.getString(cursor.getColumnIndex(ASSESSMENT_RESULT_TYPE));
        String assessmentResult = cursor.getString(cursor.getColumnIndex(ASSESSMENT_RESULT));
        long assessmentTimestamp = cursor.getLong(cursor.getColumnIndex(ASSESSMENT_TIMESTAMP));

        IMCIReport imciReport = new IMCIReport();
        imciReport.setImciType(imciType);
        imciReport.setAssessmentResult(assessmentResult);
        imciReport.setAssessmentResultType(assessmentResultType);
        imciReport.setAssessmentTimeStamp(assessmentTimestamp);
        imciReport.setAssessmentDate(HnppConstants.getDateWithHHMMFormateFromLong(assessmentTimestamp));

        return imciReport;
    }
}
