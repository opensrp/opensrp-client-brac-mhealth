

package org.smartregister.unicef.dghs.service;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.fragment.ChildImmunizationFragment;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;

import java.util.ArrayList;

public class VaccineDueUpdateIntentService extends IntentService {
    public VaccineDueUpdateIntentService() {
        super("VaccineDueUpdateIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
       //need to get all due vaccine name,date from alert table group by baseentityid
        String query = "select base_entity_id,dob from ec_child";
        ArrayList<String[]> childs = new ArrayList<>();
        Cursor cursor = null;
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){
                cursor.moveToFirst();
                String[] member = new String[2];
                while (!cursor.isAfterLast()) {
                    member[0] = cursor.getString(0);
                    member[1] = cursor.getString(1);
                    childs.add(member);
                    cursor.moveToNext();
                }


            }
        }catch (Exception e){
            e.printStackTrace();

        }
        finally {
            if(cursor!=null) cursor.close();

        }
        Log.v("CHILD_FILTER","child list ids>>>>"+childs.size());

        for (String[] member:childs) {
            if (!TextUtils.isEmpty(member[1])) {
                DateTime dateTime = new DateTime(member[1]);
                VaccineSchedule.updateOfflineAlerts(member[0], dateTime, "child");
                //ServiceSchedule.updateOfflineAlerts(member[0], dateTime);
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updatedVaccineDueDate();
    }
    public static boolean updatedVaccineDueDate(){
        String query = "select * from alerts where startDate is not null and status !=expired group by caseID";
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
