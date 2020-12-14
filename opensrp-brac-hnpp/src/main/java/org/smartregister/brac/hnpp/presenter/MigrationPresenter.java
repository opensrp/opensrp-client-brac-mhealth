package org.smartregister.brac.hnpp.presenter;

import org.smartregister.brac.hnpp.contract.MigrationContract;
import org.smartregister.brac.hnpp.contract.OtherServiceContract;
import org.smartregister.brac.hnpp.interactor.MemberOtherServiceInteractor;
import org.smartregister.brac.hnpp.interactor.MigrationInteractor;
import org.smartregister.brac.hnpp.utils.District;
import org.smartregister.brac.hnpp.utils.OtherServiceData;
import org.smartregister.brac.hnpp.utils.Pouroshova;
import org.smartregister.brac.hnpp.utils.Union;
import org.smartregister.brac.hnpp.utils.Upazila;
import org.smartregister.brac.hnpp.utils.Village;
import org.smartregister.family.util.AppExecutors;

import java.util.ArrayList;

public class MigrationPresenter implements MigrationContract.Presenter, MigrationContract.InteractorCallBack {
    private MigrationContract.View view;
    private ArrayList<District> districtArrayList;
    private ArrayList<Upazila> upazilaArrayList;
    private ArrayList<Pouroshova> pouroshovaArrayList;
    private ArrayList<Union> unionArrayList;
    private ArrayList<Village> villageArrayList;
    private MigrationContract.Interactor interactor;

    public MigrationPresenter(MigrationContract.View view){
        this.view = view;
        interactor = new MigrationInteractor(new AppExecutors());
    }

    @Override
    public void fetchDistrict() {
        interactor.fetchDistrict(this);
    }

    @Override
    public void fetchUpazila() {
        interactor.fetchUpazila(this);
    }

    @Override
    public void fetchPouroshova() {
        interactor.fetchPouroshova(this);
    }

    @Override
    public void fetchUnion() {
        interactor.fetchUnion(this);
    }

    @Override
    public void fetchVillage() {
        interactor.fetchVillage(this);
    }

    @Override
    public ArrayList<District> getDistrictList() {
        return districtArrayList;
    }

    @Override
    public ArrayList<Upazila> getUpazilaList() {
        return upazilaArrayList;
    }

    @Override
    public ArrayList<Pouroshova> getPouroshovaList() {
        return pouroshovaArrayList;
    }

    @Override
    public ArrayList<Union> getUnionList() {
        return unionArrayList;
    }

    @Override
    public ArrayList<Village> getVillageList() {
        return villageArrayList;
    }


    @Override
    public void onUpdateDistrict(ArrayList<District> districts) {
        this.districtArrayList = districts;
    }

    @Override
    public void onUpdateUpazila(ArrayList<Upazila> upazilas) {
        this.upazilaArrayList = upazilas;
    }

    @Override
    public void onUpdatePouroshova(ArrayList<Pouroshova> pouroshovas) {
        this.pouroshovaArrayList = pouroshovas;
    }

    @Override
    public void onUpdateUnion(ArrayList<Union> unions) {
        this.unionArrayList = unions;
    }

    @Override
    public void onUpdateVillage(ArrayList<Village> villages) {
        this.villageArrayList = villages;
    }

    @Override
    public MigrationContract.View getView() {
        return view;
    }
}
