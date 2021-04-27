package org.smartregister.brac.hnpp.service;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.SyncConfiguration;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.sync.intent.SyncIntentService;
import java.util.ArrayList;

public class HnppSyncIntentService extends SyncIntentService {
    @Override
    protected Response getUrlResponse(SyncConfiguration configs, HTTPAgent httpAgent, String url, Long lastSyncDatetime, boolean returnCount) {
        Response resp = null;
        try{

            if (configs.isSyncUsingPost()) {
                JSONObject syncParams = new JSONObject();
                syncParams.put(configs.getSyncFilterParam().value(), configs.getSyncFilterValue());
                syncParams.put("serverVersion", lastSyncDatetime);
                syncParams.put("limit", getEventPullLimit());
                syncParams.put(AllConstants.RETURN_COUNT, returnCount);
                syncParams.put("isEmptyToAdd",  isEmptyToAdd);
                resp = httpAgent.postWithJsonResponse(url, syncParams.toString());
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
            return resp;

        }catch (JSONException e){

        }
        return resp;
    }
}
