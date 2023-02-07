package org.smartregister.unicef.dghs.repository;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.unicef.dghs.location.DistrictModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class DistrictListRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    public static final String name = "name";
    public static final String upazila = "upazila";

    protected static final String LOCATION_TABLE = "district_list";

    protected static final String[] COLUMNS = new String[]{ID, name, upazila};

    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    name + " VARCHAR , " +upazila + " VARCHAR ) ";

    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
            + LOCATION_TABLE + "_" + name + "_ind ON " + LOCATION_TABLE + "(" + upazila + ")";


    public DistrictListRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return LOCATION_TABLE;
    }
    public boolean isExistData(){
        String sql = "select count(*) from district_list";
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
    public ArrayList<String> getDistrictNames(){
        String sql = "select name from district_list group by name";
        Cursor cursor = null;
        ArrayList<String> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    locations.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;

    }
    public ArrayList<String> getUpazilaFromDistrict(String district){
        String sql = "select upazila from district_list where name = '"+district+"' group by upazila";
        Cursor cursor = null;
        ArrayList<String> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    locations.add(cursor.getString(0));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;

    }
    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_LOCATION_TABLE);
        database.execSQL(CREATE_LOCATION_NAME_INDEX);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }


    public ArrayList<DistrictModel> getAllLocations() {
        Cursor cursor = null;
        ArrayList<DistrictModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName(), null);
            while (cursor.moveToNext()) {
                locations.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;
    }
    public boolean isExistLocation(String name) {
        Cursor cursor = null;
        ArrayList<String> locationIds = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT "+ID+" FROM " + getLocationTableName()+" where "+name+" = '"+name.trim()+"'", null);
            while (cursor.moveToNext()) {
                locationIds.add(cursor.getString(0));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locationIds.size()>0;
    }
    public void batchInsert(ArrayList<DistrictModel>dmlist){
        if(dmlist.size()==0)return;
        String sql = "INSERT INTO district_list(name,upazila) VALUES ";
        String VALUES = "";
        for(int i=0;i<dmlist.size();i++){
            VALUES += "('"+dmlist.get(i).name+"','"+dmlist.get(i).upazila+"'),";
        }
        VALUES = VALUES.substring(0,VALUES.length()-1);
        sql = sql + VALUES;
        Log.v("LOCATION_FETCH","batchInsert>>sql:"+sql);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
    }
    protected DistrictModel readCursor(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(this.name));
        String upazila = cursor.getString(cursor.getColumnIndex(this.upazila));
        DistrictModel DistrictModel = new DistrictModel();
        DistrictModel.name = name.trim();
        DistrictModel.upazila = upazila.trim();
//        try {
//            JSONArray jsonArray = new JSONArray(geoJson);
//            for(int i = 0; i <jsonArray.length();i++){
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                SSLocations locations = new Gson().fromJson(jsonObject.toString(), SSLocations.class);
//                DistrictModel.locations.add(locations);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return DistrictModel;
    }

}
