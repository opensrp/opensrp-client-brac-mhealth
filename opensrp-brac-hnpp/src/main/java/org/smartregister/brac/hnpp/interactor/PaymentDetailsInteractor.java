package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.contract.PaymentContract;
import org.smartregister.brac.hnpp.utils.MigrationSearchContentData;
import org.smartregister.brac.hnpp.utils.PaymentDetails;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class PaymentDetailsInteractor {
    private static final String PAYMENT_DETAILS_POST = "/rest/event/payment-pending";
    private AppExecutors appExecutors;

    public PaymentDetailsInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }
    public void paymentDetailsPost(ArrayList<PaymentDetails> paymentDetails, int givenAmount, PaymentContract.PaymentPostInteractorCallBack callBack)
    {
        Runnable runnable = () -> {
            //Client baseClient = generateHHClient(migrationSearchContentData);

            boolean isSuccess = postData(paymentDetails,givenAmount);
            if(isSuccess){
                appExecutors.mainThread().execute(callBack::onSuccess);
            }else{
                appExecutors.mainThread().execute(callBack::onFail);
            }

        };
        appExecutors.diskIO().execute(runnable);


    }
    private boolean postData(ArrayList<PaymentDetails> paymentDetails, int givenAmount) {
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
                return false;
            }
            String url = baseUrl + PAYMENT_DETAILS_POST;
            JSONObject request = makJsonObject(paymentDetails,givenAmount);

            Log.v("MIGRATION_POST", "url:" + url+"payload:"+request.toString());

            org.smartregister.domain.Response resp = httpAgent.post(url,request.toString());
            if (resp.isFailure()) {
                throw new NoHttpResponseException(PAYMENT_DETAILS_POST + " not returned data");
            }
            String responseStr = (String)resp.payload();
            Log.v("PAYMENT_DETAILS_POST", "responseStr:" + responseStr);
            if(responseStr.equalsIgnoreCase("ok")){
                return true;
            }
/*
            String json = new Gson().toJson(baseClient);
            ArrayList<String> list = new ArrayList<>();
            list.add(json);
            JSONObject request = new JSONObject();
            request.put(AllConstants.KEY.CLIENTS,list);

            Log.v("MIGRATION_POST", "url:" + url+"payload:"+request.toString());
            org.smartregister.domain.Response resp = httpAgent.post(url,request.toString());
            if (resp.isFailure()) {
                throw new NoHttpResponseException(MIGRATION_POST + " not returned data");
            }
            String responseStr = (String)resp.payload();
            Log.v("MIGRATION_POST", "responseStr:" + responseStr);
            if(responseStr.equalsIgnoreCase("ok")){
                return true;
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONObject makJsonObject(ArrayList<PaymentDetails> paymentDetails, int givenAmount) throws JSONException {
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
        finalobject.put("student", jsonArray);
        return finalobject;
    }
}
