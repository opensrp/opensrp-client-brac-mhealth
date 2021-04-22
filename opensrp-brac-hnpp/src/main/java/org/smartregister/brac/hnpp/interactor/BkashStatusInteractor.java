package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.activity.BkashActivity;
import org.smartregister.brac.hnpp.contract.BkashStatusContract;
import org.smartregister.brac.hnpp.utils.BkashStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class BkashStatusInteractor implements BkashStatusContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<BkashStatus> bkashStatusArrayList;
    private static final String BKASH_STATUS_URL = "/rest/event/bkash-payment-history";

    public BkashStatusInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.bkashStatusArrayList = new ArrayList<>();
    }

    private ArrayList<BkashStatus> getBkashStatusList() {
        bkashStatusArrayList.clear();

        JSONObject object = getPaymentServiceJsonArrayList();
        BkashStatus status = new Gson().fromJson(object.toString(), BkashStatus.class);
        if (status != null) {
            status.setTransactionId(status.getTransactionId());
            status.setQuantity(status.getQuantity());
            status.setTotalAmount(status.getTotalAmount());
            status.setStatus(status.getStatus());
            bkashStatusArrayList.add(status);
        }
        return bkashStatusArrayList;
    }

    @Override
    public ArrayList<BkashStatus> getStatusList() {
        return bkashStatusArrayList;
    }

    @Override
    public void fetchBkashStatus(BkashStatusContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            bkashStatusArrayList = getBkashStatusList();
            Log.v("BkashStatusList: ", bkashStatusArrayList.toString());
            appExecutors.mainThread().execute(callBack::fetchedSuccessfully);
        };
        appExecutors.diskIO().execute(runnable);
    }

    private JSONObject getPaymentServiceJsonArrayList() {
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
            String transactionId = CoreLibrary.getInstance().context().allSharedPreferences().getPreference(BkashActivity.BKASH_TRANSACTION_ID);
            String url = baseUrl + BKASH_STATUS_URL + "trxId=" + transactionId;
            /*+ "?username=" + userName;*/

            Log.v("BKASH_STATUS_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(BKASH_STATUS_URL + " not returned data");
            }
            JSONObject object = new JSONObject(resp.payload().toString());
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
