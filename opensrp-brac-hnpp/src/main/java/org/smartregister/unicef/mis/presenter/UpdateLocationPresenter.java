package org.smartregister.unicef.mis.presenter;
import static org.smartregister.util.JsonFormUtils.gson;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.contract.UpdateLocationContract;
import org.smartregister.unicef.mis.location.UpdateLocationModel;

import java.util.ArrayList;
import java.util.HashMap;


public class UpdateLocationPresenter implements UpdateLocationContract.Presenter {
    private static final String GET_PAURASAVA_URL = "/location/paurasava";
    private static final String GET_UNION_URL = "/location/union/";
    private static final String GET_OLD_WARD_URL = "/location/oldward/";
    private static final String GET_WARD_URL = "/location/ward/";
    private static final String UPDATE_BLOCK_URL = "/savelocation";
    AppExecutors appExecutors;
    UpdateLocationContract.View view;
    ArrayList<UpdateLocationModel>paurashavaList = new ArrayList<>();
    ArrayList<UpdateLocationModel>unionList = new ArrayList<>();
    ArrayList<UpdateLocationModel>oldWardList = new ArrayList<>();
    ArrayList<UpdateLocationModel>wardList = new ArrayList<>();

    public void setSelectedMapList(String key, ArrayList<Integer> value) {
//        ArrayList<String> getList = selectedMapList.get(key);
//        if(getList!=null){
//            if(getList.contains(value)){
//                Iterator<String> iter
//                        = getList.iterator();
//                while (iter.hasNext()) {
//                    iter.remove();
//                }
//
//            }else {
//                getList.add(value);
//            }
//        }
        selectedMapList.put(key,value);
    }

    HashMap<String, ArrayList<Integer>> selectedMapList = new HashMap<>();
    public UpdateLocationPresenter(UpdateLocationContract.View view){
        this.view = view;
        appExecutors = new AppExecutors();
    }


    public HashMap<String, ArrayList<Integer>> getSelectedIds() {
        return selectedMapList;
    }

    @Override
    public ArrayList<UpdateLocationModel> getPaurashavaList() {
        return paurashavaList;
    }

    @Override
    public ArrayList<UpdateLocationModel> getUnion() {
        return unionList;
    }

    @Override
    public ArrayList<UpdateLocationModel> getOldWard() {
        return oldWardList;
    }

    @Override
    public ArrayList<UpdateLocationModel> getWard() {
        return wardList;
    }

    @Override
    public void processPaurasava() {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                paurashavaList = getLocationListFromServer(GET_PAURASAVA_URL);
                Log.v("JSON array: ",paurashavaList.size()+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.updatePaurashovaAdapter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void processUnion(String paurasavaId) {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                unionList = getLocationListFromServer(GET_UNION_URL+""+paurasavaId);
                Log.v("JSON array: ","unionList"+unionList.size()+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.updateUnionAdapter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void processOldWard(String unionId) {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                oldWardList = getLocationListFromServer(GET_OLD_WARD_URL+""+unionId);
                Log.v("JSON array: ","oldWardList"+oldWardList.size()+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.updateOldWardAdapter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void processWard(String oldWardId) {
        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                wardList = getLocationListFromServer(GET_WARD_URL+""+oldWardId);
                Log.v("JSON array: ","wardList"+wardList.size()+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    view.updateWardAdapter();
                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void updateLocation() {

        view.showProgressBar();
        Runnable runnable = () -> {
            try {
                String jsonObject = submitLocation();
                Log.v("JSON array: ",jsonObject+"");

                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();
                    boolean response = jsonObject != null && jsonObject.equalsIgnoreCase("true");
                    if(response){
                        view.onBlockUpdated(true);
                    }else{
                        view.onBlockUpdated(false);
                    }


                });
            } catch (Exception e) {
                e.printStackTrace();
                appExecutors.mainThread().execute(() ->{
                    view.hideProgressBar();

                });
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public UpdateLocationContract.View getView() {
        return view;
    }

    private String submitLocation(){
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
//            String password = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityId(userName);
            //testing
            String url = baseUrl + UPDATE_BLOCK_URL ;

            Log.v("LOCATION_UPDATE","url:"+url);
           //{"location":{"oldward":[58416],"union":[58415],"ward":[58417,58420],"paurasava":[58414]}}
            JSONObject request = new JSONObject();
            request.put("location",gson.toJson(getSelectedIds()));
            String jsonPayload = request.toString();
            jsonPayload = jsonPayload.replace("\\\"","\"").replace("\"{","{").replace("}\"","}");
            Log.v("LOCATION_UPDATE","url:"+url+":jsonPayload:"+jsonPayload);
            org.smartregister.domain.Response resp = httpAgent.post(url,jsonPayload);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(UPDATE_BLOCK_URL + " not returned data");
            }


            return (String) resp.payload();
        }catch (Exception e){
            e.printStackTrace();

        }
        return null;

    }

    private ArrayList<UpdateLocationModel> getLocationListFromServer(String locationUrl){
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
            String url = baseUrl + locationUrl;

            Log.v("UPDATE_LOCATION","url:"+url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(url + " not returned data");
            }
            JSONArray jsonArray = new JSONArray((String) resp.payload());
           int length = jsonArray.length();
            ArrayList<UpdateLocationModel> locationModels = new ArrayList<>();
            for (int i=0;i< length;i++){
                UpdateLocationModel model = gson.fromJson(jsonArray.getJSONObject(i).toString(), UpdateLocationModel.class);
                locationModels.add(model);
            }


            return locationModels;
        }catch (Exception e){

        }
        return null;

    }

}

