package org.smartregister.unicef.mis.risky_patient.interactor;

import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.mis.HnppApplication;
import org.smartregister.unicef.mis.risky_patient.contract.SpecialFUpContract;
import org.smartregister.unicef.mis.risky_patient.enums.FollowUpType;
import org.smartregister.unicef.mis.risky_patient.model.AncFollowUpModel;
import org.smartregister.unicef.mis.risky_patient.model.RiskyPatientFilterType;
import org.smartregister.unicef.mis.risky_patient.utils.RiskyPatientFilterUtils;

import java.util.ArrayList;

public class SpecialFUpnteractor implements SpecialFUpContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<AncFollowUpModel> followUpList;

    public SpecialFUpnteractor() {
        this.followUpList = new ArrayList<>();
    }

    private ArrayList<AncFollowUpModel> getData() {
        followUpList.clear();

        followUpList = HnppApplication.getAncFollowUpRepository().getAncFollowUpData(FollowUpType.special,false);
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
        for (AncFollowUpModel model : followUpList) {
            //first of all, check search query exist or not/ if not
            if(searchedText.isEmpty()){
                //if not then check filter
                if(RiskyPatientFilterUtils.isFilterNeeded(riskyPatientFilterType)){
                    if(RiskyPatientFilterUtils.checkFilter(model.specialFollowUpDate,riskyPatientFilterType)){
                        listAfterSearch.add(model);
                    }
                }else {
                    listAfterSearch.add(model);
                }
            }
            else {
                //if search query found then filter with search query also
                if ((model.memberName.toLowerCase().contains(searchedText.toLowerCase()) ||
                        model.memberPhoneNum.toLowerCase().contains(searchedText.toLowerCase()))
                ) {
                    //then check extra filter options
                    if(RiskyPatientFilterUtils.isFilterNeeded(riskyPatientFilterType)){
                        if(RiskyPatientFilterUtils.checkFilter(model.specialFollowUpDate,riskyPatientFilterType)){
                            listAfterSearch.add(model);
                        }
                    }else {
                        listAfterSearch.add(model);
                    }
                }
            }

        }
        return listAfterSearch;
    }
}
