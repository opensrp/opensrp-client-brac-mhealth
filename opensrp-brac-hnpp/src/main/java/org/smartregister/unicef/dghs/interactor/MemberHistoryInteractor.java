package org.smartregister.unicef.dghs.interactor;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.unicef.dghs.utils.VisitHistory;
import org.smartregister.unicef.dghs.utils.VisitLog;
import org.smartregister.chw.anc.domain.Visit;

import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.smartregister.unicef.dghs.sync.FormParser.getFormNamesFromEventObject;
import static org.smartregister.util.JsonFormUtils.gson;

public class MemberHistoryInteractor implements MemberHistoryContract.Interactor,MemberHistoryContract.InteractorANC {

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

    @Override
    public void getVisitFormWithData(Context context,MemberHistoryData content, MemberHistoryContract.InteractorCallBack callBack){
        Runnable runnable = () -> {
            List<Visit> v = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByVisitId(content.getVisitId());
            if(v.size()>0){
                JSONObject jsonForm = null;
                    Visit visit = v.get(0);
                    if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EventType.ANC_HOME_VISIT)
                     || visit.getVisitType().equalsIgnoreCase(HnppConstants.EventType.PREGNANCY_OUTCOME)
                    || visit.getVisitType().equalsIgnoreCase(HnppConstants.EventType.PNC_HOME_VISIT)){
                        String eventJson = visit.getJson();
                        Event baseEvent = gson.fromJson(eventJson, Event.class);
                        HashMap<String,Object> form_details = getFormNamesFromEventObject(baseEvent);
                        ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
                        for(String eventType:encounter_types){
                            if(eventType.equalsIgnoreCase(content.getEventType())){
                                try{
                                    HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
                                    final CommonPersonObjectClient client = new CommonPersonObjectClient(visit.getBaseEntityId(), details, "");
                                    client.setColumnmaps(details);
                                    jsonForm = FormParser.loadFormFromAsset(eventType);
                                    int count = jsonForm.getInt("count");
                                    for(int i= 1;i<=count;i++){
                                        JSONObject steps = jsonForm.getJSONObject("step"+i);
                                        JSONArray ja = steps.getJSONArray(JsonFormUtils.FIELDS);

                                        for (int k = 0; k < ja.length(); k++) {
                                            FormParser.populateValuesForFormObject(client, ja.getJSONObject(k));
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();

                                }

                                break;
                            }
                        }
                    }else{
                        jsonForm = HnppJsonFormUtils.getVisitFormWithData(visit.getJson(),context);
                    }



                JSONObject finalJsonForm = jsonForm;
                appExecutors.mainThread().execute(() -> callBack.updateFormWithData(content, finalJsonForm));
            }


        };
        appExecutors.diskIO().execute(runnable);
    }


    private ArrayList<MemberHistoryData> getHistory(String baseEntityId) {

        ArrayList<MemberHistoryData> historyDataArrayList  = new ArrayList<>();
        ArrayList<VisitLog> visitLogs = visitLogRepository.getAllVisitLog(baseEntityId);
        int count = FormApplicability.getANCCount(baseEntityId)+1;
        int pncCount = FormApplicability.getPNCCount(baseEntityId)+1;
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            historyData.setBaseEntityId(baseEntityId);
            historyData.setVisitId(visitLog.getVisitId());
            String eventType = visitLog.getEventType();
            historyData.setEventType(eventType);
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
                count--;
                historyData.setTitle(FormApplicability.getANCTitleForHistory(count));
            }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
                pncCount--;
                historyData.setTitle(FormApplicability.getPNCTitleForHistory(pncCount));
            }
            else{
                historyData.setTitle(HnppConstants.getVisitEventTypeMapping().get(eventType));
            }

            try{
                historyData.setImageSource(HnppConstants.iconMapping.get(eventType));
            }catch(NullPointerException e){

            }

//            historyData.setVisitDetails(visitLog.getVisitJson());
            historyData.setVisitDate(visitLog.getVisitDate());
            historyDataArrayList.add(historyData);
        }

        return historyDataArrayList;

    }
    private ArrayList<MemberHistoryData> getHistoryAfterTimeLine(String baseEntityId, long timeline, long endDate) {

        ArrayList<MemberHistoryData> historyDataArrayList  = new ArrayList<>();
        ArrayList<VisitLog> visitLogs = endDate>0?visitLogRepository.getCurrentANCTimeline(baseEntityId,timeline,endDate):visitLogRepository.getCurrentANCTimeline(baseEntityId,timeline);
        int count = FormApplicability.getANCCount(baseEntityId)+1;
        int pncCount = FormApplicability.getPNCCount(baseEntityId)+1;
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            historyData.setBaseEntityId(baseEntityId);
            historyData.setVisitId(visitLog.getVisitId());
            String eventType = visitLog.getEventType();
            historyData.setEventType(eventType);
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)){
                count--;
                historyData.setTitle(FormApplicability.getANCTitleForHistory(count));
            }else if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)){
                pncCount--;
                historyData.setTitle(FormApplicability.getPNCTitleForHistory(pncCount));
            }
            else{
                historyData.setTitle(HnppConstants.getVisitEventTypeMapping().get(eventType));
            }

            try{
                historyData.setImageSource(HnppConstants.iconMapping.get(eventType));
            }catch(NullPointerException e){

            }

            Event baseEvent = gson.fromJson(visitLog.getVisitJson(), Event.class);
            List<Obs> obsList = baseEvent.getObs();
            for(Obs obs:obsList){
                String key = obs.getFormSubmissionField();
                if(key.equalsIgnoreCase("schedule_date")){
                    String sd = (String) obs.getValue();
                    historyData.setScheduleDate(sd);
                }
                if(key.equalsIgnoreCase("service_taken_date")){
                    String std = (String) obs.getValue();
                    historyData.setServiceTakenDate(std);
                }
                if(key.equalsIgnoreCase("delivery_date")){
                    String std = (String) obs.getValue();
                    historyData.setEddDate(std);
                }
            }
            historyData.setVisitDate(visitLog.getVisitDate());
            historyDataArrayList.add(historyData);
        }

        return historyDataArrayList;

    }
    @Override
    public void fetchAncData(Context context, String baseEntityId, MemberHistoryContract.InteractorCallBackANC callBack) {
        Runnable runnable = () -> {
            ArrayList<VisitHistory> visitLogs = visitLogRepository.getAncRegistrationCount(baseEntityId);
            appExecutors.mainThread().execute(() -> callBack.onUpdateAncList(visitLogs));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchCurrentTimeLineData(Context context, String baseEntityId, MemberHistoryContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            ArrayList<VisitHistory> visitLogs = visitLogRepository.getAncRegistrationCount(baseEntityId);
            if(visitLogs.size()>0){
                long startDate = visitLogs.get(0).getStartVisitDate();
                ArrayList<MemberHistoryData> memberHistoryData = getHistoryAfterTimeLine(baseEntityId,startDate,0);
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(memberHistoryData));
            }else{
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(new ArrayList<>()));
            }

        };
        appExecutors.diskIO().execute(runnable);
    }
    public void fetchCurrentTimeLineHistoryData(Context context, String baseEntityId,long startDate, long endDate, MemberHistoryContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {

                ArrayList<MemberHistoryData> memberHistoryData =getHistoryAfterTimeLine(baseEntityId,startDate,endDate);
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(memberHistoryData));

        };
        appExecutors.diskIO().execute(runnable);
    }
}
