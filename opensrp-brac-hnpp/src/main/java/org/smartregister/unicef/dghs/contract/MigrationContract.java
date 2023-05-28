package org.smartregister.unicef.dghs.contract;

import android.content.Context;

import org.smartregister.unicef.dghs.model.GlobalLocationModel;
import org.smartregister.unicef.dghs.utils.BaseLocation;

import java.util.ArrayList;

public interface MigrationContract {
    interface View{
        Presenter getPresenter();
        Context getContext();
        void updateDistrictSpinner();
        void updateUpazilaSpinner();
        void updateDivisionSpinner();
    }

    interface Presenter{
        void fetchDivision();
        void fetchDistrict(String divisionId);
        void fetchUpazila(String districtId);
        ArrayList<GlobalLocationModel> getDivisionList();
        ArrayList<GlobalLocationModel> getDistrictList();
        ArrayList<GlobalLocationModel> getUpazilaList();
        View getView();
    }
    interface Interactor{
        void fetchDivision(MigrationContract.InteractorCallBack callBack);
        void fetchDistrict(String id,MigrationContract.InteractorCallBack callBack);
        void fetchUpazila(String id,MigrationContract.InteractorCallBack callBack);
    }

    interface InteractorCallBack{
        void onUpdateDivision(ArrayList<GlobalLocationModel> divisions);
        void onUpdateDistrict(ArrayList<GlobalLocationModel> districts);
        void onUpdateUpazila(ArrayList<GlobalLocationModel> upazilas);
    }
    interface MigrationPostInteractorCallBack{
        void onSuccess();
        void onFail();
    }
}
