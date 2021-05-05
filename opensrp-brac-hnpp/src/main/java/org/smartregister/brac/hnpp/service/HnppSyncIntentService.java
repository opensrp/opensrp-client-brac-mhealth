package org.smartregister.brac.hnpp.service;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.sync.intent.SyncIntentService;
import java.util.ArrayList;

import timber.log.Timber;

public class HnppSyncIntentService extends SyncIntentService {
    @Override
    protected Response getUrlResponse(@NonNull String baseURL, @NonNull RequestParamsBuilder requestParamsBuilder, @NonNull SyncConfiguration configs, boolean returnCount) {
        Response response = null;
        HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();

        String requestUrl = baseURL;

        if (configs.isSyncUsingPost()) {
            requestParamsBuilder.addParam("isEmptyToAdd",isEmptyToAdd);
            response = httpAgent.postWithJsonResponse(requestUrl, requestParamsBuilder.returnCount(returnCount).build());

        } else {
            requestParamsBuilder.addParam("isEmptyToAdd",isEmptyToAdd);
            if(HnppConstants.isPALogin()){
                ArrayList<String> getVillageList = SSLocationHelper.getInstance().getSelectedVillageId();
                String vid = "";
                for(int i = 0; i< getVillageList.size() ; i++){
                    vid = vid + getVillageList.get(i)+",";
                }
                if(!vid.isEmpty()){
                    vid = vid.substring(0,vid.length() - 1);
                }
                requestParamsBuilder.addParam("villageIds",vid);
            }else{
                //test
                requestParamsBuilder.removeParam("locationId");
                requestParamsBuilder.addParam("providerId","testsk");
            }
            requestUrl += "?" + requestParamsBuilder.build();
            Log.v("URL: %s", requestUrl);
            response = httpAgent.fetch(requestUrl);
        }
        return response;

    }
}
