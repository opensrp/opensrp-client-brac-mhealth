package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.mvel2.sh.text.TextUtil;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.repository.TargetVsAchievementRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.TargetVsAchievementData;
import org.smartregister.chw.core.application.CoreChwApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetVsAchievementModel implements DashBoardContract.Model  {

    private Context context;
    private boolean isMonthWise = false;

    public void setMonthWise(boolean monthWise) {
        isMonthWise = monthWise;
    }

    public TargetVsAchievementModel(Context context){
        this.context = context;
    }
    private String getFilterWithAnd(String day, String month, String year, String ssName){
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
    private String getFilter(String day, String month, String year, String ssName){
        if(day.equalsIgnoreCase("0")) {
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return "";
                return  "  month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)){
                    return " ss_name = '"+ssName+"'";
                }
                return  "  month ="+month+" and year ="+year+" and ss_name = '"+ssName+"'";
            }

        }else{
            if(TextUtils.isEmpty(ssName)){
                if(TextUtils.isEmpty(month)) return " and day ="+day+"";
                return  "  day ="+day+" and month ="+month+" and year ="+year+"";
            }
            else {
                if(TextUtils.isEmpty(month)) return  " and day ="+day+" and ss_name ='"+ssName+"'";
                return  "  day ="+day+" and month ="+month+" and year ="+year+" and ss_name ='"+ssName+"'";
            }
        }

    }
    //forum

    public ArrayList<TargetVsAchievementData> getTargetVsAchievment(String fromDate,String toDate, String ssName){
//        String fromMonthStr = HnppConstants.getDateFormateFromLong(fromDate);
//        String toMonthStr = HnppConstants.getDateFormateFromLong(toDate);
        ArrayList<TargetVsAchievementData> list = new ArrayList<TargetVsAchievementData>();
        String query = null;
        String whereCluse = " where ";
        String groupBy = " group by target_name";
        String selectColumes ="sum(coalesce(target_count,0)) as target_count, sum(coalesce("+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",0)) as "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_name";
        String targetSelectColumes ="sum(coalesce(target_count,0)) as target_count, target_name";
        String achvSelectColumes ="sum(coalesce("+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",0)) as "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_name";

        if(fromDate.isEmpty() && toDate.isEmpty()){
//            if(TextUtils.isEmpty(ssName)){
//                query = "select "+selectColumes+" from target_table "+getTargetConditionWithoutAnd()+groupBy;
//            }else{
//                query = "select "+selectColumes+" from target_table "+whereCluse+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'"+getTargetCondition()+groupBy;
//            }
            if(TextUtils.isEmpty(ssName)){
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)," +
                        "t2 as (SELECT "+targetSelectColumes+" from t1 "+getTargetConditionWithoutAnd()+groupBy+")," +
                        "t3 as (SELECT "+achvSelectColumes+" from t1 "+groupBy+")"+
                        "select t2.target_count,t3."+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",t2.target_name from t2 left join t3 on t2.target_name = t3.target_name ";

            }else{
                // query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)SELECT "+selectColumes+" from t1 "+whereCluse+getSSCondition(ssName)+getBetweenCondition(fromDate,toDate,"date")+getTargetCondition()+groupBy;
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)," +
                        "t2 as (SELECT "+targetSelectColumes+" from t1 "+getTargetConditionWithoutAnd()+groupBy+")," +
                        "t3 as (SELECT "+achvSelectColumes+" from t1 "+whereCluse+getSSConditionWithoutAnd(ssName)+groupBy+")"+
                        "select t2.target_count,t3."+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",t2.target_name from t2 left join t3 on t2.target_name = t3.target_name ";

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)," +
                        "t2 as (SELECT "+targetSelectColumes+" from t1 "+whereCluse+getBetweenCondition(fromDate,toDate,"date")+getTargetCondition()+groupBy+")," +
                        "t3 as (SELECT "+achvSelectColumes+" from t1 "+whereCluse+getBetweenCondition(fromDate,toDate,"date")+groupBy+")"+
                        "select t2.target_count,t3."+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",t2.target_name from t2 left join t3 on t2.target_name = t3.target_name ";

            }else{
               // query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)SELECT "+selectColumes+" from t1 "+whereCluse+getSSCondition(ssName)+getBetweenCondition(fromDate,toDate,"date")+getTargetCondition()+groupBy;
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, "+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+", target_count, target_name,"+TargetVsAchievementRepository.IS_MONTH_DATE+","+TargetVsAchievementRepository.START_DATE+" from target_table)," +
                        "t2 as (SELECT "+targetSelectColumes+" from t1 "+whereCluse+getBetweenCondition(fromDate,toDate,"date")+getTargetCondition()+groupBy+")," +
                        "t3 as (SELECT "+achvSelectColumes+" from t1 "+whereCluse+getSSCondition(ssName)+getBetweenCondition(fromDate,toDate,"date")+groupBy+")"+
                        "select t2.target_count,t3."+TargetVsAchievementRepository.ACHIEVEMNT_COUNT+",t2.target_name from t2 left join t3 on t2.target_name = t3.target_name ";

            }
        }
        Log.v("TARGET_VS_ACHIEV","query:"+query);
        //target query
        /*

with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date,ss_name, achievemnt_count, target_count, target_name,is_month_data,star_date from target_table),
t2 as (SELECT sum(coalesce(target_count,0)) as target_count,target_name from t1 where is_month_data = 1 and  date between '2021-11-01' and '2021-11-31' group by target_name),
 t3 as (SELECT sum(coalesce(achievemnt_count,0)) as achievemnt_count, target_name from t1  where  ss_name = 'Moni(SS-2)' and  date between '2021-11-01' and '2021-11-31' group by target_name)
select t2.target_count,t3.achievemnt_count,t2.target_name from t2 left join t3

         */

        Cursor cursor = null;
        try{
            // try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
                    dashBoardData1.setTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                    dashBoardData1.setAchievementCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT)));
                    //test
                    //dashBoardData1.setAchievementCount(3);
                    if(dashBoardData1.getTargetCount() != 0){
                        int percentage = (int) ((dashBoardData1.getAchievementCount() * 100)/dashBoardData1.getTargetCount());
                        dashBoardData1.setAchievementPercentage(percentage);
                    }

                    dashBoardData1.setEventType(cursor.getString(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_NAME)));
                    dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));
                    list.add(dashBoardData1);
                    cursor.moveToNext();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null) cursor.close();
        }


        return list;
    }
    public String getTargetCondition(){
        return "";//isMonthWise?" and "+TargetVsAchievementRepository.IS_MONTH_DATE +" = 1":" and "+TargetVsAchievementRepository.IS_MONTH_DATE +" = 0";
    }
    public String getTargetConditionWithoutAnd(){
        return "";//isMonthWise?" where "+TargetVsAchievementRepository.IS_MONTH_DATE +" = 1":" where "+TargetVsAchievementRepository.IS_MONTH_DATE +" = 0";
    }
    public String getSSCondition(String ssName){
        String ssCondition;
        ssCondition = " "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"' and ";
        return ssCondition;
    }
    public String getSSConditionWithoutAnd(String ssName){
        String ssCondition;
        ssCondition = " "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        return ssCondition;
    }
    public String getBetweenCondition(String fromMonth, String toMonth, String compareDate){
        StringBuilder build = new StringBuilder();
        if(TextUtils.isEmpty(fromMonth)){
            build.append(MessageFormat.format(" {0} = {1} ",compareDate,"'"+toMonth+"'"));
        }
        else {
            build.append(MessageFormat.format(" {0} between {1} and {2} ",compareDate,"'"+fromMonth+"'","'"+toMonth+"'"));
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
    public ArrayList<TargetVsAchievementData> getForumsTargetVsAchievement(List<String> visitTypes, String day, String month, String year, String ssName){
        if(visitTypes.size() == 0) return new ArrayList<>();
        ArrayList<TargetVsAchievementData> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i< visitTypes.size();i++){
            if(TextUtils.isEmpty(visitTypes.get(i)))continue;
            if(builder.toString().isEmpty()){
                builder.append(" target_name = '"+visitTypes.get(i)+"'");
            }else{
                builder.append(" OR ");
                builder.append(" target_name = '"+visitTypes.get(i)+"'");
            }
        }
        String whereCondition  = "("+builder.toString()+")";
        String groupBy = " group by target_name";
        //String query = "select sum(coalesce(achievemnt_count,0)) as achievemnt_count,target_name,(select sum(coalesce(target_count,0)) from target_table where "+ whereCondition+""+ getFilterWithAnd(day,month,year,"") +groupBy+") as target_count from target_table where "+ whereCondition+""+ getFilterWithAnd(day,month,year,ssName)+groupBy;
        String query ="with t1 as(select sum(coalesce(target_count,0)) as target_count,target_name from target_table where "+whereCondition+"" +getFilterWithAnd(day,month,year,"")+groupBy+"),"+
                "t2 as (select sum(coalesce(achievemnt_count,0)) as achievemnt_count,target_name from target_table " +
                "where "+whereCondition+" "+getFilterWithAnd(day,month,year,ssName)+groupBy+")"+
                "select t1.target_count,t2.achievemnt_count,t1.target_name from t2 left join t1 on t1.target_name = t2.target_name group by t1.target_name";
        Log.v("TARGET_QUERY","query:"+query);
       /*
       with t1 as(select sum(coalesce(target_count,0)) as target_count,target_name from target_table
where ( target_name = 'Adolescent Forum' OR  target_name = 'NCD Forum' OR  target_name = 'Child Forum' OR  target_name = 'WOMEN Forum' OR  target_name = 'ADULT Forum')
and month =11 and year =2021 group by target_name),
t2 as (select sum(coalesce(achievemnt_count,0)) as achievemnt_count,target_name from target_table
where ( target_name = 'Adolescent Forum' OR  target_name = 'NCD Forum' OR  target_name = 'Child Forum' OR  target_name = 'WOMEN Forum' OR  target_name = 'ADULT Forum')
and month =11 and year =2021 group by target_name)
select t1.target_count,t2.achievemnt_count,t1.target_name from t2 left join t1 on t1.target_name = t2.target_name group by t1.target_name
        */
        Cursor cursor = null;
        try{
            // try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();
                    dashBoardData1.setTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                    dashBoardData1.setAchievementCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT)));
                    dashBoardData1.setEventType(cursor.getString(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_NAME)));

                    Log.v("TARGET_QUERY",""+dashBoardData1.getTargetCount()+":"+dashBoardData1.getEventType());
                    if(dashBoardData1.getTargetCount() != 0){
                        int percentage = (int) ((dashBoardData1.getAchievementCount() * 100)/dashBoardData1.getTargetCount());
                        dashBoardData1.setAchievementPercentage(percentage);
                    }
                    dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));
                    list.add(dashBoardData1);
                    cursor.moveToNext();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor!=null) cursor.close();
        }


        return list;
    }

    public ArrayList<TargetVsAchievementData> getAvgTargetVsAchievmentByVisitType(List<String> visitTypes,String day, String month, String year, String ssName){
        if(visitTypes.size() == 0) return new ArrayList<>();
        ArrayList<TargetVsAchievementData> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i< visitTypes.size();i++){
            if(builder.toString().isEmpty()){
                builder.append(" target_name = '"+visitTypes.get(i)+"'");
            }else{
                builder.append(" OR ");
                builder.append(" target_name = '"+visitTypes.get(i)+"'");
            }
        }
        String whereCondition  = "("+builder.toString()+")";
        String groupBy = " group by target_name";
        //String query = "select sum(coalesce(achievemnt_count,0)) as achievemnt_count,target_name,(select sum(coalesce(target_count,0)) from target_table where "+ whereCondition+""+ getFilterWithAnd(day,month,year,"") +groupBy+") as target_count from target_table where "+ whereCondition+""+ getFilterWithAnd(day,month,year,ssName)+groupBy;
        String query ="with t1 as(select sum(coalesce(target_count,0)) as target_count,target_name from target_table where "+whereCondition+"" +getFilterWithAnd(day,month,year,"")+groupBy+"),"+
                "t2 as (select sum(coalesce(achievemnt_count,0)) as achievemnt_count,target_name from target_table " +
                "where "+whereCondition+" "+getFilterWithAnd(day,month,year,ssName)+groupBy+")"+
                "select t1.target_count,t2.achievemnt_count,t1.target_name from t2 left join t1 on t1.target_name = t2.target_name group by t1.target_name";
        Log.v("TARGET_QUERY","avg query:"+query);
        Cursor cursor = null;
         try {
             cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
             if (cursor != null && cursor.getCount() > 0) {
                 cursor.moveToFirst();
                 while (!cursor.isAfterLast()) {
                     TargetVsAchievementData dashBoardData1 = new TargetVsAchievementData();

                     dashBoardData1.setAvgTargetCount(cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_COUNT)));
                     int achCount = cursor.getInt(cursor.getColumnIndex(TargetVsAchievementRepository.ACHIEVEMNT_COUNT));
                     dashBoardData1.setAvgAchievmentCount(achCount);
                     dashBoardData1.setEventType(cursor.getString(cursor.getColumnIndex(TargetVsAchievementRepository.TARGET_NAME)));
                     Log.v("TARGET_QUERY","AVG:"+dashBoardData1.getAvgTargetCount()+":"+dashBoardData1.getEventType());

                     dashBoardData1.setTitle(HnppConstants.targetTypeMapping.get(dashBoardData1.getEventType()));
                     list.add(dashBoardData1);
                     cursor.moveToNext();
                 }

             }
         }catch (Exception e){
             e.printStackTrace();
         }finally {
             if(cursor !=null) cursor.close();
         }


        return list;
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
