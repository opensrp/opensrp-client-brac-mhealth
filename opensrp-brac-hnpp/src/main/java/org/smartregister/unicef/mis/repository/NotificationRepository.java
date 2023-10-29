package org.smartregister.unicef.mis.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.unicef.mis.model.Notification;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

public class NotificationRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String NOTIFICATION_TABLE = "notification_table";
    protected static final String NOTIFICATION_ID = "notification_id";
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final String NOTIFICATION_SENDDATE = "notification_senddate";
    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_DETAILS = "notification_details";
    public static final String NOTIFICATION_HOUR = "notification_hour";
    public static final String NOTIFICATION_MINUTE = "notification_minute";
    public static final String NOTIFICATION_TIMESTAMP = "notification_timestamp";



    private static final String CREATE_NOTIFICATION_TABLE =
            "CREATE TABLE " + NOTIFICATION_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    NOTIFICATION_ID + " INTEGER , " +NOTIFICATION_TYPE + " VARCHAR , " +
                    NOTIFICATION_SENDDATE + " VARCHAR, " + NOTIFICATION_TITLE+ " VARCHAR, "+NOTIFICATION_DETAILS+" VARCHAR, "+NOTIFICATION_HOUR+" INTEGER, "+NOTIFICATION_MINUTE+" INTEGER ,"+NOTIFICATION_TIMESTAMP+" VARCHAR ) ";




    public NotificationRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return NOTIFICATION_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_NOTIFICATION_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public void addOrUpdate(Notification notification) {
        if(!isExistData(notification.getId())){
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_ID, notification.getId());
            contentValues.put(NOTIFICATION_TYPE, notification.getNotificationType());
            contentValues.put(NOTIFICATION_SENDDATE, notification.getSendDate());
            contentValues.put(NOTIFICATION_TITLE, notification.getTitle());
            contentValues.put(NOTIFICATION_DETAILS, notification.getDetails());
            contentValues.put(NOTIFICATION_HOUR, notification.getHour());
            contentValues.put(NOTIFICATION_MINUTE, notification.getMinute());
            contentValues.put(NOTIFICATION_TIMESTAMP, notification.getTimestamp());
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("TARGET_FETCH","inserterd:"+inserted);
        }else{
            Log.v("TARGET_FETCH","exists!!!!!!!!!");
        }


    }
    public boolean isExistData(int notificationId){
        String sql = "select count(*) from "+getLocationTableName()+" where "+NOTIFICATION_ID+" = "+notificationId;
        Cursor cursor = null;
        boolean isExist = false;

        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    if(cursor.getInt(0) >0){
                        isExist = true;
                    }
                    cursor.moveToNext();
                }

            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return isExist;
    }
    public Notification getNotificationList() {
        Cursor cursor = null;
        Notification notification = new Notification();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+"", null);
            while (cursor.moveToNext()) {
                notification = readCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return notification;
    }

    public ArrayList<Notification> getNotificationDetailsById(String notificationId) {
        Cursor cursor = null;
        ArrayList<Notification> notificationArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+NOTIFICATION_ID+" = '"+notificationId+"", null);
            while (cursor.moveToNext()) {
                notificationArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return notificationArrayList;
    }
    public ArrayList<Notification> getAllNotification() {
        Cursor cursor = null;
        ArrayList<Notification> notificationArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" order by "+NOTIFICATION_TIMESTAMP+" desc", null);
            while (cursor.moveToNext()) {
                notificationArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return notificationArrayList;
    }
    protected Notification readCursor(Cursor cursor) {
        int notificationId = cursor.getInt(cursor.getColumnIndex(NOTIFICATION_ID));
        String notificationType = cursor.getString(cursor.getColumnIndex(NOTIFICATION_TYPE));
        String notificationSendDate = cursor.getString(cursor.getColumnIndex(NOTIFICATION_SENDDATE));
        String notificationTitle = cursor.getString(cursor.getColumnIndex(NOTIFICATION_TITLE));
        String notificationDetails = cursor.getString(cursor.getColumnIndex(NOTIFICATION_DETAILS));
        int notificationHour = cursor.getInt(cursor.getColumnIndex(NOTIFICATION_HOUR));
        int notificationMinute = cursor.getInt(cursor.getColumnIndex(NOTIFICATION_MINUTE));
        String notificationTimestamp = cursor.getString(cursor.getColumnIndex(NOTIFICATION_TIMESTAMP));

        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setNotificationType(notificationType+"");
        notification.setSendDate(notificationSendDate+"");
        notification.setTitle(notificationTitle+"");
        notification.setDetails(notificationDetails+"");
        notification.setHour(notificationHour);
        notification.setMinute(notificationMinute);
        notification.setTimestamp(Long.parseLong(notificationTimestamp));

        return notification;
    }

}
