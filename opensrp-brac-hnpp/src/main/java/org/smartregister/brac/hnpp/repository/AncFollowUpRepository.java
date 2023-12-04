package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.enums.FollowUpType;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.utils.RiskyModel;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;
import java.util.Calendar;

import timber.log.Timber;

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
    protected static final String IS_CALLED_TELEPHONIC = "is_called_telephonic";


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
                    IS_CALLED_TELEPHONIC + " INTEGER," +
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

        /*int status = getReadableDatabase().update(getAncFollowupTableName(), contentValues,
                BASE_ENTITY_ID + " = ?",
                new String[]{ancFollowUpModel.baseEntityId});*/
        //if (status <= 0) {
            long updated = getWritableDatabase().insert(getAncFollowupTableName(), null, contentValues);
       // }
    }

    public void updateCallStatus(AncFollowUpModel ancFollowUpModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IS_CALLED_TELEPHONIC, 1);

        int status = getReadableDatabase().update(getAncFollowupTableName(), contentValues,
                BASE_ENTITY_ID + " = ? and ("+IS_CALLED_TELEPHONIC+" is null or "+IS_CALLED_TELEPHONIC+" = '0')",
                new String[]{ancFollowUpModel.baseEntityId});
        if (status <= 0) {

        }
    }

    public void resetTelephonicDate(AncFollowUpModel ancFollowUpModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TELEPHONY_FOLLOW_UP_DATE, 0);

        int status = getReadableDatabase().update(getAncFollowupTableName(), contentValues,
                BASE_ENTITY_ID + " = ? and "+IS_CALLED_TELEPHONIC+" = '1'",
                new String[]{ancFollowUpModel.baseEntityId});
        if (status <= 0) {

        }
    }

    public ArrayList<AncFollowUpModel> getAncFollowUpData(FollowUpType type,boolean isFromReceiver) {
        Calendar calendar = Calendar.getInstance();
        String currentDate = calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
        //String currentDate = "2023-12-21";
        Cursor cursor = null;
        ArrayList<AncFollowUpModel> ancFollowUpModelArrayList = new ArrayList<>();
        try {
            String typeQuery = "";
            String orderByQuery = "";
            String minNextFollowUp = "";
            String minSpecialFollowUp = "";
            String minTelephonicFollowUp = "";

            if(type == FollowUpType.routine){
                if(isFromReceiver){
                    typeQuery = "and (date(a.next_follow_up_date/1000, 'unixepoch') = '"+currentDate+"')";
                }else {
                    typeQuery = "and (a.next_follow_up_date > 0)";
                }
                orderByQuery = "a.next_follow_up_date asc";

                minNextFollowUp = "min(a.next_follow_up_date) as next_follow_up_date,";
                minSpecialFollowUp = "a.special_follow_up_date,";
                minTelephonicFollowUp = "a.telephony_follow_up_date,";

            }else if(type == FollowUpType.special){
                if(isFromReceiver){
                    typeQuery = "and (date(a.special_follow_up_date/1000, 'unixepoch') = '"+currentDate+"')";
                }else {
                    typeQuery = "and (a.special_follow_up_date > 0)";
                }
                orderByQuery = "a.special_follow_up_date asc";

                minNextFollowUp = "a.next_follow_up_date,";
                minSpecialFollowUp = "min(a.special_follow_up_date) as special_follow_up_date,";
                minTelephonicFollowUp = "a.telephony_follow_up_date,";

            }else if(type == FollowUpType.telephonic){
                if(isFromReceiver){
                    typeQuery = "and (date(a.telephony_follow_up_date/1000, 'unixepoch') = '"+currentDate+"')";
                }else {
                    typeQuery = "and (a.telephony_follow_up_date > 0)";
                }
                orderByQuery = "a.telephony_follow_up_date asc";

                minNextFollowUp = "a.next_follow_up_date,";
                minSpecialFollowUp = "a.special_follow_up_date,";
                minTelephonicFollowUp = "min(a.telephony_follow_up_date) as telephony_follow_up_date,";

            }

            String query = "SELECT a.base_entity_id," +
                    "a.follow_up_date," +
                    minNextFollowUp +
                    "a.visit_date," +
                    minTelephonicFollowUp +
                    minSpecialFollowUp +
                    "a.no_of_anc," +
                    "a.is_called_telephonic," +
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
                    "and a.base_entity_id not in (select base_entity_id from ec_pregnancy_outcome where base_entity_id = a.base_entity_id) and e.base_entity_id not null " +typeQuery+
                    " group by a.base_entity_id order by "+orderByQuery;

            Log.d("QQQQQQQ",query);

            cursor = getReadableDatabase().rawQuery(query, null);
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

    public static long getMinFollowupDate(String baseEntityId){
        String query = "select min(next_follow_up_date) as next_follow_up_date,min(special_follow_up_date) as special_follow_up_date from anc_follow_up where base_entity_id = '"+baseEntityId+"' " +
                "and special_follow_up_date > 0 and next_follow_up_date > 0";

        long nextFollowupDate = 0;
        long specialFollowupDate = 0;
        try (android.database.Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{})) {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                nextFollowupDate = cursor.getLong(0);
                specialFollowupDate = cursor.getLong(1);
            }

            return Math.min(nextFollowupDate, specialFollowupDate);
        } catch (Exception e) {
            Timber.e(e);

        }
        return 0;
    }

    protected AncFollowUpModel readCursor(Cursor cursor) {
        String baseEntityId = cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID));
        long followUpDate = cursor.getLong(cursor.getColumnIndex(FOLLOW_UP_DATE));
        long nextFollowUpDate = cursor.getLong(cursor.getColumnIndex(NEXT_FOLLOW_UP_DATE));
        long visitDate = cursor.getLong(cursor.getColumnIndex(VISIT_DATE));
        long telephonyFollowUpDate = cursor.getLong(cursor.getColumnIndex(TELEPHONY_FOLLOW_UP_DATE));
        long specialFollowUpDate = cursor.getLong(cursor.getColumnIndex(SPECIAL_FOLLOW_UP_DATE));
        int noOfAnc = cursor.getInt(cursor.getColumnIndex(NO_OF_ANC));
        int isCalledTelephonic = cursor.getInt(cursor.getColumnIndex(IS_CALLED_TELEPHONIC));

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
        ancFollowUpModel.isCalledTelephonic = isCalledTelephonic;
        ancFollowUpModel.riskType = riskType;

        ancFollowUpModel.memberName = memberName;
        ancFollowUpModel.memberPhoneNum = memberPhoneNo;


        return ancFollowUpModel;
    }

}
