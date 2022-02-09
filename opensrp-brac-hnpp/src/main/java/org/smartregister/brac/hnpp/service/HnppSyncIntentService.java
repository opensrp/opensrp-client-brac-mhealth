package org.smartregister.brac.hnpp.service;

import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.job.VisitLogServiceJob;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.sync.intent.SyncIntentService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

import timber.log.Timber;

public class HnppSyncIntentService extends SyncIntentService {
    protected boolean isEmptyToAdd = true;


    @Override
    protected synchronized void fetchRetry(int count) {
        try {
            SyncConfiguration configs = CoreLibrary.getInstance().getSyncConfiguration();
            if (configs.getSyncFilterParam() == null || StringUtils.isBlank(configs.getSyncFilterValue())) {
                complete(FetchStatus.fetchedFailed);
                return;
            }

            final ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
            }
            long requestedTimeStamp  = 0;

            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
            if(lastSyncDatetime>requestedTimeStamp){
                requestedTimeStamp = lastSyncDatetime;
            }
            HnppConstants.appendLog("SYNC_URL", "lastSyncDatetime:"+lastSyncDatetime+":requestedTimeStamp:"+requestedTimeStamp+":isEmptyToAdd:"+isEmptyToAdd);

            //if(requestedTimeStamp<lastSync) requested = lastsynctime
            if(!isEmptyToAdd){
                if(lastSyncDatetime< requestedTimeStamp){
                    isEmptyToAdd = true;
                }
            }
            // requested<lastsync{ isEmptyToAdd = false else isEmptyToAdd = true}

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
                return;
            }

            String url = baseUrl + SYNC_URL;
            Response resp;
            if (configs.isSyncUsingPost()) {
                JSONObject syncParams = new JSONObject();
                syncParams.put(configs.getSyncFilterParam().value(), configs.getSyncFilterValue());
                syncParams.put("serverVersion", lastSyncDatetime);
                syncParams.put("limit",  getEventPullLimit());
                syncParams.put("isFromAdd",  isEmptyToAdd);
                resp = httpAgent.postWithJsonResponse(url,syncParams.toString());
            } else {
                if(HnppConstants.isPALogin()){
                    ArrayList<String> getVillageList = SSLocationHelper.getInstance().getSelectedVillageId();
                    String vid = "";
                    for(int i = 0; i< getVillageList.size() ; i++){
                        vid = vid + getVillageList.get(i)+",";
                    }
                    if(!vid.isEmpty()){
                        vid = vid.substring(0,vid.length() - 1);
                    }
                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd+"&villageIds="+vid;
                    Log.v("SYNC_URL", url);
                }else{
                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd;
                    Log.v("SYNC_URL", url);
                }

                resp = httpAgent.fetch(url);
            }

            if (resp.isUrlError()) {
                FetchStatus.fetchedFailed.setDisplayValue(resp.status().displayValue());
                complete(FetchStatus.fetchedFailed);
                return;
            }

            if (resp.isTimeoutError()) {
                FetchStatus.fetchedFailed.setDisplayValue(resp.status().displayValue());
                complete(FetchStatus.fetchedFailed);
                return;
            }

            if (resp.isFailure() && !resp.isUrlError() && !resp.isTimeoutError()) {
                fetchFailed(count);
                //complete(FetchStatus.fetchedFailed);
                return;
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            int eCount = fetchNumberOfEvents(jsonObject);
            HnppConstants.appendLog("SYNC_URL", "response comed eCount:"+eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
                //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            } else if (eCount < 0) {
                fetchFailed(count);
            } else {

                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
                long lastServerVersion = serverVersionPair.second - 1;
                if (eCount < getEventPullLimit()) {
                    lastServerVersion = serverVersionPair.second;
                }
                boolean isSaved = ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
                HnppConstants.appendLog("SYNC_URL", "isSaved:"+isSaved+":lastServerVersion:"+lastServerVersion);

                //update sync time if all event client is save.
                if(isSaved){
                    processClient(serverVersionPair);
                    HnppConstants.appendLog("SYNC_URL", "after processClient lastServerVersion:"+lastServerVersion+":requested:"+lastSyncDatetime+":original requestedtime:"+requestedTimeStamp);

                  if(lastServerVersion>requestedTimeStamp){
                      HnppConstants.appendLog("SYNC_URL", "updateLastSyncTimeStamp lastServerVersion:"+lastServerVersion);

                      ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
                  }
                }
                fetchRetry(0);
            }
        } catch (Exception e) {
            Timber.e(e, "Fetch Retry Exception:  %s", e.getMessage());
            HnppConstants.appendLog("SYNC_URL","exception "+e.getMessage());
            try{
                fetchFailed(count);
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }
    }
    @Override
    protected void pushECToServer() {
        EventClientRepository db = CoreLibrary.getInstance().context().getEventClientRepository();
        boolean keepSyncing = true;
        isEmptyToAdd = true;

        while (keepSyncing) {
            try {
                Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);
                HnppConstants.appendLog("SYNC_URL", "pushECToServer:"+pendingEvents.size());

                if (pendingEvents.isEmpty()) {
                    return;
                }

                String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(org.smartregister.R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(org.smartregister.R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();
                if (pendingEvents.containsKey(AllConstants.KEY.CLIENTS)) {
                    request.put(AllConstants.KEY.CLIENTS, pendingEvents.get(AllConstants.KEY.CLIENTS));
                }
                if (pendingEvents.containsKey(AllConstants.KEY.EVENTS)) {
                    request.put(AllConstants.KEY.EVENTS, pendingEvents.get(AllConstants.KEY.EVENTS));
                }
                isEmptyToAdd = false;
                String jsonPayload = request.toString();
                String add_url =  MessageFormat.format("{0}/{1}",
                        baseUrl,
                        ADD_URL);
                Response<String> response = httpAgent.post(add_url
                        ,
                        jsonPayload);
                HnppConstants.appendLog("SYNC_URL", "pushECToServer:response comes"+response);
                if (response.isFailure()) {
                    HnppConstants.appendLog("SYNC_URL", "pushECToServer:response response.isFailure");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                HnppConstants.appendLog("SYNC_URL", "pushECToServer:markEventsAsSynced");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

}
