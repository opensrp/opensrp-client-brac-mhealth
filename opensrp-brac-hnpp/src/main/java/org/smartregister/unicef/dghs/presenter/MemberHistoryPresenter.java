package org.smartregister.unicef.dghs.presenter;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.fragment.MemberANCHistoryFragment;
import org.smartregister.unicef.dghs.fragment.MemberHistoryDialogFragment;
import org.smartregister.unicef.dghs.fragment.MemberHistoryFragment;
import org.smartregister.unicef.dghs.fragment.MemberOtherServiceFragment;
import org.smartregister.unicef.dghs.interactor.MemberHistoryInteractor;
import org.smartregister.unicef.dghs.interactor.MemberOtherServiceInteractor;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.dghs.utils.VisitHistory;

import java.util.ArrayList;

public class MemberHistoryPresenter implements MemberHistoryContract.Presenter,MemberHistoryContract.PresenterANC, MemberHistoryContract.InteractorCallBack,MemberHistoryContract.InteractorCallBackANC {


    private MemberHistoryContract.View view;
    private ArrayList<MemberHistoryData> data = new ArrayList<>();
    private ArrayList<VisitHistory> visitHistories = new ArrayList<>();
    private MemberHistoryContract.Interactor interactor;

    public MemberHistoryPresenter(MemberHistoryContract.View view){
        this.view = view;
        interactor = new MemberHistoryInteractor(new AppExecutors());
    }

    @Override
    public void fetchData(String baseEntityId) {
        if(getView()!=null) getView().showProgressBar();
        interactor.fetchData(getView().getContext(),baseEntityId,this);
    }

    @Override
    public void fetchCurrentTimeLineData(String baseEntityId) {
        if(getView()!=null) getView().showProgressBar();
        ((MemberHistoryInteractor)interactor).fetchCurrentTimeLineData(getView().getContext(), baseEntityId,this);
    }
    public void fetchCurrentTimeLineHistoryData(String baseEntityId, long startDate, long endDate) {
        if(getView()!=null) getView().showProgressBar();
        ((MemberHistoryInteractor)interactor).fetchCurrentTimeLineHistoryData(getView().getContext(), baseEntityId,startDate,endDate,this);
    }
    @Override
    public void getVisitFormWithData(MemberHistoryData content) {
        interactor.getVisitFormWithData(getView().getContext(),content,this);
    }

    @Override
    public ArrayList<MemberHistoryData> getMemberHistory() {
        return data;
    }
    public ArrayList<MemberHistoryData> getANCHistory(){
        ArrayList<MemberHistoryData> memberHistoryANCData = new ArrayList<>();
        for (MemberHistoryData memberData:data) {
            if(memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_REGISTRATION)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
                    memberHistoryANCData.add(memberData);

            }

        }
        return memberHistoryANCData;

    }
    public ArrayList<MemberHistoryData> getANCHistory(long startDate, long endDate){
        Log.v("ANC_HISTORY","getANCHistory>>startDate:"+startDate+":endDate:"+endDate);
        ArrayList<MemberHistoryData> memberHistoryANCData = new ArrayList<>();
        for (MemberHistoryData memberData:data) {
            if(memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_HOME_VISIT)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.PNC_REGISTRATION)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.ANC_REGISTRATION)||
                    memberData.getEventType().equalsIgnoreCase(HnppConstants.EVENT_TYPE.PREGNANCY_OUTCOME)){
                Log.v("ANC_HISTORY","getANCHistory>>startDate:"+memberData.getVisitDate()+":endDate:"+endDate+":greter start:"+(memberData.getVisitDate()>=startDate)+":lessend:"+(memberData.getVisitDate()<=endDate));
                if(endDate>0){
                    if(memberData.getVisitDate()>=startDate && memberData.getVisitDate()<=endDate){
                        memberHistoryANCData.add(memberData);
                    }
                }else{
                    memberHistoryANCData.add(memberData);
                }

            }

        }
        return memberHistoryANCData;

    }
    public ArrayList<VisitHistory> getVisitHistory(){
        return visitHistories;
    }

    @Override
    public void onUpdateList(ArrayList<MemberHistoryData> list) {
        this.data.clear();
        this.data = list;
        if(getView() != null) {
            getView().hideProgressBar();
            getView().updateAdapter();
        }
    }
    @Override
    public void updateFormWithData(MemberHistoryData content, JSONObject jsonForm) {
        if(getView() !=null) getView().startFormWithVisitData(content,jsonForm);
    }
    @Override
    public MemberHistoryContract.View getView() {
       if(view instanceof MemberHistoryDialogFragment) return (MemberHistoryDialogFragment)view;
       if(view instanceof MemberANCHistoryFragment) return (MemberANCHistoryFragment) view;
        return (MemberHistoryFragment) view;
    }

    @Override
    public void onUpdateAncList(ArrayList<VisitHistory> list) {
        this.visitHistories.clear();
        this.visitHistories = list;
        view.updateANCTitle();

    }

    @Override
    public void fetchANCData(String baseEntityId) {
        ((MemberHistoryInteractor)interactor).fetchAncData(getView().getContext(), baseEntityId,this);
    }
}
