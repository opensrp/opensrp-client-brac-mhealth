package org.smartregister.brac.hnpp.utils;

import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.model.ReferralFollowUpModel;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.Utils;
import org.smartregister.util.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FormApplicability {

    public static boolean isDueHHVisit(String baseEntityId){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY);

    }
    public static boolean isDueAnyForm(String baseEntityId, String eventType){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, eventType);

    }
    public static boolean isDueChildInfoForm(String baseEntityId, String eventType){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinChildInfoLogic(baseEntityId, eventType);

    }
    public static String isDueChildInfo(long day){

        if(HnppConstants.isPALogin()){
            return null;
        }
        //int day = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(day >= 2 && day <= 3){
            return HnppConstants.EVENT_TYPE.CHILD_INFO_EBF12;
        }
        else if((day >= 180 && day <= 210)||(day >= 331 && day <= 366)||(day >= 515 && day <= 545)) {
            return HnppConstants.EVENT_TYPE.CHILD_INFO_7_24_MONTHS;
        }
        else if((day >= 700 && day <= 730)) {
            return HnppConstants.EVENT_TYPE.CHILD_INFO_25_MONTHS;
        }
        return "";
    }

    public static ArrayList<ReferralFollowUpModel> getReferralFollowUp(String baseEntityId){
        return HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getAllReferrelFollowUp(baseEntityId);
    }
    public static boolean isPregnant(String baseEntityId){
        return HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isPregnantFromElco(baseEntityId);
    }
    public static boolean isDueCoronaForm(String baseEntityId){
        //boolean isDoneToday =  HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, HnppConstants.JSON_FORMS.CORONA_INDIVIDUAL);
        //if(!isDoneToday){
            String coronaValue = HnppDBUtils.getIsCorona(baseEntityId);
           if(!TextUtils.isEmpty(coronaValue) && coronaValue.equalsIgnoreCase("true")){
               return true;
           }
           return false;
//        }
//        return isDoneToday;
    }

    public static String getDueFormForMarriedWomen(String baseEntityId, int age){
        String lmp = getLmp(baseEntityId);
            if(!TextUtils.isEmpty(lmp)){
                int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();
                int pncDay = getDayPassPregnancyOutcome(baseEntityId);
                if(pncDay != -1&&!isClosedPregnancyOutCome(baseEntityId)){
                    if(pncDay<=41){
                        //todo prosober current datetime - prosober_date+prosober_time>48hr = PNC AFTER else PNC WITHIN 48
                        return getHourPassPregnancyOutcome(baseEntityId) > 48 ?
                                HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour : HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
                    }else{
                        return HnppConstants.EVENT_TYPE.ELCO;
                    }
                }
                if(isClosedANC(baseEntityId)){
                    if(isElco(age)){
                        return HnppConstants.EVENT_TYPE.ELCO;
                    }
                }
                else{
                    return getANCEvent(dayPass);
                }
                return "";
            }

        if(isElco(age)){
            return HnppConstants.EVENT_TYPE.ELCO;
        }
        return "";
    }
    public static String getGuestMemberDueFormForWomen(String baseEntityId, int age){
        String lmp = getLmp(baseEntityId);
        if(!TextUtils.isEmpty(lmp)){
            int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();
            int pncDay = getDayPassPregnancyOutcome(baseEntityId);
            if(pncDay != -1&&!isClosedPregnancyOutCome(baseEntityId)){
                if(pncDay<=41){
                    return getHourPassPregnancyOutcome(baseEntityId) > 48 ?
                            HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC : HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC;
                }else{
                    return HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
                }
            }
            if(isClosedANC(baseEntityId)){
                if(isElco(age)){
                    return HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
                }
            }
            else{
                return getANCEvent(dayPass);
            }
            return "";
        }

        if(isElco(age)){
            return HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
        }
        return "";
    }
    public static String getANCEvent(int dayPass){
        if(dayPass > 1 && dayPass <= 84){
            //first trimester
            return HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
        }else if(dayPass > 84 && dayPass <= 168){

            return HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
        }else if(dayPass > 168){

            return HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
        }
        return "";
    }
    public static boolean isElco(int age){
        return age >= 14 && age < 50;
    }
    public static boolean isEncVisible(Date dob){
        int dayPass = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(dayPass <= 41){
            return true;
        }
        return false;
    }
    public static boolean isImmunizationVisible(Date dob){
        int monthsDifference = getMonthsDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(monthsDifference <= 36){
            return true;
        }
        return false;
    }
    private static int getMonthsDifference(LocalDate date1, LocalDate date2) {
        return Months.monthsBetween(
                date1.withDayOfMonth(1),
                date2.withDayOfMonth(1)).getMonths();
    }

    public static String getLmp(String baseEntityId){
        String lmp = "SELECT last_menstrual_period FROM ec_anc_register where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
        if(valus.size()>0){
            return valus.get(0).get("last_menstrual_period");
        }

        return "";

    }
    public static boolean isClosedANC(String baseEntityId){
        String DeliveryDateSql = "SELECT is_closed FROM ec_anc_register where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            if("1".equalsIgnoreCase(valus.get(0).get("is_closed"))){
                return true;
            }


        }
        return false;
    }
    public static boolean isClosedPregnancyOutCome(String baseEntityId){
        String DeliveryDateSql = "SELECT is_closed FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            if("1".equalsIgnoreCase(valus.get(0).get("is_closed"))){
                return true;
            }


        }
        return false;
    }

    public static int getDayPassPregnancyOutcome(String baseEntityId){
        int dayPass = -1;
        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0&&valus.get(0).get("delivery_date")!=null){
            dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(valus.get(0).get("delivery_date")), new DateTime()).getDays();

        }
        return dayPass;
    }
    public static int getDaysFromEDD(String edd){
        int dayPass = Days.daysBetween(new DateTime(),DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(edd)).getDays();
        return 281 - dayPass;
    }

    public static int getUterusLengthInCM(String edd){
        int days = getDaysFromEDD(edd);
        if(days>=112&&days<=140){
            return 24;
        }else if(days>=141&&days<=168){
            return 28;
        }else if(days>=169&&days<=196){
            return 32;
        }else if(days>=197&&days<=224){
            return 36;
        }else if(days>=225&&days<=252){
            return 38;
        }
        return 0;
    }

    public static boolean isFirstTimeAnc(String baseEntityId){
        return HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isFirstTime(baseEntityId);

    }
    public static boolean isWomanOfReproductiveAge(CommonPersonObjectClient commonPersonObject) {
        if (commonPersonObject == null) {
            return false;
        }

        // check age and gender
        int age = getAge(commonPersonObject);
        String maritalStatus  = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "marital_status", false);
        if ( age != -1 && getGender(commonPersonObject).trim().equalsIgnoreCase("F") && !TextUtils.isEmpty(maritalStatus) && maritalStatus.equalsIgnoreCase("Married")) {

            return isElco(age);
        }

        return false;
    }
    public static int getAge(CommonPersonObjectClient commonPersonObject){
        if(commonPersonObject == null) return -1;
        String dobString = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "dob", false);
        if(!TextUtils.isEmpty(dobString) ){
            Period period = new Period(new DateTime(dobString), new DateTime());
            return period.getYears();
        }
        return -1;
    }
    public static int getAge(String dobString){
        if(!TextUtils.isEmpty(dobString) ){
            Period period = new Period(new DateTime(dobString), new DateTime());
            return period.getYears();
        }
        return -1;
    }

    public static long getDay(CommonPersonObjectClient commonPersonObject){
        String dobString = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "dob", false);
        if(!TextUtils.isEmpty(dobString) ){
            Date date = Utils.dobStringToDate(dobString);
           long day =  getDayDifference(new LocalDate(date),new LocalDate());
           return day;
        }
        return -1;
    }
    public static long getDay(String dobString){
        if(!TextUtils.isEmpty(dobString) ){
            Date date = Utils.dobStringToDate(dobString);
            long day =  getDayDifference(new LocalDate(date),new LocalDate());
            return day;
        }
        return -1;
    }
    public static String getGender(CommonPersonObjectClient commonPersonObject){
        return org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "gender", false);
    }
    //other service and package
    public static boolean isIycfApplicable(long day){
        if(HnppConstants.isPALogin()) return false;

        return day >=181 && day <=731;
    }
    public static boolean isAdolescentApplicable(int age, boolean isWomen){
        if(HnppConstants.isPALogin()) return false;
        return isWomen && age>=10 && age <=19;
    }
    //feedback comes from naimul vai to add the age limit 14 to 49
    public static boolean isWomenPackageApplicable(String baseEntityId , int age, boolean isWomen){
        if(HnppConstants.isPALogin()) return false;
        return isWomen && age >14 && age <50 && !isPragnent(baseEntityId,age);
    }

    public static boolean isPragnent(String baseEntityId, int age) {
        String eventType = getDueFormForMarriedWomen(baseEntityId,age);
        if(!TextUtils.isEmpty(eventType) && (eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)
                || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION)
                || eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION))){
            return true;
        }
        return false;

    }

    public static boolean isNcdApplicable(int age){
        return age >=18 && age<50;
    }

    private static long getDayDifference(LocalDate date1, LocalDate date2) {
        long startTime = date2.toDate().getTime() - date1.toDate().getTime();
        long difference_In_Days= startTime / 86400000L;
        return difference_In_Days;
    }

    public static int getHourPassPregnancyOutcome(String baseEntityId){
        int hoursPassed = -1;
        String DeliveryDateSql = "SELECT delivery_date, delivery_time FROM ec_pregnancy_outcome where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});
        if( valus.size() > 0 && valus.get(0).get("delivery_date")!= null && valus.get(0).get("delivery_time")!= null ){
            String day = valus.get(0).get("delivery_date");
            String time = valus.get(0).get("delivery_time");
            String dateTime = day +" "+ time;
              hoursPassed = Hours.hoursBetween(DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").parseDateTime(dateTime), new DateTime()).getHours();
        }
       // HH:mm:ss.SSS
        Log.e(FormApplicability.class.getSimpleName(), String.valueOf(hoursPassed));
        return hoursPassed;
    }
}