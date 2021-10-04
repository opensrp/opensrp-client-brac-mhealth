package org.smartregister.brac.hnpp.interactor;


import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.contract.SearchDetailsContract;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.brac.hnpp.utils.HnppConstants;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class SearchDetailsInteractor implements SearchDetailsContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<Migration> migrationArrayList = new ArrayList<>();
    private static final String MEMBER_URL = "/rest/client/search-client?";


    public SearchDetailsInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }
    private void addMember(String type,String districtId, String villageId, String gender, String startAge, String age){
        migrationArrayList.clear();
        JSONArray jsonArray = getMigrationMemberList(type,districtId,villageId, gender,startAge, age);
        if(jsonArray!=null){
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Migration migration = new Gson().fromJson(object.toString(), Migration.class);
                    if (migration != null) {
                        migrationArrayList.add(migration);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @Override
    public void fetchData(String type,String districtId,String villageId, String gender,String startAge, String age, SearchDetailsContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            addMember(type,districtId,villageId, gender,startAge, age);
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(migrationArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getMigrationMemberList(String type,String districtId,String villageId, String gender, String startAge,String age){
        //test data
//        villageId = "9315";
//        gender ="F";
//        age ="50";
        if(type.equalsIgnoreCase(HnppConstants.MIGRATION_TYPE.HH.name())){
            gender ="";
            startAge ="-1";
            age ="-1";
        }
        if(startAge.isEmpty()|startAge.equals("0")){
            startAge = "-1";
        }
        if(age.isEmpty()|age.equals("0")){
            age = "-1";
        }
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
            String url = baseUrl + MEMBER_URL + "username='"+userName+"'&district="+districtId+"&villageId=" + villageId + "&gender=" + gender + "&startAge="+startAge+"&endAge=" + age + "&type="+type;
            /*+ "?username=" + userName;*/
            Log.v("MEMBER_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(MEMBER_URL + " not returned data");
            }
            JSONObject jsonObject = new JSONObject((String)resp.payload());
            JSONArray jsonArray = jsonObject.getJSONArray("clients");
            return jsonArray;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
