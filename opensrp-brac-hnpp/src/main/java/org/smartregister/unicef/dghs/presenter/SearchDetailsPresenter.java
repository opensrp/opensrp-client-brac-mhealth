package org.smartregister.unicef.dghs.presenter;

import android.text.TextUtils;

import org.smartregister.clientandeventmodel.Client;
import org.smartregister.unicef.dghs.contract.SearchDetailsContract;
import org.smartregister.unicef.dghs.interactor.SearchDetailsInteractor;
import org.smartregister.unicef.dghs.model.GlobalSearchResult;
import org.smartregister.unicef.dghs.model.Migration;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.unicef.dghs.utils.GlobalSearchContentData;
import org.smartregister.unicef.dghs.utils.OtherVaccineContentData;

import java.util.ArrayList;

public class SearchDetailsPresenter implements SearchDetailsContract.Presenter,SearchDetailsContract.InteractorCallBack {

    private SearchDetailsContract.View view;
    private ArrayList<Client> data = new ArrayList<>();
    private ArrayList<Client> searchData = new ArrayList<>();
    private GlobalSearchResult globalSearchResult;
    private SearchDetailsContract.Interactor interactor;
    private boolean isFromSearch = false;

    public SearchDetailsPresenter(SearchDetailsContract.View view){
        this.view = view;
        interactor = new SearchDetailsInteractor(new AppExecutors());
    }

    @Override
    public void fetchData(GlobalSearchContentData globalSearchContentData) {
        view.showProgressBar();
        interactor.fetchData(globalSearchContentData,this);
    }

    public void search(String query){
        if(!TextUtils.isEmpty(query)){
            isFromSearch = true;
            view.showProgressBar();
            searchData.clear();
            for(Client migration:data){
                String name = migration.getFirstName().toLowerCase()+" "+migration.getLastName().toLowerCase();
                String phoneNo ="",hhPhoneNo="",nationalId ="",birthRegistrationID ="";
                if(migration.getAttribute("Mobile_Number")!=null){
                    phoneNo = migration.getAttribute("Mobile_Number").toString();
                }
//                if(migration.attributes!=null && migration.attributes.HOH_Phone_Number!=null){
//                    hhPhoneNo = migration.attributes.HOH_Phone_Number;
//                }
//                if(migration.attributes!=null && migration.attributes.nationalId!=null){
//                    nationalId = migration.attributes.nationalId;
//                }
//                if(migration.attributes!=null && migration.attributes.birthRegistrationID!=null){
//                    birthRegistrationID = migration.attributes.birthRegistrationID;
//                }
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
    public GlobalSearchResult getGlobalSearchResult() {
        return globalSearchResult;
    }

    @Override
    public void setGlobalSearchResult(GlobalSearchResult globalSearchResult) {
        this.globalSearchResult = globalSearchResult;
    }

    @Override
    public void onUpdateOtherVaccine(OtherVaccineContentData otherVaccineContentData) {

    }

    @Override
    public ArrayList<Client> getMemberList() {

        return isFromSearch?searchData:data;
    }

    @Override
    public SearchDetailsContract.View getView() {
        return view;
    }

    @Override
    public void onUpdateList(ArrayList<Client> list) {
        this.data.clear();
        this.data = list;
        view.hideProgressBar();
        view.updateAdapter();
    }
}
