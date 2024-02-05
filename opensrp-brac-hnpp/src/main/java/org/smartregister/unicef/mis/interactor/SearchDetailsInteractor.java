package org.smartregister.unicef.mis.interactor;


import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.contract.SearchDetailsContract;
import org.smartregister.unicef.mis.model.GlobalSearchResult;
import org.smartregister.unicef.mis.utils.GlobalSearchContentData;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;

import java.util.ArrayList;
import java.util.HashMap;


public class SearchDetailsInteractor implements SearchDetailsContract.Interactor {
    private AppExecutors appExecutors;
    private GlobalSearchResult globalSearchResult;
    private static final String GLOBAL_SEARCH_URL = "/rest/event/global-search?";
    private static final String OTHER_VACCINE_URL = "rest/api/vaccination/verify";


    public SearchDetailsInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }


    @Override
    public void fetchData(GlobalSearchContentData globalSearchContentData, SearchDetailsContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            JSONObject jsonObject = getGlobalSearchMemberList(globalSearchContentData);
            if(jsonObject!=null){
                globalSearchResult = new Gson().fromJson(jsonObject.toString(), GlobalSearchResult.class);
                callBack.setGlobalSearchResult(globalSearchResult);
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(globalSearchResult.clients));
            }else{
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(new ArrayList<>()));
            }

        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchOtherVaccineData(OtherVaccineContentData otherVaccineContentData, SearchDetailsContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            JSONObject jsonObject = getOtherVaccineInfo(otherVaccineContentData);
            if(jsonObject!=null){
                OtherVaccineContentData otherVaccineContentData1 = new Gson().fromJson(jsonObject.toString(), OtherVaccineContentData.class);
                appExecutors.mainThread().execute(() -> callBack.onUpdateOtherVaccine(otherVaccineContentData1));

            }else{
                appExecutors.mainThread().execute(() -> callBack.onUpdateOtherVaccine(new OtherVaccineContentData()));

            }

        };
        appExecutors.diskIO().execute(runnable);
    }
    private JSONObject getOtherVaccineInfo(OtherVaccineContentData contentData){
        try{
            String url = BuildConfig.citizen_url +OTHER_VACCINE_URL;//+"brn="+contentData.brn+"&dob="+contentData.dob;

            JSONObject request = new JSONObject();
            request.put("brn",contentData.brn);
            request.put("dob",contentData.dob);
            String jsonPayload = request.toString();
            HashMap<String,String> headers = new HashMap<>();
            headers.put("dd",BuildConfig.dd);
           // Response response = httpAgent.fetchWithoutAuth(url);
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();

            Response<String> response = httpAgent.postWithHeaderAndJwtToken(url
                    ,
                    jsonPayload,headers,BuildConfig.JWT_TOKEN);

            HnppConstants.appendLog("GLOBAL_SEARCH_URL", "pushECToServer:response comes"+response.payload());
            if (response.isFailure()) {
                throw new NoHttpResponseException(url + " not returned data");
            }
            JSONObject jsonObject = new JSONObject((String)response.payload());
            Log.v("GLOBAL_SEARCH_URL", "jsonObject:" + jsonObject);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getGlobalSearchMemberList(GlobalSearchContentData globalSearchContentData){

        //test request
        //http://unicef-ha.mpower-social.com/opensrp/rest/event/global-search?
        // district_id=26206&division_id=24559&gender=F&mobile=01912773007&dob=1995-05-22&epi=11111111111111111&nid=1111111111
        try {
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }
            String userName = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
            if (TextUtils.isEmpty(userName)) {
                return null;
            }
            String url = "";
            if(!TextUtils.isEmpty(globalSearchContentData.getShrId())){
                url = baseUrl + GLOBAL_SEARCH_URL+"shr_id="+globalSearchContentData.getShrId();
            }else{
                url= baseUrl + GLOBAL_SEARCH_URL + "district_id="+globalSearchContentData.getDistrictId()+"&division_id="+globalSearchContentData.getDivisionId()
                        +"&upazila_id=" + globalSearchContentData.getUpozillaId() + "&gender=" + globalSearchContentData.getGender() ;
                if(!TextUtils.isEmpty(globalSearchContentData.getPhoneNo())){
                    url+="&mobile="+globalSearchContentData.getPhoneNo();
                }
                if(!TextUtils.isEmpty(globalSearchContentData.getDob())){
                    url+="&dob=" + globalSearchContentData.getDob();
                }
                if(globalSearchContentData.getId().length()>8){
                    url+="&" + globalSearchContentData.getId();
                }
            }

            /*+ "?username=" + userName;*/
            Log.v("GLOBAL_SEARCH_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(GLOBAL_SEARCH_URL + " not returned data");
            }
            JSONObject jsonObject = new JSONObject((String)resp.payload());
            Log.v("GLOBAL_SEARCH_URL", "jsonObject:" + jsonObject);
            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
