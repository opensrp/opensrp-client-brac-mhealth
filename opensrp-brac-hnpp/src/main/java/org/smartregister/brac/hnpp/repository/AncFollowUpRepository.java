package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

/**
 * Created by tanvir on 10/25/23.
 */
public class AncFollowUpRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    protected static final String ANC_FOLLOW_UP_TABLE = "anc_follow_up";
    protected static final String BASE_ENTITY_ID = "base_entity_id";
    protected static final String FOLLOW_UP_DATE = "follow_up_date";
    protected static final String NEXT_FOLLOW_UP_DATE = "next_follow_up_date";
    protected static final String VISIT_DATE = "visit_date";
    protected static final String TELEPHONY_FOLLOW_UP_DATE = "telephony_follow_up_date";
    protected static final String SPECIAL_FOLLOW_UP_DATE = "special_follow_up_date";
    protected static final String NO_OF_ANC = "no_of_anc";


    protected static final String[] COLUMNS = new String[]{ID, BASE_ENTITY_ID};

    private static final String CREATE_ANC_FOLLOW_UP_TABLE =
            "CREATE TABLE " + ANC_FOLLOW_UP_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    BASE_ENTITY_ID + " VARCHAR , " +
                    FOLLOW_UP_DATE + " VARCHAR , " +
                    NEXT_FOLLOW_UP_DATE + " VARCHAR," +
                    VISIT_DATE + " VARCHAR NOT NULL," +
                    TELEPHONY_FOLLOW_UP_DATE + " VARCHAR," +
                    SPECIAL_FOLLOW_UP_DATE + " VARCHAR ," +
                    NO_OF_ANC + " INTEGER ) ";


    public AncFollowUpRepository(Repository repository) {
        super(repository);
    }

    protected String getAncFollowupTableName() {
        return ANC_FOLLOW_UP_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_ANC_FOLLOW_UP_TABLE);
    }

    public void dropTable() {
        getWritableDatabase().execSQL("delete from " + getAncFollowupTableName());
    }

    public void add(AncFollowUpModel ancFollowUpModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BASE_ENTITY_ID, ancFollowUpModel.baseEntityId);
        contentValues.put(FOLLOW_UP_DATE, ancFollowUpModel.followUpDate);
        contentValues.put(NEXT_FOLLOW_UP_DATE, ancFollowUpModel.nextFollowUpDate);
        contentValues.put(VISIT_DATE, ancFollowUpModel.visitDate);
        contentValues.put(TELEPHONY_FOLLOW_UP_DATE, ancFollowUpModel.telephonyFollowUpDate);
        contentValues.put(SPECIAL_FOLLOW_UP_DATE, ancFollowUpModel.specialFollowUpDate);
        contentValues.put(NO_OF_ANC, ancFollowUpModel.noOfAnc);
        long inserted = getWritableDatabase().replace(getAncFollowupTableName(), null, contentValues);
    }

    public void update(AncFollowUpModel ancFollowUpModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BASE_ENTITY_ID, ancFollowUpModel.baseEntityId);
        contentValues.put(FOLLOW_UP_DATE, ancFollowUpModel.followUpDate);
        contentValues.put(NEXT_FOLLOW_UP_DATE, ancFollowUpModel.nextFollowUpDate);
        contentValues.put(VISIT_DATE, ancFollowUpModel.visitDate);
        contentValues.put(TELEPHONY_FOLLOW_UP_DATE, ancFollowUpModel.telephonyFollowUpDate);
        contentValues.put(SPECIAL_FOLLOW_UP_DATE, ancFollowUpModel.specialFollowUpDate);
        contentValues.put(NO_OF_ANC, ancFollowUpModel.noOfAnc);
        int status = getReadableDatabase().update(getAncFollowupTableName(), contentValues,
                BASE_ENTITY_ID + " = ?",
                new String[]{ancFollowUpModel.baseEntityId});
        if (status <= 0) {
            long updated = getWritableDatabase().replace(getAncFollowupTableName(), null, contentValues);
        }
    }

    public ArrayList<RiskyModel> getRiskyKeyByEntityId(String baseEntityId) {
        Cursor cursor = null;
        ArrayList<RiskyModel> locations = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getAncFollowupTableName() + " where base_entity_id = '" + baseEntityId + "", null);
            while (cursor.moveToNext()) {
                //locations.add(readCursor(cursor));
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

    protected AncFollowUpModel readCursor(Cursor cursor) {
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        String followUpDate = cursor.getString(cursor.getColumnIndex(FOLLOW_UP_DATE));
        String nextFollowUpDate = cursor.getString(cursor.getColumnIndex(NEXT_FOLLOW_UP_DATE));
        String visitDate = cursor.getString(cursor.getColumnIndex(VISIT_DATE));
        String telephonyFollowUpDate = cursor.getString(cursor.getColumnIndex(TELEPHONY_FOLLOW_UP_DATE));
        String specialFollowUpDate = cursor.getString(cursor.getColumnIndex(SPECIAL_FOLLOW_UP_DATE));
        int noOfAnc = cursor.getInt(cursor.getColumnIndex(NO_OF_ANC));

        AncFollowUpModel ancFollowUpModel = new AncFollowUpModel();
        ancFollowUpModel.baseEntityId = baseEntityId;
        ancFollowUpModel.followUpDate = followUpDate;
        ancFollowUpModel.nextFollowUpDate = nextFollowUpDate;
        ancFollowUpModel.visitDate = visitDate;
        ancFollowUpModel.telephonyFollowUpDate = telephonyFollowUpDate;
        ancFollowUpModel.specialFollowUpDate = specialFollowUpDate;
        ancFollowUpModel.noOfAnc = noOfAnc;
        return ancFollowUpModel;
    }

}
