package org.smartregister.unicef.mis.service;

import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.DISABILITY_ENABLE;
import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.IS_URBAN;
import static org.smartregister.unicef.mis.utils.HnppConstants.KEY.USER_ID;

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
import org.smartregister.unicef.mis.location.HALocationHelper;
import org.smartregister.unicef.mis.location.SSModel;
import org.smartregister.unicef.mis.utils.HnppConstants;

public class HALocationFetchIntentService extends IntentService {

    private static final String LOCATION_FETCH = "/provider/location-tree?";
    private static final String PA_LOCATION_FETCH = "/pa-provider/location-tree?";
    private static final String TAG = "SSLocation";
    public static final String LOCATION_UPDATE = "LOCATION_UPDATE";

    public HALocationFetchIntentService() { super(TAG); }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HALocationFetchIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        JSONArray jsonObjectLocation = getLocationList();
        if(jsonObjectLocation !=null && jsonObjectLocation.length()==0){
            broadcastStatus("Need to location update");
            return;
        }
        if(jsonObjectLocation!=null){
            if(!HnppConstants.isPALogin())HnppApplication.getHALocationRepository().dropTable();
            for(int i=0;i<jsonObjectLocation.length();i++){
                try {
                    JSONObject object = jsonObjectLocation.getJSONObject(i);
//                    if(object!=null && object.has("msg")){
//                        broadcastStatus("area not found");
//                        return;
//                    }
                    SSModel ssModel =  new Gson().fromJson(object.toString(), SSModel.class);
                    if(ssModel != null){
                        if(!TextUtils.isEmpty(ssModel.user_id)){
                            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(USER_ID,ssModel.user_id+"");
                        }
                        if(ssModel.locations.get(0).district.name.contains("CITY CORPORATION")
                                || !ssModel.locations.get(0).paurasava.name.contains("NO PAURASAVA")){
                            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(IS_URBAN,"true");
                        }
                        if(ssModel.locations.get(0).district.name.equalsIgnoreCase("DHAKA NORTH CITY CORPORATION")
                                && !ssModel.locations.get(0).union.name.equalsIgnoreCase("Zone-4")){
                            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(DISABILITY_ENABLE,"true");
                        }

                        HnppApplication.getHALocationRepository().addOrUpdate(ssModel);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            HALocationHelper.getInstance().updateWardList();
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
            Log.v("LOCATION_UPDATE","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(LOCATION_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){
            e.printStackTrace();

        }
        return null;

    }
    private void broadcastStatus(String message){
        try{
            Intent broadcastIntent = new Intent(LOCATION_UPDATE);
            broadcastIntent.putExtra("PUT_EXTRA", message);
            Log.v("LOCATION_UPDATE","sendBroadcast");
            sendBroadcast(broadcastIntent);
        }catch (Exception e){
            e.printStackTrace();

        }

    }
}
