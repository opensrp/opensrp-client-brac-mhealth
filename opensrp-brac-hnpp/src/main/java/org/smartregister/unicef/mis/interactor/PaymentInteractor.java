package org.smartregister.unicef.mis.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.unicef.mis.contract.PaymentContract;
import org.smartregister.unicef.mis.model.Payment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class PaymentInteractor implements PaymentContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<Payment> paymentArrayList;
    private static final String PAYMENT_PENDING_URL = "/rest/event/payment-pending";

    public PaymentInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.paymentArrayList = new ArrayList<>();
    }

    private ArrayList<Payment> getPaymentServiceList(){
        paymentArrayList.clear();
        JSONArray jsonArray = getPaymentServiceJsonArrayList();
        int totalInit = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                Payment payment = new Gson().fromJson(object.toString(), Payment.class);
                if (payment != null) {
                    payment.setPayFor(payment.getQuantity());
                    totalInit = totalInit + payment.getTotal();
                    payment.setTotalInitialAmount(totalInit);
                    paymentArrayList.add(payment);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return paymentArrayList;
    }

    @Override
    public ArrayList<Payment> getPaymentList() {
        return paymentArrayList;
    }

    @Override
    public void fetchPaymentService(PaymentContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            paymentArrayList = getPaymentServiceList();
            Log.v("PaymentList: ", paymentArrayList.toString());
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
            String url = baseUrl + PAYMENT_PENDING_URL;
            /*+ "?username=" + userName;*/

            Log.v("PAYMENT_PENDING_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(PAYMENT_PENDING_URL + " not returned data");
            }
            JSONObject object = new JSONObject(resp.payload().toString());
            JSONArray Jarray  = object.getJSONArray("pending");
            Log.v("PAYMENT_PENDING_URL", "data:" + Jarray.toString());
            return Jarray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
