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

import java.text.MessageFormat;

public class ImmunizationSummeryDashBoardModel implements DashBoardContract.Model {
    private Context context;

    public ImmunizationSummeryDashBoardModel(Context context){
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
