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
import org.smartregister.brac.hnpp.model.HHVisitDurationModel;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

public class HHVisitDurationFetchIntentService extends IntentService {

    private static final String HH_VISIT_DURATION_FETCH = "/rest/event/form-visible-rule";
    private static final String TAG = "HHVisitDuration";

    public HHVisitDurationFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HHVisitDurationFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectLocation = getHHVisitDurationList();
        if(jsonObjectLocation!=null){
            HnppApplication.getHHVisitDurationRepository().dropTable();
            for(int i=0;i<jsonObjectLocation.length();i++){
                try {
                    JSONObject object = jsonObjectLocation.getJSONObject(i);
                    HHVisitDurationModel model =  new Gson().fromJson(object.toString(), HHVisitDurationModel.class);
                    if(model != null){
                        HnppApplication.getHHVisitDurationRepository().addOrUpdate(model);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            SSLocationHelper.getInstance().updateModel();



        }

    }

    private JSONArray getHHVisitDurationList(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }

            String url;
            url = baseUrl + HH_VISIT_DURATION_FETCH;
            Log.v("HH_VISIT_FETCH","getHhVisitList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(HH_VISIT_DURATION_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){
            Log.v("HH_VISIT_FETCH_ERRRR","getHhVisitList>>url:"+e.getMessage());
        }
        return null;

    }
}
