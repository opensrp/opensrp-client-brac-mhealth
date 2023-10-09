package org.smartregister.unicef.mis.service;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.sync.intent.SyncIntentService;

import java.text.MessageFormat;
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
            Log.v("INVALID_REQ","lastSyncDatetime:"+lastSyncDatetime+":requestedTimeStamp:"+requestedTimeStamp+":isEmptyToAdd:"+isEmptyToAdd);

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

                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd;
                    Log.v("INVALID_REQ","url"+url);


                resp = httpAgent.fetch(url);
            }
            HnppConstants.appendLog("SYNC_URL", "url:"+url);

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
            if(jsonObject.has("msg")&& !TextUtils.isEmpty(jsonObject.getString("msg"))){
//                Intent i = new Intent(this, BlockUpdateActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(i);
                Toast.makeText(this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                return;
            }
            int eCount = fetchNumberOfEvents(jsonObject);
            Log.v("INVALID_REQ","response comed eCount:"+eCount);
            HnppConstants.appendLog("SYNC_URL", "response comed eCount:"+eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
                //VisitLogServiceJob.scheduleJobImmediately(VisitLogServiceJob.TAG);
            } else if (eCount < 0) {
                Thread.sleep(60000);
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
                   try{
                       processClient(serverVersionPair);
                   }catch (Exception e){
                       HnppConstants.appendLog("SYNC_URL", "processClient exception"+lastServerVersion);
                   }
                    HnppConstants.appendLog("SYNC_URL", "after processClient lastServerVersion:"+lastServerVersion+":requested:"+lastSyncDatetime+":original requestedtime:"+requestedTimeStamp);
                    Log.v("INVALID_REQ","lastServerVersion:"+lastServerVersion+":requestedTimeStamp:"+requestedTimeStamp);
                  if(lastServerVersion>requestedTimeStamp){
                      HnppConstants.appendLog("SYNC_URL", "updateLastSyncTimeStamp lastServerVersion:"+lastServerVersion);
                      Log.v("INVALID_REQ","updateLastSyncTimeStamp:"+lastServerVersion);

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
    public void fetchFailed(int count) {
        if (count < CoreLibrary.getInstance().getSyncConfiguration().getSyncMaxRetries()) {
            int newCount = count + 1;
            fetchRetry(newCount);
        } else {
            complete(FetchStatus.fetchedFailed);
        }
    }
    @Override
    protected void pushECToServer() {
        EventClientRepository db = CoreLibrary.getInstance().context().getEventClientRepository();
        boolean keepSyncing = true;
        isEmptyToAdd = true;

        while (keepSyncing) {
            try {
                Map<String, Object> pendingEvents = db.getUnSyncedEventsClients(EVENT_PUSH_LIMIT);
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
                HnppConstants.appendLog("SYNC_URL", "Exception:"+e.getMessage());
                return;

            }
        }
    }

}
