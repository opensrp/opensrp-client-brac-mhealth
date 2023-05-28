package org.smartregister.unicef.dghs.presenter;

import org.smartregister.unicef.dghs.contract.MigrationContract;
import org.smartregister.unicef.dghs.interactor.GlobalSearchInteractor;
import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class GlobalSearchPresenter implements MigrationContract.Presenter, MigrationContract.InteractorCallBack {
    private MigrationContract.View view;
    private ArrayList<GlobalLocationModel> districtArrayList;
    private ArrayList<GlobalLocationModel> upazilaArrayList;
    private ArrayList<GlobalLocationModel> divisionArrayList;
    private MigrationContract.Interactor interactor;

    public GlobalSearchPresenter(MigrationContract.View view){
        this.view = view;
        interactor = new GlobalSearchInteractor(new AppExecutors());
    }

    @Override
    public void fetchDistrict(String divisionId) {
        interactor.fetchDistrict(divisionId,this);
    }

    @Override
    public void fetchUpazila(String districtId) {
        interactor.fetchUpazila(districtId,this);
    }

    @Override
    public void fetchDivision() {
        interactor.fetchDivision(this);
    }

    @Override
    public ArrayList<GlobalLocationModel> getDistrictList() {
        return districtArrayList;
    }

    @Override
    public ArrayList<GlobalLocationModel> getUpazilaList() {
        return upazilaArrayList;
    }

    @Override
    public ArrayList<GlobalLocationModel> getDivisionList() {
        return divisionArrayList;
    }


    @Override
    public void onUpdateDistrict(ArrayList<GlobalLocationModel> districts) {
        this.districtArrayList = districts;
        view.updateDistrictSpinner();

    }

    @Override
    public void onUpdateUpazila(ArrayList<GlobalLocationModel> upazilas) {
        this.upazilaArrayList = upazilas;
        view.updateUpazilaSpinner();
    }

    @Override
    public void onUpdateDivision(ArrayList<GlobalLocationModel> division) {
        this.divisionArrayList = division;
        GlobalLocationModel globalLocationModel = new GlobalLocationModel();
        globalLocationModel.name = "সিলেক্ট করুন";
        divisionArrayList.add(0,globalLocationModel);
        view.updateDivisionSpinner();
    }

    @Override
    public MigrationContract.View getView() {
        return view;
    }
}
