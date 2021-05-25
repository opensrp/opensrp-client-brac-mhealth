package org.smartregister.brac.hnpp.service;

import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
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
    protected boolean isEmptyToAdd = false;

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

            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
            Timber.i("LAST SYNC DT %s", new DateTime(lastSyncDatetime));

            if (httpAgent == null) {
                complete(FetchStatus.fetchedFailed);
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
                    Log.i("URL:PA %s", url);
                }else{
                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd;
                    Log.i("URL: %s", url);
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
            }

            if (resp.isFailure() && !resp.isUrlError() && !resp.isTimeoutError()) {
                fetchFailed(count);
            }

            JSONObject jsonObject = new JSONObject((String) resp.payload());

            int eCount = fetchNumberOfEvents(jsonObject);
            Timber.i("Parse Network Event Count: %s", eCount);

            if (eCount == 0) {
                complete(FetchStatus.nothingFetched);
            } else if (eCount < 0) {
                fetchFailed(count);
            } else if (eCount > 0) {
                final Pair<Long, Long> serverVersionPair = getMinMaxServerVersions(jsonObject);
                long lastServerVersion = serverVersionPair.second - 1;
                if (eCount < getEventPullLimit()) {
                    lastServerVersion = serverVersionPair.second;
                }

                boolean isSaved = ecSyncUpdater.saveAllClientsAndEvents(jsonObject);
                //update sync time if all event client is save.
                if(isSaved){
                    processClient(serverVersionPair);
                    ecSyncUpdater.updateLastSyncTimeStamp(lastServerVersion);
                }
                fetchRetry(0);
            }
        } catch (Exception e) {
            Timber.e(e, "Fetch Retry Exception:  %s", e.getMessage());
            fetchFailed(count);
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
                Log.i("URL: %s", add_url);
                Response<String> response = httpAgent.post(add_url
                        ,
                        jsonPayload);
                if (response.isFailure()) {
                    Timber.e("Events sync failed.");
                    return;
                }
                db.markEventsAsSynced(pendingEvents);
                Timber.i("Events synced successfully.");
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

}
