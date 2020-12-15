package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.SearchDetailsContract;
import org.smartregister.brac.hnpp.interactor.SearchDetailsInteractor;
import org.smartregister.brac.hnpp.model.Migration;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class SearchDetailsPresenter implements SearchDetailsContract.Presenter,SearchDetailsContract.InteractorCallBack {

    private SearchDetailsContract.View view;
    private ArrayList<Migration> data = new ArrayList<>();
    private SearchDetailsContract.Interactor interactor;

    public SearchDetailsPresenter(SearchDetailsContract.View view){
        this.view = view;
        interactor = new SearchDetailsInteractor(new AppExecutors());
    }

    @Override
    public void fetchData() {
        view.showProgressBar();
        interactor.fetchData(this);
    }

    @Override
    public ArrayList<Migration> getMemberList() {
        return data;
    }

    @Override
    public SearchDetailsContract.View getView() {
        return view;
    }

    @Override
    public void onUpdateList(ArrayList<Migration> list) {
        this.data.clear();
        this.data = list;
        view.hideProgressBar();
        if(getView() != null) getView().updateAdapter();
    }
}
