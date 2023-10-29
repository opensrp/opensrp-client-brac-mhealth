package org.smartregister.unicef.mis.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.CoreLibrary;

import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class MigrationFetchIntentService extends IntentService {

    private static final String MIGRATED_FETCH = "/rest/client/migrated?";
    private static final String REJECTED_FETCH = "/rest/client/rejected?";
    private static final String TAG = "MigrationFetchIntentService";
    private static final String LAST_SYNC_TIME_MEMEBER_MIGRATED = "last_sync_mb_mig";
    private static final String LAST_SYNC_TIME_HH_MIGRATED = "last_sync_hh_mig";
    private static final String LAST_SYNC_TIME_MEMEBER_REJECTED = "last_sync_mb_rej";
    private static final String LAST_SYNC_TIME_HH_REJECTED = "last_sync_hh_rej";

    public enum LOCATION_TYPE {
        MIGRATEDMEMBER, MIGRATEDHH, REJECTEDMEMBER, REJECTEDHH
    }

    public MigrationFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MigrationFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectMBMI = getDataList(LOCATION_TYPE.MIGRATEDMEMBER);
        JSONArray jsonObjectHHMI = getDataList(LOCATION_TYPE.MIGRATEDHH);
        JSONArray jsonObjectMBREJ = getDataList(LOCATION_TYPE.REJECTEDMEMBER);
        JSONArray jsonObjectHHREJ = getDataList(LOCATION_TYPE.MIGRATEDHH);

        if(jsonObjectMBMI!=null){
            processJSONArrayLIST(jsonObjectMBMI,LAST_SYNC_TIME_MEMEBER_MIGRATED);
        }
        if(jsonObjectHHMI!=null){
            processJSONArrayLIST(jsonObjectHHMI,LAST_SYNC_TIME_HH_MIGRATED);
        }
        if(jsonObjectMBREJ!=null){
            processJSONArrayLIST(jsonObjectMBREJ,LAST_SYNC_TIME_MEMEBER_REJECTED);
        }
        if(jsonObjectHHREJ!=null){
            processJSONArrayLIST(jsonObjectHHREJ,LAST_SYNC_TIME_HH_REJECTED);
        }
    }

    private void processJSONArrayLIST(JSONArray jsonArray, String lastsync) {
        ArrayList<String> baseEntityList = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            try {
                baseEntityList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(baseEntityList.size()>0){
            for(String id: baseEntityList){
                if(lastsync.equalsIgnoreCase(LAST_SYNC_TIME_HH_REJECTED) || lastsync.equalsIgnoreCase(LAST_SYNC_TIME_HH_MIGRATED)){
                    HnppDBUtils.updateMigratedOrRejectedHH(id);
                }else if(lastsync.equalsIgnoreCase(LAST_SYNC_TIME_MEMEBER_REJECTED) || lastsync.equalsIgnoreCase(LAST_SYNC_TIME_MEMEBER_MIGRATED)){
                    HnppDBUtils.updateMigratedOrRejectedMember(id);
                }
            }

            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(lastsync, String.valueOf(System.currentTimeMillis()));
        }
    }

    private JSONArray getDataList(LOCATION_TYPE typeUrl){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String lastSynTime = "0";
        String urlMigration = null, type = null;
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

            switch (typeUrl){
                case MIGRATEDMEMBER:
                    lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME_MEMEBER_MIGRATED);
                    urlMigration = MIGRATED_FETCH;
                    type  = "Member";
                    break;
                case MIGRATEDHH:
                    lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME_HH_MIGRATED);
                    urlMigration = MIGRATED_FETCH;
                    type  = "HH";
                    break;
                case REJECTEDMEMBER:
                    lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME_MEMEBER_REJECTED);
                    urlMigration = REJECTED_FETCH;
                    type  = "Member";
                    break;
                case REJECTEDHH:
                    lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME_HH_REJECTED);
                    urlMigration = REJECTED_FETCH;
                    type  = "HH";
                    break;
            }
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + urlMigration + "username=" + userName+"&type="+type+"&timestamp="+lastSynTime;
            Log.v("MIGRATION_FETCH","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(typeUrl + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }

}
