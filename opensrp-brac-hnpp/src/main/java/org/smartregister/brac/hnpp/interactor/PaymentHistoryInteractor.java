package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.contract.PaymentHistoryContract;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class PaymentHistoryInteractor implements PaymentHistoryContract.Interactor{
    private static final String API_TO_GET_PAYMENT_HISTORY = "/rest/event/bkash-payment-history?timestamp=0";
    private AppExecutors appExecutors;
    private ArrayList<PaymentHistory> paymentHistoryArrayList;

    public PaymentHistoryInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.paymentHistoryArrayList = new ArrayList<>();
    }

    private ArrayList<PaymentHistory> getPaymentServiceList(){
        paymentHistoryArrayList.clear();
        JSONArray jsonArray = getPaymentServiceJsonArrayList();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                PaymentHistory paymentHistory = new Gson().fromJson(object.toString(), PaymentHistory.class);
                if (paymentHistory != null) {
                    paymentHistoryArrayList.add(paymentHistory);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return paymentHistoryArrayList;
    }

    @Override
    public ArrayList<PaymentHistory> getPaymentHistoryList() {
        return paymentHistoryArrayList;
    }

    @Override
    public void fetchPaymentService(PaymentHistoryContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            paymentHistoryArrayList = getPaymentServiceList();
            Log.v("PaymenthistoryList: ", paymentHistoryArrayList.toString());
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getPaymentServiceJsonArrayList() {
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
            String url = baseUrl + API_TO_GET_PAYMENT_HISTORY;
            /*+ "?username=" + userName;*/

            Log.v("API_payment_History", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(API_TO_GET_PAYMENT_HISTORY + " not returned data");
            }
        //    JSONObject object = new JSONObject(resp.payload().toString());
          //  JSONArray Jarray  = object.getJSONArray("pending");
            JSONArray Jarray  = new JSONArray(resp.payload().toString());

            Log.e("response: History", Jarray.toString());

            return Jarray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }}
