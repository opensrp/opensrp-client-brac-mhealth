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
import org.smartregister.unicef.dghs.location.GeoLocation;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.unicef.dghs.location.WardLocation;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class GeoLocationRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    protected static final String COUNTRY_ID = "country_id";
    protected static final String COUNTRY_GEO = "country_geo";
    protected static final String DIVISION_ID = "division_id";
    protected static final String DIVISION_GEO = "division_geo";
    protected static final String DISTRICT_ID = "district_id";
    protected static final String DISTRICT_GEO = "district_geo";
    protected static final String UPAZILA_ID = "upazila_id";
    protected static final String UPAZILA_GEO = "upazila_geo";
    protected static final String UNION_ID = "union_id";
    protected static final String UNION_GEO = "union_geo";
    protected static final String BLOCK_ID = "block_id";
    protected static final String BLOCK_GEO = "block_geo";
    protected static final String WARD_ID = "ward_id";
    protected static final String WARD_GEO = "ward_geo";
    protected static final String LOCATION_TABLE = "geo_location";


    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    COUNTRY_ID + " INTEGER , " +COUNTRY_GEO + " VARCHAR , " +
                    DIVISION_ID + " INTEGER , " +DIVISION_GEO + " VARCHAR , " +
                    DISTRICT_ID + " INTEGER , " +DISTRICT_GEO + " VARCHAR , " +
                    UPAZILA_ID + " INTEGER , " +UPAZILA_GEO + " VARCHAR , " +
                    UNION_ID + " INTEGER , " +UNION_GEO + " VARCHAR , " +
                    BLOCK_ID + " INTEGER , " +BLOCK_GEO + " VARCHAR , " +
                    WARD_ID + " INTEGER , " +WARD_GEO + " VARCHAR  ) ";

//    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
//            + LOCATION_TABLE + "_" + SS_NAME + "_ind ON " + LOCATION_TABLE + "(" + SS_NAME + ")";


    public GeoLocationRepository(Repository repository) {
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

    public void addOrUpdate(SSModel ssModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COUNTRY_ID, ssModel.locations.get(0).country.id);
        contentValues.put(COUNTRY_GEO,  gson.toJson(ssModel.locations.get(0).country));
        contentValues.put(DIVISION_ID, ssModel.locations.get(0).division.id);
        contentValues.put(DIVISION_GEO,  gson.toJson(ssModel.locations.get(0).division));
        contentValues.put(DISTRICT_ID, ssModel.locations.get(0).district.id);
        contentValues.put(DISTRICT_GEO,  gson.toJson(ssModel.locations.get(0).district));
        contentValues.put(UPAZILA_ID, ssModel.locations.get(0).upazila.id);
        contentValues.put(UPAZILA_GEO,  gson.toJson(ssModel.locations.get(0).upazila));
        contentValues.put(UNION_ID, ssModel.locations.get(0).union.id);
        contentValues.put(UNION_GEO,  gson.toJson(ssModel.locations.get(0).union));
        contentValues.put(WARD_ID, ssModel.locations.get(0).ward.id);
        contentValues.put(WARD_GEO,  gson.toJson(ssModel.locations.get(0).ward));
        contentValues.put(BLOCK_ID, ssModel.locations.get(0).block.id);
        contentValues.put(BLOCK_GEO,  gson.toJson(ssModel.locations.get(0).block));
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);


    }

    public ArrayList<WardLocation> getAllWard() {
        Cursor cursor = null;
        ArrayList<WardLocation> locations = new ArrayList<>();
        try {
            String sql = "SELECT ward_id,ward_geo FROM " + getLocationTableName()+" group by "+WARD_ID;
            Log.v("BLOCK_LOCATION","getAllWard>>>"+sql);
            cursor = getReadableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                locations.add(readWardCursor(cursor));
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
    public ArrayList<BlockLocation> getOnlyBlockLocationByWardId(String wardId) {
        Cursor cursor = null;
        ArrayList<BlockLocation> locations = new ArrayList<>();
        try {
            String sql = "SELECT block_id,block_geo FROM " + getLocationTableName()+" where "+WARD_ID+" = '"+wardId+"' group by "+BLOCK_ID;
            Log.v("BLOCK_LOCATION","getOnlyBlockLocationByWardId>>>"+sql);
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
    public ArrayList<GeoLocation> getLocationByWardId(String wardId) {
        Cursor cursor = null;
        ArrayList<GeoLocation> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT ward_id,ward_geo FROM " + getLocationTableName()+" where "+WARD_ID+" = '"+wardId+"' group by "+BLOCK_ID, null);
            while (cursor.moveToNext()) {
                locations.add(readCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locations;
    }

    public GeoLocation getLocationByBlock(String blockId) {
        Cursor cursor = null;
        GeoLocation geoLocation = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+BLOCK_ID+" = '"+blockId+"' group by "+BLOCK_ID, null);
            while (cursor.moveToNext()) {
                geoLocation = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return geoLocation;
    }

    protected GeoLocation readCursor(Cursor cursor) {
        String countryId = cursor.getString(cursor.getColumnIndex(COUNTRY_ID));
        String countryGeo = cursor.getString(cursor.getColumnIndex(COUNTRY_GEO));
        String divisionId = cursor.getString(cursor.getColumnIndex(DIVISION_ID));
        String divisionGeo = cursor.getString(cursor.getColumnIndex(DIVISION_GEO));
        String districtId = cursor.getString(cursor.getColumnIndex(DISTRICT_ID));
        String districtGeo = cursor.getString(cursor.getColumnIndex(DISTRICT_GEO));
        String upazilaId = cursor.getString(cursor.getColumnIndex(UPAZILA_ID));
        String upazilaGeo = cursor.getString(cursor.getColumnIndex(UPAZILA_GEO));
        String unionId = cursor.getString(cursor.getColumnIndex(UNION_ID));
        String unionGeo = cursor.getString(cursor.getColumnIndex(UNION_GEO));
        String wardId = cursor.getString(cursor.getColumnIndex(WARD_ID));
        String wardGeo = cursor.getString(cursor.getColumnIndex(WARD_GEO));
        String blockId = cursor.getString(cursor.getColumnIndex(BLOCK_ID));
        String blockGeo = cursor.getString(cursor.getColumnIndex(BLOCK_GEO));
        GeoLocation ssModel = new GeoLocation();
        ssModel.country = gson.fromJson(countryGeo, BaseLocation.class);
        ssModel.division = gson.fromJson(divisionGeo, BaseLocation.class);
        ssModel.district = gson.fromJson(districtGeo, BaseLocation.class);
        ssModel.upazila = gson.fromJson(upazilaGeo, BaseLocation.class);
        ssModel.union = gson.fromJson(unionGeo, BaseLocation.class);
        ssModel.ward = gson.fromJson(wardGeo, BaseLocation.class);
        ssModel.block = gson.fromJson(blockGeo, BaseLocation.class);
        return ssModel;
    }
    private WardLocation readWardCursor(Cursor cursor){
        String wardId = cursor.getString(cursor.getColumnIndex(WARD_ID));
        String wardGeo = cursor.getString(cursor.getColumnIndex(WARD_GEO));
        WardLocation wardLocation = new WardLocation();
        wardLocation.ward = gson.fromJson(wardGeo, BaseLocation.class);
        return wardLocation;
    }
    private BlockLocation readBlockCursor(Cursor cursor){
        String blockId = cursor.getString(cursor.getColumnIndex(BLOCK_ID));
        String blockGeo = cursor.getString(cursor.getColumnIndex(BLOCK_GEO));
        BlockLocation blockLocation = new BlockLocation();
        blockLocation.block = gson.fromJson(blockGeo, BaseLocation.class);
        return blockLocation;
    }
}
