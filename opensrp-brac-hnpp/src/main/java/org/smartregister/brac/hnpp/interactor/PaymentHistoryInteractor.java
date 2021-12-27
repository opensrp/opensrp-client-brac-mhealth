package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.PaymentHistoryContract;
import org.smartregister.brac.hnpp.model.PaymentHistory;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class PaymentHistoryInteractor implements PaymentHistoryContract.Interactor{
    private static final String API_TO_GET_PAYMENT_HISTORY = "/rest/event/bkash-payment-history?";
    private AppExecutors appExecutors;
    private ArrayList<PaymentHistory> paymentHistoryArrayList;
    private static final String LAST_PAYMENT_HISTORY_SYNC = "last_payment_history_sync";
    private int totalPayment;

    public PaymentHistoryInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.paymentHistoryArrayList = new ArrayList<>();
    }

    @Override
    public int getTotalPayment() {
        return totalPayment;
    }

    private ArrayList<PaymentHistory> getPaymentServiceList(boolean isNew){
        paymentHistoryArrayList.clear();
        JSONArray jsonArray = getPaymentServiceJsonArrayList(isNew);
        long tempTimestamp = 0;
        if(jsonArray!=null){
            if(jsonArray.length() >0 && isNew){
                HnppApplication.getPaymentHistoryRepository().dropTable();
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object = jsonArray.getJSONObject(i);
                    PaymentHistory paymentHistory = new Gson().fromJson(object.toString(), PaymentHistory.class);
                    if (paymentHistory != null) {
                        if(paymentHistory.getPaymentTimestamp() > tempTimestamp){
                            tempTimestamp = paymentHistory.getPaymentTimestamp();
                        }
                        HnppApplication.getPaymentHistoryRepository().addOrUpdate(paymentHistory);
                    }
//                ArrayList<PaymentHistory> paymentHistoryList =  HnppApplication.getPaymentHistoryRepository().getAllPayment();
//                paymentHistoryArrayList.addAll(paymentHistoryList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if(tempTimestamp > 0){
            CoreLibrary.getInstance().context().allSharedPreferences().savePreference(LAST_PAYMENT_HISTORY_SYNC, tempTimestamp+"");
        }
        loadLocalData();


        return paymentHistoryArrayList;
    }
    private void loadLocalData(){
        ArrayList<PaymentHistory> paymentHistoryList =  HnppApplication.getPaymentHistoryRepository().getAllPayment();
        if(paymentHistoryList.size()>0)paymentHistoryArrayList.addAll(paymentHistoryList);
    }

    @Override
    public ArrayList<PaymentHistory> getPaymentHistoryList() {
        return paymentHistoryArrayList;
    }

    @Override
    public void fetchAllData(PaymentHistoryContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            getPaymentServiceList(true);

            totalPayment = HnppApplication.getPaymentHistoryRepository().getTotalPayment("","");
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void fetchPaymentService(PaymentHistoryContract.InteractorCallBack callBack,boolean isLocal) {
        Runnable runnable = () -> {
            if(!isLocal){
                getPaymentServiceList(false);
            }else {
                paymentHistoryArrayList.clear();
                loadLocalData();
            }
            totalPayment = HnppApplication.getPaymentHistoryRepository().getTotalPayment("","");
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void filterByFromToDate(PaymentHistoryContract.InteractorCallBack callBack, String fromDate, String toDate) {
        Runnable runnable = () -> {

                paymentHistoryArrayList.clear();
            ArrayList<PaymentHistory> paymentHistoryList =  HnppApplication.getPaymentHistoryRepository().getFilterPayment(fromDate,toDate);
            if(paymentHistoryList.size()>0)paymentHistoryArrayList.addAll(paymentHistoryList);
            totalPayment = HnppApplication.getPaymentHistoryRepository().getTotalPayment(fromDate,toDate);

            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONArray getPaymentServiceJsonArrayList(boolean isNew) {
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
            String lastHistorySyncTime = isNew? "":CoreLibrary.getInstance().context().allSharedPreferences().getPreference(LAST_PAYMENT_HISTORY_SYNC);
            if(TextUtils.isEmpty(lastHistorySyncTime)){
                LocalDate currentDate = LocalDate.now();
                LocalDate currentDateMinus6Months = currentDate.minusMonths(6);

                lastHistorySyncTime =currentDateMinus6Months.toDate().getTime()/1000+"";
            }

            String url = baseUrl + API_TO_GET_PAYMENT_HISTORY + "&timestamp=" + lastHistorySyncTime;
            /*+ "?username=" + userName;*/

            Log.v("PAYMENT_HISTORY", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(API_TO_GET_PAYMENT_HISTORY + " not returned data");
            }
        //    JSONObject object = new JSONObject(resp.payload().toString());
          //  JSONArray Jarray  = object.getJSONArray("pending");
            JSONArray Jarray  = new JSONArray(resp.payload().toString());


            return Jarray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }}
