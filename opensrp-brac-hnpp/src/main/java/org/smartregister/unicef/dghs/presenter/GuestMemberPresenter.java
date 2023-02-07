package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.GuestMemberContract;
import org.smartregister.unicef.dghs.interactor.GuestMemberInterator;
import org.smartregister.unicef.dghs.model.GuestMemberModel;
import org.smartregister.unicef.dghs.utils.GuestMemberData;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class GuestMemberPresenter implements GuestMemberContract.Presenter,GuestMemberContract.InteractorCallBack {

    private GuestMemberContract.View view;
    private GuestMemberInterator interactor;

    public GuestMemberPresenter(GuestMemberContract.View view){
        this.view = view;
        interactor = new GuestMemberInterator(new AppExecutors(),new GuestMemberModel(view.getContext()));

    }

    @Override
    public ArrayList<GuestMemberData> getData() {
        return interactor.getAllGuestMemberList();
    }

    @Override
    public void saveMember(String jsonString) {

        interactor.processAndSaveRegistration(jsonString,this);

    }

    @Override
    public void filterData(String query, String ssName) {
        view.showProgressBar();
        interactor.filterData(view.getContext(),query,ssName,this);

    }

    @Override
    public void fetchData() {
        view.showProgressBar();
        interactor.fetchData(view.getContext(),this);

    }

    @Override
    public GuestMemberContract.View getView() {
        return view;
    }

    @Override
    public void updateAdapter() {
        view.hideProgressBar();
        view.updateAdapter();

    }

    @Override
    public void successfullySaved() {
        view.hideProgressBar();
        fetchData();



    }
}
