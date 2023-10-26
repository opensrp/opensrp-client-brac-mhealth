package org.smartregister.brac.hnpp.interactor;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.repository.HnppVisitLogRepository;
import org.smartregister.brac.hnpp.service.VisitLogIntentService;
import org.smartregister.brac.hnpp.sync.FormParser;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.brac.hnpp.utils.HnppDBUtils;
import org.smartregister.brac.hnpp.utils.HnppJsonFormUtils;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.brac.hnpp.utils.VisitLog;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.smartregister.util.JsonFormUtils.gson;

public class FamilyHistoryInteractor implements MemberHistoryContract.Interactor {

    private AppExecutors appExecutors;
    private HnppVisitLogRepository visitLogRepository;

    public FamilyHistoryInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
        visitLogRepository = HnppApplication.getHNPPInstance().getHnppVisitLogRepository();
    }
    @Override
    public void getVisitFormWithData(Context context,MemberHistoryData content, MemberHistoryContract.InteractorCallBack callBack){
        String uniqueId = "";
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
        ArrayList<VisitLog> visitLogs = visitLogRepository.getAllVisitLogForFamily(baseEntityId);
        for(VisitLog visitLog : visitLogs){
            MemberHistoryData historyData = new MemberHistoryData();
            String eventType = visitLog.getEventType();
            Log.v("EVENT_TYPE","history:"+eventType);
            historyData.setVisitId(visitLog.getVisitId());
            historyData.setEventType(eventType);
            historyData.setMemberName(HnppDBUtils.getNameBaseEntityId(visitLog.getBaseEntityId()));
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
