package org.smartregister.brac.hnpp.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.joda.time.LocalDate;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.DashBoardContract;
import org.smartregister.brac.hnpp.utils.DashBoardData;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;

import java.util.ArrayList;
import java.util.Date;

public class CountSummeryDashBoardModel implements DashBoardContract.Model {
    private Context context;

    public CountSummeryDashBoardModel(Context context){
        this.context = context;
    }

    public ArrayList<DashBoardData> getHHCount(){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select count(*) as count from ec_family where date_removed is null ";
        Cursor cursor = null;
        // try {
        cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DashBoardData dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(cursor.getInt(0));
                dashBoardData1.setEventType(HnppConstants.EventType.FAMILY_REGISTRATION);
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
    public ArrayList<DashBoardData> getMemberCount(){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select count(*) as count from ec_family_member where date_removed is null";
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
    public ArrayList<DashBoardData> getGirlChildUnder5(){
        return getChildUnder5("F");
    }
    public ArrayList<DashBoardData> getBoyChildUnder5(){
        return getChildUnder5("M");
    }
    public ArrayList<DashBoardData> getMenUp50(){
        return getUp50("M");
    }
    public ArrayList<DashBoardData> getWoMenUp50(){
        return getUp50("F");
    }
    public ArrayList<DashBoardData> getGirlChild5To9(){
        return getChildAgeBased("F",5,9);
    }
    public ArrayList<DashBoardData> getBoyChild5To9(){
        return getChildAgeBased("M",5,9);
    }

    public ArrayList<DashBoardData> getGirlChild10To19(){
        return getChildAgeBased("F",10,19);
    }
    public ArrayList<DashBoardData> getBoyChild10To19(){
        return getChildAgeBased("M",10,19);
    }

    public ArrayList<DashBoardData> getGirlChild20To50(){
        return getChildAgeBased("F",20,50);
    }
    public ArrayList<DashBoardData> getBoyChild20To50(){
        return getChildAgeBased("M",20,50);
    }



    public ArrayList<DashBoardData> getChildUnder5(String gender){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and ((( julianday('now') - julianday('dob'))/365) <5 ";
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
    public ArrayList<DashBoardData> getUp50(String gender){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and ((( julianday('now') - julianday('dob'))/365) >50 ";
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
    public ArrayList<DashBoardData> getChildAgeBased(String gender,int startYear, int endYear){
        ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();
        String query = "select count(*) as count from ec_family_member where date_removed is null and gender = '"+gender+"' and ((( julianday('now') - julianday('dob'))/365) >="+startYear+" and  ((( julianday('now') - julianday('dob'))/365) <="+endYear;
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
