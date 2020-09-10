package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MemberHistoryInteractor implements MemberHistoryContract.Interactor {

    private AppExecutors appExecutors;
    private HnppVisitLogRepository visitLogRepository;

    public MemberHistoryInteractor(AppExecutors appExecutors){
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
            historyData.setBaseEntityId(baseEntityId);
            String eventType = visitLog.getEventType();
            historyData.setEventType(eventType);
            historyData.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
            try{
                historyData.setImageSource(HnppConstants.iconMapping.get(eventType));
            }catch(NullPointerException e){

            }
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION) || eventType.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)){
                String isDelay = FamilyLibrary.getInstance().context().allSharedPreferences().getPreference(visitLog.visitId+"_IS_DELAY");
                if(!TextUtils.isEmpty(isDelay)){
                    historyData.setDelay(isDelay.equalsIgnoreCase("true"));
                }
            }
            historyData.setVisitDetails(visitLog.getVisitJson());
            historyData.setVisitDate(visitLog.getVisitDate());
            historyDataArrayList.add(historyData);
        }

        return historyDataArrayList;

    }

}
