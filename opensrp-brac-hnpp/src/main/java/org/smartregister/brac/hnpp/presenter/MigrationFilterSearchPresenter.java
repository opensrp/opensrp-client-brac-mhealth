package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.interactor.MigrationFilterSearchInteractor;
import org.smartregister.brac.hnpp.utils.BaseLocation;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MigrationFilterSearchPresenter implements MigrationContract.Presenter, MigrationContract.InteractorCallBack {
    private MigrationContract.View view;
    private ArrayList<BaseLocation> districtArrayList;
    private ArrayList<BaseLocation> upazilaArrayList;
    private ArrayList<BaseLocation> pouroshovaArrayList;
    private ArrayList<BaseLocation> unionArrayList;
    private ArrayList<BaseLocation> villageArrayList;
    private MigrationContract.Interactor interactor;

    public MigrationFilterSearchPresenter(MigrationContract.View view){
        this.view = view;
        interactor = new MigrationFilterSearchInteractor(new AppExecutors());
    }

    @Override
    public void fetchDistrict() {
        interactor.fetchDistrict(this);
    }

    @Override
    public void fetchUpazila(String districtId) {
        interactor.fetchUpazila(districtId,this);
    }

    @Override
    public void fetchPouroshova(String upozilaId) {
        interactor.fetchPouroshova(upozilaId,this);
    }

    @Override
    public void fetchUnion(String poroshovaId) {
        interactor.fetchUnion(poroshovaId,this);

    }

    @Override
    public void fetchVillage(String unionId) {
        interactor.fetchVillage(unionId,this);

    }


    @Override
    public ArrayList<BaseLocation> getDistrictList() {
        return districtArrayList;
    }

    @Override
    public ArrayList<BaseLocation> getUpazilaList() {
        return upazilaArrayList;
    }

    @Override
    public ArrayList<BaseLocation> getPouroshovaList() {
        return pouroshovaArrayList;
    }

    @Override
    public ArrayList<BaseLocation> getUnionList() {
        return unionArrayList;
    }

    @Override
    public ArrayList<BaseLocation> getVillageList() {
        return villageArrayList;
    }


    @Override
    public void onUpdateDistrict(ArrayList<BaseLocation> districts) {
        this.districtArrayList = districts;
        view.updateDistrictSpinner();

    }

    @Override
    public void onUpdateUpazila(ArrayList<BaseLocation> upazilas) {
        this.upazilaArrayList = upazilas;
        view.updateUpazilaSpinner();
    }

    @Override
    public void onUpdatePouroshova(ArrayList<BaseLocation> pouroshovas) {
        this.pouroshovaArrayList = pouroshovas;
        view.updatePouroshovaSpinner();
    }

    @Override
    public void onUpdateUnion(ArrayList<BaseLocation> unions) {
        this.unionArrayList = unions;
        view.updateUnionSpinner();
    }

    @Override
    public void onUpdateVillage(ArrayList<BaseLocation> villages) {
        this.villageArrayList = villages;
        view.updateVillageSpinner();
    }

    @Override
    public MigrationContract.View getView() {
        return view;
    }
}
