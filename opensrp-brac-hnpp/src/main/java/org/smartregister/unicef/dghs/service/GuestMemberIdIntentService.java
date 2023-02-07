package org.smartregister.unicef.dghs.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.unicef.dghs.HnppApplication;
import org.smartregister.unicef.dghs.domain.HouseholdId;
import org.smartregister.unicef.dghs.exception.PullHouseholdIdsException;
import org.smartregister.unicef.dghs.repository.GuestMemberIdRepository;
import org.smartregister.unicef.dghs.repository.HouseholdIdRepository;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.List;

public class GuestMemberIdIntentService extends IntentService {
    public static final String ID_URL = "/household/guest/generated-code";
    public static final String IDENTIFIERS = "identifiers";
    private static final String TAG = GuestMemberIdIntentService.class.getCanonicalName();
    private GuestMemberIdRepository householdIdRepo;

    //    @Override
//    public void onCreate() {
//        super.onCreate();
//        startForeground(1,new Notification());
//    }
    public GuestMemberIdIntentService() {
        super("GuestMemberIdIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            JSONArray ids = fetchOpenMRSIds();
            if (ids != null && ids.length()>0) {
                parseResponse(ids);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
//            try {
//                parseResponse(new JSONArray(IDUtils.hhids));
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
        }
    }

    private JSONArray fetchOpenMRSIds() throws Exception {
        HTTPAgent httpAgent = HnppApplication.getInstance().getContext().getHttpAgent();
        String baseUrl = HnppApplication.getInstance().getContext().
                configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }
        String userName = HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        String vid = householdIdRepo.getUnusedVillageId();
        if(vid.equalsIgnoreCase("-1")){
            return new JSONArray();
        }
        String url = baseUrl + ID_URL + "?villageId=" + vid + "&username=" + userName;
        Log.i(GuestMemberIdIntentService.class.getName(), "URL: " + url);

        if (httpAgent == null) {
            throw new PullHouseholdIdsException(ID_URL + " http agent is null");
        }

        Response resp = httpAgent.fetch(url);
        if (resp.isFailure()) {
            throw new PullHouseholdIdsException(ID_URL + " not returned data");
        }
        Log.i(GuestMemberIdIntentService.class.getName(), "response: " + (String) resp.payload());
        return new JSONArray((String) resp.payload());
    }

    private void parseResponse(JSONArray jsonArray) throws Exception {

        if (jsonArray != null && jsonArray.length() > 0) {


            List<HouseholdId> ids = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.has("village_id")&&jsonObject.has("generated_code")){
                    String village_id = jsonObject.getString("village_id");
                    JSONArray generated_code = jsonObject.getJSONArray("generated_code");

                    for(int k=0;k<generated_code.length();k++){
                        HouseholdId hhid = new HouseholdId();
                        hhid.setOpenmrsId(generated_code.getString(k));
                        hhid.setVillageId(village_id);
                        ids.add(hhid);
                    }
                }

            }
            householdIdRepo.bulkInserOpenmrsIds(ids);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        householdIdRepo = HnppApplication.getHNPPInstance().getGuestMemberIdRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
