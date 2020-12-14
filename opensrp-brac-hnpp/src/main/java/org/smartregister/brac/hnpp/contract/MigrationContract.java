package org.smartregister.brac.hnpp.contract;

import android.content.Context;

import org.smartregister.brac.hnpp.utils.District;
import org.smartregister.brac.hnpp.utils.Pouroshova;
import org.smartregister.brac.hnpp.utils.Union;
import org.smartregister.brac.hnpp.utils.Upazila;
import org.smartregister.brac.hnpp.utils.Village;

import java.util.ArrayList;

public interface MigrationContract {
    interface View{
        Presenter getPresenter();
        Context getContext();
    }

    interface Presenter{
        void fetchDistrict();
        void fetchUpazila();
        void fetchPouroshova();
        void fetchUnion();
        void fetchVillage();
        ArrayList<District> getDistrictList();
        ArrayList<Upazila> getUpazilaList();
        ArrayList<Pouroshova> getPouroshovaList();
        ArrayList<Union> getUnionList();
        ArrayList<Village> getVillageList();
        View getView();
    }
    interface Interactor{
        void fetchDistrict(MigrationContract.InteractorCallBack callBack);
        void fetchUpazila(MigrationContract.InteractorCallBack callBack);
        void fetchPouroshova(MigrationContract.InteractorCallBack callBack);
        void fetchUnion(MigrationContract.InteractorCallBack callBack);
        void fetchVillage(MigrationContract.InteractorCallBack callBack);
    }

    interface InteractorCallBack{
        void onUpdateDistrict(ArrayList<District> districts);
        void onUpdateUpazila(ArrayList<Upazila> upazilas);
        void onUpdatePouroshova(ArrayList<Pouroshova> pouroshovas);
        void onUpdateUnion(ArrayList<Union> unions);
        void onUpdateVillage(ArrayList<Village> villages);
    }
}
