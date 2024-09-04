package org.smartregister.unicef.mis.repository;

import android.annotation.SuppressLint;
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
import org.smartregister.unicef.mis.location.CampModel;
import org.smartregister.unicef.mis.model.SbkCenter;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 25/08/2024.
 */
public class SBKRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    protected static final String id = "id";
    protected static final String ReferralFacility = "ReferralFacility";
    protected static final String division = "division";
    protected static final String district = "district";
    protected static final String upazila = "upazila";
    protected static final String code = "code";
    protected static final String divisionId = "divisionId";
    protected static final String districtId = "districtId";
    protected static final String created = "created";
    protected static final String updated = "updated";
    protected static final String mobile = "mobile";
    protected static final String ServerVersion = "ServerVersion";
    protected static final String TABLE = "sbk_table";

    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    id + " INTEGER , " +ReferralFacility + " VARCHAR , "+division + " VARCHAR , " +district + " VARCHAR , " +
                    upazila + " VARCHAR , " +code + " VARCHAR , " +
                    divisionId + " VARCHAR , " +districtId + " VARCHAR , " +
                    created + " VARCHAR , " +updated + " VARCHAR , " +
                    mobile + " VARCHAR , " +ServerVersion + " VARCHAR  ) ";

//    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
//            + LOCATION_TABLE + "_" + SS_NAME + "_ind ON " + LOCATION_TABLE + "(" + SS_NAME + ")";


    public SBKRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_LOCATION_TABLE);
    }
    public void dropTable(){
       try{
           getWritableDatabase().execSQL("delete from "+getLocationTableName());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void addOrUpdate(SbkCenter sbkCenter) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(id, sbkCenter.id);
        contentValues.put(ReferralFacility,  sbkCenter.ReferralFacility);
        contentValues.put(division, sbkCenter.division);
        contentValues.put(district,  sbkCenter.district);
        contentValues.put(upazila, sbkCenter.upazila);
        contentValues.put(code,  sbkCenter.code);
        contentValues.put(divisionId, sbkCenter.divisionId);
        contentValues.put(districtId,  sbkCenter.districtId);
        contentValues.put(upazila, sbkCenter.upazila);
        contentValues.put(created,  sbkCenter.created);
        contentValues.put(updated, sbkCenter.updated);
        contentValues.put(mobile,  sbkCenter.mobile);
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);
        Log.v("BLOCK_LOCATION","contentValues>>>"+inserted+":contentValues:"+contentValues);

    }
    @SuppressLint("Range")
    public ArrayList<String> getSbkCenterDivision() {
        Cursor cursor = null;
        ArrayList<String> locations = new ArrayList<>();
        try {
            String sql = "SELECT distinct(division) from " + getLocationTableName();
            Log.v("BLOCK_LOCATION","getAllWard>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String divisionName = cursor.getString(cursor.getColumnIndex(division));
                locations.add(divisionName);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("BLOCK_LOCATION","getAllWard>>>"+locations);
        return locations;
    }
    @SuppressLint("Range")
    public ArrayList<String> getSbkCenterDistrict(String divisionName) {
        Cursor cursor = null;
        ArrayList<String> locations = new ArrayList<>();
        try {
            String sql = "SELECT distinct(district) from " + getLocationTableName()+" where "+division+ " = '"+divisionName+"' ORDER by district asc";
            Log.v("SBK_CENTER","getAllWard>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String districtName = cursor.getString(cursor.getColumnIndex(district));
                locations.add(districtName);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("SBK_CENTER","getAllWard>>>"+locations);
        return locations;
    }
    @SuppressLint("Range")
    public ArrayList<String> getSbkCenterByDistrict(String divisionName, String districtName) {
        Cursor cursor = null;
        ArrayList<String> locations = new ArrayList<>();
        try {
            String sql = "SELECT distinct(ReferralFacility) from " + getLocationTableName()+" where "+division+ " = '"+divisionName+"' and "+district+" ='"+districtName+"' ORDER by ReferralFacility asc";
            Log.v("SBK_CENTER","getSbkCenterByDistrict>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String sbkCenterName = cursor.getString(cursor.getColumnIndex(ReferralFacility));
                locations.add(sbkCenterName);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("SBK_CENTER","getAllWard>>>"+locations);
        return locations;
    }

    @SuppressLint("Range")
    protected SbkCenter readCursor(Cursor cursor) {
        String facilityName = cursor.getString(cursor.getColumnIndex(ReferralFacility));
        String districtName = cursor.getString(cursor.getColumnIndex(district));
        String districtIdStr = cursor.getString(cursor.getColumnIndex(districtId));
        String mobileNo = cursor.getString(cursor.getColumnIndex(mobile));
        String divisionIdStr = cursor.getString(cursor.getColumnIndex(divisionId));
        SbkCenter sbkCenter = new SbkCenter();
        sbkCenter.ReferralFacility = facilityName;
        sbkCenter.district = districtName;
        sbkCenter.districtId = districtIdStr;
        sbkCenter.mobile = mobileNo;
        sbkCenter.divisionId = divisionIdStr;
        return sbkCenter;
    }
}
