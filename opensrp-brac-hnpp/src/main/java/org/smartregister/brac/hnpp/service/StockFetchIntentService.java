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
import org.smartregister.brac.hnpp.utils.StockData;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

public class StockFetchIntentService extends IntentService {

    private static final String LOCATION_FETCH = "/get_stock_info?";
    private static final String TAG = "StockFetchIntentService";
    private static final String LAST_SYNC_TIME = "last_stock_sync";

    public StockFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public StockFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectLocation = getStockList();
        if(jsonObjectLocation!=null){
            long timestamp = 0;
            for(int i=0;i<jsonObjectLocation.length();i++){
                try {
                    JSONObject object = jsonObjectLocation.getJSONObject(i);
                    StockData stockData =  new Gson().fromJson(object.toString(), StockData.class);
                    if(stockData != null){
                        HnppApplication.getStockRepository().addOrUpdate(stockData);
                        timestamp = stockData.getTimestamp();
                        Log.v("TARGET_FETCH","lasttime:"+timestamp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObjectLocation.length()>0){
                CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_SYNC_TIME,timestamp+"");
            }


        }

    }

    private JSONArray getStockList(){
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
            String lastSynTime = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_SYNC_TIME);
            if(TextUtils.isEmpty(lastSynTime)){
                lastSynTime ="0";
            }
            //testing
            String url = baseUrl + LOCATION_FETCH + "username=" + userName+"&timestamp="+lastSynTime;
            Log.v("STOCK_FETCH","getLocationList>>url:"+url);
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
