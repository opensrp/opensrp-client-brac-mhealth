package org.smartregister.brac.hnpp.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import org.joda.time.DateTime;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.Repository;
import org.smartregister.util.DateTimeTypeConverter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

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
                    PAYMENT_STATUS+" VARCHAR, "+ PAYMENT_TIMESTAMP+" LONG ) ";

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
        //if(!isExistData(paymentHistory.getPaymentId())){
            ContentValues contentValues = new ContentValues();
            contentValues.put(PAYMENT_ID, paymentHistory.getPaymentId());
            contentValues.put(PAYMENT_TYPE, paymentHistory.getServiceType());
            contentValues.put(PAYMENT_PRICE, paymentHistory.getPrice());
            contentValues.put(PAYMENT_DATE, paymentHistory.getPaymentDate());
            contentValues.put(PAYMENT_STATUS, paymentHistory.getStatus());
            contentValues.put(PAYMENT_TIMESTAMP, paymentHistory.getPaymentTimestamp() * 1000);
            long inserted = getWritableDatabase().insert(getLocationTableName(), null, contentValues);
            Log.v("TARGET_FETCH","inserterd:"+inserted+":contentValues:"+contentValues);
//        }else{
//            Log.v("TARGET_FETCH","exists!!!!!!!!!");
//        }


    }
    public int getTotalPayment(String fromDate, String toDate){

        String sql;
        if(TextUtils.isEmpty(fromDate) && TextUtils.isEmpty(toDate)){
            sql = "select sum(price) from "+getLocationTableName()+" where "+PAYMENT_STATUS+" = 'COMPLETED'";

        }else{
            sql = "select sum(price) from "+getLocationTableName()+" where "+PAYMENT_STATUS+" = 'COMPLETED' and "+PAYMENT_DATE+" between '"+fromDate+"' and '"+toDate+"'";

        }
        Cursor cursor = null;
        int total = 0;

        try {
            cursor = getReadableDatabase().rawQuery(sql, null);
            if(cursor!=null&&cursor.getCount()>0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    total = cursor.getInt(0);
                    cursor.moveToNext();
                }

            }
        } catch (Exception e) {
            Log.e(LocationRepository.class.getCanonicalName(), e.getMessage(), e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return total;
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

    public ArrayList<PaymentHistory> getFilterPayment(String fromDate, String toDate) {
        Cursor cursor = null;
        ArrayList<PaymentHistory> paymentArrayList = new ArrayList<>();
        try {
            String rawQuery= "";
            if(!TextUtils.isEmpty(fromDate) && !TextUtils.isEmpty(toDate)){
                rawQuery = "select serviceType, paymentDate, status, count(*) as quantity, paymentTimestamp,sum(price) as price " +
                        "from payment_table where paymentDate between '"+fromDate+"' and '"+toDate+"' group by paymentId order by paymentTimestamp desc ";
                Log.v("HISTORY","rawQuery:"+rawQuery);
            }

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
    public ArrayList<PaymentHistory> getAllPayment() {
        Cursor cursor = null;
        ArrayList<PaymentHistory> paymentArrayList = new ArrayList<>();
        try {

            String rawQuery = "select serviceType, paymentDate, status, count(*) as quantity, paymentTimestamp,sum(price) as price " +
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
        long paymentTimestamp = cursor.getLong(cursor.getColumnIndex(PAYMENT_TIMESTAMP));
        int quantity = cursor.getInt(cursor.getColumnIndex("quantity"));

        PaymentHistory paymentHistory = new PaymentHistory();

       // paymentHistory.setPaymentId(paymentId);
        paymentHistory.setServiceType(paymentType+"");
        paymentHistory.setPrice(paymentPrice+"");
        paymentHistory.setPaymentDate(HnppConstants.getDateWithHHMMFormateFromLong(paymentTimestamp));//HnppConstants.DDMMYYHM.format(new Date(paymentTimestamp)));

        paymentHistory.setStatus(paymentStaus+"");
        paymentHistory.setQuantity(quantity);
     //   paymentHistory.setPaymentTimestamp(paymentTimestamp);

        return paymentHistory;
    }
}
