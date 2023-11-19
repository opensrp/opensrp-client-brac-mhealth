package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.contract.SpecialFUpContract;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.enums.FollowUpType;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class SpecialFUpnteractor implements SpecialFUpContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<AncFollowUpModel> followUpList;

    public SpecialFUpnteractor() {
        this.followUpList = new ArrayList<>();
    }

    private ArrayList<AncFollowUpModel> getData() {
        followUpList.clear();

        followUpList = HnppApplication.getAncFollowUpRepository().getAncFollowUpData(FollowUpType.special);
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
    public ArrayList<AncFollowUpModel> getFollowUpList() {
       return getData();
    }
}
