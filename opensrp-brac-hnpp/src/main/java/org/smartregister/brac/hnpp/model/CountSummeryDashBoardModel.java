package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.joda.time.LocalDate;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
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

    public DashBoardData getHHCount(String ssName){
        String query;

        DashBoardData dashBoardData1 = new DashBoardData();
        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family where date_removed is null ";
        }
        else{
            query = "select count(*) as count from ec_family where ss_name = '"+ssName+"' and date_removed is null ";
        }
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
    public DashBoardData getMemberCount(String ssName){
        DashBoardData  dashBoardData1 = new DashBoardData();

        String query;
        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null";
        }else {
           query = MessageFormat.format("select count(*) as count from {0} {1}", "ec_family_member", getFilterCondition(ssName));

        }
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dashBoardData1 = new DashBoardData();
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
    public DashBoardData getGirlChildUnder5(String ssName){
        return getChildUnder5("F",ssName,"মেয়ে শিশু ( ৫ বছরের কম)");
    }
    public DashBoardData getBoyChildUnder5(String ssName){
        return getChildUnder5("M",ssName,"ছেলে শিশু ( ৫ বছরের কম)");
    }
    public DashBoardData getMenUp50(String ssName){
        return getUp50("M",ssName,"৫০ উর্ধ পুরুষ");
    }
    public DashBoardData getWoMenUp50(String ssName){
        return getUp50("F",ssName,"৫০ উর্ধ মহিলা");
    }
    public DashBoardData getGirlChild5To9(String ssName){
        return getChildAgeBased("F",5,9,ssName,"৫-৯ বছরের মেয়ে শিশু");
    }
    public DashBoardData getBoyChild5To9(String ssName){
        return getChildAgeBased("M",5,9,ssName,"৫-৯ বছরের ছেলে শিশু");
    }

    public DashBoardData getGirlChild10To19(String ssName){
        return getChildAgeBased("F",10,19,ssName,"১০-১৯ বছরের মেয়ে");
    }
    public DashBoardData getBoyChild10To19(String ssName){
        return getChildAgeBased("M",10,19,ssName,"১০-১৯ বছরের ছেলে");
    }
    public DashBoardData getAdoGirl(String ssName){
        return getChildAgeBased("F",10,19,ssName,"কিশোরী");
    }
    public DashBoardData getAdoBoy(String ssName){
        return getChildAgeBased("M",10,19,ssName,"কিশোর");
    }
    public DashBoardData getAdoElco(String ssName){
        return getAdoElco("F",10,19,ssName,"কিশোরী সক্ষম দম্পতি");
    }

    public DashBoardData getGirlChild20To50(String ssName){
        return getChildAgeBased("F",20,50,ssName,"২০-৫০ বছরের মহিলা");
    }
    public DashBoardData getBoyChild20To50(String ssName){
        return getChildAgeBased("M",20,50,ssName,"২০-৫০ বছরের পুরুষ");
    }



    public DashBoardData getChildUnder5(String gender , String ssName, String title){
        String query;
        DashBoardData dashBoardData1 = new DashBoardData();

        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) <5 ";
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1} and gender = {2} and {3}", "ec_family_member", getFilterCondition(ssName),"'"+gender+"'","(( julianday('now') - julianday(dob))/365) <5");
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
    public DashBoardData getUp50(String gender, String ssName, String title){
        DashBoardData dashBoardData1 = new DashBoardData();
        String query;
        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >50 ";
        } else {
            query = MessageFormat.format("select count(*) as count from {0} {1} and gender = {2} and {3}", "ec_family_member", getFilterCondition(ssName),"'"+gender+"'","(( julianday('now') - julianday(dob))/365) >50");
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
    public DashBoardData getChildAgeBased(String gender,int startYear, int endYear , String ssName, String title){
        String query;
        DashBoardData dashBoardData1 = new DashBoardData();

        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear;
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1} and gender = {2} and {3} and  {4} ","ec_family_member", getFilterCondition(ssName),"'"+gender+"'","(( julianday('now') - julianday(dob))/365) >="+startYear,"(( julianday('now') - julianday(dob))/365) <="+endYear+"");
        }
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
                    dashBoardData1.setImageSource((int) HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                }catch (Exception e){

                }
                cursor.moveToNext();
            }
            cursor.close();

        }


        return dashBoardData1;
    }
    public DashBoardData getAdoElco(String gender,int startYear, int endYear , String ssName, String title){
        String query;
        DashBoardData   dashBoardData1 = new DashBoardData();

        if(TextUtils.isEmpty(ssName)){
            query = "select count(*) as count from ec_family_member where date_removed is null and marital_status = 'Married' and gender = '"+gender+"' and (( julianday('now') - julianday(dob))/365) >="+startYear+" and  (( julianday('now') - julianday(dob))/365) <="+endYear;
        }else{
            query = MessageFormat.format("select count(*) as count from {0} {1} and marital_status = {2} and gender = {3} and {4} and  {5} ","ec_family_member",
                    getFilterCondition(ssName),"'Married'","'"+gender+"'","(( julianday('now') - julianday(dob))/365) >="+startYear+"","(( julianday('now') - julianday(dob))/365) <="+endYear+"");
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

    public String getFilterCondition(String ssName){
        String mainCondition =
                MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY) +
                MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                        CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID) +
                MessageFormat.format(" where {0}.{1} is null and {2} = {3}", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED, HnppConstants.KEY.SS_NAME,"'"+ssName+"'");

        return mainCondition;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
