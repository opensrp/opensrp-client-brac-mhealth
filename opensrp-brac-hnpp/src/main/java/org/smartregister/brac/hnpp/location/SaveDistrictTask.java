package org.smartregister.brac.hnpp.location;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.repository.DistrictListRepository;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class SaveDistrictTask extends AsyncTask {
    JSONArray locationArray = new JSONArray();
    public SaveDistrictTask(){
    }
    private String LOCATION_FETCH = "/location/district-upazila";

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
            String url = baseUrl + LOCATION_FETCH;
            Log.v("LOCATION_FETCH","getLocationList>>url:"+url);
            Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new org.apache.http.NoHttpResponseException(LOCATION_FETCH + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
    @Override
    protected Object doInBackground(Object[] objects) {
        this.locationArray = getLocationList();
        if(locationArray==null)return null;
        ArrayList<DistrictModel>dmlist = new ArrayList<>();
        for(int i=0;i<locationArray.length();i++){
            try {
                JSONObject district = locationArray.getJSONObject(i);
                String district_name = district.getString("name");
                JSONArray upazila = district.getJSONArray("upazilas");
                for(int k=0;k<upazila.length();k++){
                    DistrictModel dm = new DistrictModel();
                    dm.name = district_name;
                    dm.upazila = upazila.getString(k);
                    dmlist.add(dm);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        DistrictListRepository districtListRepository = new DistrictListRepository(HnppApplication.getInstance().getRepository());
        districtListRepository.batchInsert(dmlist);
        return null;
    }

}
