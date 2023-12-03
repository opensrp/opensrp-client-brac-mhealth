package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.R;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.VisitLogIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.smartregister.brac.hnpp.sync.FormParser.getFormNamesFromEventObject;
import static org.smartregister.util.JsonFormUtils.gson;

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
                        HashMap<String,Object> form_details = getFormNamesFromEventObject(baseEvent);
                        ArrayList<String> encounter_types = (ArrayList<String>) form_details.get("form_name");
                        for(String eventType:encounter_types){
                            if(eventType.equalsIgnoreCase(content.getEventType())){
                                try{
                                    HashMap<String,String>details = (HashMap<String, String>) form_details.get("details");
                                    final CommonPersonObjectClient client = new CommonPersonObjectClient(visit.getBaseEntityId(), details, "");
                                    client.setColumnmaps(details);
                                    Log.v("EVENT_TYPE","getVisitFormWithData>>>eventType:"+eventType);
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
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            historyData.setBaseEntityId(baseEntityId);
            historyData.setVisitId(visitLog.getVisitId());
            String eventType = visitLog.getEventType();
            historyData.setEventType(eventType);
            historyData.setTitle(HnppConstants.visitEventTypeMapping.get(eventType));
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

}
