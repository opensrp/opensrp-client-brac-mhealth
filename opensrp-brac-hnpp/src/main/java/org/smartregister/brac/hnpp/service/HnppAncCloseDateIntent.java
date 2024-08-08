//package org.smartregister.brac.hnpp.service;
//
//
//import android.app.IntentService;
//import android.content.Intent;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import net.sqlcipher.database.SQLiteDatabase;
//import org.smartregister.brac.hnpp.HnppApplication;
//
//public class HnppAncCloseDateIntent extends IntentService {
//
//    /**
//     * @param name
//     * @deprecated
//     */
//    public HnppAncCloseDateIntent(String name) {
//        super(name);
//    }
//
//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        try {
//            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
//            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE cast(julianday(datetime('now')) - julianday(datetime(substr(edd, 7,4)  || '-' || substr(edd, 4,2) || '-' || substr(edd, 1,2))) as integer)+60 >= 1";
//            Log.v("ANC_CLOSE","closeAncAfterEDD>>"+sql);
//            database.execSQL(sql);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//}
