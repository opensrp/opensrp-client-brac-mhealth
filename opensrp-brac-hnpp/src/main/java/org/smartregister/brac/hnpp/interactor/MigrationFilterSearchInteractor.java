package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.utils.BaseLocation;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.Collections;

public class MigrationFilterSearchInteractor implements MigrationContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<BaseLocation> districtArrayList = new ArrayList<>();
    private ArrayList<BaseLocation> upazilaArrayList = new ArrayList<>();
    private ArrayList<BaseLocation> pouroshovaArrayList = new ArrayList<>();
    private ArrayList<BaseLocation> unionArrayList= new ArrayList<>();
    private ArrayList<BaseLocation> villageArrayList= new ArrayList<>();
    private static final String DISTRICT_URL = "/location/district-list";
    private static final String CHILD_URL = "/location/child-location?";

    public enum LOCATION_TYPE {
        DISTRICT, UPOZILA, POUROSHOVA, UNION,VILLAGE;

    }


    public MigrationFilterSearchInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    private void addDistrict(){
        districtArrayList.clear();
        JSONArray jsonArray = getDistrictList();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                BaseLocation district = new Gson().fromJson(object.toString(), BaseLocation.class);
                if (district != null) {
                    districtArrayList.add(district);
                }
                Collections.sort(districtArrayList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void addChild(String childId,LOCATION_TYPE locationType ){
        JSONArray jsonArray = getChildList(childId);
        switch (locationType){
            case UPOZILA:
                upazilaArrayList.clear();
                break;
            case POUROSHOVA:
                pouroshovaArrayList.clear();
                break;
            case UNION:
                unionArrayList.clear();
                break;
            case VILLAGE:
                villageArrayList.clear();
                break;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                BaseLocation baseLocation = new Gson().fromJson(object.toString(), BaseLocation.class);
                if(baseLocation!=null){
                    switch (locationType){
                        case UPOZILA:
                            upazilaArrayList.add(baseLocation);
                            break;
                        case POUROSHOVA:
                            pouroshovaArrayList.add(baseLocation);
                            break;
                        case UNION:
                            unionArrayList.add( baseLocation);
                            break;
                        case VILLAGE:
                            villageArrayList.add( baseLocation);
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fetchDistrict(MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            addDistrict();
            appExecutors.mainThread().execute(() -> callBack.onUpdateDistrict(districtArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchUpazila(String jilaId, MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            addChild(jilaId,LOCATION_TYPE.UPOZILA);

            appExecutors.mainThread().execute(() -> callBack.onUpdateUpazila(upazilaArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchPouroshova(String upojilaId, MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            addChild(upojilaId,LOCATION_TYPE.POUROSHOVA);

            appExecutors.mainThread().execute(() -> callBack.onUpdatePouroshova(pouroshovaArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchUnion(String pourosovaId, MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            addChild(pourosovaId,LOCATION_TYPE.UNION);

            appExecutors.mainThread().execute(() -> callBack.onUpdateUnion(unionArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchVillage(String unionId, MigrationContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            addChild(unionId,LOCATION_TYPE.VILLAGE);


            appExecutors.mainThread().execute(() -> callBack.onUpdateVillage(villageArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getDistrictList() {
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
            String url = baseUrl + DISTRICT_URL;
                    /*+ "?username=" + userName;*/

            Log.v("DISTRICT_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(DISTRICT_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private JSONArray getChildList(String  id) {
        try {
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }

            String url = baseUrl + CHILD_URL + "id=" + id;

            Log.v("CHILD_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(CHILD_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        } catch (Exception e) {

        }
        return null;

    }
}
