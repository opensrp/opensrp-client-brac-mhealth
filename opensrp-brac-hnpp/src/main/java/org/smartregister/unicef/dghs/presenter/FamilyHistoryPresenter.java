package org.smartregister.unicef.dghs.presenter;

import org.json.JSONObject;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.fragment.FamilyHistoryFragment;
import org.smartregister.unicef.dghs.fragment.MemberHistoryFragment;
import org.smartregister.unicef.dghs.interactor.FamilyHistoryInteractor;
import org.smartregister.unicef.dghs.interactor.MemberHistoryInteractor;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class FamilyHistoryPresenter implements MemberHistoryContract.Presenter, MemberHistoryContract.InteractorCallBack {


    private MemberHistoryContract.View view;
    private ArrayList<MemberHistoryData> data;
    private MemberHistoryContract.Interactor interactor;

    public FamilyHistoryPresenter(MemberHistoryContract.View view){
        this.view = view;
        interactor = new FamilyHistoryInteractor(new AppExecutors());
    }

    @Override
    public void fetchData(String baseEntityId) {
        interactor.fetchData(getView().getContext(),baseEntityId,this);
    }
    @Override
    public void getVisitFormWithData(MemberHistoryData content) {
        interactor.getVisitFormWithData(getView().getContext(),content,this);
    }

    @Override
    public ArrayList<MemberHistoryData> getMemberHistory() {
        return data;
    }

    @Override
    public void onUpdateList(ArrayList<MemberHistoryData> list) {
        this.data = list;
        if(getView() != null) getView().updateAdapter();
    }

    @Override
    public void updateFormWithData(MemberHistoryData content, JSONObject jsonForm) {
        if(getView() !=null) getView().startFormWithVisitData(content,jsonForm);
    }
    @Override
    public FamilyHistoryFragment getView() {
        return (FamilyHistoryFragment) view;
    }
}