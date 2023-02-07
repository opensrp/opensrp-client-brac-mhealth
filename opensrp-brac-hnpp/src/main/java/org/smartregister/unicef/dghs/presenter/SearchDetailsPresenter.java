package org.smartregister.unicef.dghs.presenter;

import android.text.TextUtils;

import org.smartregister.unicef.dghs.contract.SearchDetailsContract;
import org.smartregister.unicef.dghs.interactor.SearchDetailsInteractor;
import org.smartregister.unicef.dghs.model.Migration;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class SearchDetailsPresenter implements SearchDetailsContract.Presenter,SearchDetailsContract.InteractorCallBack {

    private SearchDetailsContract.View view;
    private ArrayList<Migration> data = new ArrayList<>();
    private ArrayList<Migration> searchData = new ArrayList<>();
    private SearchDetailsContract.Interactor interactor;
    private boolean isFromSearch = false;

    public SearchDetailsPresenter(SearchDetailsContract.View view){
        this.view = view;
        interactor = new SearchDetailsInteractor(new AppExecutors());
    }

    @Override
    public void fetchData(String type,String districtId,String villageId, String gender,String startAge, String age) {
        view.showProgressBar();
        interactor.fetchData(type,districtId,villageId,gender,startAge,age,this);
    }
    public void search(String query){
        if(!TextUtils.isEmpty(query)){
            isFromSearch = true;
            view.showProgressBar();
            searchData.clear();
            for(Migration migration:data){
                String name = migration.firstName.toLowerCase();
                String phoneNo ="",hhPhoneNo="",nationalId ="",birthRegistrationID ="";
                if(migration.attributes!=null && migration.attributes.Mobile_Number!=null){
                    phoneNo = migration.attributes.Mobile_Number;
                }
                if(migration.attributes!=null && migration.attributes.HOH_Phone_Number!=null){
                    hhPhoneNo = migration.attributes.HOH_Phone_Number;
                }
                if(migration.attributes!=null && migration.attributes.nationalId!=null){
                    nationalId = migration.attributes.nationalId;
                }
                if(migration.attributes!=null && migration.attributes.birthRegistrationID!=null){
                    birthRegistrationID = migration.attributes.birthRegistrationID;
                }
                if(name.contains(query)){
                    searchData.add(migration);
                }else if(phoneNo.contains(query)){
                    searchData.add(migration);
                }else if(hhPhoneNo.contains(query)){
                    searchData.add(migration);
                }else if(nationalId.contains(query)){
                    searchData.add(migration);
                }else if(birthRegistrationID.contains(query)){
                    searchData.add(migration);
                }
            }
        }else{
            isFromSearch = false;
            searchData.clear();
        }
        view.hideProgressBar();
        view.updateAdapter();

    }

    @Override
    public ArrayList<Migration> getMemberList() {

        return isFromSearch?searchData:data;
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
        view.updateAdapter();
    }
}
