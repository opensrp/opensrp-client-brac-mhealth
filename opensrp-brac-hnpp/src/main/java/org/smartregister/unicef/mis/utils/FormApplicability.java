package org.smartregister.unicef.mis.utils;

import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.ANC_HOME_VISIT;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.ANC_HOME_VISIT_FACILITY;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.KMC_SERVICE_HOME;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.KMC_SERVICE_HOSPITAL;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
import static org.smartregister.unicef.mis.utils.HnppConstants.EVENT_TYPE.SCANU_FOLLOWUP;

import android.text.TextUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.model.ReferralFollowUpModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.Utils;
import org.smartregister.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FormApplicability {

    public static boolean isDueHHVisit(String baseEntityId){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY);

    }
    public static boolean isDueChildProfileVisit(String baseEntityId){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, HnppConstants.EVENT_TYPE.CHILD_PROFILE_VISIT);

    }
    public static boolean isDueMemberProfileVisit(String baseEntityId){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, HnppConstants.EVENT_TYPE.MEMBER_PROFILE_VISIT);

    }
    public static boolean isDueAnyForm(String baseEntityId, String eventType){
        return true;//!HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinTwentyFourHours(baseEntityId, eventType);

    }
    public static boolean isDueChildInfoForm(String baseEntityId, String eventType){
        return !HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isDoneWihinChildInfoLogic(baseEntityId, eventType);

    }
    public static String isDueChildEccd(Date dob){
        if(!HnppConstants.isDisabilityEnable()) return null;

        if(HnppConstants.isPALogin()){
            return null;
        }
        int day = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));

        double month = Math.ceil( (day/30.417));
        Log.v("isDueChildEccd","isDueChildEccd>>>day:"+day+":month:"+month);
        if(month >= 2 && month <= 3){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_2_3_MONTH;
        }else if(month >= 4 && month <= 6){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_4_6_MONTH;//4-6
        }else if(month >= 7 && month <= 9){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_7_9_MONTH;//7-9
        }else if(month >= 10 && month <= 12){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_10_12_MONTH;//10-15 month
        } else if(month >= 16 && month <= 18){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_18_MONTH;//16-18
        }else if(month >= 19 && month <= 24 ){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_24_MONTH;//19-24
        }else if(month >= 25 && month <= 36){
            return HnppConstants.EVENT_TYPE.CHILD_ECCD_36_MONTH;//25-36
        }

        return "";
    }
    public static String getKMCForm(String baseEntityId){
        return HnppDBUtils.getKMCStatus(baseEntityId);

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
                        return HnppConstants.EVENT_TYPE.PNC_REGISTRATION;

                        //todo prosober current datetime - prosober_date+prosober_time>48hr = PNC AFTER else PNC WITHIN 48
//                        return getHourPassPregnancyOutcome(baseEntityId) > 48 ?
//                                HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour : HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour;
//
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
                    return ANC_HOME_VISIT;//getANCEvent(dayPass);
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
                if(pncDay<=41) {
                    return HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
//                    return getHourPassPregnancyOutcome(baseEntityId) > 48 ?
//                            HnppConstants.EVENT_TYPE.PNC_REGISTRATION_AFTER_48_hour_OOC : HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour_OOC;
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
                return ANC_HOME_VISIT;//getANCEvent(dayPass);
            }
            return "";
        }

        if(isElco(age)){
            return HnppConstants.EVENT_TYPE.ANC_REGISTRATION;
        }
        return "";
    }
    public static String getPncTitle(String baseEntityId){
        return HnppConstants.getPncTitle(FormApplicability.getPNCCount(baseEntityId)+1)[0];
    }
    public static String getANCTitleForHistory(int count) {
        return HnppConstants.getAncTitle(count)[0];
    }
    public static String getPNCTitleForHistory(int count) {
        return HnppConstants.getPncTitle(count)[0];
    }
    public static String getNewBornTitleForHistory(int count) {
        return HnppConstants.getNewBornPncTitle(count)[0];
    }
    public static String getKmcHomeTitleForHistory(int count) {
        return HnppConstants.getKMCHomeTitle(count)[0];
    }
    public static String getKmcHospitalTitleForHistory(int count) {
        return HnppConstants.getKMCHospitalTitle(count)[0];
    }
    public static String getNewBornTitle(String baseEntityId){
        return HnppConstants.getNewBornPncTitle(FormApplicability.getNewBornPNCCount(baseEntityId)+1)[0];
    }
    public static String getIMCITitle(Date dob){
        int dayPass = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(dayPass<=60){
            return "ছোট শিশুর (০-২ মাস) রোগ নিরূপণ ";
        }
        return "শিশুর (২-৫৯ মাস) রোগ নিরূপণ";
    }
    public static String getKMCHomeTitle(String baseEntityId){
        return HnppConstants.getKMCHomeTitle(FormApplicability.getKMCHomeCount(baseEntityId)+1)[0];
    }
    public static String getScanuTitle(String baseEntityId){
        return HnppConstants.getScanuTitle(FormApplicability.getScanuCount(baseEntityId)+1)[0];
    }
    public static String getKMCHospitalTitle(String baseEntityId){
        return HnppConstants.getKMCHospitalTitle(FormApplicability.getKMCHospitalCount(baseEntityId)+1)[0];
    }
    public static String getANCTitle(String baseEntityId){
        return HnppConstants.getAncTitle(FormApplicability.getANCCount(baseEntityId)+1)[0];
//        String lmp = getLmp(baseEntityId);
//        int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();
//
//        if(dayPass > 1 && dayPass <= 84){
//            //first trimester
//            return HnppConstants.getAncTitle(1)[0];
//        }else if(dayPass > 84 && dayPass <= 140){
//
//            return HnppConstants.getAncTitle(2)[0];
//        }else if(dayPass > 140 && dayPass <= 182){
//
//            return HnppConstants.getAncTitle(3)[0];
//        }else if(dayPass > 182 && dayPass <= 210){
//
//            return HnppConstants.getAncTitle(4)[0];
//        }else if(dayPass > 210 && dayPass <= 238){
//
//            return HnppConstants.getAncTitle(5)[0];
//        }else if(dayPass > 238 && dayPass <= 252){
//
//            return HnppConstants.getAncTitle(6)[0];
//        }else if(dayPass > 252 && dayPass <= 266){
//
//            return HnppConstants.getAncTitle(7)[0];
//        }else if(dayPass > 266 && dayPass <= 280){
//
//            return HnppConstants.getAncTitle(8)[0];
//        }
//        return "";
    }
    public static String getANCType(String baseEntityId){
        String lmp = getLmp(baseEntityId);
        int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();

        if(dayPass > 1 && dayPass <= 84){
            //first trimester
            return HnppConstants.getAncTitle(1)[1];
        }else if(dayPass > 84 && dayPass <= 140){

            return HnppConstants.getAncTitle(2)[1];
        }else if(dayPass > 140 && dayPass <= 182){

            return HnppConstants.getAncTitle(3)[1];
        }else if(dayPass > 182 && dayPass <= 210){

            return HnppConstants.getAncTitle(4)[1];
        }else if(dayPass > 210 && dayPass <= 238){

            return HnppConstants.getAncTitle(5)[1];
        }else if(dayPass > 238 && dayPass <= 252){

            return HnppConstants.getAncTitle(6)[1];
        }else if(dayPass > 252 && dayPass <= 266){

            return HnppConstants.getAncTitle(7)[1];
        }else if(dayPass > 266 && dayPass <= 280){

            return HnppConstants.getAncTitle(8)[1];
        }
        return "";
    }
    public static boolean isWomenImmunizationApplicable(CommonPersonObjectClient commonPersonObject){
        if(getGender(commonPersonObject).trim().equalsIgnoreCase("F") && getAge(commonPersonObject)>=10){
            return true;
        }
        return false;
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
    public static boolean isIMCIVisible(Date dob){
        int dayPass = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(dayPass <= 1825){
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
    public static int getMonthsDifference(LocalDate date1, LocalDate date2) {
        return Months.monthsBetween(
                date1.withDayOfMonth(1),
                date2.withDayOfMonth(1)).getMonths();
    }

    public static String getLmp(String baseEntityId){
        String lmp = "SELECT last_menstrual_period FROM ec_anc_register where base_entity_id = ? ";
        List<Map<String, String>> valus = HnppDBUtils.readData(lmp, new String[]{baseEntityId});
        if(valus.size()>0){
            return valus.get(0).get("last_menstrual_period");
        }

        return "";

    }
    public static boolean isClosedANC(String baseEntityId){
        String DeliveryDateSql = "SELECT is_closed FROM ec_anc_register where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            if("1".equalsIgnoreCase(valus.get(0).get("is_closed"))){
                return true;
            }


        }
        return false;
    }
    public static boolean isClosedPregnancyOutCome(String baseEntityId){
        String DeliveryDateSql = "SELECT is_closed FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            if("1".equalsIgnoreCase(valus.get(0).get("is_closed"))){
                return true;
            }


        }
        return false;
    }
    public static boolean getNoOfBornChild(String baseEntityId){
        String DeliveryDateSql = "SELECT no_born_alive FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            String no = valus.get(0).get("no_born_alive");

            if(!TextUtils.isEmpty(no) && Integer.parseInt(no)>0){
                return true;
            }


        }
        return false;
    }

    public static int getDayPassPregnancyOutcome(String baseEntityId){
        int dayPass = -1;
        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0&&valus.get(0).get("delivery_date")!=null){
            dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(valus.get(0).get("delivery_date")), new DateTime()).getDays();

        }
        return dayPass;
    }
    public static String  getDeliveryDate(String baseEntityId){
        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0&&valus.get(0).get("delivery_date")!=null){
           return valus.get(0).get("delivery_date");

        }
        return "";
    }
    public static String  getEdd(String baseEntityId){
        String DeliveryDateSql = "SELECT edd FROM ec_anc_register where base_entity_id = ? ";

        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0&&valus.get(0).get("edd")!=null){
            return valus.get(0).get("edd");

        }
        return "";
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
        return false;//HnppApplication.getHNPPInstance().getHnppVisitLogRepository().isFirstTime(baseEntityId);

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
        if(!TextUtils.isEmpty(eventType) && (eventType.equalsIgnoreCase(ANC_HOME_VISIT)
                )){
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
    public static int getANCCount(String baseEntityId){
        long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as anc_count from ec_visit_log where base_entity_id ='"+baseEntityId+"' and (visit_type ='"+ ANC_HOME_VISIT+"' or visit_type ='"+ ANC_HOME_VISIT_FACILITY+"' )and visit_date>="+maxVisitDate;
        Log.v("HOME_VISIT","getANCCount>>ancQuery:"+ancQuery);
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("anc_count")!= null){
            ancCount = Integer.parseInt(values.get(0).get("anc_count"));
        }
        return ancCount;

    }
    public static int getPNCCount(String baseEntityId){
        long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as anc_count from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+PNC_REGISTRATION+"' and visit_date>="+maxVisitDate;
        Log.v("HOME_VISIT","getPNCCount>>ancQuery:"+ancQuery);
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("anc_count")!= null){
            ancCount = Integer.parseInt(values.get(0).get("anc_count"));
        }
        return ancCount;

    }
    public static int getNewBornPNCCount(String baseEntityId){
       // long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as new_pnc_count from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+NEW_BORN_PNC_1_4+"'";
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("new_pnc_count")!= null){
            ancCount = Integer.parseInt(values.get(0).get("new_pnc_count"));
        }
        return ancCount;

    }
    public static int getKMCHomeCount(String baseEntityId){
        // long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as kmc_service from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+KMC_SERVICE_HOME+"'";
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("kmc_service")!= null){
            ancCount = Integer.parseInt(values.get(0).get("kmc_service"));
        }
        return ancCount;

    }
    public static int getKMCHospitalCount(String baseEntityId){
        // long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as kmc_service from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+KMC_SERVICE_HOSPITAL+"'";
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("kmc_service")!= null){
            ancCount = Integer.parseInt(values.get(0).get("kmc_service"));
        }
        return ancCount;

    }
    public static int getScanuCount(String baseEntityId){
        // long maxVisitDate = getMaxVisitDate(baseEntityId);
        int ancCount = 0;
        String ancQuery = "select count(*) as kmc_service from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+SCANU_FOLLOWUP+"'";
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("kmc_service")!= null){
            ancCount = Integer.parseInt(values.get(0).get("kmc_service"));
        }
        return ancCount;

    }
    public static int getNewPNCCount(String baseEntityId){
        return getNewBornPNCCount(baseEntityId);
//        String dobSql = "SELECT dob FROM ec_child where base_entity_id = ? ";
//        List<Map<String, String>> valus = HnppDBUtils.readData(dobSql, new String[]{baseEntityId});
//
//        if( valus.size() > 0 && valus.get(0).get("dob")!= null){
//            String dob = valus.get(0).get("dob");
//            dob = dob.substring(0,dob.indexOf("T"));
//            LocalDate dobDate = new LocalDate(dob);
//            LocalDate today = new LocalDate(System.currentTimeMillis());//.isBefore(new LocalDate(System.currentTimeMillis())
//            if(today.isBefore(dobDate.plusDays(0))){
//                return 1;
//            }else if(today.isBefore(dobDate.plusDays(3))){
//                return 2;
//            }else if(today.isBefore(dobDate.plusDays(7))){
//                return 3;
//            }else if(today.isBefore(dobDate.plusDays(42))){
//                return 4;
//            }else{
//                return -1;
//            }
//        }
//        return -1;

    }
    public static int getKMCServiceHomeCount(String baseEntityId){
        return getKMCHomeCount(baseEntityId);
    }
    public static int getKMCServiceHospitalCount(String baseEntityId){
        return getKMCHospitalCount(baseEntityId);
    }
    public static long getMaxVisitDate(String baseEntityId){
        long visitDate = 0;
        String ancQuery = "select max(visit_date) as max_visit_date from ec_visit_log where base_entity_id ='"+baseEntityId+"' and visit_type ='"+ANC_REGISTRATION+"' ";
        List<Map<String, String>> values = HnppDBUtils.readData(ancQuery, null);
        if( values.size() > 0 && values.get(0).get("max_visit_date")!= null){
            visitDate = Long.parseLong(values.get(0).get("max_visit_date"));
        }
        return visitDate;

    }
    public static int getHourPassPregnancyOutcome(String baseEntityId){
        int hoursPassed = -1;
        String DeliveryDateSql = "SELECT delivery_date, delivery_time FROM ec_pregnancy_outcome where base_entity_id = ? ";
        List<Map<String, String>> valus = HnppDBUtils.readData(DeliveryDateSql, new String[]{baseEntityId});
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