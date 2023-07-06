

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
                while (!cursor.isAfterLast()) {
                    String[] member = new String[2];

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
        String query = "select * from alerts where startDate is not null and status !='expired' order by status desc,startDate asc";
        Cursor cursor = null;
        ArrayList<Alert> alerts = new ArrayList<>();
        ArrayList<String> baseEntityIdList = new ArrayList<>();
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){

                cursor.moveToFirst();
                int baseEntityIdColumn = cursor.getColumnIndex("caseID");
                int vaccinenameColumn = cursor.getColumnIndex("scheduleName");
                int dueDateColumn = cursor.getColumnIndex("startDate");
                while (!cursor.isAfterLast()) {
                    Alert alert = new Alert(cursor.getString(baseEntityIdColumn), cursor.getString(vaccinenameColumn), "", null, cursor.getString(dueDateColumn), "");

                    /*
                     checking case id exist or not
                     if exist then skip otherwise add case id
                     */
                    Alert processedAlert = getProcessedAlert(alert);
                    if(!baseEntityIdList.contains(processedAlert.caseId())){
                        alerts.add(processedAlert);
                        baseEntityIdList.add(processedAlert.caseId());
                    }

                    cursor.moveToNext();
                }


            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(cursor!=null) cursor.close();
        }
        //now need to update child table with vaccine name and due date
        for (Alert alert: alerts) {

            try{
                SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
                String sql = "update ec_child set due_vaccine_date='"+alert.startDate()+"',due_vaccine_name='"+alert.scheduleName()+"' where " +
                        "base_entity_id = '"+alert.caseId()+"' ;";
                database.execSQL(sql);
            }catch(Exception e){
                e.printStackTrace();

            }
        }
        return true;
    }

    /**
     * checking vaccine exist or not in vaccine table
     * if exist return
     * or else return existing alert
     * @param alert
     * @return
     */
    static Alert getProcessedAlert(Alert alert){
        String baseEntityId = alert.caseId();
        String query = "select * from vaccines where base_entity_id = '"+baseEntityId+"' and is_invalid != '0' and updated_at is not null order by updated_at asc limit 1";
        Cursor cursor = null;
        Alert processedAlert = alert;

        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();

        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0 ){

                cursor.moveToFirst();
                int baseEntityIdColumn = cursor.getColumnIndex("base_entity_id");
                int vaccineNameColumn = cursor.getColumnIndex("name");
                int dueDateColumn = cursor.getColumnIndex("updated_at");


                calendar.setTimeInMillis(Long.parseLong(cursor.getString(dueDateColumn)));
                String date = sp.format(calendar.getTime());

                Date alertDate = sp.parse(alert.startDate());
                Date vaccineDate = sp.parse(date);

                assert alertDate != null;
                if(alertDate.before(vaccineDate)){
                    processedAlert = new Alert(cursor.getString(baseEntityIdColumn), cursor.getString(vaccineNameColumn), "", null, date, "");
                }



            }
        }catch (Exception e){
            e.printStackTrace();

        }
        finally {
            if(cursor!=null) cursor.close();
        }

        return  processedAlert;
    }

}
