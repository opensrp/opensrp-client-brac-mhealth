package org.smartregister.brac.hnpp.service;

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
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.location.SSLocationHelper;
import org.smartregister.brac.hnpp.location.SSModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class SSLocationFetchIntentService extends IntentService {

    private static final String LOCATION_FETCH = "/provider/location-tree?";
    private static final String PA_LOCATION_FETCH = "/pa-provider/location-tree?";
    private static final String TAG = "SSLocation";
    public static final String WITHOUT_SK = "without_sk";
    public SSLocationFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SSLocationFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectLocation = getLocationList();
        if(jsonObjectLocation!=null){
            if(!HnppConstants.isPALogin())HnppApplication.getSSLocationRepository().dropTable();
            for(int i=0;i<jsonObjectLocation.length();i++){
                try {
                    JSONObject object = jsonObjectLocation.getJSONObject(i);
                    SSModel ssModel =  new Gson().fromJson(object.toString(), SSModel.class);
                    if(ssModel != null){
                        if(!TextUtils.isEmpty(ssModel.withoutsk) && ssModel.withoutsk.equalsIgnoreCase("PA")){
                            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(WITHOUT_SK,"PA");
                        }
                        HnppApplication.getSSLocationRepository().addOrUpdate(ssModel);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SSLocationHelper.getInstance().updateModel();



        }
        if(HnppConstants.isPALogin()){
            Intent intent1 = new Intent(HnppConstants.ACTION_LOCATION_UPDATE);
            sendBroadcast(intent1);
        }
    }

    private JSONArray getLocationList(){
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
            //testing
            String url;
           if(HnppConstants.isPALogin()) url = baseUrl + PA_LOCATION_FETCH + "username=" + userName;
           else url = baseUrl + LOCATION_FETCH + "username=" + userName;
            Log.v("LOCATION_FETCH","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(LOCATION_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
}
