package org.smartregister.unicef.dghs.model;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.smartregister.unicef.dghs.R;
import org.smartregister.unicef.dghs.contract.DashBoardContract;
import org.smartregister.unicef.dghs.utils.DashBoardData;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppDBUtils;
import org.smartregister.chw.core.application.CoreChwApplication;

import java.util.ArrayList;

import timber.log.Timber;

public class DashBoardModel implements DashBoardContract.Model {
    private Context context;

    public DashBoardModel(Context context){
        this.context = context;
    }

    private ArrayList<DashBoardData> dashBoardDataArrayList = new ArrayList<>();

    public ArrayList<DashBoardData> getDashBoardDataArrayList() {
        return dashBoardDataArrayList;
    }

    /**
     * Date formate should be Y-m-d (2019-10-24)
     * @param todate
     * @param fromDate
     * @return
     */
    public ArrayList<DashBoardData> getDashData(String todate, String fromDate){
        String query = "select eventType,count(*),eventDate as count from event where strftime('%Y-%m-%d', eventDate) BETWEEN '"+fromDate+"' AND '"+todate+"' group by eventType";
        Cursor cursor = null;
        dashBoardDataArrayList.clear();
       // try {
            cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
            if(cursor !=null && cursor.getCount() > 0){
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    DashBoardData dashBoardData1 = new DashBoardData();
                    dashBoardData1.setCount(cursor.getInt(1));
                    dashBoardData1.setEventType(cursor.getString(0));
                    if(dashBoardData1.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.MEMBER_REFERRAL)){
                        dashBoardData1.setTitle("সদস্য রেফারেল");
                    }else if(dashBoardData1.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.WOMEN_REFERRAL)){
                        dashBoardData1.setTitle("নারী রেফারেল");
                    }else if(dashBoardData1.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.CHILD_REFERRAL)){
                        dashBoardData1.setTitle("শিশু রেফারেল");
                    }else{
                        dashBoardData1.setTitle(HnppConstants.eventTypeMapping.get(dashBoardData1.getEventType()));
                    }
                    try{
                        dashBoardData1.setImageSource((int)HnppConstants.iconMapping.get(dashBoardData1.getEventType()));
                    }catch (Exception e){
                        dashBoardData1.setImageSource(R.drawable.rowavatar_member);
                    }
                    if(!TextUtils.isEmpty(dashBoardData1.getEventType())){
                        dashBoardDataArrayList.add(dashBoardData1);
                    }
                    cursor.moveToNext();
                }
                cursor.close();

            }
            int countSimprints = HnppDBUtils.getCoutByFingerPrint(fromDate,todate);
            if(countSimprints>0){
                DashBoardData dashBoardData1 = new DashBoardData();
                dashBoardData1.setCount(countSimprints);
                dashBoardData1.setTitle("ফিঙ্গার প্রিন্ট দ্বারা নিবন্ধিত");
                dashBoardData1.setImageSource(R.drawable.ic_fingerprint_id);

                dashBoardDataArrayList.add(dashBoardData1);
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
