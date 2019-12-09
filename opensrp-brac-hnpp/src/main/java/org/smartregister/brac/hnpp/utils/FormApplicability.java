package org.smartregister.brac.hnpp.utils;

import android.text.TextUtils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FormApplicability {
    private HnppVisitLogRepository visitLogRepository;
    private FormApplicability instance;
    private FormApplicability(){
        visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
    }
    public FormApplicability getInstance(){
        if(instance == null){
            instance = new FormApplicability();
        }
        return instance;
    }
    public boolean isAncOptionVisible(int age, boolean isMarried){
        return isElco(age,isMarried);
    }
    public boolean isPregnancyOutcomeVisible(int age, boolean isMarried, String baseEntityId){
        return isElco(age,isMarried) && !isDonePregnancyOutCome(baseEntityId);
    }
    public static boolean isPncVisible(CommonPersonObjectClient commonPersonObject) {
        String baseEntityId = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "base_entity_id", false);

        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0){
            String deliveryDate = valus.get(0).get("delivery_date");

            if(deliveryDate!=null){
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date deliveryDateformatted = null;
                try {
                    deliveryDateformatted = dateFormat.parse(deliveryDate);
                    int day = Days.daysBetween((new DateTime(deliveryDateformatted)), new DateTime(System.currentTimeMillis())).getDays();
                    return day < 41;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;

    }

    public String getDueFormForWomen(String baseEntityId, int age, boolean isMarried, String lmp){
        String formName = "";
        if(!TextUtils.isEmpty(lmp)){
            int dayPass = Days.daysBetween(DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(lmp), new DateTime()).getDays() / 7;
            if(dayPass > 1 && dayPass < 84){
                //first trimester
                if(isFirstTimeAnc(baseEntityId)){
                    return HnppConstants.JSON_FORMS.PREGNANCY_HISTORY;
                }
                return HnppConstants.JSON_FORMS.ANC1_FORM;
            }else if(dayPass > 84 && dayPass < 168){
                return HnppConstants.JSON_FORMS.ANC2_FORM;
            }else if(dayPass > 168){
                return HnppConstants.JSON_FORMS.ANC3_FORM;
            }
            return "";
        }
        if(isElco(age,isMarried)){
            return HnppConstants.JSON_FORMS.ELCO;
        }
        return formName;
    }
    public boolean isElco(int age, boolean isMarried){
        return isMarried && age > 10 && age < 50;
    }
    public boolean isDonePregnancyOutCome(String baseEntityId){
        String DeliveryDateSql = "SELECT delivery_date FROM ec_pregnancy_outcome where base_entity_id = ? ";

        List<Map<String, String>> valus = AbstractDao.readData(DeliveryDateSql, new String[]{baseEntityId});
        if(valus.size() > 0) return true;
        return false;
    }
    public boolean isFirstTimeAnc(String baseEntityId){
        visitLogRepository.getAllVisitLog(baseEntityId);
        return false;

    }
    public static boolean isWomanOfReproductiveAge(CommonPersonObjectClient commonPersonObject) {
        if (commonPersonObject == null) {
            return false;
        }

        // check age and gender
        String dobString = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "dob", false);
        String gender = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "gender", false);
        String maritalStatus  = org.smartregister.util.Utils.getValue(commonPersonObject.getColumnmaps(), "marital_status", false);
        if (!TextUtils.isEmpty(dobString) && gender.trim().equalsIgnoreCase("F") && !TextUtils.isEmpty(maritalStatus) && maritalStatus.equalsIgnoreCase("Married")) {
            Period period = new Period(new DateTime(dobString), new DateTime());
            int age = period.getYears();
            return age >= 15 && age <= 49;
        }

        return false;
    }

}