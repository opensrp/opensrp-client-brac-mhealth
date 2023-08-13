

package org.smartregister.unicef.dghs.service;
import static org.smartregister.util.Utils.getValue;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.fragment.ChildImmunizationFragment;
import org.smartregister.unicef.dghs.utils.HnppDBConstants;
import org.smartregister.unicef.dghs.utils.VaccineScheduleUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VaccineDueUpdateIntentService extends IntentService {
    public VaccineDueUpdateIntentService() {
        super("VaccineDueUpdateIntentService");
    }
    private final String TEST_BASE="362243b3-9c00-45eb-8fc5-778ff08db909-pros";
    public enum VACCINE_WEEK {
        AT_BIRTH, SIX_WEEK, TEN_WEEK, FOURTEEN_WEEK,NINE_MONTH,FIFTEEN_MONTH
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
    public  boolean updatedVaccineDueDate(){
//        String query = "select * from alerts where startDate is not null and status !='expired' order by status desc,startDate asc";
        String query ="select alerts.*,ec_child.dob from alerts INNER JOIN  ec_child on ec_child.base_entity_id  = alerts.caseID  where startDate is not null and status !='expired' and scheduleName!='Vitamin A1' order by status desc,startDate asc";
        Cursor cursor = null;
        HashMap<String,Alert> vaccineMapByBase = new HashMap<>();
        HashMap<String,List<Alert>> allVaccineMapById = new HashMap<>();
        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() >0){

                cursor.moveToFirst();
                int baseEntityIdColumn = cursor.getColumnIndex("caseID");
                int vaccinenameColumn = cursor.getColumnIndex("scheduleName");
                int dueDateColumn = cursor.getColumnIndex("startDate");
                int dobColumn = cursor.getColumnIndex("dob");
                while (!cursor.isAfterLast()) {
                    String baseEntityId = cursor.getString(baseEntityIdColumn);
                    String vaccineName = cursor.getString(vaccinenameColumn);
                    String dob = cursor.getString(dobColumn);
                    String dueDate = cursor.getString(dueDateColumn);
                    String updatedDueDate = getUpDatedVaccineDueDate(vaccineName,dob,dueDate,baseEntityId);
                    if(baseEntityId.equalsIgnoreCase(TEST_BASE)){
                        Log.v("CHILD_FILTER","vaccineName>>"+vaccineName+":original due:"+dueDate+":updated:"+updatedDueDate+":baseEntityId:"+baseEntityId);

                    }
                    Alert alert = new Alert(baseEntityId,vaccineName, "", null, updatedDueDate, "");
                    Alert processedAlert = getProcessedAlert(alert);
                    if(baseEntityId.equalsIgnoreCase(TEST_BASE)){
                        Log.v("CHILD_FILTER","After>>>>original>>"+vaccineName+":original due:"+dueDate+":updated:"+updatedDueDate+":processedAlert:"+processedAlert.startDate()+":processedAlertName>>"+processedAlert.scheduleName());
                    }
                    /*
                     checking case id exist or not
                     if exist then skip otherwise add case id
                     */
                        Alert dd = vaccineMapByBase.get(baseEntityId);
                        List<Alert> alerts = allVaccineMapById.get(baseEntityId);
                        if(alerts == null){
                            if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                                Log.v("VACCINE_DUE_FILTER", "new" + processedAlert.scheduleName() + ":" + processedAlert.startDate() + ":" + baseEntityId);
                            }
                            alerts = new ArrayList<>();
                            alerts.add(processedAlert);
                            allVaccineMapById.put(baseEntityId,alerts);
                        }else{
                            if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                                Log.v("VACCINE_DUE_FILTER", "addedd" + processedAlert.scheduleName() + ":" + processedAlert.startDate() + ":" + baseEntityId);
                            }
                            alerts.add(processedAlert);
                            allVaccineMapById.put(baseEntityId,alerts);
                        }
                        if(dd==null){
                            if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                                Log.v("CHILD_FILTER", "1added>>" + processedAlert.scheduleName() + ":" + processedAlert.startDate() + ":" + baseEntityId);
                            }
                            vaccineMapByBase.put(baseEntityId,processedAlert);
                        }else {
                            LocalDate localDate = new LocalDate(dd.startDate());
                            LocalDate vaccineLocalDate = new LocalDate(processedAlert.startDate());
                            if(baseEntityId.equalsIgnoreCase(TEST_BASE)){
                                Log.v("CHILD_FILTER","1.5added>>dd:"+dd.startDate()+"ddname:"+dd.scheduleName()+":alert:"+processedAlert.startDate()+":"+processedAlert.scheduleName()+">>>"+getVaccineOrder(dd.scheduleName())+">>"+getVaccineOrder(processedAlert.scheduleName()));
                            }
                                if(vaccineLocalDate.isBefore(localDate)){
                                    if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                                        Log.v("CHILD_FILTER", "2added>>" + processedAlert.scheduleName() + ":" + processedAlert.startDate() + ":" + baseEntityId);
                                    }
                                    vaccineMapByBase.put(baseEntityId,processedAlert);
                            }else if(vaccineLocalDate.isEqual(localDate) && (getVaccineOrder(dd.scheduleName())>getVaccineOrder(processedAlert.scheduleName()))){
                                    if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                                        Log.v("CHILD_FILTER", "3added>>" + processedAlert.scheduleName() + ":" + processedAlert.startDate() + ":" + baseEntityId);
                                    }
                                vaccineMapByBase.put(baseEntityId,processedAlert);
                            }
                        }
                    if(baseEntityId.equalsIgnoreCase(TEST_BASE)) {
                        Log.v("VACCINE_DUE_FILTER", "allVaccineMapById" + allVaccineMapById.get(TEST_BASE).size());
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
        for (Map.Entry<String, Alert> map : vaccineMapByBase.entrySet()) {
            Alert alert = map.getValue();
            if(alert.caseId().equalsIgnoreCase(TEST_BASE)) {
                Log.v("CHILD_FILTER", "alerts>>" + alert.scheduleName() + ":" + alert.startDate() + ":" + alert.caseId());
            }
            try{
                SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
                String sql = "update ec_child set due_vaccine_date='"+alert.startDate()+"',due_vaccine_name='"+alert.scheduleName()+"' where " +
                        "base_entity_id = '"+alert.caseId()+"' ;";
                database.execSQL(sql);
            }catch(Exception e){
                e.printStackTrace();

            }

        }
        for (Map.Entry<String, List<Alert>> map : allVaccineMapById.entrySet()) {
            List<Alert> alerts = map.getValue();
            StringBuilder atBirthBuilder = new StringBuilder();
            StringBuilder sixWeekBuilder = new StringBuilder();
            StringBuilder tenWeekBuilder = new StringBuilder();
            StringBuilder fourteenWeekBuilder = new StringBuilder();
            StringBuilder nineMonthBuilder = new StringBuilder();
            StringBuilder fifteenMonthBuilder = new StringBuilder();
            StringBuilder dueVaccineWeekBuilder = new StringBuilder();
            for(Alert alert : alerts){
                if(dueVaccineWeekBuilder.length()>1){
                    dueVaccineWeekBuilder.append(" ");
                }
                dueVaccineWeekBuilder.append(alert.startDate());
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.AT_BIRTH.name())) {
//                    if(atBirthBuilder.length()>1){
//                        atBirthBuilder.append(" ");
//                    }
//                    atBirthBuilder.append(alert.startDate());
//
//                }
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.SIX_WEEK.name())) {
//                    if(sixWeekBuilder.length()>1){
//                        sixWeekBuilder.append(" ");
//                    }
//                    sixWeekBuilder.append(alert.startDate());
//
//                }
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.TEN_WEEK.name())){
//                    if(tenWeekBuilder.length()>1){
//                        tenWeekBuilder.append(" ");
//                    }
//                    tenWeekBuilder.append(alert.startDate());
//
//                }
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.FOURTEEN_WEEK.name())){
//                    if(fourteenWeekBuilder.length()>1){
//                        fourteenWeekBuilder.append(" ");
//                    }
//                    fourteenWeekBuilder.append(alert.startDate());
//
//                }
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.NINE_MONTH.name())){
//                    if(nineMonthBuilder.length()>1){
//                        nineMonthBuilder.append(" ");
//                    }
//                    nineMonthBuilder.append(alert.startDate());
//
//                }
//                if(getVaccineWeek(alert.scheduleName()).equalsIgnoreCase(VACCINE_WEEK.FIFTEEN_MONTH.name())){
//                    if(fifteenMonthBuilder.length()>1){
//                        fifteenMonthBuilder.append(" ");
//                    }
//                    fifteenMonthBuilder.append(alert.startDate());
//
//                }
                if(alert.caseId().equalsIgnoreCase(TEST_BASE)) {

                    Log.v("VACCINE_DUE_FILTER", "getVaccineWeek>>" + dueVaccineWeekBuilder.toString().replace(" ",",")+"");
                }

            }

            try{
                SQLiteDatabase database = HnppApplication.getInstance().getRepository().getWritableDatabase();
                String sql = "update ec_child set due_vaccine_week='"+dueVaccineWeekBuilder.toString().replace(" ",",")+"' where " +
                        "base_entity_id = '"+alerts.get(0).caseId()+"' ;";
                database.execSQL(sql);
            }catch(Exception e){
                e.printStackTrace();

            }

        }
        broadcastStatus("updated");
        return true;
    }
    private void broadcastStatus(String message){
        try{
            Intent broadcastIntent = new Intent("VACCINE_UPDATE");
            broadcastIntent.putExtra("EXTRA_VACCINE_UPDATE", message);
            sendBroadcast(broadcastIntent);
        }catch (Exception e){

        }

    }
    private static String getVaccineWeek(String vaccineName){
        switch (vaccineName){
            case "BCG":
            case "bcg":
            case "OPV 0":
            case "opv_0":
                return VACCINE_WEEK.AT_BIRTH.name();

            case "OPV 1":
            case "opv_1":
            case "PENTA 1":
            case "penta_1":
            case "PCV 1":
            case "pcv_1":
            case "fIPV 1":
            case "fipv_1":
                return VACCINE_WEEK.SIX_WEEK.name();
            case "OPV 2":
            case "opv_2":
            case "PENTA 2":
            case "penta_2":
            case "PCV 2":
            case "pcv_2":
                return VACCINE_WEEK.TEN_WEEK.name();
            case "OPV 3":
            case "opv_3":
            case "PENTA 3":
            case "penta_3":
            case "PCV 3":
            case "pcv_3":
            case "fIPV 2":
            case "fipv_2":
                return VACCINE_WEEK.FOURTEEN_WEEK.name();
            case "MR 1":
            case "mr_1":
                return VACCINE_WEEK.NINE_MONTH.name();
            case "MR 2":
            case "mr_2":
                return VACCINE_WEEK.FIFTEEN_MONTH.name();
        }
        return "";
    }
    private static int getVaccineOrder(String vaccineName){
        switch (vaccineName){
            case "BCG":
            case "bcg":
                return 0;
            case "OPV 0":
            case "opv_0":
                return 1;
            case "OPV 1":
            case "opv_1":
                return 2;
            case "PENTA 1":
            case "penta_1":
                return 3;
            case "PCV 1":
            case "pcv_1":
                return 4;
            case "fIPV 1":
            case "fipv_1":
                return 5;
            case "OPV 2":
            case "opv_2":
                return 6;
            case "PENTA 2":
            case "penta_2":
                return 7;
            case "PCV 2":
            case "pcv_2":
                return 8;
            case "OPV 3":
            case "opv_3":
                return 9;
            case "PENTA 3":
            case "penta_3":
                return 10;
            case "PCV 3":
            case "pcv_3":
                return 11;
            case "fIPV 2":
            case "fipv_2":
                return 12;
        }
        return -1;
    }

    private static String getUpDatedVaccineDueDate(String vaccineName, String dobString, String dueDate, String baseEntityId) {
        dobString = dobString.substring(0,dobString.indexOf("T"));
        switch (vaccineName){
            case "BCG":
            case "OPV 0":
                return dueDate;
            case "OPV 1":
            case "PENTA 1":
            case "PCV 1":
            case "fIPV 1":
                DateTime donDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                LocalDate sixWeekV = new LocalDate(donDate).plusDays(42);
                return DateTimeFormat.forPattern("yyyy-MM-dd").print(sixWeekV);
            case "OPV 2":
            case "PENTA 2":
            case "PCV 2":
            case "OPV 3":
            case "PENTA 3":
            case "PCV 3":
                String vName = getApplicableVaccineName(vaccineName);
                Vaccine vaccine = ImmunizationLibrary.getInstance().vaccineRepository().getVaccineByName(baseEntityId,vName);
                if(vaccine!=null && !vaccine.isInvalid()){
                    Log.v("CHILD_FILTER","updatedVaccineDueDate>>"+vaccineName+":"+vaccine.getDate());
                    DateTime opv1GivenDate = new DateTime(vaccine.getDate());
                    LocalDate tenWeekV = new LocalDate(opv1GivenDate).plusDays(28);
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                }else{
                    DateTime dobDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                    if(vaccineName.equalsIgnoreCase("OPV 2")||
                            vaccineName.equalsIgnoreCase("PENTA 2") ||
                            vaccineName.equalsIgnoreCase("PCV 2")){
                        LocalDate tWeekV = new LocalDate(dobDate).plusDays(70);
                        return DateTimeFormat.forPattern("yyyy-MM-dd").print(tWeekV);
                    }else{
                        LocalDate tWeekV = new LocalDate(dobDate).plusDays(98);
                        return DateTimeFormat.forPattern("yyyy-MM-dd").print(tWeekV);
                    }

                }
            case "fIPV 2":

                Vaccine vaccine1 = ImmunizationLibrary.getInstance().vaccineRepository().getVaccineByName(baseEntityId,"fipv_1");
                if(vaccine1!=null && !vaccine1.isInvalid()){
                    DateTime opv1GivenDate = new DateTime(vaccine1.getDate());
                    LocalDate tenWeekV = new LocalDate(opv1GivenDate).plusDays(56);
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print(tenWeekV);
                }else{
                    DateTime dobDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                    LocalDate tWeekV = new LocalDate(dobDate).plusDays(98);
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print(tWeekV);
                }
            case "MR 1":
                DateTime dobDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                LocalDate tWeekV = new LocalDate(dobDate).plusDays(274);
                return DateTimeFormat.forPattern("yyyy-MM-dd").print(tWeekV);
            case "MR 2":
                Vaccine vc = ImmunizationLibrary.getInstance().vaccineRepository().getVaccineByName(baseEntityId,"mr_1");
                if(vc!=null){
                    //mr1 if>dob+13m mr2 = mr+1m else dob+15m
                    DateTime mr1GivenDate = new DateTime(vc.getDate());
                    LocalDate mr1 = new LocalDate(mr1GivenDate);
                    DateTime dobdt = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                    LocalDate dobld = new LocalDate(dobdt).plusDays(390);
                    if(mr1.isAfter(dobld)){
                        mr1 = mr1.plusDays(30);
                    }else {
                        mr1 = dobld.plusDays(65);
                    }
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print(mr1);

                }else{
                    DateTime dobdt = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(dobString);
                    LocalDate dobld = new LocalDate(dobdt).plusDays(455);
                    return DateTimeFormat.forPattern("yyyy-MM-dd").print(dobld);
                }
            default:
                return dueDate;


        }
    }
    private static String getApplicableVaccineName(String vaccineName){
        switch (vaccineName){
            case "OPV 2":
                return "opv_1";
            case "PENTA 2":
                return "penta_1";
            case "PCV 2":
                return "pcv_1";
            case "OPV 3":
                return "opv_2";
            case "PENTA 3":
                return "penta_2";
            case "PCV 3":
                return "pcv_2";
            case "Penta 2":
                return "penta_1";
            case "Penta 3":
                return "penta_2";
        }
        return "";
    }

    /**
     * checking vaccine exist or not in vaccine table
     * if exist return
     * or else return existing alert
     * @param alert
     * @return
     */
    static Alert getProcessedAlert(Alert alert){
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = Calendar.getInstance();

        String baseEntityId = alert.caseId();
        long alertDateLong = 0;
        try {
            if(alert.startDate() != null){
                alertDateLong = sp.parse(alert.startDate()).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String query = "select * from vaccines where base_entity_id = '"+baseEntityId+"' and is_invalid != '0' and due_date < "+alertDateLong+" order by due_date asc limit 1";
        Cursor cursor = null;
        Alert processedAlert = alert;

        try{
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0 ){

                cursor.moveToFirst();
                int baseEntityIdColumn = cursor.getColumnIndex("base_entity_id");
                int vaccineNameColumn = cursor.getColumnIndex("name");
                int dueDateColumn = cursor.getColumnIndex("due_date");


                calendar.setTimeInMillis(Long.parseLong(cursor.getString(dueDateColumn)));
                String date = sp.format(calendar.getTime());

                processedAlert = new Alert(cursor.getString(baseEntityIdColumn), cursor.getString(vaccineNameColumn), "", null, date, "");



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
