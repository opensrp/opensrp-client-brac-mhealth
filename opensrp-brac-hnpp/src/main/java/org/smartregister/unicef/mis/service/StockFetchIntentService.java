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
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.R;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.StockData;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

public class StockFetchIntentService extends IntentService {

    private static final String LOCATION_FETCH = "/get_stock_info?";
    private static final String TAG = "StockFetchIntentService";
    public static final String LAST_SYNC_TIME = "stock_last_sync_time";

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
        StringBuilder nameCount = new StringBuilder();
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
                        try{
                            nameCount.append(getString(R.string.stock_name)+HnppConstants.getWorkSummeryTypeMapping().get(stockData.getProductName())+"\n");
                        }catch (Exception e){
                            nameCount.append(getString(R.string.stock_name)+stockData.getProductName()+"\n");

                        }

                        nameCount.append(getString(R.string.stock_no)+stockData.getQuantity()+"\n");
                        Log.v("STOCK_FETCH","lasttime:"+timestamp);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(jsonObjectLocation.length()>0){
                if(nameCount.length() > 0){
                    HnppConstants.insertAtNotificationTable(getString(R.string.new_stock),nameCount.toString());
                    intent = new Intent(HnppConstants.ACTION_STOCK_COME);
                    intent.putExtra(HnppConstants.EXTRA_STOCK_COME, nameCount.toString());
                    sendBroadcast(intent);

                }
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
