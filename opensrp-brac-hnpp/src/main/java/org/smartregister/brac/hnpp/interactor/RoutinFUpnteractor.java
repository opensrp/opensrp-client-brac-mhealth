package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.contract.BkashStatusContract;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.utils.BkashStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;

public class RoutinFUpnteractor implements RoutinFUpContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<FollowUpModel> followUpList;

    public RoutinFUpnteractor() {
        this.followUpList = new ArrayList<>();
    }

    private ArrayList<FollowUpModel> getData() {
        followUpList.clear();

        //JSONObject object = getPaymentServiceJsonArrayList();
        /*if (status != null) {
            followUpList.add(status);
        }*/
        followUpList.add(new FollowUpModel(
                "wwww",
                "25-10-2023",
                "27-10-2023",
                "25-10-2023",
                "25-10-2023",
                "",
                1,
                "Rahima Khatun",
                "01787878787",
                "27-10-2023",
                0
        ));
        return followUpList;
    }

/*    private JSONObject getPaymentServiceJsonArrayList() {
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
            String transactionId = CoreLibrary.getInstance().context().allSharedPreferences().getPreference("bkash_transaction_id");
            String url = baseUrl + BKASH_STATUS_URL + "trxId=" + transactionId;
            *//*+ "?username=" + userName;*//*

            Log.v("BKASH_STATUS_URL", "url:" + url);
            org.smartregister.domain.Response resp = httpAgent.fetch(url);
            if (resp.isFailure()) {
                throw new NoHttpResponseException(BKASH_STATUS_URL + " not returned data");
            }
            JSONObject object = new JSONObject(resp.payload().toString());
            Log.v("BKASH_STATUS_URL", "url:" + object.toString());
            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }*/

    @Override
    public ArrayList<FollowUpModel> getFollowUpList() {
       return getData();
    }
}
