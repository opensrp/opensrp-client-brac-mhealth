package org.smartregister.unicef.dghs.presenter;

import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.unicef.dghs.contract.MemberHistoryContract;
import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.fragment.MemberHistoryDialogFragment;
import org.smartregister.unicef.dghs.fragment.MemberHistoryFragment;
import org.smartregister.unicef.dghs.fragment.MemberOtherServiceFragment;
import org.smartregister.unicef.dghs.interactor.MemberHistoryInteractor;
import org.smartregister.unicef.dghs.interactor.MemberOtherServiceInteractor;
import org.smartregister.unicef.dghs.utils.HnppConstants;
import org.smartregister.unicef.dghs.utils.MemberHistoryData;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MemberHistoryPresenter implements MemberHistoryContract.Presenter, MemberHistoryContract.InteractorCallBack {


    private MemberHistoryContract.View view;
    private ArrayList<MemberHistoryData> data = new ArrayList<>();
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
        return view instanceof MemberHistoryDialogFragment?(MemberHistoryDialogFragment)view: (MemberHistoryFragment) view;
    }
}
