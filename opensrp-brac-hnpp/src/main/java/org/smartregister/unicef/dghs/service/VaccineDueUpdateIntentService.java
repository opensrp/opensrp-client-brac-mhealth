

package org.smartregister.unicef.dghs.service;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;

import java.util.ArrayList;

public class VaccineDueUpdateIntentService extends IntentService {
    public VaccineDueUpdateIntentService() {
        super("VaccineDueUpdateIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
       //need to get all due vaccine name,date from alert table group by baseentityid

        updatedVaccineDueDate();
    }
    public static boolean updatedVaccineDueDate(){
        String query = "select * from alerts where startDate is not null group by caseID";
        Cursor cursor = null;
        ArrayList<Alert> alerts = new ArrayList<>();
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){

                cursor.moveToFirst();
                int baseEntityIdColumn = cursor.getColumnIndex("caseID");
                int vaccinenameColumn = cursor.getColumnIndex("scheduleName");
                int dueDateColumn = cursor.getColumnIndex("startDate");
                while (!cursor.isAfterLast()) {
                    Alert alert = new Alert(cursor.getString(baseEntityIdColumn), cursor.getString(vaccinenameColumn), "", null, cursor.getString(dueDateColumn), "");
                    alerts.add(alert);
                    cursor.moveToNext();
                }


            }
        }catch (Exception e){
            e.printStackTrace();

        }
        finally {
            if(cursor!=null) cursor.close();
        }
        Log.v("CHILD_FILTER","alert>>>>"+alerts.size());
        //now need to update child table with vaccine name and due date
        for (Alert alert: alerts) {

            try{
                SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
                String sql = "update ec_child set due_vaccine_date='"+alert.startDate()+"',due_vaccine_name='"+alert.scheduleName()+"' where " +
                        "base_entity_id = '"+alert.caseId()+"' ;";
                database.execSQL(sql);
                Log.v("CHILD_FILTER","executed>>>>>");
            }catch(Exception e){
                e.printStackTrace();

            }
        }
        return true;
    }

}
