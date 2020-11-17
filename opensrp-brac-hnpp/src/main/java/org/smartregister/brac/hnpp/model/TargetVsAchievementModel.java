package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

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
    private String getFilter(int day, int month, int year){
        if(day==0) return  " and month ="+month+" and year ="+year+"";
        return  " and day ="+day+" and month ="+month+" and year ="+year+"";
    }
    public ArrayList<TargetVsAchievementData> getHHVisitTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getElcoTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ELCO,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getMethodUserTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.METHOD_USER,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAdoMethodUserTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ADO_METHOD_USER,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getPregnencyIdentiTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_IDENTIFIED,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getDeliveryTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getInstitutionDeliveryTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.INSTITUTIONALIZES_DELIVERY,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getPregnancyVisitTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PREGNANCY_VISIT,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAncRegistrationTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.ANC_REGISTRATION,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getPncRegistrationTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.PNC_REGISTRATION,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getNcdTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.NCD_PACKAGE,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getIYCFTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getWomenTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAvgAdoTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADO_FORUM,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAvgNcdTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_NCD_FORUM,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAvgAdultTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_ADULT_FORUM,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getAvgIycfTarget(int day, int month, int year){
        return getTargetVsAchievmentByVisitType(HnppConstants.EVENT_TYPE.AVG_ATTEND_IYCF_FORUM,day,month,year);
    }
    public ArrayList<TargetVsAchievementData> getTargetVsAchievmentByVisitType(String visitType,int day, int month, int year){
        ArrayList<TargetVsAchievementData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select * from target_table where target_name ='"+ visitType+"'"+ getFilter(day,month,year);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
                dashBoardData1.setTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                dashBoardData1.setAchievementCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT)));
                dashBoardData1.setEventType(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY);
                dashBoardData1.setTitle(HnppConstants.eventTypeMapping.get(dashBoardData1.getEventType()));

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                if(!TextUtils.isEmpty(dashBoardData1.getEventType())){
                    dashBoardDataArrayList.add(dashBoardData1);
                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardDataArrayList;
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
