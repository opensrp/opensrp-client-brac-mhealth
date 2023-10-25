package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.RiskListModel;
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by tanvir on 10/25/18.
 */
public class RiskListRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    protected static final String RISK_LIST_TABLE = "risk_list";
    protected static final String BASE_ENTITY_ID = "base_entity_id";
    protected static final String HIGH_RISKY_KEY = "high_risky_key";
    protected static final String HIGH_RISKY_VALUE = "high_risky_value";
    protected static final String LOW_RISKY_KEY = "low_risky_key";
    protected static final String LOW_RISKY_VALUE = "low_risky_value";
    protected static final String RISK_TYPE = "risk_type";


    protected static final String[] COLUMNS = new String[]{ID, BASE_ENTITY_ID};

    private static final String CREATE_RISK_LIST_TABLE =
            "CREATE TABLE " + RISK_LIST_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    BASE_ENTITY_ID + " VARCHAR , " +
                    HIGH_RISKY_KEY + " VARCHAR , " +
                    HIGH_RISKY_VALUE+ " VARCHAR,"+
                    LOW_RISKY_KEY + " VARCHAR , " +
                    LOW_RISKY_VALUE+ " VARCHAR,"+
                    RISK_TYPE + " INTEGER ) ";



    public RiskListRepository(Repository repository) {
        super(repository);
    }

    protected String getRiskListTableName() {
        return RISK_LIST_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_RISK_LIST_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getRiskListTableName());
    }

    public void addOrUpdate(RiskListModel riskListModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BASE_ENTITY_ID, riskListModel.baseEntityId);
        contentValues.put(HIGH_RISKY_KEY, riskListModel.highRiskValue);
        contentValues.put(HIGH_RISKY_VALUE, riskListModel.highRiskValue);
        contentValues.put(LOW_RISKY_KEY, riskListModel.lowRiskKey);
        contentValues.put(LOW_RISKY_VALUE, riskListModel.lowRiskValue);
        contentValues.put(RISK_TYPE, riskListModel.riskType);
        long inserted = getWritableDatabase().replace(getRiskListTableName(), null, contentValues);


    }

    public ArrayList<RiskyModel> getRiskyKeyByEntityId(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<RiskyModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getRiskListTableName()+" where base_entity_id = '"+baseEntityId+"", null);
            while (cursor.moveToNext()) {
               // locations.add(readCursor(cursor));
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

    protected RiskListModel readCursor(Cursor cursor) {
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        String highRiskKey = cursor.getString(cursor.getColumnIndex(HIGH_RISKY_KEY));
        String highRiskValue = cursor.getString(cursor.getColumnIndex(HIGH_RISKY_VALUE));
        String lowRiskKey = cursor.getString(cursor.getColumnIndex(LOW_RISKY_KEY));
        String lowRiskValue = cursor.getString(cursor.getColumnIndex(LOW_RISKY_VALUE));
        int riskType = cursor.getInt(cursor.getColumnIndex(RISK_TYPE));

        RiskListModel riskListModel = new RiskListModel();
        riskListModel.baseEntityId = baseEntityId;
        riskListModel.highRiskKey = highRiskKey;
        riskListModel.highRiskValue = highRiskValue;
        riskListModel.lowRiskKey = lowRiskKey;
        riskListModel.lowRiskValue = lowRiskValue;
        riskListModel.riskType = riskType;
        return riskListModel;
    }

}
