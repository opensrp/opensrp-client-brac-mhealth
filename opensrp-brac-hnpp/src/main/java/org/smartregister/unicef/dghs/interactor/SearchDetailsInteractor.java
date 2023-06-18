package org.smartregister.unicef.dghs.interactor;


import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.dghs.contract.SearchDetailsContract;
import org.smartregister.unicef.dghs.model.GlobalSearchResult;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;


public class SearchDetailsInteractor implements SearchDetailsContract.Interactor {
    private AppExecutors appExecutors;
    private GlobalSearchResult globalSearchResult;
    private static final String GLOBAL_SEARCH_URL = "/rest/event/global-search?";


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
            if(!globalSearchContentData.getShrId().isEmpty()){
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
            return jsonObject;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
