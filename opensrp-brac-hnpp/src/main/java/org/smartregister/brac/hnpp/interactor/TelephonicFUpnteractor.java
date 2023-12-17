package org.smartregister.brac.hnpp.interactor;

import org.smartregister.brac.hnpp.HnppApplication;
import org.smartregister.brac.hnpp.contract.RoutinFUpContract;
import org.smartregister.brac.hnpp.contract.TelephonicFUpContract;
import org.smartregister.brac.hnpp.enums.FollowUpType;
import org.smartregister.brac.hnpp.model.AncFollowUpModel;
import org.smartregister.brac.hnpp.model.RiskyPatientFilterType;
import org.smartregister.brac.hnpp.utils.RiskyPatientFilterUtils;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class TelephonicFUpnteractor implements TelephonicFUpContract.Interactor {
    private AppExecutors appExecutors;
    private ArrayList<AncFollowUpModel> followUpList;

    public TelephonicFUpnteractor() {
        this.followUpList = new ArrayList<>();
    }

    private ArrayList<AncFollowUpModel> getData() {
        followUpList.clear();

        followUpList = HnppApplication.getAncFollowUpRepository().getAncFollowUpData(FollowUpType.telephonic,false);
        return followUpList;
    }

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
                    if(RiskyPatientFilterUtils.checkFilter(model.telephonyFollowUpDate,riskyPatientFilterType)){
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
                        if(RiskyPatientFilterUtils.checkFilter(model.telephonyFollowUpDate,riskyPatientFilterType)){
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