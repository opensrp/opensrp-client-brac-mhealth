package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.repository.TargetVsAchievementRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.chw.core.application.CoreChwApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetVsAchievementModel implements DashBoardContract.Model  {

    private Context context;

    public TargetVsAchievementModel(Context context){
        this.context = context;
    }
    private String getFilter(String day, String month, String year, String ssName){
        if(day.equalsIgnoreCase("0")) {
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return "";
                return  " and month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)){
                    return "and ss_name = '"+ssName+"'";
                }
                return  " and month ="+month+" and year ="+year+" and ss_name = '"+ssName+"'";
            }
            
        }else{
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return " and day ="+day+"";
                return  " and day ="+day+" and month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)) return  " and day ="+day+" and ss_name ='"+ssName+"'";
                return  " and day ="+day+" and month ="+month+" and year ="+year+" and ss_name ='"+ssName+"'";
            }
        }
        
    }
   /* private String getFilter(String fromDate, String toDate, String ssName){
        if(day.equalsIgnoreCase("0")) {
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return "";
                return  " and month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)){
                    return "and ss_name = '"+ssName+"'";
                }
                return  " and month ="+month+" and year ="+year+" and ss_name = '"+ssName+"'";
            }

        }else{
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return " and day ="+day+"";
                return  " and day ="+day+" and month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)) return  " and day ="+day+" and ss_name ='"+ssName+"'";
                return  " and day ="+day+" and month ="+month+" and year ="+year+" and ss_name ='"+ssName+"'";
            }
        }

    }*/
    //PA
//    public TargetVsAchievementData getAdultForum(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_ADULT,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getAttendancAdultForum(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getServiceCountAdultForum(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getAdultPackage(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.NCD_BY_PA,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getMarkedPresbyopia(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getPresbyopiaCorrection(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getEstimateDiabetes(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getEstimateHBS(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getCataractSurgeryRefer(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER,day,month,year,ssName);
//    }
//    public TargetVsAchievementData getCataractSurgery(String day, String month, String year,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY,day,month,year,ssName);
//    }

    //PA For DailyVisitFromToDate

    public TargetVsAchievementData getAdultForum(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_ADULT,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getAttendancAdultForum(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getServiceCountAdultForum(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,fromDate,toDate,ssName);
    }
//    public TargetVsAchievementData getAdultPackage(long fromDate, long toDate,String ssName){
//        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.NCD_BY_PA,fromDate,toDate,ssName);
//    }
    public TargetVsAchievementData getMarkedPresbyopia(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getPresbyopiaCorrection(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getEstimateDiabetes(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getEstimateHBS(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getCataractSurgeryRefer(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getCataractSurgery(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY,fromDate,toDate,ssName);
    }

    // DailyVisit Filter By FromToDate
    public TargetVsAchievementData getHHVisitTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getElcoTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ELCO,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getMethodUserTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.METHOD_USER,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getAdoMethodUserTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getPregnencyIdentiTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getDeliveryTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getInstitutionDeliveryTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData get0to6ChildVisitTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_0_6,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData get7to24ChildVisitTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_7_24,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData get18to36ChildVisitTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_18_36,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData get0to59ChildImmunizationTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59,fromDate,toDate,ssName);
    }

    // service

    public TargetVsAchievementData getAncServiceTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ANC_SERVICE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getPncServiceTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PNC_SERVICE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getNcdTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.NCD_PACKAGE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getIYCFTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getWomenTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,fromDate,toDate,ssName);
    }
    public TargetVsAchievementData getAdoTarget(long fromDate, long toDate,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,fromDate,toDate,ssName);
    }
    //forum
    public TargetVsAchievementData getAdoForumTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_ADO,day,month,year,ssName);
    }
    public TargetVsAchievementData getNcdForumTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_NCD,day,month,year,ssName);
    }
    public TargetVsAchievementData getChildForumTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_CHILD,day,month,year,ssName);
    }
    public TargetVsAchievementData getWomenForumTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_WOMEN,day,month,year,ssName);
    }
    public TargetVsAchievementData getAdultForumTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_ADULT,day,month,year,ssName);
    }

    public TargetVsAchievementData getAvgAdoTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgNcdTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgAdultTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgAdultPATarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_ATTENDANCE,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgChildTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgWomenTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getTargetVsAchievmentByVisitType(String visitType,String day, String month, String year, String ssName){
        TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
        //String query = "select sum(target_count) as target_count, sum(achievemnt_count) as achievemnt_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);
        String query = "select sum(coalesce(achievemnt_count,0)) as achievemnt_count,(select sum(coalesce(target_count,0)) from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,"") +") as target_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);

        Log.v("TARGET_QUERY","query:"+query);
        Cursor cursor = null;
        try{
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1.setTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                dashBoardData1.setAchievementCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT)));
                //test
                //dashBoardData1.setAchievementCount(3);
                if(dashBoardData1.getTargetCount() != 0){
                    int percentage = (int) ((dashBoardData1.getAchievementCount() * 100)/dashBoardData1.getTargetCount());
                    dashBoardData1.setAchievementPercentage(percentage);
                }

                dashBoardData1.setEventType(visitType);
                dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));

                cursor.moveToNext();
            }

        }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null) cursor.close();
        }


        return dashBoardData1;
    }
    public TargetVsAchievementData getTargetVsAchievmentByVisitType(String visitType,long fromDate,long toDate, String ssName){
        String fromMonthStr = HnppConstants.getDateFormateFromLong(fromDate);
        String toMonthStr = HnppConstants.getDateFormateFromLong(toDate);
        TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
        String query = null;
        String whereCluse = " where target_name ='"+visitType+"'";
        if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_SERVICE)){
            whereCluse = " where (target_name ='"+visitType+"' or target_name ='"+HnppConstants.EVENT_TYPE.PNC_REGISTRATION_BEFORE_48_hour+"')";
        }
        if(fromDate == -1 && toDate == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select sum(coalesce(target_count,0)) as target_count, sum(coalesce(achievemnt_count,0)) as achievemnt_count from target_table "+whereCluse;
            }else{
                query = "select sum(coalesce(target_count,0)) as target_count, sum(coalesce(achievemnt_count,0)) as achievemnt_count from target_table "+whereCluse+getSSCondition(ssName);
            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, achievemnt_count, target_count, target_name from target_table)SELECT sum(coalesce(achievemnt_count,0)) as achievemnt_count, sum(coalesce(target_count,0)) as target_count from t1 "+whereCluse+getBetweenCondition(fromMonthStr,toMonthStr,"date");
            }else{
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, achievemnt_count, target_count, target_name from target_table)SELECT sum(coalesce(achievemnt_count,0)) as achievemnt_count, sum(coalesce(target_count,0)) as target_count from t1 "+whereCluse+getSSCondition(ssName)+getBetweenCondition(fromMonthStr,toMonthStr,"date");
            }
        }
        Log.v("TARGET_VS_ACHIEV","query:"+query);

        Cursor cursor = null;
        try{
            // try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    dashBoardData1.setTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                    dashBoardData1.setAchievementCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT)));
                    //test
                    //dashBoardData1.setAchievementCount(3);
                    if(dashBoardData1.getTargetCount() != 0){
                        int percentage = (int) ((dashBoardData1.getAchievementCount() * 100)/dashBoardData1.getTargetCount());
                        dashBoardData1.setAchievementPercentage(percentage);
                    }

                    dashBoardData1.setEventType(visitType);
                    dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));

                    cursor.moveToNext();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null) cursor.close();
        }


        return dashBoardData1;
    }
    public String getSSCondition(String ssName){
        String ssCondition;
        ssCondition = " and "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        return ssCondition;
    }
    public String getBetweenCondition(String fromMonth, String toMonth, String compareDate){
        StringBuilder build = new StringBuilder();
        if(TextUtils.isEmpty(fromMonth)){
            build.append(MessageFormat.format(" and {0} = {1} ",compareDate,"'"+toMonth+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",compareDate,"'"+fromMonth+"'","'"+toMonth+"'"));
        }
        return build.toString();
    }
    public String getBetweenCondition(long fromMonth, long toMonth, String compareDate){
        String query = null;
        if(fromMonth == -1){
            query = " and "+compareDate+" ='"+toMonth+"'";
        }
        else {
            query = " and ("+compareDate+" between '"+HnppConstants.getDateFormateFromLong(fromMonth)+"' and '"+HnppConstants.getDateFormateFromLong(toMonth)+"')";
        }
        return query;
    }
    public TargetVsAchievementData getAvgTargetVsAchievmentByVisitType(String visitType,String day, String month, String year, String ssName){
        TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
        //String query = "select sum(target_count) as target_count, sum(achievemnt_count) as achievemnt_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);
        String query = "select sum(coalesce(achievemnt_count,0)) as achievemnt_count,(select sum(coalesce(target_count,0)) from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,"") +") as target_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);

        Log.v("TARGET_QUERY","avg query:"+query);
        Cursor cursor = null;
         try {
             cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
             if (cursor != null && cursor.getCount() > 0) {
                 cursor.moveToFirst();
                 while (!cursor.isAfterLast()) {
                     dashBoardData1.setAvgTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                     int achCount = cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT));
                     dashBoardData1.setAvgAchievmentCount(achCount);


                     dashBoardData1.setEventType(visitType);
                     dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));

                     cursor.moveToNext();
                 }

             }
         }catch (Exception e){
             e.printStackTrace();
         }finally {
             if(cursor !=null) cursor.close();
         }


        return dashBoardData1;
    }

    @Override
    public DashBoardContract.Model getDashBoardModel() {
        return this;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
