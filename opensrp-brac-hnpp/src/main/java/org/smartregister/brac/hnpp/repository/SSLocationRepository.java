package org.smartregister.brac.hnpp.repository;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.location.SSLocations;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahmud on 11/23/18.
 */
public class SSLocationRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";
    protected static final String SS_NAME = "ss_name";
    protected static final String SK_NAME = "sk_name";
    protected static final String SK_USER_NAME = "sk_user_name";
    protected static final String IS_SELECTED = "is_selected";
    protected static final String WITHOUT_SK = "without_sk";
    protected static final String SS_ID = "ss_id";
    protected static final String IS_SIMPRINT_ENABLE = "simprints_enable";
    protected static final String PAYMENT_ENABLE = "payment_enable";
    protected static final String GEOJSON = "geojson";

    protected static final String LOCATION_TABLE = "ss_location";

    protected static final String[] COLUMNS = new String[]{ID, SS_NAME, GEOJSON};

    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + LOCATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    SS_NAME + " VARCHAR , " +IS_SIMPRINT_ENABLE + " VARCHAR , " +PAYMENT_ENABLE + " VARCHAR , " +WITHOUT_SK + " VARCHAR , " +
                    GEOJSON + " VARCHAR NOT NULL ) ";

    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
            + LOCATION_TABLE + "_" + SS_NAME + "_ind ON " + LOCATION_TABLE + "(" + SS_NAME + ")";
    public static final String ALTER_PAYMENT=" ALTER TABLE "+LOCATION_TABLE+" ADD COLUMN "+PAYMENT_ENABLE+" VARCHAR;";
    public static final String ALTER_PA=" ALTER TABLE "+LOCATION_TABLE+" ADD COLUMN "+WITHOUT_SK+" VARCHAR;";

    public SSLocationRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return LOCATION_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_LOCATION_TABLE);
        database.execSQL(CREATE_LOCATION_NAME_INDEX);
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
        contentValues.put(SK_NAME, ssModel.skName.trim());
        contentValues.put(SK_USER_NAME, ssModel.skUserName);
        contentValues.put(SS_NAME, ssModel.username.trim());
        contentValues.put(SS_ID, ssModel.ss_id);
        contentValues.put(IS_SIMPRINT_ENABLE, ssModel.simprints_enable);
        contentValues.put(PAYMENT_ENABLE, ssModel.payment_enable);
        contentValues.put(WITHOUT_SK, ssModel.withoutsk);
        contentValues.put(GEOJSON, gson.toJson(ssModel.locations));
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);


    }
    public int updateSelection(boolean isSelected, String ssId){
        ContentValues contentValues = new ContentValues();
        contentValues.put(IS_SELECTED,isSelected?"1":"0");
        int isUpdated = getWritableDatabase().update(getLocationTableName(), contentValues,
                SS_ID + " = ?  ", new String[]{ssId});
        Log.v("IS_UPDATED","updateSelection:SS_ID:"+ssId+":isUpdated:"+isUpdated);
        return isUpdated;
    }

    public ArrayList<SSModel> getAllSks() {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" group by "+SK_USER_NAME, null);
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
    public ArrayList<SSModel> getAllSelectedSks() {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+IS_SELECTED+" = '1' group by "+SK_USER_NAME, null);
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
    public ArrayList<SSModel> getAllSS(String userName) {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+SK_USER_NAME+" = '"+userName+"' group by "+SS_ID, null);
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
    public SSLocations getSSLocationStr(String ssName) {
        android.database.Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery("select geojson from ss_location where ss_name ='"+ssName+"'",null);
            if (cursor.moveToNext()) {
                String jsonEventStr = (cursor.getString(0));
                JSONArray jsonArray = new JSONArray(jsonEventStr);
                SSLocations locations = null;
                for(int i = 0; i <jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    locations = new Gson().fromJson(jsonObject.toString(), SSLocations.class);
                }
                return locations;
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
    public ArrayList<SSModel> getAllSelectedSS(String userName) {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+SK_USER_NAME+" = '"+userName+"' and "+IS_SELECTED+" = '1' group by "+SS_ID, null);
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
    public ArrayList<SSModel> getAllLocations() {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
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
    public ArrayList<SSModel> getAllSelectedLocations() {
        Cursor cursor = null;
        ArrayList<SSModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+IS_SELECTED+" = '1' group by "+SS_ID, null);
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
    public boolean isExistLocation(String name) {
        Cursor cursor = null;
        ArrayList<String> locationIds = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT "+ID+" FROM " + getLocationTableName()+" where "+SS_NAME+" = '"+name.trim()+"'", null);
            while (cursor.moveToNext()) {
                locationIds.add(cursor.getString(0));
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return locationIds.size()>0;
    }
    @SuppressLint("Range")
    protected SSModel readCursor(Cursor cursor) {
        String geoJson = cursor.getString(cursor.getColumnIndex(GEOJSON));
        String name = cursor.getString(cursor.getColumnIndex(SS_NAME));
        String ssId= cursor.getString(cursor.getColumnIndex(SS_ID));
        String skName= cursor.getString(cursor.getColumnIndex(SK_NAME));
        String skUserName= cursor.getString(cursor.getColumnIndex(SK_USER_NAME));
        String simprints = cursor.getString(cursor.getColumnIndex(IS_SIMPRINT_ENABLE));
        String paymentEnable = cursor.getString(cursor.getColumnIndex(PAYMENT_ENABLE));
        String isSelected = cursor.getString(cursor.getColumnIndex(IS_SELECTED));
        String withoutSk = cursor.getString(cursor.getColumnIndex(WITHOUT_SK));
        SSModel ssModel = new SSModel();
        ssModel.username = name.trim();
        ssModel.ss_id = ssId;
        ssModel.skName = skName;
        ssModel.withoutsk = withoutSk;
        ssModel.skUserName = skUserName;
        ssModel.simprints_enable = simprints != null && simprints.equalsIgnoreCase("1");
        ssModel.payment_enable = paymentEnable != null && paymentEnable.equalsIgnoreCase("1");
        ssModel.is_selected = isSelected!=null && isSelected.equalsIgnoreCase("1");
        try {
            JSONArray jsonArray = new JSONArray(geoJson);
            for(int i = 0; i <jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SSLocations locations = new Gson().fromJson(jsonObject.toString(), SSLocations.class);
                ssModel.locations.add(locations);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ssModel;
    }

}
