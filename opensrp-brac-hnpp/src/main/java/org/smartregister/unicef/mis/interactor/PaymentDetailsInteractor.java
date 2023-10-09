package org.smartregister.unicef.mis.interactor;

import android.text.TextUtils;
import android.util.Log;


import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.contract.PaymentContract;
import org.smartregister.unicef.mis.model.Payment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PaymentDetailsInteractor {
    private static final String PAYMENT_GET_TOKEN = "/get-token";
    private static final String PAYMENT_DETAILS_POST = "/rest/event/create-payment";
    private static final String PAYMENT_EXECUTE_POST = "/rest/event/execute-bkash-payment";
    private AppExecutors appExecutors;
    private ArrayList<String> responseList;
    private int retry = 0;

    public PaymentDetailsInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        responseList = new ArrayList<>();
    }
    public void executeBKashPayment(String paymentId, PaymentContract.PaymentPostInteractorCallBack callBack)
    {
        retry++;
        Runnable runnable = () -> {
            String token = getToken();
            if(TextUtils.isEmpty(token)){
                appExecutors.mainThread().execute(() -> callBack.onFail("Token not found"));
            }else{
                JSONObject object = postPaymentExecute(paymentId,token);
                if(object!=null){
                    try {
                        String unauthorised = object.optString("msg");
                        if(!TextUtils.isEmpty(unauthorised)){
                            appExecutors.mainThread().execute(() -> callBack.onFail(unauthorised));
                        }else{
                            String statusCode = object.getString("statusCode");
                            String statusMessage = object.getString("statusMessage");
                            if(!TextUtils.isEmpty(statusCode) && statusCode.equalsIgnoreCase("0000")) {
                                appExecutors.mainThread().execute(() -> callBack.onSuccess(statusMessage));
                            }else {
                                appExecutors.mainThread().execute(() -> callBack.onFail(statusMessage));
                            }
                        }

                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                        appExecutors.mainThread().execute(() -> callBack.onFail("Failed to execute payment"));
                    }
                }else{
                    Log.v("PAYMENT_EXECUTE","retry:"+retry);
                    if(retry>3){
                        appExecutors.mainThread().execute(() -> callBack.onFail("Failed response"));
                    }else{
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.v("PAYMENT_EXECUTE","next call:");
                        executeBKashPayment(paymentId,callBack);
                    }

                }
            }


        };
        appExecutors.diskIO().execute(runnable);


    }
    public void paymentDetailsPost(ArrayList<Payment> paymentDetails, int givenAmount, PaymentContract.PaymentPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            String token = getToken();
            if(TextUtils.isEmpty(token)){
                appExecutors.mainThread().execute(()->callBack.onFail("token not found"));
            }else{
                responseList = getPostResponseList(paymentDetails,givenAmount,token);
                boolean isSuccess = responseList.size()>1;
                if(isSuccess){
                    appExecutors.mainThread().execute(() -> callBack.onSuccess(responseList));
                }else{
                    appExecutors.mainThread().execute(()->callBack.onFail("Fail to post,unauthorised or trxId not found"));
                }
            }


        };
        appExecutors.diskIO().execute(runnable);


    }
    private ArrayList<String> getPostResponseList(ArrayList<Payment> paymentDetails, int givenAmount,String token){
        responseList.clear();
        try {
            JSONObject object = getResponseJsonObject(paymentDetails,givenAmount,token);

          if(object !=null){
            String url = object.getString("url");
            String transactionId = object.getString("trxId");
            if(!TextUtils.isEmpty(url) && !TextUtils.isEmpty(transactionId)){
                responseList.add(url);
                responseList.add(transactionId);
            }
          }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseList;
    }
    private String getToken() {
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
            String url = baseUrl + PAYMENT_GET_TOKEN;
            /*+ "?username=" + userName;*/

            Log.v("PAYMENT_GET_TOKEN", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(PAYMENT_GET_TOKEN + " not returned data");
            }
            JSONObject object = new JSONObject(resp.payload().toString());
            String token = object.getString("token_no");
                Log.v("PAYMENT_GET_TOKEN", "token:" + token);
            return token;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";

    }
    private JSONObject postPaymentExecute(String paymentId,String token){
        try{
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
            String url = baseUrl + PAYMENT_EXECUTE_POST;
            JSONObject finalobject = new JSONObject();
            finalobject.put("paymentID", paymentId);

            HashMap<String,String> header = new HashMap<>();
            header.put("tokenno",token);
            org.smartregister.domain.Response resp = httpAgent.post(url,finalobject.toString(),header);
            Log.v("PAYMENT_EXECUTE", "url:" + url+"payload:"+finalobject.toString()+":code:"+resp.status().displayValue());

            if (resp.isFailure()) {
               return null;
            }
            String responseStr = (String)resp.payload();
            Log.v("PAYMENT_EXECUTE","responseStr>>"+responseStr);
            if(!TextUtils.isEmpty(responseStr) ){
                JSONObject object = new JSONObject(resp.payload().toString());
                return object;
            }
        }catch (Exception e){
        e.printStackTrace();
        }
        return null;

    }
    private JSONObject getResponseJsonObject(ArrayList<Payment> paymentDetails, int givenAmount, String token) {
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
            String url = baseUrl + PAYMENT_DETAILS_POST;
            JSONObject request = makJsonObject(paymentDetails,givenAmount);

            HashMap<String,String> header = new HashMap<>();
            header.put("tokenno",token);

            org.smartregister.domain.Response resp = httpAgent.post(url,request.toString(),header);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(PAYMENT_DETAILS_POST + " not returned data");
            }
            String responseStr = (String)resp.payload();
            Log.v("PAYMENT_DETAILS_POST", "responseStr:" + responseStr);

            JSONObject object = new JSONObject(resp.payload().toString());
            return object;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean postData(ArrayList<String> responseList){
        for(int i=0; i<responseList.size();i++){
            if(responseList.get(i).contains("trxId")) {
                return true;
            }
        }

         return false;
    }

    private JSONObject makJsonObject(ArrayList<Payment> paymentDetails, int givenAmount) throws JSONException {
        JSONObject obj = null;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < paymentDetails.size(); i++) {
            obj = new JSONObject();
            try {
                obj.put("serviceType", paymentDetails.get(i).getServiceType());
                obj.put("serviceCode", paymentDetails.get(i).getServiceCode());
                obj.put("unitPrice", paymentDetails.get(i).getUnitPrice());
                obj.put("payFor", paymentDetails.get(i).getPayFor());

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            jsonArray.put(obj);
        }
        String providerId =  HnppApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM();
        JSONObject finalobject = new JSONObject();
        finalobject.put("providerId", providerId);
        finalobject.put("totalAmount", givenAmount);
        finalobject.put("services", jsonArray);
        finalobject.put("unique_id", JsonFormUtils.generateRandomUUIDString());
        return finalobject;
    }
}
