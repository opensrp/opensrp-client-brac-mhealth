package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.HHVisitDurationModel;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

/**
 * Created by tanvir on 06/13/23.
 */
public class HHVisitDurationRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "id";
    protected static final String SERVICE_ID = "service_id";
    protected static final String SERVICE_NAME = "service_name";
    protected static final String VALUE = "value"; //duration

    protected static final String HH_VISIT_DURATION_TABLE = "hh_visit_duration";

    protected static final String[] COLUMNS = new String[]{ID, SERVICE_ID, SERVICE_NAME,VALUE};

    private static final String CREATE_HH_VISIT_DURATION_TABLE =
            "CREATE TABLE " + HH_VISIT_DURATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    SERVICE_ID + " INTEGER , " +SERVICE_NAME + " VARCHAR , "+
                    VALUE + " INTEGER ) ";



    public HHVisitDurationRepository(Repository repository) {
        super(repository);
    }

    protected String getHhVisitDurationTableName() {
        return HH_VISIT_DURATION_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_HH_VISIT_DURATION_TABLE);
    }
    public void dropTable(){
       try{
           getWritableDatabase().execSQL("delete from "+getHhVisitDurationTableName());
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public void addOrUpdate(HHVisitDurationModel model) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SERVICE_ID, model.serviceId);
        contentValues.put(SERVICE_NAME, model.serviceName);
        contentValues.put(VALUE, model.value);
        long inserted = getWritableDatabase().replace(getHhVisitDurationTableName(), null, contentValues);


    }

    public HHVisitDurationModel getHhVisitDurationByType(String type) {
        HHVisitDurationModel hhVisitDurationModel = null;
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getHhVisitDurationTableName() + " where " + SERVICE_NAME + " = '" + type+"'", null)) {
            /*while (cursor.moveToNext()) {
                hhVisitDurationModel = readCursor(cursor);
            }*/
            if(cursor.getCount()>0){
                cursor.moveToFirst();
                hhVisitDurationModel = readCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        }
        return hhVisitDurationModel;
    }


    protected HHVisitDurationModel readCursor(Cursor cursor) {
        String serviceName = cursor.getString(cursor.getColumnIndex(SERVICE_NAME));
        int serviceId = cursor.getInt(cursor.getColumnIndex(SERVICE_ID));
        int value = cursor.getInt(cursor.getColumnIndex(VALUE));


        HHVisitDurationModel hhVisitDurationModel = new HHVisitDurationModel();
        hhVisitDurationModel.serviceId = serviceId;
        hhVisitDurationModel.serviceName = serviceName.trim();
        hhVisitDurationModel.value = value;
        return hhVisitDurationModel;
    }

}
