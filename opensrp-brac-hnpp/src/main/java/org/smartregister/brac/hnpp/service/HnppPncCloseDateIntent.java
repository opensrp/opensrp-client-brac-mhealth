package org.smartregister.brac.hnpp.service;

import android.content.Intent;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.brac.hnpp.sync.intent.HnppPncCloseDateIntentFlv;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.intent.CoreChwPncCloseDateIntent;
import org.smartregister.chw.pnc.util.PncUtil;

import timber.log.Timber;

public class HnppPncCloseDateIntent extends CoreChwPncCloseDateIntent {


    public HnppPncCloseDateIntent() {
        super(new HnppPncCloseDateIntentFlv());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SQLiteDatabase database = CoreChwApplication.getInstance().getRepository().getWritableDatabase();
          /*  String sql1 = "UPDATE ec_anc_register SET is_closed = 1 WHERE ec_anc_register.base_entity_id IN " +
                    "(select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = 1) ";*/

            //database.execSQL(sql1);
            String sql = "UPDATE ec_anc_register SET is_closed = 1 WHERE ec_anc_register.base_entity_id IN " +
                    "(select ec_pregnancy_outcome.base_entity_id from ec_pregnancy_outcome where ec_pregnancy_outcome.is_closed = 0) ";

            database.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onHandleIntent(intent);
    }
}
