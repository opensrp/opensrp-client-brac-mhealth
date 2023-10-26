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
                    FOLLOW_UP_DATE + " INTEGER , " +
                    NEXT_FOLLOW_UP_DATE + " INTEGER," +
                    VISIT_DATE + " INTEGER," +
                    TELEPHONY_FOLLOW_UP_DATE + " INTEGER," +
                    SPECIAL_FOLLOW_UP_DATE + " INTEGER ," +
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

    public ArrayList<AncFollowUpModel> getAncFollowUpData(String type) {
        Cursor cursor = null;
        ArrayList<AncFollowUpModel> ancFollowUpModelArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT a.base_entity_id," +
                    "a.follow_up_date," +
                    "a.next_follow_up_date," +
                    "a.visit_date," +
                    "a.telephony_follow_up_date," +
                    "a.special_follow_up_date," +
                    "a.no_of_anc," +
                    "r.high_risky_key," +
                    "r.high_risky_value," +
                    "r.low_risky_key," +
                    "r.low_risky_value," +
                    "r.risk_type," +
                    "e.first_name," +
                    "e.phone_number," +
                    "e.base_entity_id as b " +
                    "from anc_follow_up as a " +
                    "left join risk_list as r on a.base_entity_id = r.base_entity_id " +
                    "left join ec_family_member as e on a.base_entity_id = e.base_entity_id " +
                    "where (r.risk_type is null or r.risk_type = (select max(risk_type) from risk_list as rl where rl.base_entity_id = a.base_entity_id)) " +
                    "and e.base_entity_id not null " +
                    "group by a.base_entity_id", null);
            while (cursor.moveToNext()) {
                ancFollowUpModelArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return ancFollowUpModelArrayList;
    }

    protected AncFollowUpModel readCursor(Cursor cursor) {
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        long followUpDate = cursor.getLong(cursor.getColumnIndex(FOLLOW_UP_DATE));
        long nextFollowUpDate = cursor.getLong(cursor.getColumnIndex(NEXT_FOLLOW_UP_DATE));
        long visitDate = cursor.getLong(cursor.getColumnIndex(VISIT_DATE));
        long telephonyFollowUpDate = cursor.getLong(cursor.getColumnIndex(TELEPHONY_FOLLOW_UP_DATE));
        long specialFollowUpDate = cursor.getLong(cursor.getColumnIndex(SPECIAL_FOLLOW_UP_DATE));
        int noOfAnc = cursor.getInt(cursor.getColumnIndex(NO_OF_ANC));

        int riskType = cursor.getInt(cursor.getColumnIndex("risk_type"));

        String memberName = cursor.getString(cursor.getColumnIndex("first_name"));
        String memberPhoneNo = cursor.getString(cursor.getColumnIndex("phone_number"));

        AncFollowUpModel ancFollowUpModel = new AncFollowUpModel();
        ancFollowUpModel.baseEntityId = baseEntityId;
        ancFollowUpModel.followUpDate = followUpDate;
        ancFollowUpModel.nextFollowUpDate = nextFollowUpDate;
        ancFollowUpModel.visitDate = visitDate;
        ancFollowUpModel.telephonyFollowUpDate = telephonyFollowUpDate;
        ancFollowUpModel.specialFollowUpDate = specialFollowUpDate;
        ancFollowUpModel.noOfAnc = noOfAnc;
        ancFollowUpModel.riskType = riskType;

        ancFollowUpModel.memberName = memberName;
        ancFollowUpModel.memberPhoneNum = memberPhoneNo;


        return ancFollowUpModel;
    }

}
