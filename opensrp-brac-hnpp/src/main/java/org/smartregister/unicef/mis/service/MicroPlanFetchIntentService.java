package org.smartregister.unicef.mis.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.utils.MicroPlanEpiData;
import org.smartregister.unicef.mis.utils.OutreachContentData;
import org.smartregister.unicef.mis.utils.TargetVsAchievementData;

public class MicroPlanFetchIntentService extends IntentService {

    private static final String CENTER_FETCH = "/rest/event/sync-center";
    private static final String MICROPLAN_FETCH = "/rest/event/sync-microplan";
    private static final String TAG = "MicroplanFetchIntentService";
    private static final String LAST_SYNC_CENTER = "last_sync_center";
    private static final String LAST_SYNC_MICROPLAN = "last_sync_microplan";

    public MicroPlanFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MicroPlanFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectCenterData = getCenter();
        if(jsonObjectCenterData!=null){
            long timestamp = 0;
            for(int i=0;i<jsonObjectCenterData.length();i++){
                try {
                    JSONObject object = jsonObjectCenterData.getJSONObject(i);
                    OutreachContentData outreachContentData =  new Gson().fromJson(object.toString(), OutreachContentData.class);
                    if(outreachContentData != null){
                        HnppApplication.getOutreachRepository().addAndUpdateOutreach(outreachContentData,true);
                        timestamp = outreachContentData.serverVersion;
                        Log.v("MICROPLAN_DATA","center lasttime:"+timestamp+":outreachContentData id:"+outreachContentData.blockId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObjectCenterData.length() > 0){
                CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_CENTER,timestamp+"");
            }


        }
        JSONArray jsonObjectMicroPlan = getMicroPlan();
        if(jsonObjectMicroPlan!=null){
            long timestamp = 0;
            for(int i=0;i<jsonObjectMicroPlan.length();i++){
                try {
                    JSONObject object = jsonObjectMicroPlan.getJSONObject(i);
                    MicroPlanEpiData microPlanEpiData =  new Gson().fromJson(object.toString(), MicroPlanEpiData.class);
                    if(microPlanEpiData != null){
                        OutreachContentData outreachContentData = HnppApplication.getOutreachRepository().getOutreachInformation(microPlanEpiData.blockId);
                        Log.v("MICROPLAN_DATA","outreachContentData>>"+microPlanEpiData.blockId);
                        if(outreachContentData!=null){
                            Log.v("MICROPLAN_DATA","<<<outreachContentData>>");
                            microPlanEpiData.outreachId = outreachContentData.outreachId;
                            microPlanEpiData.outreachName =outreachContentData.outreachName;
                            microPlanEpiData.unionName = outreachContentData.unionName;
                            microPlanEpiData.unionId = outreachContentData.unionId;
                            microPlanEpiData.oldWardName = outreachContentData.oldWardName;
                            microPlanEpiData.oldWardId = outreachContentData.oldWardId;
                            microPlanEpiData.newWardName = outreachContentData.newWardName;
                            microPlanEpiData.newWardId =outreachContentData.newWardId;
                            microPlanEpiData.blockId = outreachContentData.blockId;
                            microPlanEpiData.blockName = outreachContentData.blockName;
                            microPlanEpiData.centerType = outreachContentData.centerType;
                            microPlanEpiData.year = Integer.parseInt(microPlanEpiData.sessionPlanData.year);
                        }

                        HnppApplication.getMicroPlanRepository().addAndUpdateMicroPlan(microPlanEpiData,true);
//                        String status = microPlanEpiData.microPlanStatus;
//                        String comment = microPlanEpiData.comments;
//                        int blockId = microPlanEpiData.outreachContentData.blockId;
//                        Log.v("MICROPLAN_STATUS","status>>"+status+":comment:"+comment+":blockId:"+blockId);
//                        HnppApplication.getMicroPlanRepository().updateMicroPlanStatus(blockId,Integer.parseInt(microPlanEpiData.sessionPlanData.year),status,comment);
//
                        timestamp = microPlanEpiData.serverVersion;
                        Log.v("MICROPLAN_DATA","lasttime:"+timestamp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObjectMicroPlan.length() > 0){
                CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_MICROPLAN,timestamp+"");
            }


        }



    }

    private JSONArray getMicroPlan(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_MICROPLAN);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + MICROPLAN_FETCH + "/"+lastSynTime;
            Log.v("TARGET_FETCH","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(MICROPLAN_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
    private JSONArray getCenter(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if(TextUtils.isEmpty(userName)){
                return null;
            }
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_CENTER);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + CENTER_FETCH + "/"+lastSynTime;
            Log.v("TARGET_FETCH","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(CENTER_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
}
