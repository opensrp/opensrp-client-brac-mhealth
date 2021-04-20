package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.repository.StockRepository;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

import java.text.MessageFormat;

public class WorkSummeryDashBoardModel implements DashBoardContract.Model {

    private Context context;

    public WorkSummeryDashBoardModel(Context context){
        this.context = context;
    }

    public DashBoardData getHHCount(String ssName, long fromMonth, long toMonth){
        String query = null, compareDate = DBConstants.KEY.LAST_INTERACTED_WITH;

        DashBoardData  dashBoardData1 = new DashBoardData();
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null", "ec_family");
            }else{
                query = MessageFormat.format("select count(*) as count from {0}  where date_removed is null {1}", "ec_family",getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null {1}", "ec_family",getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null {1} {2}", "ec_family",getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }
        Log.v("WORD_QUERY","log:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_REGISTRATION);
                dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    public DashBoardData getMemberCount(String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();

        String query;

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is null";
            }else{
                query = MessageFormat.format("select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is null {8}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName));
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1}", "ec_family_member", getFilterCondition(ssName,fromMonth,toMonth));
        }
        Log.v("WORK_QUERY","member:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
                dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public String getFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));
        }
        return build.toString();
    }
    public String getBetweenMemberCondition(long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} = {1} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }

    //for PA from to month
    public DashBoardData getEyeTestCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.EYE_TEST,ssName,fromMonth,toMonth);
    }
    public DashBoardData getBloodGroupingCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.BLOOD_GROUP,ssName,fromMonth,toMonth);
    }
    public DashBoardData getTotalGlassCount(long fromDate, long toDate){
        String query;
        if(fromDate == -1 && toDate == -1){
            query =  "select sum(coalesce("+StockRepository.ACHIEVEMNT_COUNT+",0)) as acount from "+StockRepository.STOCK_TABLE+" "+getGlassNames();

        }
        else{
                query = "with t1 as (SELECT year||'-'||printf('%02d',month)||'-'||printf('%02d',day) as date, achievemnt_count from stock_table)SELECT sum(coalesce(achievemnt_count,0)) as achievemnt_count  from t1 "+getGlassNames()+" "+getBetweenCondition(fromDate,toDate,"date");

        }
        DashBoardData dashBoardData1 = new DashBoardData();
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EVENT_TYPE.GLASS);
                dashBoardData1.setTitle("চশমা বিক্রি");
                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }
        return dashBoardData1;
    }

    private String getGlassNames() {
        return
       " where ("
                +StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_1_5+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_2_5+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SV_3+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_1_5+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_2_5+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.BF_3+"' or "+
                StockRepository.STOCK_PRODUCT_NAME+" = '"+HnppConstants.EVENT_TYPE.SUN_GLASS+"')";
    }
    // for from to month

    public DashBoardData getANCRegisterCount(String ssName,long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC_REGISTRATION,ssName,fromMonth,toMonth);
    }
    public DashBoardData getFirstTrimsterRegisterCount(String ssName, long fromMonth, long toMonth){
        return getANcTrimesterCount("প্রথম ট্রাইসেমিস্টার-এ সনাক্ত",ssName,fromMonth,toMonth,1,84);
    }
    public DashBoardData getSecondTrimsterRegisterCount(String ssName, long fromMonth, long toMonth){
        return getANcTrimesterCount("দ্বিতীয় ট্রাইসেমিস্টার-এ সনাক্ত",ssName,fromMonth,toMonth,85,168);
    }
    public DashBoardData getThirdTrimsterRegisterCount(String ssName, long fromMonth, long toMonth){
        return getANcTrimesterCount("তৃতীয় ট্রাইসেমিস্টার-এ সনাক্ত",ssName,fromMonth,toMonth,169,0);
    }
    public DashBoardData getAnc1Count(String ssName,long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION,ssName,fromMonth,toMonth);
    }
    public DashBoardData getHHVisitCount(String ssName,long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAnc2Count(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAnc3Count(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION,ssName,fromMonth,toMonth);
    }
    public DashBoardData getElcoCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ELCO,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAncCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("ANC",ssName,fromMonth,toMonth);
    }
    public DashBoardData getDeliveryCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,ssName,fromMonth,toMonth);
    }
    public DashBoardData getPncCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount("pnc",ssName,fromMonth,toMonth);
    }
    public DashBoardData getEncCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ENC_REGISTRATION,ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildFollowUpCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP,ssName,fromMonth,toMonth);
    }
    public DashBoardData getNcdForumCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_NCD,ssName,fromMonth,toMonth);
    }
    public DashBoardData getNcdServiceCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.NCD_PACKAGE,ssName,fromMonth,toMonth);
    }
    public DashBoardData getWomenForumCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_WOMEN,ssName,fromMonth,toMonth);
    }
    public DashBoardData getWomenServiceCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAdoForumCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_ADO,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAdoServiceCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildForumCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_CHILD,ssName,fromMonth,toMonth);
    }
    public DashBoardData getChildServiceCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,ssName,fromMonth,toMonth);
    }
    public DashBoardData getAdultForumCount(String ssName, long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_ADULT,ssName,fromMonth,toMonth);
    }

    public DashBoardData getVisitTypeCount(String visitType, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition = "", ssCondition;
        if(visitType.equalsIgnoreCase("ANC")){
            mainCondition = "where (event_type = '"+ HnppConstants.EVENT_TYPE.ANC1_REGISTRATION+"' or event_type ='"+ HnppConstants.EVENT_TYPE.ANC2_REGISTRATION+"' or event_type ='"+ HnppConstants.EVENT_TYPE.ANC3_REGISTRATION+"')";
        }else if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION)){
            mainCondition = "where event_type = '"+ HnppConstants.EVENT_TYPE.ANC1_REGISTRATION+"'";
        }
        else if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION)){
            mainCondition = "where event_type = '"+ HnppConstants.EVENT_TYPE.ANC2_REGISTRATION+"'" ;
        }
        else if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION)){
            mainCondition = "where event_type = '"+ HnppConstants.EVENT_TYPE.ANC3_REGISTRATION+"'" ;
        }
        else if(visitType.equalsIgnoreCase("pnc")){
            mainCondition = "where visit_type = '"+ HnppConstants.EVENT_TYPE.PNC_REGISTRATION+"' or visit_type = '"+HnppConstants.EventType.PNC_HOME_VISIT+"'" ;

        }else if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY)){

           if(!TextUtils.isEmpty(ssName)){
               mainCondition = "inner join ec_family on ec_family.base_entity_id = ec_visit_log.family_id";
           }

            mainCondition += " where visit_type ='"+visitType+"'";
        }
        else{
            mainCondition= " where visit_type ='"+visitType+"'";
        }
        String query = null, compareDate = "visit_date";
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition,visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY)?getSSCondition(ssName,"ec_family"):getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition, getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2} {3}", "ec_visit_log", mainCondition,visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY)?getSSCondition(ssName,"ec_family"):getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }


        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(visitType);
                if(visitType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NCD_PACKAGE) && HnppConstants.isPALogin()){
                    dashBoardData1.setTitle("অ্যাডাল্ট প্যাকেজ");
                }else{
                    dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));

                }

                try{
                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public String getSSCondition(String ssName){
        return getSSCondition(ssName,"");
    }
    public String getSSCondition(String ssName,String tableName){
        if(TextUtils.isEmpty(tableName)){
            return " and "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        }else{
           return  " and "+tableName+"."+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        }
    }
    public String getBetweenCondition(long fromMonth, long toMonth, String compareDate){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} <= {1} ",compareDate,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",compareDate,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }

    public DashBoardData getANcTrimesterCount(String title,String ssName, long fromMonth, long toMonth,int startDate,int endDate){
        String compareDate = "ec_anc_register."+DBConstants.KEY.LAST_INTERACTED_WITH;
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition = "";

        StringBuilder build = new StringBuilder();
        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.RELATIONAL_ID));

            build.append(mainCondition);
        }
        if(endDate==0){

            mainCondition   = " where dayPass>="+startDate;
            build.append(mainCondition);

        }else{

            mainCondition = " where dayPass>="+startDate+" and dayPass<="+endDate+"";
            build.append(mainCondition);

        }

        String query;
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count,{2} from {0} {1}", "ec_anc_register", build.toString(),getDateFormate());
            }else{
                query = MessageFormat.format("select count(*) as count,{3} from {0} {1} {2}", "ec_anc_register", build.toString(),getSSCondition(ssName),getDateFormate());

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count,{3} from {0} {1} {2}", "ec_anc_register", build.toString(), getBetweenCondition(fromMonth,toMonth,compareDate),getDateFormate());
            }else{
                query = MessageFormat.format("select count(*) as count,{4} from {0} {1} {2} {3}", "ec_anc_register", build.toString(),getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate),getDateFormate());

            }
        }


//        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
//            query = MessageFormat.format("select count(*) as count,{2} from {0} {1}", "ec_anc_register", mainCondition,getDateFormate());
//        }else{
//            query = MessageFormat.format("select count(*) as count,{2} from {0} {1}", "ec_anc_register", getTrimesterFilterCondition(ssName,month,year,mainCondition),getDateFormate());
//
//        }
        Log.v("ANC_TRIMESTER","anc_trimester:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle(title);
                dashBoardData1.setImageSource(R.mipmap.ic_anc_pink);

                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    private String getDateFormate() {
        return "(julianday() - julianday(substr(last_menstrual_period, 7, 4)||'-'||substr(last_menstrual_period, 4, 2)||'-'||substr(last_menstrual_period, 1, 2))) as dayPass";
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
