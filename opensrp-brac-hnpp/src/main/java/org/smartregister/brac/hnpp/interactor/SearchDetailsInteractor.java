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
import org.smartregister.brac.hnpp.utils.District;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class SearchDetailsInteractor implements SearchDetailsContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<Migration> migrationArrayList;
    private static final String MEMBER_URL = "/rest/client/search-client?villageId=9315&gender=F&startAge=0&endAge=50&type=Member";

    public SearchDetailsInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }

    @Override
    public void fetchData(SearchDetailsContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            JSONArray jsonArray = getMigrationMemberList();
            Log.v("Member JSON array: ", jsonArray + "");

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
            appExecutors.mainThread().execute(() -> callBack.onUpdateList(migrationArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getMigrationMemberList(){
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
            String url = baseUrl + MEMBER_URL;
            /*+ "?username=" + userName;*/

            Log.v("Migration Member Fetch", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(MEMBER_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
