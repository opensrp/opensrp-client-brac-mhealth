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
import org.smartregister.unicef.mis.location.CampModel;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class CampRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    protected static final String id = "id";
    protected static final String provider = "provider";
    protected static final String ward = "ward";
    protected static final String upazila = "upazila";
    protected static final String locationType = "locationType";
    protected static final String address = "address";
    protected static final String date = "date";
    protected static final String union = "union_s";
    protected static final String status = "status";
    protected static final String type = "type";
    protected static final String centerName = "centerName";
    protected static final String campName = "campName";
    protected static final String CAMP_TABLE = "camp_table";


    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + CAMP_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    id + " INTEGER , " +provider + " VARCHAR , "+ward + " VARCHAR , " +upazila + " VARCHAR , " +
                    date + " VARCHAR , " +locationType + " VARCHAR , " +
                    union + " VARCHAR , " +address + " VARCHAR , " +
                    status + " VARCHAR , " +centerName + " VARCHAR , " +
                    type + " VARCHAR , " +campName + " VARCHAR  ) ";

//    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
//            + LOCATION_TABLE + "_" + SS_NAME + "_ind ON " + LOCATION_TABLE + "(" + SS_NAME + ")";


    public CampRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return CAMP_TABLE;
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

    public void addOrUpdate(CampModel campModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(id, campModel.id);
        contentValues.put(campName,  campModel.campName);
        contentValues.put(provider, campModel.provider);
        contentValues.put(ward,  campModel.ward);
        contentValues.put(union, campModel.union);
        contentValues.put(centerName,  campModel.centerName);
        contentValues.put(date, campModel.date);
        contentValues.put(status,  campModel.status);
        contentValues.put(upazila, campModel.upazila);
        contentValues.put(locationType,  campModel.locationType);
        contentValues.put(address, campModel.address);
        contentValues.put(type,  campModel.type);
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);
        Log.v("BLOCK_LOCATION","contentValues>>>"+inserted+":contentValues:"+contentValues);

    }

    public ArrayList<CampModel> getAllCamp() {
        Cursor cursor = null;
        ArrayList<CampModel> locations = new ArrayList<>();
        try {
            String sql = "SELECT * from " + getLocationTableName();
            Log.v("BLOCK_LOCATION","getAllWard>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                locations.add(readCursor(cursor));
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


    protected CampModel readCursor(Cursor cursor) {
        String center = cursor.getString(cursor.getColumnIndex(centerName));
        String ty = cursor.getString(cursor.getColumnIndex(type));
        CampModel campModel = new CampModel();
        campModel.centerName = center;
        campModel.type = ty;
        return campModel;
    }
}
