package org.smartregister.brac.hnpp.interactor;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NoHttpResponseException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.BkashStatusContract;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.enums.FollowUpType;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.FollowUpModel;
import org.smartregister.brac.hnpp.model.RiskyPatientFilterType;
import org.smartregister.brac.hnpp.utils.BkashStatus;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.service.HTTPAgent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RoutinFUpnteractor implements RoutinFUpContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<AncFollowUpModel> followUpList;

    public RoutinFUpnteractor() {
        this.followUpList = new ArrayList<>();
    }

    private ArrayList<AncFollowUpModel> getData() {
        followUpList.clear();

        followUpList = HnppApplication.getAncFollowUpRepository().getAncFollowUpData(FollowUpType.routine, false);
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

    @Override
    public ArrayList<AncFollowUpModel> getFollowUpListAfterSearch(String searchedText, RiskyPatientFilterType riskyPatientFilterType) {
        ArrayList<AncFollowUpModel> listAfterSearch = new ArrayList<>();
        Calendar visScheduleToday = Calendar.getInstance();
        Calendar visScheduleNextThree = Calendar.getInstance();
        if(riskyPatientFilterType.getVisitScheduleToday() != 0){
            visScheduleToday.setTimeInMillis(System.currentTimeMillis());
        }

        if(riskyPatientFilterType.getVisitScheduleNextThree() != 0){
            visScheduleNextThree.setTimeInMillis(System.currentTimeMillis());
            visScheduleNextThree.add(Calendar.DAY_OF_MONTH,3);
        }

        for (AncFollowUpModel model : followUpList) {
            if ((model.memberName.toLowerCase().contains(searchedText.toLowerCase()) ||
                    model.memberPhoneNum.toLowerCase().contains(searchedText.toLowerCase())) &&
                    riskyPatientFilterType.getVisitScheduleToday() == 0 ||
                    (riskyPatientFilterType.getVisitScheduleToday() != 0 && (visScheduleToday.)) &&
                    riskyPatientFilterType.getVisitScheduleNextThree() == 0 ||
                    (riskyPatientFilterType.getVisitScheduleNextThree() != 0 &&
                            (visScheduleNextThree.getTimeInMillis() >= System.currentTimeMillis() &&
                                    riskyPatientFilterType.getVisitScheduleNextThree() <= visScheduleNextThree.getTimeInMillis()))
            ) {
                listAfterSearch.add(model);
            }
        }
        return listAfterSearch;
    }
}
