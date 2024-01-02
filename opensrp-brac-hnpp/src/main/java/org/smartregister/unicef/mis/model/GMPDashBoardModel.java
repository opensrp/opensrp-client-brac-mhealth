package org.smartregister.unicef.mis.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.family.util.DBConstants;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.utils.DashBoardData;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class GMPDashBoardModel implements DashBoardContract.Model {
    private Context context;

    public GMPDashBoardModel(Context context){
        this.context = context;
    }
    public String getSSCondition(String blockId){
        String ssCondition;
        ssCondition = " and "+HnppConstants.KEY.BLOCK_ID +" = '"+blockId+"'";
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
    public DashBoardData getChildRefCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getChildRefCount(fromMonth,toMonth);
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getChildRefFollowupCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getChildRefFollowupCount(fromMonth,toMonth);
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getGMPCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getGMPCount(fromMonth,toMonth);
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getUniqueInThisMonthCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int notUniqueCount = HnppDBUtils.getNotUniqueCount(fromMonth,toMonth);
        int totalGMPCount = HnppDBUtils.getGMPCount(fromMonth,toMonth);
        int uniqueCount = totalGMPCount - notUniqueCount;
        if(uniqueCount<0) uniqueCount = 0;

        dashBoardData1.setCount(uniqueCount);
        return dashBoardData1;
    }
    public DashBoardData getWeightCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getGMPCount(fromMonth,toMonth,"weights");
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getHeightCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getGMPCount(fromMonth,toMonth,"heights");
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getMUACCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getGMPCount(fromMonth,toMonth,"muac_tbl");
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    public DashBoardData getChildGmpCounselingCount(String title, long fromMonth, long toMonth){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        int noChildRef = HnppDBUtils.getChildGmpCounselingCount(fromMonth,toMonth);
        dashBoardData1.setCount(noChildRef);
        return dashBoardData1;
    }
    private String getUnderWeightLogic(){
        return "z_score<-2 and z_score>-3";
    }
    private String getSeverelyUnderWeightLogic(){
        return "z_score<-2 and z_score>-3";
    }
    private String getStuntedLogic(){
        return "z_score<-2 and z_score>-3";
    }
    private String getSeverelyStuntedLogic(){
        return "z_score<-2 and z_score>-3";
    }
    private String getMUACMAMLogic(){
        //MUAC between >11.5 cm and <12.5 cm
        return "cm>11.5 and cm<12.5";
    }
    private String getMUACSAMLogic(){
        //MUAC < 11.5 cm
        return "cm<11.5";
    }
    private String getRefVisitedLogic(){
        //MUAC < 11.5 cm
        return "refer_place ='Yes' or refer_place ='yes' or refer_place ='হ্যাঁ'";
    }
    public DashBoardData getUnderWeightCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "weights";
        if(fromMonth == -1 && toMonth == -1){
                query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getUnderWeightLogic());
        }
        else{
                query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getUnderWeightLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getSeverelyUnderWeightCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "weights";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getSeverelyUnderWeightLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getSeverelyUnderWeightLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getStuntedCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "heights";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getStuntedLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getStuntedLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getSeverelyStuntedCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "heights";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getSeverelyStuntedLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getSeverelyStuntedLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getGrowthFaltering(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query= "select DISTINCT(base_entity_id),kg from weights where date  between '"+fromMonth+"' and '"+toMonth+"'";

        Log.v("GMP_REPORT","getGrowthFaltering:"+query);
        Cursor cursor = null;
        // try {
        HashMap<String,String> weights = new HashMap<>();
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                weights.put(cursor.getString(0),cursor.getString(1));
                cursor.moveToNext();
            }
            cursor.close();

        }
        int growthFaltering = 0;
        for (String key: weights.keySet()){
            String weight = weights.get(key);
            String q = "select kg from weights where date<"+fromMonth+" and kg>"+weight+" and base_entity_id ='"+key+"'";
            Log.v("GMP_REPORT","getGrowthFaltering>>"+q);
            cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(q, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String lastWeight = cursor.getString(0);
                    Log.v("GMP_REPORT","lastWeight>>"+lastWeight+":currentWeight:"+weight);

//                    if(Integer.parseInt(lastWeight)>Integer.parseInt(weight))
//                    {
                        growthFaltering++;
                    //}
                    cursor.moveToNext();
                }
                cursor.close();

            }

        }
        dashBoardData1.setCount(growthFaltering);
        return dashBoardData1;
    }
    public DashBoardData getMuacMamCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "muac_tbl";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getMUACMAMLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getMUACMAMLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getMuacSamCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "muac_tbl";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getMUACSAMLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getMUACSAMLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getRefVisitedCount(String title, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "visit_date", vaccineTable = "ec_visit_log";
        if(fromMonth == -1 && toMonth == -1){
            query = MessageFormat.format("select count(*) as count from {0} where {1}", vaccineTable,getRefVisitedLogic());
        }
        else{
            query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate)," and "+getRefVisitedLogic()+"");

        }
        Log.v("GMP_REPORT","getUnderWeightCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }
    public DashBoardData getTotalChildCount(String title, String blockId, long fromMonth, long toMonth) {
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = DBConstants.KEY.LAST_INTERACTED_WITH, vaccineTable = "ec_child";
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select count(*) as count from {0} ", vaccineTable);
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where block_id  = {1} ", vaccineTable,blockId);

            }
        }
        else{
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate));
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where "+compareDate+" is not null {1} {2} GROUP by base_entity_id ", vaccineTable,getSSCondition(blockId),getBetweenCondition(fromMonth,toMonth,compareDate));

            }
        }
        Log.v("IMMUNIZATION_QUERY","getTotalChildCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardData1;
    }

        public DashBoardData getImmunizationCount(String title, String blockId, long fromMonth, long toMonth,String vaccineName){
        DashBoardData  dashBoardData1 = new DashBoardData();
        dashBoardData1.setTitle(title);
        String query = null, compareDate = "date", vaccineTable = "vaccines";
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select count(*) as count from {0} where name = {1} GROUP by base_entity_id ", vaccineTable,"'"+vaccineName+"'");
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where block_id  = {1} and name = {2} GROUP by base_entity_id ", vaccineTable,blockId,"'"+vaccineName+"'");

            }
        }
        else{
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select count(*) as count from {0} where date is not null {1} and name = {2} GROUP by base_entity_id ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate),"'"+vaccineName+"'");
            }else{
                query = MessageFormat.format("select count(*) as count from {0} where date is not null {1} {2} and name = {3} GROUP by base_entity_id ", vaccineTable,getSSCondition(blockId),getBetweenCondition(fromMonth,toMonth,compareDate),"'"+vaccineName+"'");

            }
        }
        Log.v("IMMUNIZATION_QUERY","getHHCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                dashBoardData1.setCount(cursor.getInt(0));

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
