package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.brac.hnpp.contract.DashBoardContract;
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

    public DashBoardData getHHCount(String ssName, String month){
        String query;

        DashBoardData  dashBoardData1 = new DashBoardData();
        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family where date_removed is null ";
        }else if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and date_removed is null ";

        }
        else if(!TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and date_removed is null ";
        }else {
            query = "select count(*) as count from ec_family where strftime('%m', datetime("+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime')) = '"+month+"' and date_removed is null ";
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
    public DashBoardData getMemberCount(String ssName, String month){
        DashBoardData dashBoardData1 = new DashBoardData();

        String query;
        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = "select count(*) as count from ec_family_member where date_removed is null";
        }else {
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getFilterCondition(ssName,month));

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

    public DashBoardData getANCRegisterCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC_REGISTRATION,ssName,month);
//        DashBoardData dashBoardData1 = new DashBoardData();
//        String mainCondition = MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER) +
//                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
//                        CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID) +
//                MessageFormat.format(" inner join {0} ", "ec_anc_log") +
//                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
//                        "ec_anc_log", DBConstants.KEY.BASE_ENTITY_ID) +
//                MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY) +
//                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
//                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID) +
//                MessageFormat.format(" where {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED) +
//                MessageFormat.format(" and {0}.{1} is 0 ", CoreConstants.TABLE_NAME.ANC_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.IS_CLOSED);
//        String query;
//        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
//            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_anc_register", mainCondition);
//
//        }else{
//            query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_anc_register", mainCondition,getAncFilterCondition(ssName,month));
//        }
//        Cursor cursor = null;
//        // try {
//        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//        if(cursor !=null && cursor.getCount() > 0){
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//
//                dashBoardData1.setCount(cursor.getInt(0));
//                dashBoardData1.setEventType(HnppConstants.EventType.ANC_REGISTRATION);
//                dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));
//
//                try{
//                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
//                }catch (Exception e){
//
//                }
//                cursor.moveToNext();
//            }
//            cursor.close();
//
//        }
//
//
//        return dashBoardData1;
    }

    public DashBoardData getAnc1Count(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION,ssName,month);
    }
    public DashBoardData getAnc2Count(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION,ssName,month);
    }
    public DashBoardData getAnc3Count(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION,ssName,month);
    }
    public DashBoardData getAncCount(String ssName, String month){
        return getVisitTypeCount("ANC",ssName,month);
    }
    public DashBoardData getDeliveryCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME,ssName,month);
//        DashBoardData dashBoardData1 = new DashBoardData();
//        StringBuilder build = new StringBuilder();
//        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
//        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
//                CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, DBConstants.KEY.BASE_ENTITY_ID));
//
//        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
//
//        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
//                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
//
//        build.append(MessageFormat.format(" where {0}.{1} is not null AND {0}.{2} is 0 ", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, ChwDBConstants.DELIVERY_DATE, ChwDBConstants.IS_CLOSED));
//        build.append(MessageFormat.format(" and {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED));
//
//        String mainCondition = build.toString();
//        String query;
//        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
//            query = MessageFormat.format("select count(*) as count from {0} {1}", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, mainCondition);
//
//        }else{
//            query = MessageFormat.format("select count(*) as count from {0} {1} {2}", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, mainCondition,getDeliveryFilterCondition(ssName,month));
//        }
//        Cursor cursor = null;
//        // try {
//        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
//        if(cursor !=null && cursor.getCount() > 0){
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()) {
//
//                dashBoardData1.setCount(cursor.getInt(0));
//                dashBoardData1.setEventType(HnppConstants.EventType.PREGNANCY_OUTCOME);
//                dashBoardData1.setTitle(HnppConstants.workSummeryTypeMapping.get(dashBoardData1.getEventType()));
//
//                try{
//                    dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
//                }catch (Exception e){
//
//                }
//                cursor.moveToNext();
//            }
//            cursor.close();
//
//        }
//
//
//        return dashBoardData1;
    }
    public DashBoardData getPncCount(String ssName, String month){
        return getVisitTypeCount("pnc",ssName,month);
    }
    public DashBoardData getEncCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.ENC_REGISTRATION,ssName,month);
    }
    public DashBoardData getChildFollowUpCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.CHILD_FOLLOWUP,ssName,month);
    }
    public DashBoardData getNcdForumCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_NCD,ssName,month);
    }
    public DashBoardData getNcdServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.NCD_PACKAGE,ssName,month);
    }
    public DashBoardData getWomenForumCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_WOMEN,ssName,month);
    }
    public DashBoardData getWomenServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.WOMEN_PACKAGE,ssName,month);
    }
    public DashBoardData getAdoForumCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_ADO,ssName,month);
    }
    public DashBoardData getAdoServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.GIRL_PACKAGE,ssName,month);
    }
    public DashBoardData getChildForumCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_CHILD,ssName,month);
    }
    public DashBoardData getChildServiceCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.IYCF_PACKAGE,ssName,month);
    }
    public DashBoardData getAdultForumCount(String ssName, String month){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.FORUM_ADULT,ssName,month);
    }

    public DashBoardData getVisitTypeCount(String visitType, String ssName, String month){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition;
        if(visitType.equalsIgnoreCase("ANC")){
            mainCondition = "where visit_type = '"+ HnppConstants.EVENT_TYPE.ANC1_REGISTRATION+"' or visit_type = '"+HnppConstants.EVENT_TYPE.ANC2_REGISTRATION+"'" +
                    " or visit_type = '"+ HnppConstants.EVENT_TYPE.ANC3_REGISTRATION+"' or visit_type = '"+ HnppConstants.EventType.ANC_HOME_VISIT+"'";
        }else if(visitType.equalsIgnoreCase("pnc")){
            mainCondition = "where visit_type = '"+ HnppConstants.EVENT_TYPE.PNC_REGISTRATION+"' or visit_type = '"+HnppConstants.EventType.PNC_HOME_VISIT+"'" ;

        }else{
            mainCondition= " where visit_type ='"+visitType+"'";
        }
        String query;
        if(TextUtils.isEmpty(ssName) && TextUtils.isEmpty(month)){
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", getVisitFilterCondition(ssName,month,mainCondition));

        }
        Log.v("WORK_SUMMERY","visit_type:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(visitType);
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
    public String getFilterCondition(String ssName, String month){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
    }
    public String getAncFilterCondition(String ssName, String month){
        StringBuilder build = new StringBuilder();

        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.ANC_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.ANC_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
    }
    public String getDeliveryFilterCondition(String ssName, String month){
        StringBuilder build = new StringBuilder();

        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2}  = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,"strftime('%m', datetime("+CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME+"."+DBConstants.KEY.LAST_INTERACTED_WITH+"/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
    }
    public String getVisitFilterCondition(String ssName, String month,String mainCondition){
        StringBuilder build = new StringBuilder();
//        if(!TextUtils.isEmpty(ssName)){
//            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
//            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
//                    "visits", DBConstants.KEY.BASE_ENTITY_ID));
//            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
//            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
//                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
//        }

        build.append(mainCondition);
        if(!TextUtils.isEmpty(ssName) && !TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(month)){
            build.append(MessageFormat.format(" and {0} = {1} ", "strftime('%m', datetime(ec_visit_log.visit_date/1000,'unixepoch','localtime'))" ,"'"+month+"'"));

        }
        else if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" and {0} = {1} ", HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));

        }

        return build.toString();
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
