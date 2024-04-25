package org.smartregister.unicef.mis.interactor;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.domain.Response;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.service.HTTPAgent;
import org.smartregister.unicef.mis.BuildConfig;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.DashBoardContract;
import org.smartregister.unicef.mis.contract.HPVImmunizationContract;
import org.smartregister.unicef.mis.contract.SearchDetailsContract;
import org.smartregister.unicef.mis.location.HALocation;
import org.smartregister.unicef.mis.location.HPVLocation;
import org.smartregister.unicef.mis.model.HPVImmunizationModel;
import org.smartregister.unicef.mis.utils.DashBoardData;
import org.smartregister.unicef.mis.utils.HPVCenterList;
import org.smartregister.unicef.mis.utils.HPVEnrolmentData;
import org.smartregister.unicef.mis.utils.HPVResponse;
import org.smartregister.unicef.mis.utils.HnppConstants;
import org.smartregister.unicef.mis.utils.HnppDBUtils;
import org.smartregister.unicef.mis.utils.OtherVaccineContentData;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class HPVImmunizationInteractor implements HPVImmunizationContract.Interactor {

    private static final String TAG = "HPV_IMMUNIZATION";

    private AppExecutors appExecutors;
    private ArrayList<HPVLocation> centerList;
    private static final String CENTER_SEARCH_URL = "rest/api/vaccination/centerlist/";
    private static final String POST_ENROLMENT_URL = "rest/api/vaccination/enrolment";
    private static final String OTHER_VACCINE_URL = "rest/api/vaccination/verify";

    public HPVImmunizationInteractor(){
        this.appExecutors = new AppExecutors();
        centerList = new ArrayList<>();
    }

    public ArrayList<HPVLocation> getCenterList() {
        return centerList;
    }

    public void fetchCenterList(String baseEntityData, HPVImmunizationContract.InteractorCallBack callBack){

        Runnable runnable = () -> {
            String blockId =  HnppDBUtils.getBlocksIdFromMember(baseEntityData);
            HALocation selectedLocation = HnppApplication.getHALocationRepository().getLocationByBlock(blockId);
            String centerListGetId ="";
            if(selectedLocation!=null){
                //div,dis,upozila,paurasava,union,old_ward,ward codes
                centerListGetId =selectedLocation.division.code+""+selectedLocation.district.code+""+selectedLocation.upazila.code+""+selectedLocation.paurasava.code+""+selectedLocation.union.code+""+selectedLocation.old_ward.code+""+selectedLocation.ward.code;
                Log.v(TAG,"centerListGetId>>>"+centerListGetId);

            }
            //test
//            centerListGetId = "30567899239999";
            JSONObject jsonObject = getCenterList(centerListGetId);
            if(jsonObject!=null){
               HPVCenterList centerList = new Gson().fromJson(jsonObject.toString(), HPVCenterList.class);
               if(centerList.location.size()>0){
                   appExecutors.mainThread().execute(() -> callBack.onUpdateList(centerList.location));
               }else{
                   appExecutors.mainThread().execute(() -> callBack.onUpdateList(new ArrayList<>()));
               }

            }else{
                appExecutors.mainThread().execute(() -> callBack.onUpdateList(new ArrayList<>()));

            }

        };
        appExecutors.diskIO().execute(runnable);
    }
    public void postEnrolmentData(String baseEntityData, HPVLocation centerLocation,HPVImmunizationContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            HPVEnrolmentData enrolmentData = createEnrolmentData(baseEntityData,centerLocation);
            JSONObject results = postEnrolmentData(enrolmentData);
            if(results!=null){
                HPVResponse hpvResponse = new Gson().fromJson(results.toString(), HPVResponse.class);
                if(hpvResponse.status.equalsIgnoreCase("CM")){
                    appExecutors.mainThread().execute(()->callBack.enrolSuccessfully(hpvResponse.message));
                }else{
                    appExecutors.mainThread().execute(()->callBack.enrolFail(hpvResponse.message));
                }
            }else{
                appExecutors.mainThread().execute(()->callBack.enrolFail("No response found"));
            }


        };
        appExecutors.diskIO().execute(runnable);

    }
    public void fetchOtherVaccineData(String baseEntityData, HPVImmunizationContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            OtherVaccineContentData memberInfo = HnppDBUtils.getMemberInfo(baseEntityData);
            if(memberInfo == null){
                appExecutors.mainThread().execute(() -> callBack.onUpdateOtherVaccine(null));
            }else{
                JSONObject jsonObject = getOtherVaccineInfo(memberInfo.brn,memberInfo.dob);
                if(jsonObject!=null){
                    OtherVaccineContentData otherVaccineContentData1 = new Gson().fromJson(jsonObject.toString(), OtherVaccineContentData.class);
                    appExecutors.mainThread().execute(() -> callBack.onUpdateOtherVaccine(otherVaccineContentData1));

                }else{
                    appExecutors.mainThread().execute(() -> callBack.onUpdateOtherVaccine(null));

                }
            }


        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONObject getCenterList(String code){
        try{
            String url = BuildConfig.citizen_url +CENTER_SEARCH_URL+code;//https://api-training-vaxepi.dghs.gov.bd/rest/api/vaccination/centerlist/30567899239999

            // Response response = httpAgent.fetchWithoutAuth(url);
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();

            Response<String> response = httpAgent.fetchWithoutAuth(url);

            HnppConstants.appendLog(TAG, "getCenterList"+response.payload());
            if (response.isFailure()) {
                throw new NoHttpResponseException(url + " not returned data");
            }
            JSONObject jsonObject = new JSONObject((String)response.payload());
            Log.v(TAG, "jsonObject:" + jsonObject);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private  JSONObject postEnrolmentData(HPVEnrolmentData enrolmentData){
        String jsonPayload = JsonFormUtils.gson.toJson(enrolmentData);

        Log.v(TAG,"postEnrolmentData>>"+jsonPayload);
        try{
            String add_url =  MessageFormat.format("{0}{1}",
                    BuildConfig.citizen_url,
                    POST_ENROLMENT_URL);
            jsonPayload = jsonPayload.replace("\\","").replace("\"[","[").replace("]\"","]");
            HTTPAgent httpAgent = CoreLibrary.getInstance().context().getHttpAgent();
            HashMap<String,String> headers = new HashMap<>();
            headers.put("dd",BuildConfig.dd);
            Log.v(TAG,"jsonPayload after replace>>>"+jsonPayload);
            Response<String> response = httpAgent.postWithHeaderAndJwtToken(add_url, jsonPayload,headers,BuildConfig.JWT_TOKEN);
            if (response.isFailure() || response.isTimeoutError()) {
                HnppConstants.appendLog(TAG, "message>>"+response.payload()+"status:"+response.status().displayValue());
                return null;
            }
            HnppConstants.appendLog("SYNC_URL", "pushECToServer:response comes"+response.payload());
            //{"error":[],"notFound":[]}
            JSONObject results = new JSONObject((String) response.payload());
            return results;



        }catch (Exception e){
            e.printStackTrace();

        }
        return null;
    }


    private JSONObject getOtherVaccineInfo(String brn, String dob){
        try{
            String url = BuildConfig.citizen_url +OTHER_VACCINE_URL;//+"brn="+contentData.brn+"&dob="+contentData.dob;

            JSONObject request = new JSONObject();
            request.put("brn",brn);
            request.put("dob",dob);
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
    private HPVEnrolmentData createEnrolmentData(String baseEntityData, HPVLocation centerLocation) {
//        {"firstNameBN":"test","firstName":"testdd","lastName":"teteddt","dob":"2010-02-02","brn":"99020230301400007","mobile":"01637373737","residentType":"bangladeshi",
//                "motherName":"Jeny De","motherNid":"1215252525","fatherName":"ff","fatherNameEn":"ddd","mothernameEn":"rere","gender":"F","village":"",
//                "division":"DHAKA","district":"DHAKA DISTRICT","upazila":"DHAMRAI","paurasava":"No Paurasava","union_name":"AMTA","ward":"WARD 7","divisionId":"24545",
//                "districtId":"24546","upazilaId":"42808","paurasavaId":"113203","unionId":"113204","wardId":"113230","schoolId":"167433",
//                "school":"sfssaf","education":"six","educationType":"OUTREACH"}
        OtherVaccineContentData memberInfo = HnppDBUtils.getMemberInfo(baseEntityData);
        HPVEnrolmentData enrolmentData = new HPVEnrolmentData();
        if(memberInfo!=null){
            enrolmentData.brn = memberInfo.brn;
            enrolmentData.dob = memberInfo.dob;
            enrolmentData.gender = memberInfo.gender;
            enrolmentData.firstName = memberInfo.firstName;
            enrolmentData.lastName = memberInfo.lastName;
            enrolmentData.fatherName = memberInfo.fatherName;
            enrolmentData.motherName = memberInfo.motherName;
        }
        else{
            return null;
        }

        if(centerLocation!=null){
            enrolmentData.division = centerLocation.division;
            enrolmentData.divisionId = centerLocation.division_id+"";
            enrolmentData.district = centerLocation.district;
            enrolmentData.districtId = centerLocation.district_id+"";
            enrolmentData.upazila = centerLocation.cc_upazila;
            enrolmentData.upazilaId = centerLocation.cc_upazila_id+"";
            enrolmentData.paurasava = centerLocation.paurasava;
            enrolmentData.paurasavaId = centerLocation.paurasava_id+"";
            enrolmentData.union_name = centerLocation.union_name;
            enrolmentData.unionId = centerLocation.union_id+"";
            enrolmentData.ward = centerLocation.ward;
            enrolmentData.wardId = centerLocation.ward_id+"";
        }

        return enrolmentData;
    }

}
