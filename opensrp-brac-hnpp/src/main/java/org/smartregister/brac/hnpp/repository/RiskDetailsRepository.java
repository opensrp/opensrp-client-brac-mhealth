package org.smartregister.brac.hnpp.repository;

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
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by mahmud on 11/23/18.
 */
public class RiskDetailsRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    protected static final String RISKY_TABLE = "risky_details";
    protected static final String BASE_ENTITY_ID = "base_entity_id";
    protected static final String EVENT_TYPE = "event_type";
    protected static final String RISKY_KEY = "risky_key";
    protected static final String RISKY_VALUE = "risky_value";


    protected static final String[] COLUMNS = new String[]{ID, BASE_ENTITY_ID, EVENT_TYPE,RISKY_KEY};

    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE " + RISKY_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    BASE_ENTITY_ID + " VARCHAR , " +EVENT_TYPE + " VARCHAR , " + RISKY_VALUE+ " VARCHAR NOT NULL,"+
                    RISKY_KEY + " VARCHAR NOT NULL ) ";

    private static final String CREATE_LOCATION_NAME_INDEX = "CREATE INDEX "
            + RISKY_TABLE + "_" + BASE_ENTITY_ID + "_ind ON " + RISKY_TABLE + "(" + BASE_ENTITY_ID + ")";


    public RiskDetailsRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return RISKY_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_LOCATION_TABLE);
        database.execSQL(CREATE_LOCATION_NAME_INDEX);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public void addOrUpdate(RiskyModel riskyModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BASE_ENTITY_ID, riskyModel.baseEntityId);
        contentValues.put(RISKY_KEY, riskyModel.riskyKey);
        contentValues.put(EVENT_TYPE, riskyModel.eventType);
        contentValues.put(RISKY_VALUE, riskyModel.riskyValue);
        long inserted = getWritableDatabase().replace(getLocationTableName(), null, contentValues);


    }

    public ArrayList<RiskyModel> getRiskyKeyByEntityId(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<RiskyModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where base_entity_id = '"+baseEntityId+"'", null);
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

    protected RiskyModel readCursor(Cursor cursor) {
        String riskyKey = cursor.getString(cursor.getColumnIndex(RISKY_KEY));
        String eventType = cursor.getString(cursor.getColumnIndex(EVENT_TYPE));
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        String riskyValue = cursor.getString(cursor.getColumnIndex(RISKY_VALUE));
        RiskyModel riskyModel = new RiskyModel();
        riskyModel.baseEntityId = baseEntityId;
        riskyModel.eventType = eventType;
        riskyModel.riskyKey = riskyKey;
        riskyModel.riskyValue = riskyValue;
        return riskyModel;
    }

}
