package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.LocalDate;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.utils.ChildDBConstants;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

public class CountSummeryDashBoardModel implements DashBoardContract.Model {
    private Context context;

    public CountSummeryDashBoardModel(Context context){
        this.context = context;
    }
    public String getFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        if(!TextUtils.isEmpty(ssName)) {
            build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.FAMILY));
            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));
        }

        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenMemberCondition(fromMonth,toMonth)));
        }
        return build.toString();
    }
    public String getRiskFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.ANC_MEMBER));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID));

        build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenEDDCondition(fromMonth,toMonth)));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenEDDCondition(fromMonth,toMonth)));
        }
        return build.toString();
    }
    public String getEddFilterCondition(String ssName, long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID));

        build.append(MessageFormat.format(" inner join {0}", CoreConstants.TABLE_NAME.FAMILY));
        build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

        if(!TextUtils.isEmpty(ssName)){
            build.append(MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'"));
            build.append(MessageFormat.format(" and {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenEDDCondition(fromMonth,toMonth)));

        }
        else{
            build.append(MessageFormat.format(" where {0}.{1} is null {2}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getBetweenEDDCondition(fromMonth,toMonth)));
        }
        return build.toString();
    }
    public String getBetweenMemberCondition(long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} <= {1} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",CoreConstants.TABLE_NAME.FAMILY_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }
    public String getBetweenEDDCondition(long fromMonth, long toMonth){
        StringBuilder build = new StringBuilder();
        if(fromMonth == -1){
            build.append(MessageFormat.format(" and {0} <= {1} ",CoreConstants.TABLE_NAME.ANC_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,"'"+Long.toString(toMonth)+"'"));
        }
        else {
            build.append(MessageFormat.format(" and {0} between {1} and {2} ",CoreConstants.TABLE_NAME.ANC_MEMBER+"."+DBConstants.KEY.LAST_INTERACTED_WITH,Long.toString(fromMonth),Long.toString(toMonth)));
        }
        return build.toString();
    }
    public String getSSCondition(String ssName){
        String ssCondition;
        ssCondition = " and "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        return ssCondition;
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

    public DashBoardData getOOCCount(String ssName,long fromMonth, long toMonth){
        String query,compareDate = DBConstants.KEY.LAST_INTERACTED_WITH;
        DashBoardData dashBoardData1 = new DashBoardData();
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null", "ec_guest_member");
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null  {1} ", "ec_guest_member",getSSCondition(ssName));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null {1}", "ec_guest_member",getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date_removed is null {1} {2}", "ec_guest_member",getSSCondition(ssName),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EVENT_TYPE.GUEST_MEMBER_REGISTRATION);
                dashBoardData1.setTitle(context.getString(R.string.guest_member));

                try{
                    dashBoardData1.setImageSource(R.drawable.rowavatar_member);
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }

    public DashBoardData getHHCount(String ssName,long fromMonth, long toMonth){
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
        Log.v("COUNT_QUERY","getHHCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_REGISTRATION);
                dashBoardData1.setTitle(HnppConstants.countSummeryTypeMapping.get(dashBoardData1.getEventType()));

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
    public DashBoardData getMemberCount(String ssName,long fromMonth, long toMonth){
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
        Log.v("DASHBOARD","getMemberCount:"+query);

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
                dashBoardData1.setTitle(HnppConstants.countSummeryTypeMapping.get(dashBoardData1.getEventType()));

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
    public DashBoardData getHHVisitCount(String ssName,long fromMonth, long toMonth){
        return getVisitTypeCount(HnppConstants.EVENT_TYPE.HOME_VISIT_FAMILY,ssName,fromMonth,toMonth);
    }
    public DashBoardData getVisitTypeCount(String visitType, String ssName, long fromMonth, long toMonth){
        DashBoardData dashBoardData1 = new DashBoardData();
        String mainCondition = "";

        if(!TextUtils.isEmpty(ssName)){
            mainCondition = "inner join ec_family on ec_family.base_entity_id = ec_visit_log.family_id";
        }

        mainCondition += " where visit_type ='"+visitType+"'";
        String query = null, compareDate = "visit_date";
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_visit_log", mainCondition);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition,getSSConditionWithTable(ssName,"ec_family"));

            }
        }
        else{
            if(TextUtils.isEmpty(ssName)){
                query = MessageFormat.format("select count(*) as count from {0} {1} {2}", "ec_visit_log", mainCondition, getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} {1} {2} {3}", "ec_visit_log", mainCondition,getSSConditionWithTable(ssName,"ec_family"),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }
            Log.v("HOMEVISIT","visit_type:"+query);


        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(visitType);
                dashBoardData1.setTitle(HnppConstants.countSummeryTypeMapping.get(dashBoardData1.getEventType()));

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
    public String getSSConditionWithTable(String ssName,String tableName){
        if(TextUtils.isEmpty(tableName)){
            return " and "+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        }else{
            return  " and "+tableName+"."+HnppConstants.KEY.SS_NAME+" = '"+ssName+"'";
        }
    }

    public DashBoardData getSimprintsCount(String ssName,long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();

        String query;

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is null and gu_id IS NOT NULL and gu_id !='test'";
            }else{
                query = MessageFormat.format("select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is null {8}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName));
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1} {2}", "ec_family_member", getFilterCondition(ssName,fromMonth,toMonth)," and gu_id IS NOT NULL and gu_id !='test'");
        }
        Log.v("COUNT_QUERY","count q:"+query);


        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
                dashBoardData1.setTitle("ফিঙ্গার প্রিন্ট দ্বারা নিবন্ধিত");
                dashBoardData1.setImageSource(R.drawable.ic_fingerprint_id);
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public DashBoardData getGirlChildUnder5(String ssName,long fromMonth, long toMonth){
        return getChildUnder5("F",ssName,fromMonth,toMonth,"মেয়ে শিশু ( ৫ বছরের কম)");
    }
    public DashBoardData getBoyChildUnder5(String ssName,long fromMonth, long toMonth){
        return getChildUnder5("M",ssName,fromMonth,toMonth,"ছেলে শিশু ( ৫ বছরের কম)");
    }
    public DashBoardData getMenUp50(String ssName,long fromMonth, long toMonth){
        return getUp50("M",ssName,fromMonth,toMonth,"৫০ উর্ধ পুরুষ");
    }
    public DashBoardData getWoMenUp50(String ssName,long fromMonth, long toMonth){
        return getUp50("F",ssName,fromMonth,toMonth,"৫০ উর্ধ মহিলা");
    }
    public DashBoardData getGirlChild5To9(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("F",5,9,ssName,fromMonth,toMonth,"৫-৯ বছরের মেয়ে শিশু");
    }
    public DashBoardData getBoyChild5To9(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("M",5,9,ssName,fromMonth,toMonth,"৫-৯ বছরের ছেলে শিশু");
    }

    public DashBoardData getGirlChild10To19(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("F",10,19,ssName,fromMonth,toMonth,"১০-১৯ বছরের মেয়ে");
    }
    public DashBoardData getBoyChild10To19(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("M",10,19,ssName,fromMonth,toMonth,"১০-১৯ বছরের ছেলে");
    }
    public DashBoardData getAdoGirl(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("F",10,19,ssName,fromMonth,toMonth,"কিশোরী");
    }
    public DashBoardData getAdoBoy(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("M",10,19,ssName,fromMonth,toMonth,"কিশোর");
    }
    public DashBoardData getAdoElco(String ssName,long fromMonth, long toMonth){
        return getAdoElco("F",10,19,ssName,fromMonth,toMonth,"কিশোরী সক্ষম দম্পতি");
    }

    public DashBoardData getGirlChild20To50(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("F",20,50,ssName,fromMonth,toMonth,"২০-৫০ বছরের মহিলা");
    }
    public DashBoardData getBoyChild20To50(String ssName,long fromMonth, long toMonth){
        return getChildAgeBased("M",20,50,ssName,fromMonth,toMonth,"২০-৫০ বছরের পুরুষ");
    }
    public DashBoardData getEddThisMonth(String ssName,long fromMonth, long toMonth){
        String query = getEddQuery(ssName,fromMonth,toMonth);
        Log.v("EDD_QUERY","getHHCount:"+query);
        DashBoardData dashBoardData1 = new DashBoardData();
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle("প্রসবের সম্ভাব্য তারিখ এই মাসে");
                if(fromMonth ==-1 || toMonth == -1){
                    dashBoardData1.setTitle("সম্ভাব্য প্রসব");
                }
                dashBoardData1.setImageSource(R.mipmap.ic_anc_pink);

                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public DashBoardData getRiskMother(String ssName,long fromMonth, long toMonth){
        String query = getRiskyQuery(ssName,fromMonth,toMonth);
        Log.v("EDD_QUERY","getHHCount:"+query);
        DashBoardData dashBoardData1 = new DashBoardData();
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setTitle("ঝুঁকিপূর্ণ মা");
                dashBoardData1.setImageSource(R.mipmap.ic_anc_pink);

                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    private String getRiskyQuery(String ssName,long fromMonth, long toMonth){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_anc_register  " +
                        "inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id " +
                        "inner join ec_family on ec_family.base_entity_id = ec_family_member."+DBConstants.KEY.RELATIONAL_ID +
                        " where ec_family_member.date_removed is null "+ChildDBConstants.riskAncPatient();

            }else{
                query = "select count(*) as count from ec_anc_register  " +
                        "inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id " +
                        "inner join ec_family on ec_family.base_entity_id = ec_family_member."+DBConstants.KEY.RELATIONAL_ID +
                        " where ec_family.ss_name='"+ssName+"'"+ChildDBConstants.riskAncPatient();
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1} {2}", "ec_family_member", getRiskFilterCondition(ssName,fromMonth,toMonth),ChildDBConstants.riskAncPatient());
        }


        return query;
    }
    private String getEddQuery(String ssName,long fromMonth, long toMonth){
        String query;
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_anc_register  " +
                        "inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id " +
                        "inner join ec_family on ec_family.base_entity_id = ec_family_member."+DBConstants.KEY.RELATIONAL_ID +
                        " where ec_family_member.date_removed is null ";

            }else{
                query = "select count(*) as count from ec_anc_register  " +
                        "inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id " +
                        "inner join ec_family on ec_family.base_entity_id = ec_family_member."+DBConstants.KEY.RELATIONAL_ID +
                        " where ec_family.ss_name='"+ssName+"'";
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1}", "ec_anc_register", getEddFilterCondition(ssName,fromMonth,toMonth));
        }


        return query;
    }


    public DashBoardData getChildUnder5(String gender , String ssName,long fromMonth, long toMonth, String title){
        String query;
        DashBoardData dashBoardData1 = new DashBoardData();

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) <5";
            }else{
                query = MessageFormat.format("select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is null {8} and {9}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) <5");
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1} and {2}", "ec_family_member", getFilterCondition(ssName,fromMonth,toMonth),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) <5");
        }

        Log.v("Child_QUERY","title:"+query);
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.CHILD_REGISTRATION);
                dashBoardData1.setTitle(title);

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
    public DashBoardData getUp50(String gender,String ssName,long fromMonth, long toMonth, String title){
        DashBoardData dashBoardData1 = new DashBoardData();
        String query;


        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >50";
            }else{
                query = MessageFormat.format("select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is null {8} and {9}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365)>50");
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1} and {2}", "ec_family_member", getFilterCondition(ssName,fromMonth,toMonth),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >50");
        }

        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_MEMBER_REGISTRATION);
                dashBoardData1.setTitle(title);

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
    public DashBoardData getChildAgeBased(String gender,int startYear, int endYear , String ssName,long fromMonth, long toMonth, String title){
        String query;
        DashBoardData dashBoardData1 = new DashBoardData();

        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(ssName)){
                query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear;
            }else{
                query = MessageFormat.format("select count(*) as count from {0} inner join {1} on {2}.{3} = {4}.{5} where {6}.{7} is null {8} and {9}",
                        "ec_family_member",
                        CoreConstants.TABLE_NAME.FAMILY,CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED,getSSCondition(ssName),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear);
            }
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0}{1} and {2}", "ec_family_member", getFilterCondition(ssName,fromMonth,toMonth),"gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear);
        }
        Log.v("COUNT_QUERY","count:"+query);
        Cursor cursor;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.CHILD_REGISTRATION);
                dashBoardData1.setTitle(title);

                try{
                    dashBoardData1.setImageSource((int) HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public DashBoardData getAdoElco(String gender,int startYear, int endYear , String ssName,long fromMonth, long toMonth, String title){
        String query;
        DashBoardData   dashBoardData1 = new DashBoardData();

        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null and marital_status = 'Married' and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear;
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1} and marital_status = {2} and gender = {3} and {4} and  {5} ","ec_family_member",
                    getFilterCondition(ssName,fromMonth,toMonth),"'Married'","'"+gender+"'","(( julianday('now') - julianday(dob))/365) >="+startYear+"","(( julianday('now') - julianday(dob))/365) <="+endYear+"");
        }
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EVENT_TYPE.ELCO);
                dashBoardData1.setTitle(title);

                try{
                    dashBoardData1.setImageSource((int) HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

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
