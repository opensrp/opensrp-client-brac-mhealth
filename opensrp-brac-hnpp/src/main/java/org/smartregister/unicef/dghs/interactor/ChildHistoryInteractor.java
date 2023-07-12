package org.smartregister.unicef.dghs.interactor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.repository.HnppVisitLogRepository;
import org.smartregister.unicef.dghs.sync.FormParser;
import org.smartregister.unicef.dghs.utils.FormApplicability;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.HnppJsonFormUtils;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.unicef.dghs.utils.VisitLog;
import org.smartregister.chw.anc.domain.Visit;

import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.smartregister.util.JsonFormUtils.gson;

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
    @Override
    public void getVisitFormWithData(Context context,MemberHistoryData content, MemberHistoryContract.InteractorCallBack callBack){
        Runnable runnable = () -> {
            List<Visit> v = HnppApplication.getHNPPInstance().getHnppVisitLogRepository().getVisitByVisitId(content.getVisitId());
            if(v.size()>0){
                JSONObject jsonForm = null;
                Visit visit = v.get(0);
                if(visit.getVisitType().equalsIgnoreCase(HnppConstants.EventType.ANC_HOME_VISIT)){
                    String eventJson = visit.getJson();
                    Event baseEvent = gson.fromJson(eventJson, Event.class);
                    HashMap<String,Object> form_details = FormParser.getFormNamesFromEventObject(baseEvent);
                    ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
                    for(String eventType:encounter_types){
                        if(eventType.equalsIgnoreCase(content.getEventType())){
                            try{
                                HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
                                final CommonPersonObjectClient client = new CommonPersonObjectClient(visit.getBaseEntityId(), details, "");
                                client.setColumnmaps(details);
                                jsonForm = FormParser.loadFormFromAsset(eventType);
                                JSONObject stepOne = jsonForm.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                                for (int k = 0; k < jsonArray.length(); k++) {
                                    FormParser.populateValuesForFormObject(client, jsonArray.getJSONObject(k));
                                }
                            }catch (Exception e){

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
        int count = FormApplicability.getNewBornPNCCount(baseEntityId)+1;
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            String eventType = visitLog.getEventType();
            historyData.setVisitId(visitLog.getVisitId());
            historyData.setEventType(eventType);
            if(eventType.equalsIgnoreCase(HnppConstants.EVENT_TYPE.NEW_BORN_PNC_1_4)){
                count--;
                historyData.setTitle(FormApplicability.getNewBornTitleForHistory(count));
            }else{
                historyData.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
            }

            try{
                historyData.setImageSource(HnppConstants.iconMapping.get(eventType));
            }catch(NullPointerException e){

            }
//            historyData.setVisitDetails(visitLog.getVisitJson());
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
        String query = "select eventType,eventDate,json  from event where (eventType = 'Vaccination' or eventType = 'Recurring Service') and baseEntityId = '"+baseEntityId+"'  order by eventDate desc";
        Cursor cursor = HnppApplication.getInstance().getRepository().getReadableDatabase().rawQuery(query, new String[]{});
        if(cursor !=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                MemberHistoryData memberHistoryData1 = new MemberHistoryData();
                String eventType = cursor.getString(0);
                String date = cursor.getString(1);
                Event baseEvent = gson.fromJson(cursor.getString(2), Event.class);
                List<Obs> obsList = baseEvent.getObs();
                String vaccineName ="";
                for(Obs obs : obsList){
                   vaccineName =  obs.getFormSubmissionField();
                   String vaccineDate =(String) obs.getValue();
                    memberHistoryData1.setVisitDetails(vaccineName+" \n"+vaccineDate);
                    break;
                }
                if(!TextUtils.isEmpty(vaccineName)){
                    memberHistoryData1.setTitle(HnppConstants.vaccineNameMapping.get(vaccineName));

                }else{
                    memberHistoryData1.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));

                }
                try{
                    memberHistoryData1.setImageSource(HnppConstants.iconMapping.get(eventType));
                }catch(NullPointerException e){

                }
                memberHistoryData1.setEventType(eventType);
                memberHistoryData1.setVisitDay(date);
                memberHistoryDataList.add(memberHistoryData1);
                cursor.moveToNext();

            }

        }
        if(cursor!=null) cursor.close();
        return memberHistoryDataList;
    }

}
