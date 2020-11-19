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
import java.util.ArrayList;

public class WorkSummeryDashBoardModel implements DashBoardContract.Model {

    private Context context;

    public WorkSummeryDashBoardModel(Context context){
        this.context = context;
    }

    public DashBoardData getHHCount(String ssName, String month){
        String query;

        DashBoardData dashBoardData1 = null;
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
                dashBoardData1.setTitle(HnppConstants.eventTypeMapping.get(dashBoardData1.getEventType()));

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
        DashBoardData dashBoardData1 = null;

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
                dashBoardData1.setTitle(HnppConstants.eventTypeMapping.get(dashBoardData1.getEventType()));

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
    public ArrayList<DashBoardData> getANCRegisterCount(){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String mainCondition = MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER) +
                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID) +
                MessageFormat.format(" inner join {0} ", "ec_anc_log") +
                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                        "ec_anc_log", DBConstants.KEY.BASE_ENTITY_ID) +
                MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY) +
                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID) +
                MessageFormat.format(" where {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED) +
                MessageFormat.format(" and {0}.{1} is 0 ", CoreConstants.TABLE_NAME.ANC_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.IS_CLOSED);
        String query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_anc_register", mainCondition);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DashBoardData dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
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

    public ArrayList<DashBoardData> getAnc1Count(){
        return getANCVisitTypeCount(HnppConstants.EVENT_TYPE.ANC1_REGISTRATION);
    }
    public ArrayList<DashBoardData> getAnc2Count(){
        return getANCVisitTypeCount(HnppConstants.EVENT_TYPE.ANC2_REGISTRATION);
    }
    public ArrayList<DashBoardData> getAnc3Count(){
        return getANCVisitTypeCount(HnppConstants.EVENT_TYPE.ANC3_REGISTRATION);
    }
    public ArrayList<DashBoardData> getAncCount(){
        return getANCVisitTypeCount("ANC");
    }
    public ArrayList<DashBoardData> getDeliveryCount(){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, DBConstants.KEY.BASE_ENTITY_ID));

        build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));

        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

        build.append(MessageFormat.format(" where {0}.{1} is not null AND {0}.{2} is 0 ", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, ChwDBConstants.DELIVERY_DATE, ChwDBConstants.IS_CLOSED));
        build.append(MessageFormat.format(" and {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED));

        String mainCondition = build.toString();
        String query = MessageFormat.format("select count(*) as count from {0} {1}", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, mainCondition);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DashBoardData dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
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
    public ArrayList<DashBoardData> getPncCount(){
        return getANCVisitTypeCount("pnc");
    }

    public ArrayList<DashBoardData> getANCVisitTypeCount( String visitType){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String mainCondition;
        if(visitType.equalsIgnoreCase("ANC")){
            mainCondition = "where visit_type = '"+ HnppConstants.EVENT_TYPE.ANC1_REGISTRATION+"' or visit_type = '"+HnppConstants.EVENT_TYPE.ANC2_REGISTRATION+"'" +
                    " or visit_type = '"+ HnppConstants.EVENT_TYPE.ANC3_REGISTRATION+"' or visit_type = '"+ HnppConstants.EventType.ANC_HOME_VISIT+"'";
        }else if(visitType.equalsIgnoreCase("pnc")){
            mainCondition = "where visit_type = '"+ HnppConstants.EVENT_TYPE.PNC_REGISTRATION+"' or visit_type = '"+HnppConstants.EventType.PNC_HOME_VISIT+"'" ;

        }else{
            mainCondition= " where visit_type ='"+visitType+"'";
        }

         String query = MessageFormat.format("select count(*) as count from {0} {1}", "visits", mainCondition);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DashBoardData dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
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
