package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.database.Cursor;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class ChildHistoryInteractor implements MemberHistoryContract.Interactor {

    private AppExecutors appExecutors;
    private HnppVisitLogRepository visitLogRepository;

    public ChildHistoryInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
        visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
    }

    @Override
    public void fetchData(Context context, String baseEntityId, MemberHistoryContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            ArrayList<MemberHistoryData> memberHistoryData = getHistory(baseEntityId);
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(memberHistoryData));
        };
        appExecutors.diskIO().execute(runnable);

    }

    private ArrayList<MemberHistoryData> getHistory(String baseEntityId) {

        ArrayList<MemberHistoryData> historyDataArrayList  = new ArrayList<>();
        ArrayList<VisitLog> visitLogs = visitLogRepository.getAllVisitLog(baseEntityId);
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            String eventType = visitLog.getEventType();
            historyData.setEventType(eventType);
            historyData.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
            try{
                historyData.setImageSource(HnppConstants.iconMapping.get(eventType));
            }catch(NullPointerException e){

            }
            historyData.setVisitDetails(visitLog.getVisitJson());
            historyData.setVisitDate(visitLog.getVisitDate());
            historyDataArrayList.add(historyData);
        }
        ArrayList<MemberHistoryData> moreHistory = getImmunizationData(baseEntityId);
        if(moreHistory.size()>0){
            historyDataArrayList.addAll(moreHistory);
        }

        return historyDataArrayList;

    }
    private ArrayList<MemberHistoryData> getImmunizationData(String baseEntityId){
        ArrayList<MemberHistoryData> memberHistoryDataList = new ArrayList<>();
        String query = "select eventType,eventDate  from event where (eventType = 'Vaccination' or eventType = 'Recurring Service') and baseEntityId = '"+baseEntityId+"' group by eventDate";
        Cursor cursor = CoreChwApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                MemberHistoryData memberHistoryData1 = new MemberHistoryData();
                String eventType = cursor.getString(0);
                String date = cursor.getString(1);
                memberHistoryData1.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
                try{
                    memberHistoryData1.setImageSource(HnppConstants.iconMapping.get(eventType));
                }catch(NullPointerException e){

                }
                memberHistoryData1.setEventType(eventType);
                memberHistoryData1.setVisitDay(date);
                memberHistoryDataList.add(memberHistoryData1);
                cursor.moveToNext();

            }
            cursor.close();

        }
        return memberHistoryDataList;
    }

}
