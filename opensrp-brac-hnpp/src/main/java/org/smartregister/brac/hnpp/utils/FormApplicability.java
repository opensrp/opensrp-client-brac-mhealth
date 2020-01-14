package org.smartregister.brac.hnpp.utils;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FormApplicability {

    public static String getDueFormForMarriedWomen(String baseEntityId, int age){
        String lmp = getLmp(baseEntityId);
            if(!TextUtils.isEmpty(lmp)&&!isClosedPregnancyOutCome(baseEntityId)){
                int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays();
                if(isDonePregnancyOutCome(baseEntityId)){
                    return HnppConstants.EVENT_TYPE.PNC_REGISTRATION;
                }
                if(dayPass > 1 && dayPass <= 84){
                    //first trimester
                    if(isFirstTimeAnc(baseEntityId)){
                        return HnppConstants.EVENT_TYPE.ANC_PREGNANCY_HISTORY;
                    }
                    return HnppConstants.EVENT_TYPE.ANC1_REGISTRATION;
                }else if(dayPass > 84 && dayPass <= 168){
                    return HnppConstants.EVENT_TYPE.ANC2_REGISTRATION;
                }else if(dayPass > 168){
                    return HnppConstants.EVENT_TYPE.ANC3_REGISTRATION;
                }
                return "";
            }

        if(isElco(age)){
            return HnppConstants.EVENT_TYPE.ELCO;
        }
        return "";
    }
    public static boolean isElco(int age){
        return age > 15 && age < 50;
    }
    public static boolean isEncVisible(Date dob){
        int dayPass = DateUtil.dayDifference(new LocalDate(dob),new LocalDate(System.currentTimeMillis()));
        if(dayPass <= 41){
            return true;
        }
        return false;
    }

    public static String getLmp(String baseEntityId){
        String lmp = "SELECT last_menstrual_period FROM ec_anc_register where base_entity_id = ? ";
        List<Map<String, String>> valus = AbstractDao.readData(lmp, new String[]{baseEntityId});
        if(valus.size()>0){
            return valus.get(0).get("last_menstrual_period");
        }

        return "";

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
    public static boolean isDonePregnancyOutCome(String baseEntityId){
        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});

        if(valus.size() > 0){
            int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(valus.get(0).get("delivery_date")), new DateTime()).getDays();
            if(dayPass<=41)
                return true;
        }
        return false;
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
        String dobString = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "dob", false);
        if(!TextUtils.isEmpty(dobString) ){
            Period period = new Period(new DateTime(dobString), new DateTime());
            return period.getYears();
        }
        return -1;
    }
    public static String getGender(CommonPersonObjectClient commonPersonObject){
        return org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "gender", false);
    }
    //other service and package
    public static boolean isIycfApplicable(int age){
        return age <=5;
    }
    public static boolean isAdolescentApplicable(int age, boolean isWomen){
        return isWomen && age>=10 && age <=19;
    }
    public static boolean isWomenPackageApplicable(int age, boolean isWomen){
        return isWomen && age >=10;
    }

}