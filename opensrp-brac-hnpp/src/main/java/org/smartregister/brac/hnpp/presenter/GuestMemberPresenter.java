package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.GuestMemberContract;
import org.smartregister.brac.hnpp.interactor.GuestMemberInterator;
import org.smartregister.brac.hnpp.model.GuestMemberModel;
import org.smartregister.brac.hnpp.utils.GuestMemberData;
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
