package org.smartregister.brac.hnpp.interactor;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.utils.District;
import org.smartregister.brac.hnpp.utils.Pouroshova;
import org.smartregister.brac.hnpp.utils.Union;
import org.smartregister.brac.hnpp.utils.Upazila;
import org.smartregister.brac.hnpp.utils.Village;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class MigrationInteractor implements MigrationContract.Interactor{
    private AppExecutors appExecutors;
    private ArrayList<District> districtArrayList;
    private ArrayList<Upazila> upazilaArrayList;
    private ArrayList<Pouroshova> pouroshovaArrayList;
    private ArrayList<Union> unionArrayList;
    private ArrayList<Village> villageArrayList;
    private static final String DISTRICT_URL = "/location/district-list";
    private static final String CHILD_URL = "/location/child-location?";


    public MigrationInteractor(AppExecutors appExecutors){
        this.appExecutors = appExecutors;
    }

    @Override
    public void fetchDistrict(MigrationContract.InteractorCallBack callBack){
        JSONArray jsonArray = getDistrictList();
        Log.v("District JSON array: ",jsonArray+"");

        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                        District district =  new Gson().fromJson(object.toString(), District.class);
                        if(district != null){
                            districtArrayList.add(district);

                        }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = () -> {

            appExecutors.mainThread().execute(() -> callBack.onUpdateDistrict(districtArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchUpazila(MigrationContract.InteractorCallBack callBack) {
        int id;
        District district = new District();
        id = district.getId();
        JSONArray jsonArray = getChildList(id);
        Log.v("Upazila JSON array: ",jsonArray+"");

        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Upazila upazila =  new Gson().fromJson(object.toString(), Upazila.class);
                if(district != null){
                    upazilaArrayList.add(upazila);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = () -> {

            appExecutors.mainThread().execute(() -> callBack.onUpdateUpazila(upazilaArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchPouroshova(MigrationContract.InteractorCallBack callBack) {
        int id;
        Upazila upazila = new Upazila();
        id = upazila.getId();
        JSONArray jsonArray = getChildList(id);
        Log.v("Pouroshova JSON array: ",jsonArray+"");

        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Pouroshova pouroshova =  new Gson().fromJson(object.toString(), Pouroshova.class);
                if(pouroshova != null){
                    pouroshovaArrayList.add(pouroshova);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = () -> {

            appExecutors.mainThread().execute(() -> callBack.onUpdatePouroshova(pouroshovaArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchUnion(MigrationContract.InteractorCallBack callBack) {
        int id;
        Pouroshova pouroshova = new Pouroshova();
        id = pouroshova.getId();
        JSONArray jsonArray = getChildList(id);
        Log.v("Union JSON array: ",jsonArray+"");

        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Union union =  new Gson().fromJson(object.toString(), Union.class);
                if(union != null){
                    unionArrayList.add(union);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = () -> {

            appExecutors.mainThread().execute(() -> callBack.onUpdateUnion(unionArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchVillage(MigrationContract.InteractorCallBack callBack) {
        int id;
        Union union = new Union();
        id = union.getId();
        JSONArray jsonArray = getChildList(id);
        Log.v("Village JSON array: ",jsonArray+"");

        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Village village =  new Gson().fromJson(object.toString(), Village.class);
                if(village != null){
                    villageArrayList.add(village);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Runnable runnable = () -> {

            appExecutors.mainThread().execute(() -> callBack.onUpdateVillage(villageArrayList));
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getDistrictList(){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }

            String url = baseUrl + DISTRICT_URL;

            Log.v("District Fetch","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(DISTRICT_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
    private JSONArray getChildList(int id){
        try{
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            String baseUrl = CoreLibrary.getInstance().context().
                    configuration().dristhiBaseURL();
            String endString = "/";
            if (baseUrl.endsWith(endString)) {
                baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
            }

            String url = baseUrl + CHILD_URL+"id="+id;

            Log.v("CHILD Fetch","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(CHILD_URL + " not returned data");
            }

            return new JSONArray((String) resp.payload());
        }catch (Exception e){

        }
        return null;

    }
}
