package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;


import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.PaymentContract;
import org.smartregister.brac.hnpp.model.Payment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class PaymentDetailsInteractor {
    private static final String PAYMENT_DETAILS_POST = "/rest/event/create-payment";
    private AppExecutors appExecutors;
    private ArrayList<String> responseList;

    public PaymentDetailsInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        responseList = new ArrayList<>();
    }
    public void paymentDetailsPost(ArrayList<Payment> paymentDetails, int givenAmount, PaymentContract.PaymentPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            responseList = getPostResponseList(paymentDetails,givenAmount);
            boolean isSuccess = postData(responseList);
            if(isSuccess){
                appExecutors.mainThread().execute(() -> callBack.onSuccess(responseList));
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }
    private ArrayList<String> getPostResponseList(ArrayList<Payment> paymentDetails, int givenAmount){
        responseList.clear();
        try {
            JSONObject object = getResponseJsonObject(paymentDetails,givenAmount);

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
    private JSONObject getResponseJsonObject(ArrayList<Payment> paymentDetails, int givenAmount) {
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

            Log.v("PAYMENT_DETAILS_POST", "url:" + url+"payload:"+request.toString());

            org.smartregister.domain.Response resp = httpAgent.post(url,request.toString());
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
        return finalobject;
    }
}
