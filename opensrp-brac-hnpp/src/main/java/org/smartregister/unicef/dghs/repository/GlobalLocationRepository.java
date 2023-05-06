package org.smartregister.unicef.dghs.repository;

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
import org.smartregister.unicef.dghs.location.BaseLocation;
import org.smartregister.unicef.dghs.location.BlockLocation;
import org.smartregister.unicef.dghs.location.HALocation;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class GlobalLocationRepository extends BaseRepository {
    public enum LOCATION_TAG {
        DIVISION(11),
        DISTRICT(12),
        UPAZILA(13),
        PAUROSOVA(21),
        UNION(14),
        WARD(15),
        BLOCK(16),
        COUNTRY(10),
        OLD_WARD(17),
        POST_OFFICE(18);
        int value;
        private LOCATION_TAG(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }
    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    protected static final String GEO_ID = "id";
    protected static final String PARENT_LOCATION_ID = "parent_location_id";
    protected static final String LOCATION_TAG_ID = "location_tag_id";
    protected static final String CODE = "code";
    protected static final String NAME = "name";
    protected static final String LOCATION_TABLE = "global_location";


    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    GEO_ID + " INTEGER , " +CODE + " VARCHAR , " +LOCATION_TAG_ID + " INTEGER , "+
                    PARENT_LOCATION_ID + " INTEGER , " +NAME + " VARCHAR ) ";

//    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
//            + LOCATION_TABLE + "_" + SS_NAME + "_ind ON " + LOCATION_TABLE + "(" + SS_NAME + ")";


    public GlobalLocationRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return LOCATION_TABLE;
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

    public void addOrUpdate(GlobalLocationModel globalLocationModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(GEO_ID, globalLocationModel.id);
        contentValues.put(PARENT_LOCATION_ID,  globalLocationModel.parentLocationId);
        contentValues.put(LOCATION_TAG_ID, globalLocationModel.locationTagId);
        contentValues.put(CODE,  globalLocationModel.code);
        contentValues.put(NAME, globalLocationModel.name);
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);


    }

    public ArrayList<GlobalLocationModel> getLocationByParentId(int parentId) {
        Cursor cursor = null;
        ArrayList<GlobalLocationModel> locations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getLocationTableName()+" where "+PARENT_LOCATION_ID+" = "+parentId;
            Log.v("BLOCK_LOCATION","getLocationByParentId>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                locations.add(readBlockCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("BLOCK_LOCATION","locations>>>"+locations);
        return locations;
    }
    public ArrayList<GlobalLocationModel> getLocationByTagId(int locationTagId) {
        Cursor cursor = null;
        ArrayList<GlobalLocationModel> locations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getLocationTableName()+" where "+LOCATION_TAG_ID+" = "+locationTagId;
            Log.v("BLOCK_LOCATION","getLocationByParentId>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                locations.add(readBlockCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("BLOCK_LOCATION","locations>>>"+locations);
        return locations;
    }
    public ArrayList<GlobalLocationModel> getLocationByTagIdWithParentId(int locationTagId, int parentId) {
        Cursor cursor = null;
        ArrayList<GlobalLocationModel> locations = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + getLocationTableName()+" where "+LOCATION_TAG_ID+" = "+locationTagId+" and "+PARENT_LOCATION_ID+" = "+parentId;
            Log.v("LOCATION","getLocationByTagIdWithParentId>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                locations.add(readBlockCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        Log.v("BLOCK_LOCATION","locations>>>"+locations);
        return locations;
    }


    private GlobalLocationModel readBlockCursor(Cursor cursor){
        String name = cursor.getString(cursor.getColumnIndex(NAME));
        String code = cursor.getString(cursor.getColumnIndex(CODE));
        int id = cursor.getInt(cursor.getColumnIndex(GEO_ID));
        int parentLocationId = cursor.getInt(cursor.getColumnIndex(PARENT_LOCATION_ID));
        int locationTagId = cursor.getInt(cursor.getColumnIndex(LOCATION_TAG_ID));
        GlobalLocationModel globalLocationModel = new GlobalLocationModel();
        globalLocationModel.parentLocationId = parentLocationId;
        globalLocationModel.locationTagId = locationTagId;
        globalLocationModel.id = id;
        globalLocationModel.code = code;
        globalLocationModel.name = name;
        return globalLocationModel;
    }
}
