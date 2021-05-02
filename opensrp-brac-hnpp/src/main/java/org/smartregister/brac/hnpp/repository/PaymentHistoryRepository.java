package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.util.ArrayList;

public class PaymentHistoryRepository extends BaseRepository {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    protected static final String ID = "_id";

    public static final String PAYMENT_TABLE = "payment_table";
    protected static final String PAYMENT_ID = "paymentId";
    public static final String PAYMENT_TYPE = "serviceType";
    public static final String PAYMENT_PRICE = "price";
    public static final String PAYMENT_DATE = "paymentDate";
    public static final String PAYMENT_STATUS = "status";
    public static final String PAYMENT_TIMESTAMP = "paymentTimestamp";

    private static final String CREATE_PAYMENT_TABLE =
            "CREATE TABLE " + PAYMENT_TABLE + " (" +
                    ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    PAYMENT_ID + " VARCHAR , "  + PAYMENT_TYPE + " VARCHAR, " +
                    PAYMENT_PRICE+ " VARCHAR, "+ PAYMENT_DATE+" VARCHAR, "+
                    PAYMENT_STATUS+" VARCHAR, "+ PAYMENT_TIMESTAMP+" INTEGER ) ";

    public PaymentHistoryRepository(Repository repository) {
        super(repository);
    }

    protected String getLocationTableName() {
        return PAYMENT_TABLE;
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_PAYMENT_TABLE);
    }
    public void dropTable(){
        getWritableDatabase().execSQL("delete from "+getLocationTableName());
    }

    public void addOrUpdate(PaymentHistory paymentHistory) {
        if(!isExistData(paymentHistory.getPaymentId())){
            ContentValues contentValues = new ContentValues();
            contentValues.put(PAYMENT_ID, paymentHistory.getPaymentId());
            contentValues.put(PAYMENT_TYPE, paymentHistory.getServiceType());
            contentValues.put(PAYMENT_PRICE, paymentHistory.getPrice());
            contentValues.put(PAYMENT_DATE, paymentHistory.getPaymentDate());
            contentValues.put(PAYMENT_STATUS, paymentHistory.getStatus());
            contentValues.put(PAYMENT_TIMESTAMP, paymentHistory.getPaymentTimestamp());
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("TARGET_FETCH","inserterd:"+inserted);
        }else{
            Log.v("TARGET_FETCH","exists!!!!!!!!!");
        }


    }
    public boolean isExistData(String paymentId){
        String sql = "select count(*) from "+getLocationTableName()+" where "+PAYMENT_ID+" = "+paymentId;
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
    public PaymentHistory getPaymentList() {
        Cursor cursor = null;
        PaymentHistory payment = new PaymentHistory();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+"", null);
            while (cursor.moveToNext()) {
                payment = readCursor(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return payment;
    }

    public ArrayList<PaymentHistory> getPaymentDetailsById(String paymentId) {
        Cursor cursor = null;
        ArrayList<PaymentHistory> paymentArrayList = new ArrayList<>();
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * FROM " + getLocationTableName()+" where "+PAYMENT_ID+" = '"+paymentId+"", null);
            while (cursor.moveToNext()) {
                paymentArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return paymentArrayList;
    }
    public ArrayList<PaymentHistory> getAllPayment() {
        Cursor cursor = null;
        ArrayList<PaymentHistory> paymentArrayList = new ArrayList<>();
        try {

            String rawQuery = "select serviceType, paymentDate, status, count(*) as quantity, sum(price) as price " +
                              "from payment_table group by paymentId order by paymentTimestamp desc";

            cursor = getReadableDatabase().rawQuery(rawQuery, null);

            while (cursor.moveToNext()) {
                paymentArrayList.add(readCursor(cursor));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return paymentArrayList;
    }
    protected PaymentHistory readCursor(Cursor cursor) {
       // String paymentId = cursor.getString(cursor.getColumnIndex(PAYMENT_ID));
        String paymentType = cursor.getString(cursor.getColumnIndex(PAYMENT_TYPE));
        String paymentPrice = cursor.getString(cursor.getColumnIndex(PAYMENT_PRICE));
        String paymentDate = cursor.getString(cursor.getColumnIndex(PAYMENT_DATE));
        String paymentStaus = cursor.getString(cursor.getColumnIndex(PAYMENT_STATUS));
      //  long paymentTimestamp = cursor.getLong(cursor.getColumnIndex(PAYMENT_TIMESTAMP));

        PaymentHistory paymentHistory = new PaymentHistory();

       // paymentHistory.setPaymentId(paymentId);
        paymentHistory.setServiceType(paymentType+"");
        paymentHistory.setPrice(paymentPrice+"");
        paymentHistory.setPaymentDate(paymentDate+"");
        paymentHistory.setStatus(paymentStaus+"");
     //   paymentHistory.setPaymentTimestamp(paymentTimestamp);

        return paymentHistory;
    }
}
