package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.utils.BaseLocation;

import java.util.ArrayList;

public interface MigrationContract {
    interface View{
        Presenter getPresenter();
        Context getContext();
        void updateDistrictSpinner();
        void updateUpazilaSpinner();
        void updatePouroshovaSpinner();
        void updateUnionSpinner();
        void updateVillageSpinner();
    }

    interface Presenter{
        void fetchDistrict();
        void fetchUpazila(String districtId);
        void fetchPouroshova(String upozilaId);
        void fetchUnion(String poroshovaId);
        void fetchVillage(String unionId);
        ArrayList<BaseLocation> getDistrictList();
        ArrayList<BaseLocation> getUpazilaList();
        ArrayList<BaseLocation> getPouroshovaList();
        ArrayList<BaseLocation> getUnionList();
        ArrayList<BaseLocation> getVillageList();
        View getView();
    }
    interface Interactor{
        void fetchDistrict(MigrationContract.InteractorCallBack callBack);
        void fetchUpazila(String id,MigrationContract.InteractorCallBack callBack);
        void fetchPouroshova(String id,MigrationContract.InteractorCallBack callBack);
        void fetchUnion(String id,MigrationContract.InteractorCallBack callBack);
        void fetchVillage(String id,MigrationContract.InteractorCallBack callBack);
    }

    interface InteractorCallBack{
        void onUpdateDistrict(ArrayList<BaseLocation> districts);
        void onUpdateUpazila(ArrayList<BaseLocation> upazilas);
        void onUpdatePouroshova(ArrayList<BaseLocation> pouroshovas);
        void onUpdateUnion(ArrayList<BaseLocation> unions);
        void onUpdateVillage(ArrayList<BaseLocation> villages);
    }
    interface MigrationPostInteractorCallBack{
        void onSuccess();
        void onFail();
    }
}
