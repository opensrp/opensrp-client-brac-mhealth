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

import java.util.ArrayList;

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
    //PA
    public TargetVsAchievementData getAdultForum(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.FORUM_ADULT,day,month,year,ssName);
    }
    public TargetVsAchievementData getAttendancAdultForum(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getServiceCountAdultForum(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADULT_FORUM_SERVICE_TAKEN,day,month,year,ssName);
    }
    public TargetVsAchievementData getMarkedPresbyopia(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.MARKED_PRESBYOPIA,day,month,year,ssName);
    }
    public TargetVsAchievementData getPresbyopiaCorrection(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PRESBYOPIA_CORRECTION,day,month,year,ssName);
    }
    public TargetVsAchievementData getEstimateDiabetes(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_DIABETES,day,month,year,ssName);
    }
    public TargetVsAchievementData getEstimateHBS(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ESTIMATE_HBP,day,month,year,ssName);
    }
    public TargetVsAchievementData getCataractSurgeryRefer(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY_REFER,day,month,year,ssName);
    }
    public TargetVsAchievementData getCataractSurgery(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CATARACT_SURGERY,day,month,year,ssName);
    }

    //
    public TargetVsAchievementData getHHVisitTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,day,month,year,ssName);
    }
    public TargetVsAchievementData getElcoTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ELCO,day,month,year,ssName);
    }
    public TargetVsAchievementData getMethodUserTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.METHOD_USER,day,month,year,ssName);
    }
    public TargetVsAchievementData getAdoMethodUserTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,day,month,year,ssName);
    }
    public TargetVsAchievementData getPregnencyIdentiTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED,day,month,year,ssName);
    }
    public TargetVsAchievementData getDeliveryTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,day,month,year,ssName);
    }
    public TargetVsAchievementData getInstitutionDeliveryTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,day,month,year,ssName);
    }
    public TargetVsAchievementData get0to6ChildVisitTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_0_6,day,month,year,ssName);
    }
    public TargetVsAchievementData get7to24ChildVisitTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_7_24,day,month,year,ssName);
    }
    public TargetVsAchievementData get18to36ChildVisitTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_VISIT_18_36,day,month,year,ssName);
    }
    public TargetVsAchievementData get0to59ChildImmunizationTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.CHILD_IMMUNIZATION_0_59,day,month,year,ssName);
    }

    // service

    public TargetVsAchievementData getAncServiceTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ANC_SERVICE,day,month,year,ssName);
    }
    public TargetVsAchievementData getPncServiceTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PNC_SERVICE,day,month,year,ssName);
    }
    public TargetVsAchievementData getNcdTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.NCD_PACKAGE,day,month,year,ssName);
    }
    public TargetVsAchievementData getIYCFTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,day,month,year,ssName);
    }
    public TargetVsAchievementData getWomenTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,day,month,year,ssName);
    }
    public TargetVsAchievementData getAdoTarget(String day, String month, String year,String ssName){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,day,month,year,ssName);
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
    public TargetVsAchievementData getAvgChildTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getAvgWomenTarget(String day, String month, String year,String ssName){
        return getAvgTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_WOMEN_FORUM,day,month,year,ssName);
    }
    public TargetVsAchievementData getTargetVsAchievmentByVisitType(String visitType,String day, String month, String year, String ssName){
        TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
        String query = "select sum(target_count) as target_count, sum(achievemnt_count) as achievemnt_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);
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
    public TargetVsAchievementData getAvgTargetVsAchievmentByVisitType(String visitType,String day, String month, String year, String ssName){
        TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
        String query = "select sum(target_count) as target_count, sum(achievemnt_count) as achievemnt_count from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year,ssName);
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