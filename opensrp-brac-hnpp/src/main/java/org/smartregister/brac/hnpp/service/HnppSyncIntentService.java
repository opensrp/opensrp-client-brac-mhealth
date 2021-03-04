package org.smartregister.brac.hnpp.service;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Response;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.service.HTTPAgent;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.sync.intent.SyncIntentService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.AllConstants.PerformanceMonitoring.FETCH;
import static org.smartregister.AllConstants.PerformanceMonitoring.PUSH;
import static org.smartregister.util.PerformanceMonitoringUtils.stopTrace;

public class HnppSyncIntentService extends SyncIntentService {
//    protected boolean isEmptyToAdd = false;
//    Context context;
//    HTTPAgent httpAgent;
//
//    protected synchronized void fetchRetry(final int count, boolean returnCount) {
//        try {
//            SyncConfiguration configs = CoreLibrary.getInstance().getSyncConfiguration();
//            if (configs.getSyncFilterParam() == null || StringUtils.isBlank(configs.getSyncFilterValue())) {
//                complete(FetchStatus.fetchedFailed);
//                return;
//            }
//
//            final ECSyncHelper ecSyncUpdater = ECSyncHelper.getInstance(context);
//            String baseUrl = getFormattedBaseUrl();
//
//            Long lastSyncDatetime = ecSyncUpdater.getLastSyncTimeStamp();
//            Timber.i("LAST SYNC DT %s", new DateTime(lastSyncDatetime));
//
//            if (httpAgent == null) {
//                complete(FetchStatus.fetchedFailed);
//                return;
//            }
//
//            //startEventTrace(FETCH, 0);
//
//            String url = baseUrl + SYNC_URL;
//            Response resp;
//            if (configs.isSyncUsingPost()) {
//                JSONObject syncParams = new JSONObject();
//                syncParams.put(configs.getSyncFilterParam().value(), configs.getSyncFilterValue());
//                syncParams.put("serverVersion", lastSyncDatetime);
//                syncParams.put("limit", getEventPullLimit());
//                syncParams.put(AllConstants.RETURN_COUNT, returnCount);
//                syncParams.put("isFromAdd",  isEmptyToAdd);
//                resp = httpAgent.postWithJsonResponse(url, syncParams.toString());
//            } else {
//                if(HnppConstants.isPALogin()){
//                    ArrayList<String> getVillageList = SSLocationHelper.getInstance().getSelectedVillageId();
//                    String vid = "";
//                    for(int i = 0; i< getVillageList.size() ; i++){
//                        vid = vid + getVillageList.get(i)+",";
//                    }
//                    if(!vid.isEmpty()){
//                        vid = vid.substring(0,vid.length() - 1);
//                    }
//                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd+"&villageIds="+vid;
//                    Log.i("URL:PA %s", url);
//                }else{
//                    url += "?" + configs.getSyncFilterParam().value() + "=" + configs.getSyncFilterValue() + "&serverVersion=" + lastSyncDatetime + "&limit=" + getEventPullLimit()+"&isEmptyToAdd="+isEmptyToAdd;
//                    Log.i("URL: %s", url);
//                }
//
//                resp = httpAgent.fetch(url);
//            }
//
//            if (resp.isUrlError()) {
//                FetchStatus.fetchedFailed.setDisplayValue(resp.status().displayValue());
//                complete(FetchStatus.fetchedFailed);
//                return;
//            }
//
//            if (resp.isTimeoutError()) {
//                FetchStatus.fetchedFailed.setDisplayValue(resp.status().displayValue());
//                complete(FetchStatus.fetchedFailed);
//                return;
//            }
//
//            if (resp.isFailure() && !resp.isUrlError() && !resp.isTimeoutError()) {
//                fetchFailed(count);
//                return;
//            }
//
//            if (returnCount) {
//               // totalRecords = resp.getTotalRecords();
//            }
//
//           // processFetchedEvents(resp, ecSyncUpdater, count);
//
//        } catch (Exception e) {
//            Timber.e(e, "Fetch Retry Exception:  %s", e.getMessage());
//            fetchFailed(count);
//        }
//    }
//    protected boolean pushECToServer(EventClientRepository db) {
//        boolean isSuccessfulPushSync = true;
//        isEmptyToAdd = true;
//
//        // push foreign events to server
//        int totalEventCount = db.getUnSyncedEventsCount();
//        int eventsUploadedCount = 0;
//
//        while (true) {
//            Map<String, Object> pendingEvents = db.getUnSyncedEvents(EVENT_PUSH_LIMIT);
//
//            if (pendingEvents.isEmpty()) {
//                break;
//            }
//
//            String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
//            if (baseUrl.endsWith(context.getString(org.smartregister.R.string.url_separator))) {
//                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(org.smartregister.R.string.url_separator)));
//            }
//            // create request body
//            JSONObject request = new JSONObject();
//            try {
//                if (pendingEvents.containsKey(AllConstants.KEY.CLIENTS)) {
//                    Object value = pendingEvents.get(AllConstants.KEY.CLIENTS);
//                    request.put(AllConstants.KEY.CLIENTS, value);
//
//                    if (value instanceof List) {
//                        eventsUploadedCount += ((List) value).size();
//                    }
//                }
//                if (pendingEvents.containsKey(AllConstants.KEY.EVENTS)) {
//                    request.put(AllConstants.KEY.EVENTS, pendingEvents.get(AllConstants.KEY.EVENTS));
//                }
//            } catch (JSONException e) {
//                Timber.e(e);
//            }
//            isEmptyToAdd = false;
//            String jsonPayload = request.toString();
//            startEventTrace(PUSH, eventsUploadedCount);
//            Response<String> response = httpAgent.post(
//                    MessageFormat.format("{0}/{1}",
//                            baseUrl,
//                            ADD_URL),
//                    jsonPayload);
//            if (response.isFailure()) {
//                Timber.e("Events sync failed.");
//                isSuccessfulPushSync = false;
//            } else {
//                db.markEventsAsSynced(pendingEvents);
//                Timber.i("Events synced successfully.");
//            }
//            stopTrace(eventSyncTrace);
//            updateProgress(eventsUploadedCount, totalEventCount);
//        }
//
//        return isSuccessfulPushSync;
//    }

}
