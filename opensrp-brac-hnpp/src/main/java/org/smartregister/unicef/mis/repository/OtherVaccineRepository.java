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
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class OtherVaccineRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String OTHER_VACCINE_TABLE = "other_vaccine_table";
    protected static final String OTHER_VACCINE_ID = "vaccine_id";
    public static final String VACCINE_NAME = "vaccine_name";
    public static final String BRN = "brn";
    public static final String DOB = "dob";
    public static final String VACCINE_DATE = "vaccine_date";
    public static final String VACCINE_JSON = "vaccine_json";
    public static final String IS_SYNC = "is_sync";

    private static final String CREATE_TARGET_TABLE =
            "CREATE TABLE " + OTHER_VACCINE_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +VACCINE_NAME + " VARCHAR , " +
                    BRN + " VARCHAR, " + DOB+ " VARCHAR, "+VACCINE_DATE+" VARCHAR,"+VACCINE_JSON+" VARCHAR,"+IS_SYNC+" INT) ";


    public OtherVaccineRepository(Repository repository) {
        super(repository);
    }

    protected String getTableName() {
        return OTHER_VACCINE_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TARGET_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getTableName());
    }

    public  void addOtherVaccine(OtherVaccineContentData otherVaccineContentData){
        ContentValues contentValues = new ContentValues();
        contentValues.put(VACCINE_NAME, otherVaccineContentData.vaccine_name);
        contentValues.put(VACCINE_DATE, otherVaccineContentData.date);
        contentValues.put(BRN, otherVaccineContentData.brn);
        contentValues.put(DOB, otherVaccineContentData.dob);
        contentValues.put(VACCINE_JSON,gson.toJson(otherVaccineContentData));
        contentValues.put(IS_SYNC, 0);
        SQLiteDatabase database = getWritableDatabase();
        try{
            //if(findUnique(database,otherVaccineContentData)){
                long inserted = database.insert(getTableName(), null, contentValues);
                Log.v("INDICATOR_INSERTED","inserted:"+inserted);
//            }else{
//                Log.v("INDICATOR_INSERTED","failed value:"+contentValues);
//            }
        }catch (Exception e){
            e.printStackTrace();
        }


//        getWritableDatabase().execSQL("update "+getLocationTableName()+" set achievemnt_count = achievemnt_count +1,"+DAY+" = "+day+" , "+MONTH+" = "+month+" , "+YEAR+" = "+year+" where "+INDICATOR_NAME+" = '"+targetName+"'");
    }
    public void updateOtherVaccineStatus(OtherVaccineContentData otherVaccineContentData){
        getWritableDatabase().execSQL("update "+getTableName()+" set is_sync = 1 where "+VACCINE_NAME+" = '"+otherVaccineContentData.vaccine_name+"' and "+BRN+" ='"+otherVaccineContentData.brn+"'");
    }
    public boolean findUnique(SQLiteDatabase db, OtherVaccineContentData otherVaccineContentData) {
        SQLiteDatabase database = (db == null) ? getReadableDatabase() : db;
        String selection = VACCINE_NAME + " = ? " + COLLATE_NOCASE + " and " + VACCINE_DATE + " = ? " + COLLATE_NOCASE+" and "+BRN+" = ?"+COLLATE_NOCASE+" and "+DOB+" = ?"+COLLATE_NOCASE;
        String[] selectionArgs = new String[]{otherVaccineContentData.vaccine_name, otherVaccineContentData.date,otherVaccineContentData.brn,otherVaccineContentData.dob};
        Cursor cursor = database.query(getTableName(), null, selection, selectionArgs, null, null, null, null);
        if(cursor!=null && cursor.getCount() > 0){
            cursor.close();
            return false;
        }
        if(cursor!=null) cursor.close();
        return true;
    }
    public ArrayList<OtherVaccineContentData> getUnSyncData() {
        Cursor cursor = null;
        ArrayList<OtherVaccineContentData> otherVaccineContentData = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getTableName()+" where "+IS_SYNC+" = '0' limit 30";
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                otherVaccineContentData.add(readCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("OtherVaccineContentData","locations>>>"+otherVaccineContentData.size());
        return otherVaccineContentData;
    }

    protected OtherVaccineContentData readCursor(Cursor cursor) {
//        String vaccineName = cursor.getString(cursor.getColumnIndex(VACCINE_NAME));
//        String vaccineDate = cursor.getString(cursor.getColumnIndex(VACCINE_DATE));
//        String brn = cursor.getString(cursor.getColumnIndex(BRN));
//        String dob = cursor.getString(cursor.getColumnIndex(DOB));
        String vaccineJson = cursor.getString(cursor.getColumnIndex(VACCINE_JSON));

        return gson.fromJson(vaccineJson,OtherVaccineContentData.class);
    }

}
