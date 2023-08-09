package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.OtherServiceContract;
import org.smartregister.unicef.dghs.fragment.MemberOtherServiceFragment;
import org.smartregister.unicef.dghs.interactor.MemberOtherServiceInteractor;
import org.smartregister.unicef.dghs.utils.OtherServiceData;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MemberOtherServicePresenter implements OtherServiceContract.Presenter, OtherServiceContract.InteractorCallBack {


    private OtherServiceContract.View view;
    private ArrayList<OtherServiceData> data;
    private OtherServiceContract.Interactor interactor;

    public MemberOtherServicePresenter(OtherServiceContract.View view){
        this.view = view;
        data = new ArrayList<>();
        interactor = new MemberOtherServiceInteractor(new AppExecutors());
    }
    @Override
    public void fetchData(CommonPersonObjectClient commonPersonObjectClient) {
        interactor.fetchData(commonPersonObjectClient,getView().getContext(),this);
    }

    @Override
    public ArrayList<OtherServiceData> getData() {
        return data;

    }

    @Override
    public void onUpdateList(ArrayList<OtherServiceData> otherServiceData) {
        this.data.clear();
        this.data = otherServiceData;
        if(getView() != null) getView().updateView();

    }

    @Override
    public MemberOtherServiceFragment getView() {
        return (MemberOtherServiceFragment) view;
    }
}