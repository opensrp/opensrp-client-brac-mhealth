package org.smartregister.unicef.mis.service;

import android.content.Intent;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.sync.intent.HnppPncCloseDateIntentFlv;

import org.smartregister.chw.core.intent.CoreChwPncCloseDateIntent;

public class HnppPncCloseDateIntent extends CoreChwPncCloseDateIntent {


    public HnppPncCloseDateIntent() {
        super(new HnppPncCloseDateIntentFlv());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE ec_anc_register.base_entity_id IN " +
                    "(select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = 0) ";
            database.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
       // closeAncAfterEDD();
        super.onHandleIntent(intent);
    }
    private void closeAncAfterEDD(){
        try {
            SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE cast(julianday(datetime('now')) - julianday(datetime(substr(edd, 7,4)  || '-' || substr(edd, 4,2) || '-' || substr(edd, 1,2))) as integer)+42 >= 1";
            Log.v("ANC_CLOSE","closeAncAfterEDD>>"+sql);
            database.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}