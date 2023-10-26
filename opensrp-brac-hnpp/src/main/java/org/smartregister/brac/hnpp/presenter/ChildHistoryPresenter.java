package org.smartregister.brac.hnpp.presenter;

import org.json.JSONObject;
import org.smartregister.brac.hnpp.contract.MemberHistoryContract;
import org.smartregister.brac.hnpp.fragment.ChildHistoryFragment;
import org.smartregister.brac.hnpp.fragment.MemberHistoryFragment;
import org.smartregister.brac.hnpp.interactor.ChildHistoryInteractor;
import org.smartregister.brac.hnpp.interactor.MemberHistoryInteractor;
import org.smartregister.brac.hnpp.utils.MemberHistoryData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class ChildHistoryPresenter implements MemberHistoryContract.Presenter, MemberHistoryContract.InteractorCallBack {


    private MemberHistoryContract.View view;
    private ArrayList<MemberHistoryData> data = new ArrayList<>();
    private MemberHistoryContract.Interactor interactor;

    public ChildHistoryPresenter(MemberHistoryContract.View view){
        this.view = view;
        interactor = new ChildHistoryInteractor(new AppExecutors());
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
        this.data.clear();
        this.data = list;
        if(getView() != null) getView().updateAdapter();
    }
    @Override
    public void updateFormWithData(MemberHistoryData content, JSONObject jsonForm) {
        if(getView() != null) getView().startFormWithVisitData(content,jsonForm);
    }
    @Override
    public ChildHistoryFragment getView() {
        return (ChildHistoryFragment) view;
    }
}
