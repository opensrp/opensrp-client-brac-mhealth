package org.smartregister.unicef.mis.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.utils.DashBoardData;
import org.smartregister.unicef.mis.utils.HnppConstants;

import java.text.MessageFormat;
import java.util.ArrayList;

public class HPVImmunizationModel implements DashBoardContract.Model {
    private Context context;

    public HPVImmunizationModel(Context context){
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

        public ArrayList<DashBoardData> getHPVCount(String title, String blockId, long fromMonth, long toMonth,String vaccineName){
        ArrayList<DashBoardData>  dashBoardDataList = new ArrayList<>();
        String query = null, compareDate = "vaccine_date", vaccineTable = "other_vaccine_table";
        if(fromMonth == -1 && toMonth == -1){
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select vaccine_date,count(*) as count from {0} where vaccine_name = {1} GROUP by vaccine_date ", vaccineTable,"'"+vaccineName+"'");
            }
//            else{
//                query = MessageFormat.format("select count(*) as count from {0} where block_id  = {1} and name = {2} GROUP by base_entity_id ", vaccineTable,blockId,"'"+vaccineName+"'");
//
//            }
        }
        else{
            if(TextUtils.isEmpty(blockId)){
                query = MessageFormat.format("select vaccine_date,count(*) as count from {0} where vaccine_name = {2} GROUP by vaccine_date ", vaccineTable,getBetweenCondition(fromMonth,toMonth,compareDate),"'"+vaccineName+"'");
            }
//            else{
//                query = MessageFormat.format("select count(*) as count from {0} where date is not null {1} {2} and name = {3} GROUP by base_entity_id ", vaccineTable,getSSCondition(blockId),getBetweenCondition(fromMonth,toMonth,compareDate),"'"+vaccineName+"'");
//
//            }
        }
        Log.v("IMMUNIZATION_QUERY","getHHCount:"+query);
        Cursor cursor = null;
        // try {
        cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DashBoardData dashBoardData = new DashBoardData();
                dashBoardData.setTitle(cursor.getString(0));
                dashBoardData.setCount(cursor.getInt(1));
                dashBoardDataList.add(dashBoardData);
                cursor.moveToNext();
            }
            cursor.close();

        }

        return dashBoardDataList;
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
