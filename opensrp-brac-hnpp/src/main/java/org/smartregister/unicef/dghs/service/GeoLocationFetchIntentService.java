package org.smartregister.unicef.dghs.service;

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
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.location.GeoLocationHelper;
import org.smartregister.unicef.dghs.location.SSModel;
import org.smartregister.unicef.dghs.utils.HnppConstants;

public class GeoLocationFetchIntentService extends IntentService {

    private static final String LOCATION_FETCH = "/provider/location-tree?";
    private static final String PA_LOCATION_FETCH = "/pa-provider/location-tree?";
    private static final String TAG = "SSLocation";

    public GeoLocationFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeoLocationFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectLocation = getLocationList();
        if(jsonObjectLocation!=null){
            if(!HnppConstants.isPALogin())HnppApplication.getGeoLocationRepository().dropTable();
            for(int i=0;i<jsonObjectLocation.length();i++){
                try {
                    JSONObject object = jsonObjectLocation.getJSONObject(i);
                    SSModel ssModel =  new Gson().fromJson(object.toString(), SSModel.class);
                    if(ssModel != null){
                        HnppApplication.getGeoLocationRepository().addOrUpdate(ssModel);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            GeoLocationHelper.getInstance().updateWardList();
        }
    }

    private JSONArray getLocationList(){
//        try{
//            String testResponse = "[{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1:KA 1\",\"id\":3354},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3351\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"2\",\"name\":\"AZIZNAGAR:WARD 1:KA 2\",\"id\":3355},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"3\",\"name\":\"AZIZNAGAR:WARD 1:KHA 1\",\"id\":3356},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"4\",\"name\":\"AZIZNAGAR:WARD 1:KHA 2\",\"id\":3357},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"5\",\"name\":\"AZIZNAGAR:WARD 1:GA 1\",\"id\":3358},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"6\",\"name\":\"AZIZNAGAR:WARD 1:GA 2\",\"id\":3359},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"7\",\"name\":\"AZIZNAGAR:WARD 1:GHA 1\",\"id\":3360},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"},{\"full_name\":\"Sumon Kanti\",\"locations\":[{\"division\":{\"code\":\"20\",\"name\":\"CHITTAGONG\",\"id\":1871},\"country\":{\"code\":\"0\",\"name\":\"BANGLADESH\",\"id\":142},\"district\":{\"code\":\"3\",\"name\":\"BANDARBAN\",\"id\":3350},\"block\":{\"code\":\"8\",\"name\":\"AZIZNAGAR:WARD 1:GHA 2\",\"id\":3361},\"union\":{\"code\":\"15\",\"name\":\"AZIZNAGAR\",\"id\":3352},\"ward\":{\"code\":\"1\",\"name\":\"AZIZNAGAR:WARD 1\",\"id\":3353},\"upazila\":{\"code\":\"51\",\"name\":\"LAMA\",\"id\":\"3355\"}}],\"team_uuid\":\"131093a2-9eab-4261-bd5d-6916f654f24c\",\"team\":\"Phaitanj Bachuuri Para Cc - Lama\"}]";
//            return new JSONArray((testResponse));
//        }catch (Exception e){
//
//        }
//        return null;

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
            String url = baseUrl + LOCATION_FETCH + "username=" + userName;
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
